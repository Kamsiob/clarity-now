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
         * Only the forms that are unambiguously British. `grey` is absent on
         * purpose: the design system uses it and it is not on the list of words
         * MASTER_BUILD_PROMPT 2.9 names.
         */
        val BRITISH_FORMS = listOf(
            Regex("""\bcolour""", RegexOption.IGNORE_CASE) to "color",
            Regex("""\blicence""", RegexOption.IGNORE_CASE) to "license",
            Regex("""\bbehaviour""", RegexOption.IGNORE_CASE) to "behavior",
            Regex("""\bfavourite""", RegexOption.IGNORE_CASE) to "favorite",
            Regex("""\bhonour""", RegexOption.IGNORE_CASE) to "honor",
            Regex("""\bneighbour""", RegexOption.IGNORE_CASE) to "neighbor",
            Regex("""\borganis(e|ed|ing|ation)""", RegexOption.IGNORE_CASE) to "organize",
            Regex("""\bprioritis(e|ed|ing|ation)""", RegexOption.IGNORE_CASE) to "prioritize",
            Regex("""\brecognis(e|ed|ing)""", RegexOption.IGNORE_CASE) to "recognize",
            Regex("""\banalys(e|ed|ing)\b""", RegexOption.IGNORE_CASE) to "analyze",
            Regex("""\bcentre\b""", RegexOption.IGNORE_CASE) to "center",
            Regex("""\bdefence\b""", RegexOption.IGNORE_CASE) to "defense",
            Regex("""\bwhilst\b""", RegexOption.IGNORE_CASE) to "while",
            Regex("""\bamongst\b""", RegexOption.IGNORE_CASE) to "among",
            Regex("""\btravelling\b""", RegexOption.IGNORE_CASE) to "traveling",
        )
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
