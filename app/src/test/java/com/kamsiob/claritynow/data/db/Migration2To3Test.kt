package com.kamsiob.claritynow.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kamsiob.claritynow.data.event.inTotalOrder
import com.kamsiob.claritynow.domain.replay.ClarityReducer
import com.kamsiob.claritynow.domain.replay.ClarityState
import com.kamsiob.claritynow.domain.replay.GoldenFixture
import com.kamsiob.claritynow.domain.replay.ItemState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Modifier as JavaModifier
import java.lang.reflect.Proxy

/**
 * The Room 2 to 3 migration, checked against the golden log. Addendum 01 2c,
 * DECISIONS.md C8, issue #24.
 *
 * Schema 3 is what made an item's area nullable and gave an item a first step and an
 * estimate. `clarity_item` had a `NOT NULL` on `areaId` and SQLite cannot drop one in
 * place, so the migration rebuilds the table, copies every row across, drops the
 * original and takes its name. That is three chances to lose a column, put a value in
 * the wrong place, or leave an index behind, and a person's items are what would be
 * lost.
 *
 * **What this test proves, and what it does not.** It reads the two committed schema
 * exports, which are Room's own record of the shape it will validate against on the
 * next open, and checks the migration's statements against them. It then replays the
 * golden log and checks that every state it passes through survives the round trip
 * through the schema 3 row, including the unfiled item that schema 2 had no column
 * for. What it does not do is execute the SQL against a real SQLite: that needs an
 * instrumented test, `MigrationTestHelper`, and the schema directory on the androidTest
 * asset path, none of which exist in this project yet. **The gap is worth naming
 * rather than leaving to be discovered**, and it is narrow: the statements are checked
 * against the schema Room validates against, so a mismatch fails here rather than on
 * somebody's phone.
 *
 * The load bearing assertion is the third one. The cache is rebuildable from the log
 * at any time, which is the safety net under every table in this file that the log
 * table does not have, so what the migration actually has to guarantee is that **the
 * log crosses it untouched and the rebuilt cache can hold everything the log produces.**
 * Both halves are asserted below.
 */
class Migration2To3Test {

    private val schema2 = schema(2)
    private val schema3 = schema(3)
    private val statements = record(ClarityDatabase.MIGRATION_2_3)

    // The log crosses the migration untouched -------------------------------

    @Test
    fun `the migration touches one table and it is not the log`() {
        assertTrue(
            "the migration wrote nothing at all",
            statements.isNotEmpty(),
        )
        assertFalse(
            "the migration touches the event log, which is the one table that is truth",
            statements.any { it.contains("clarity_event") },
        )

        // Everything the migration names is `clarity_item` or one of its indices. The
        // temporary table is `clarity_item_new`, which this matches by prefix.
        val tables = TABLE_NAME.findAll(statements.joinToString(" ")).map { it.groupValues[1] }.toSet()
        assertEquals(
            "the migration named a table it has no business in: $tables",
            setOf("clarity_item", "clarity_item_new"),
            tables.filterNot { it.startsWith("index_") }.toSet(),
        )
    }

    @Test
    fun `clarity_item is the only table that changed between schema 2 and schema 3`() {
        // If any other table's shape moved, the migration would have to move it too,
        // and the assertion above would be checking the wrong thing rather than a
        // real guarantee.
        val changed = schema3.keys.filter { schema2[it] != null && schema2[it] != schema3[it] }
        assertEquals(listOf("clarity_item"), changed)
        assertEquals(
            "schema 3 added or dropped a table, which this migration does not do",
            schema2.keys,
            schema3.keys,
        )
    }

    // The rebuilt table is the one Room will validate against ----------------

    @Test
    fun `the table the migration builds is exactly the table schema 3 declares`() {
        val created = statements.single { it.trimStart().startsWith("CREATE TABLE") }
        assertEquals(
            "the migrated table does not match the committed schema, so Room will " +
                "refuse to open the database after the migration runs",
            normalize(schema3.getValue("clarity_item").createSql.replace(TABLE_PLACEHOLDER, "clarity_item_new")),
            normalize(created),
        )
    }

    @Test
    fun `every index schema 3 declares is recreated`() {
        // DROP TABLE takes a table's indices with it, and Room compares indices
        // against the entity on the next open, so a forgotten one is a refusal to
        // open rather than a slow query.
        val recreated = statements.filter { it.trimStart().startsWith("CREATE INDEX") }.map { normalize(it) }
        val expected = schema3.getValue("clarity_item").indexSql
            .map { normalize(it.replace(TABLE_PLACEHOLDER, "clarity_item")) }
        assertEquals(expected.toSet(), recreated.toSet())
    }

    @Test
    fun `every column schema 2 held is copied by name, and only those`() {
        val insert = statements.single { it.trimStart().startsWith("INSERT") }
        val (into, select) = insert.split("SELECT", limit = 2)
        // The first identifier is the table, the last in the SELECT half is the table
        // it reads from. Everything between them is a column.
        val target = identifiers(into).drop(1)
        val source = identifiers(select).dropLast(1)

        assertEquals("the copy binds by position and the two sides disagree", target, source)
        assertEquals(
            "a column schema 2 held is not copied, so its values are lost",
            schema2.getValue("clarity_item").columns,
            target,
        )
        // The two columns schema 3 adds are deliberately absent and default to null,
        // which is the right answer for both: schema 2 had nowhere to record either.
        assertTrue(
            "the copy names a column schema 2 does not have",
            target.none { it == "firstStep" || it == "estimateMinutes" },
        )
    }

    @Test
    fun `the migrated columns are the columns the cache writer writes`() {
        // Room derives the table from `ItemRow`, so these three have to agree: the
        // entity, the committed schema, and the table the migration hands over.
        // Compared as sets rather than as lists, because Room binds a row by column
        // name and the JVM makes no promise about the order of `declaredFields`. The
        // one place order is load bearing is the copy, which `INSERT ... SELECT` binds
        // by position, and that is asserted as an ordered list above.
        val entityColumns = ItemRow::class.java.declaredFields
            .filterNot { JavaModifier.isStatic(it.modifiers) }
            .map { it.name }
            .toSet()
        assertEquals(schema3.getValue("clarity_item").columns.toSet(), entityColumns)

        val created = statements.single { it.trimStart().startsWith("CREATE TABLE") }
        // `id` appears twice, once as a column and once in the primary key clause.
        val declared = identifiers(created).drop(1).distinct()
        assertEquals(entityColumns, declared.toSet())
    }

    // The golden log, replayed across it -------------------------------------

    @Test
    fun `the golden log replays across the migration to the same state`() {
        // Every state the golden log passes through, not only the state it ends in.
        // The unfiled item in the fixture is filed on day 12, so a check on the final
        // state alone would never see an item with no area at all, which is the exact
        // thing schema 2 could not hold and the whole reason this migration exists.
        var state = ClarityState.EMPTY
        var sawUnfiled = false
        var sawFirstStep = false
        var sawEstimate = false

        for (event in GoldenFixture.log().inTotalOrder()) {
            state = ClarityReducer.apply(state, event)
            for (item in state.items.values) {
                if (item.areaId == null) sawUnfiled = true
                if (item.firstStep != null) sawFirstStep = true
                if (item.estimateMinutes != null) sawEstimate = true
                assertEquals(
                    "item ${item.id} does not survive the schema 3 row",
                    item,
                    item.throughCache(),
                )
            }
        }

        assertEquals(
            "the golden log no longer replays to the committed state",
            GoldenFixture.state().canonical(),
            state.canonical(),
        )
        // Without these the loop above could pass on a fixture that never exercises
        // one of the three things schema 3 was raised for.
        assertTrue("the fixture never holds an unfiled item", sawUnfiled)
        assertTrue("the fixture never holds a first step", sawFirstStep)
        assertTrue("the fixture never holds an estimate", sawEstimate)
    }

    private fun ItemState.throughCache(): ItemState = toRow().toState()

    // Reading the migration and the committed schemas -------------------------

    private data class TableShape(
        val createSql: String,
        val columns: List<String>,
        val indexSql: List<String>,
    )

    /**
     * The committed schema export for [version], by table name.
     *
     * Room writes these on every build and they are committed for exactly this
     * reason: a migration written against a remembered shape is a migration written
     * against a guess.
     */
    private fun schema(version: Int): Map<String, TableShape> {
        val file = File("schemas/com.kamsiob.claritynow.data.db.ClarityDatabase/$version.json")
        assertTrue("missing ${file.absolutePath}", file.isFile)
        val entities = Json.parseToJsonElement(file.readText())
            .jsonObject.getValue("database")
            .jsonObject.getValue("entities")
            .jsonArray
        return entities.associate { entity ->
            val table = entity.jsonObject
            table.getValue("tableName").jsonPrimitive.content to TableShape(
                createSql = table.getValue("createSql").jsonPrimitive.content,
                columns = table.getValue("fields").jsonArray.map {
                    it.jsonObject.getValue("columnName").jsonPrimitive.content
                },
                indexSql = table["indices"]?.jsonArray.orEmpty().map {
                    it.jsonObject.getValue("createSql").jsonPrimitive.content
                },
            )
        }
    }

    /**
     * Every statement [migration] executes, in order.
     *
     * A reflection proxy rather than a hand written stub, because
     * `SupportSQLiteDatabase` has some fifty members and forty nine of them would be
     * noise. The handler refuses anything but `execSQL`, which makes the recorder an
     * assertion in its own right: a migration that reached for a query or a
     * transaction would fail here rather than being recorded incompletely.
     */
    private fun record(migration: Migration): List<String> {
        val statements = mutableListOf<String>()
        val handler = InvocationHandler { _, method, args ->
            when (method.name) {
                "execSQL" -> {
                    statements += args?.get(0) as String
                    null
                }

                "toString" -> "recording database"
                "hashCode" -> 0
                "equals" -> false
                else -> error("the migration called ${method.name}, which this recorder cannot answer")
            }
        }
        val database = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            handler,
        ) as SupportSQLiteDatabase
        migration.migrate(database)
        return statements
    }

    private companion object {
        /** Room's own placeholder in an exported `createSql`. */
        const val TABLE_PLACEHOLDER = "\${TABLE_NAME}"

        /** Every backtick quoted identifier, which is how Room writes all of them. */
        val IDENTIFIER = Regex("`([^`]+)`")

        /** An identifier that follows a keyword naming a table. */
        val TABLE_NAME = Regex("(?:TABLE|INTO|FROM|ON|EXISTS)\\s+`([^`]+)`", RegexOption.IGNORE_CASE)

        fun identifiers(sql: String): List<String> =
            IDENTIFIER.findAll(sql).map { it.groupValues[1] }.toList()

        /**
         * SQL as a shape rather than as a string. Room writes its exports on one line
         * and this file writes its statements over many, so a comparison has to
         * discount whitespace that no parser can see.
         */
        fun normalize(sql: String): String = sql
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\s*([(),])\\s*"), "\$1")
            .trim()
    }
}
