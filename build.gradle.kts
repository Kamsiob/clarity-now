import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

// The Kotlin Android plugin is deliberately absent. AGP 9 has built-in Kotlin
// support and rejects it. The Kotlin version comes from the Compose and
// serialization plugins below, which pull a newer Kotlin Gradle Plugin onto the
// build classpath than the one AGP bundles.
plugins {
    alias(libs.plugins.android.application) apply false
    // The generator module in :baselineprofile is a com.android.test module. It is
    // declared here so AGP lands on the build classpath once with a known version.
    // Requesting it with a version from the subproject alone fails, because AGP is
    // already on the classpath by then and Gradle cannot check the two against
    // each other.
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

/**
 * The language hygiene gate from MASTER_BUILD_PROMPT section 17.
 *
 * Every one of these is a build test rather than a proofreading pass, because a
 * proofreading pass is something that happens once and then stops happening.
 */
abstract class VerifyLanguageHygiene : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun verify() {
        val failures = mutableListOf<String>()

        for (file in sources.files.sortedBy { it.path }) {
            if (!file.isFile) continue
            file.readLines().forEachIndexed { index, line ->
                val where = "${file.name}:${index + 1}"

                if (line.contains(EM_DASH)) failures += "$where em dash"
                if (line.contains(EN_DASH)) failures += "$where en dash"

                line.forEach { character ->
                    if (character.code > 127 && character !in ALLOWED_NON_ASCII) {
                        failures += "$where non ASCII character U+%04X".format(character.code)
                    }
                }

                BRITISH_FORMS.forEach { (pattern, american) ->
                    if (pattern.containsMatchIn(line)) {
                        failures += "$where British spelling, use $american"
                    }
                }
            }
        }

        val output = report.get().asFile
        output.parentFile.mkdirs()
        if (failures.isEmpty()) {
            output.writeText("clean: ${sources.files.size} files checked\n")
            return
        }
        output.writeText(failures.joinToString("\n"))
        throw GradleException(
            "language hygiene failed with ${failures.size} problem(s):\n" +
                failures.take(40).joinToString("\n") { "  $it" },
        )
    }

    private companion object {
        // Written as escapes so this file stays pure ASCII and passes its own check.
        const val EM_DASH = '\u2014'
        const val EN_DASH = '\u2013'

        /**
         * The one character allowed above ASCII. MASTER_BUILD_PROMPT 2.8 bans
         * non ASCII "outside standard punctuation", and both authority documents
         * then specify UI strings that use a middle dot as a separator, in the
         * About version line and the Report eyebrow. It is standard punctuation
         * and it is required copy, so it is permitted here and nowhere else.
         */
        val ALLOWED_NON_ASCII = setOf('\u00B7')

        /**
         * Stems where American English uses `-ize` and British uses `-ise`.
         *
         * Curated rather than a blanket `is(e|ed|ing)` rule, because a great many
         * `-ise` words are correct in American English too: advertise, exercise,
         * surprise, compromise, franchise, improvise, supervise, televise, revise,
         * devise, disguise, enterprise. A blanket rule fails the build on correct
         * copy, which is worse than missing a word, because a gate people have to
         * argue with is a gate people switch off.
         */
        val ISE_STEMS = listOf(
            "apologi", "authori", "categori", "critici", "customi", "digiti",
            "emphasi", "finali", "generali", "hospitali", "initiali", "maximi",
            "memori", "minimi", "normali", "optimi", "organi", "personali",
            "prioriti", "randomi", "reali", "recogni", "seriali", "speciali",
            "stabili", "standardi", "sterili", "summari", "synchroni", "utili",
            "visuali",
        )

        /**
         * Stems where American English uses `-yze`.
         *
         * `paralys` is here because the earlier `analys` pattern could not catch it:
         * the word boundary before `analys` never matches inside `paralysing`, and
         * the word reached a committed document before anyone noticed. That is the
         * argument for this whole list being longer than it feels like it needs.
         *
         * The noun `paralysis` is correct in American English and is deliberately
         * not matched, which is why these patterns require a letter after the stem.
         */
        val YSE_STEMS = listOf("analys", "paralys", "catalys")

        /** Everything that is not a regular family. */
        val IRREGULAR_BRITISH = listOf(
            "colour" to "color",
            "behaviour" to "behavior",
            "favourite" to "favorite",
            "honour" to "honor",
            "neighbour" to "neighbor",
            "labour" to "labor",
            "humour" to "humor",
            "flavour" to "flavor",
            "rumour" to "rumor",
            "endeavour" to "endeavor",
            "vapour" to "vapor",
            "valour" to "valor",
            "armour" to "armor",
            "harbour" to "harbor",
            "odour" to "odor",
            "saviour" to "savior",
            "splendour" to "splendor",
            "licence" to "license",
            "defence" to "defense",
            "offence" to "offense",
            "pretence" to "pretense",
            "practise" to "practice",
            "centre" to "center",
            "theatre" to "theater",
            "fibre" to "fiber",
            "litre" to "liter",
            "calibre" to "caliber",
            "sombre" to "somber",
            "spectre" to "specter",
            "lustre" to "luster",
            "travelling" to "traveling",
            "travelled" to "traveled",
            "cancelling" to "canceling",
            "cancelled" to "canceled",
            "labelling" to "labeling",
            "labelled" to "labeled",
            "modelling" to "modeling",
            "modelled" to "modeled",
            "signalling" to "signaling",
            "signalled" to "signaled",
            "levelled" to "leveled",
            "fuelled" to "fueled",
            "marvellous" to "marvelous",
            "skilful" to "skillful",
            "wilful" to "willful",
            "fulfil" to "fulfill",
            "enrol" to "enroll",
            "instalment" to "installment",
            "judgement" to "judgment",
            "ageing" to "aging",
            "cosy" to "cozy",
            "learnt" to "learned",
            "spelt" to "spelled",
            "dreamt" to "dreamed",
            "programme" to "program",
            "aluminium" to "aluminum",
            "jewellery" to "jewelry",
            "storey" to "story",
            "tyre" to "tire",
            "kerb" to "curb",
            "cheque" to "check",
            "draught" to "draft",
            "mould" to "mold",
            "smoulder" to "smolder",
            "sceptic" to "skeptic",
            "aeroplane" to "airplane",
            "plough" to "plow",
            "gaol" to "jail",
            "whilst" to "while",
            "amongst" to "among",
        )

        /**
         * `grey` is deliberately absent. The design system uses it, it is not on the
         * list MASTER_BUILD_PROMPT 2.9 names, and both spellings are current in
         * American English.
         */
        val BRITISH_FORMS: List<Pair<Regex, String>> = buildList {
            ISE_STEMS.forEach { stem ->
                add(
                    Regex("""\b${stem}s(e|es|ed|ing|ation|ations)\b""", RegexOption.IGNORE_CASE)
                        to "${stem}z...",
                )
            }
            YSE_STEMS.forEach { stem ->
                add(
                    Regex("""\b${stem}(e|es|ed|ing)\b""", RegexOption.IGNORE_CASE)
                        to "${stem.dropLast(1)}z...",
                )
            }
            IRREGULAR_BRITISH.forEach { (british, american) ->
                add(Regex("""\b${british}""", RegexOption.IGNORE_CASE) to american)
            }
        }
    }
}

val verifyLanguageHygiene = tasks.register<VerifyLanguageHygiene>("verifyLanguageHygiene") {
    group = "verification"
    description = "Fails on an em dash, an en dash, a non ASCII character or a British spelling."
    sources.from(
        fileTree(layout.projectDirectory) {
            include("**/*.kt", "**/*.kts", "**/*.xml", "**/*.md", "**/*.pro")
            exclude(
                "**/build/**",
                "**/.gradle/**",
                "**/.git/**",
                // The superseded review documents are history, not shipped text.
                "rationale/**",
                // Third party license texts are not ours to edit.
                "app/src/main/res/raw/**",
                // This file holds the patterns it would otherwise match on itself.
                "build.gradle.kts",
            )
        },
    )
    report.set(layout.buildDirectory.file("reports/language-hygiene.txt"))
}

tasks.register("verifyClarity") {
    group = "verification"
    description = "Every automated gate from MASTER_BUILD_PROMPT section 17 that can run offline."
    dependsOn(verifyLanguageHygiene)
    dependsOn(":app:verifyNoInternetPermission")
    dependsOn(":app:testDebugUnitTest")
}
