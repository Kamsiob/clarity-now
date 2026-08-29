import com.android.build.api.artifact.SingleArtifact
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.baselineprofile)
}

// Semantic version, chosen per MASTER_BUILD_PROMPT 16.7.
// versionCode is derived as major * 10000 + minor * 100 + patch.
//
// 0.4.0: Addendum 01's schema window, and phase 3b, the executive function retrofit.
// A minor bump rather than a patch because the event catalog went from 24 types to 28
// and one was renamed, which is a change to the contract in docs/EVENT_FORMAT.md that
// a second implementation is built from. Not a major one because nothing a person can
// see behaves differently than it did, apart from being calmer when they ask for it.
// 0.4.1: phase 3c, the foundations half of the polish pass. A patch rather than a
// minor bump because nothing new can be done with the app: the value ladder, the
// tracking and the type steps correct things that were already specified or already
// wrong, and no contract, capability or event changed.
// 0.5.0: phase 4, focus sessions and the first Contemplative surface. A minor bump
// because the app can now do something it could not do before, and because the
// Contemplative world arrives with it.
// 0.6.0: phase 5, the engine's five layers and the simulator. A minor bump even
// though nothing on screen changed, because the thing phases 6 through 8 render now
// exists and is the largest single addition since the event log itself.
// 0.7.0: phase 6, the Pulse. The first screen that renders a sentence the engine
// wrote about a person's own life, rather than a label or a readout.
// 0.8.0: phases 7 and 8, Momentum and the Report. All four tabs now render, and all
// three Contemplative worlds exist: the indigo Focus night, the amber Pulse night and
// the gold Report.
// 0.9.0: phases 10, 11, 12, 12b, the routing pass, the accessibility pass and the four
// executive function items 14b assigned to phase 8. A minor bump rather than a patch
// because the app can now do three things it could not: adjust its own text size,
// reach a surface from a widget or a shortcut, and withhold a decline observation
// from somebody whose dip has a precedent. Not a major bump because 1.0 is the
// release, and phase 9 has not written a line of it yet.
// 0.10.0: phase 9, the corpus, plus 12c and the archive view. A minor bump because the
// app says three thousand things it could not say yesterday, and because two screens
// arrived: the one a returning person meets, and the one that gets an archived area
// back. The version is 0.10.0 rather than 0.9.1 because a patch is for a correction and
// this triples the language. versionCode is 1000, which is why the scheme multiplies
// minor by a hundred.
// 0.10.1: the reach pass and its follow ups. A patch rather than a minor bump because
// nothing new can be done with the app: every change corrects something that was already
// specified and already wrong, and the largest of them is eight lines restoring a bound
// three documents already stated.
// 0.11.0: phase 9b, layer six, the last unbuilt phase. A minor bump because the app can
// now do something it could not: end a report with one optional, completable thing and
// remember it in the person's own words if they take it. Not a major bump because 1.0 is
// the release and the closing device pass has not run.
val versionMajor = 0
val versionMinor = 11
val versionPatch = 0

// The application id and the one suffix that changes it, written once.
//
// `res/xml/shortcuts.xml` needs the running package name inside an intent, and a
// resource file is one of the few places `${applicationId}` does not reach: manifest
// placeholders are substituted in the manifest and nowhere else. So the id is generated
// as a string resource per build type below. A literal in the resource would have named
// the release package on every debug install, which is the build every device check
// runs on, and three shortcuts that do nothing is a defect nothing in the build can see.
val clarityApplicationId = "com.kamsiob.claritynow"
val clarityDebugSuffix = ".debug"

android {
    namespace = "com.kamsiob.claritynow"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = clarityApplicationId
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schemas are committed so migrations can be written against a known shape.
        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    }

    buildTypes {
        debug {
            applicationIdSuffix = clarityDebugSuffix
            versionNameSuffix = "-debug"
            manifestPlaceholders["appLabel"] = "Clarity Now debug"
            resValue(
                "string",
                "clarity_application_id",
                clarityApplicationId + clarityDebugSuffix,
            )
        }
        release {
            resValue("string", "clarity_application_id", clarityApplicationId)
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["appLabel"] = "Clarity Now"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        // The widget provider needs the application id as a resource, because a Glance
        // provider's metadata is XML and cannot read BuildConfig. The debug variant
        // carries the suffix, so the two builds can be installed side by side without
        // their widgets colliding.
        resValues = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/*.version",
        )
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// Regenerating the golden fixture is deliberate, never a side effect of a test run:
//   ./gradlew :app:testDebugUnitTest -PregenerateGolden=true
tasks.withType<Test>().configureEach {
    systemProperty(
        "clarity.regenerateGolden",
        providers.gradleProperty("regenerateGolden").getOrElse("false"),
    )
    // Every file `CorpusFixture` opens off the repository root at runtime, rather than off the
    // test classpath. Gradle cannot see those reads, so without this the task reports
    // UP-TO-DATE after a corpus edit and every gate over the corpus goes green without
    // running. Found in phase 9: `verifyClarity` passed in 499ms over two thousand lines it
    // had never read. A gate that only runs when somebody remembers to force it is the
    // failure this repository documents in three other places.
    //
    // The list is `CorpusFixture`'s and has to move with it: the three volumes, the anchors
    // file `CorpusAnchorsTest` verifies character for character, and the engine specification,
    // whose stated totals `CorpusTotalsAuditTest` recounts.
    inputs.files(
        rootProject.layout.projectDirectory.file("CORPUS_1_PULSE.md"),
        rootProject.layout.projectDirectory.file("CORPUS_2_REPORT.md"),
        rootProject.layout.projectDirectory.file("CORPUS_3_MOMENTUM.md"),
        rootProject.layout.projectDirectory.file("CLARITY_LOGIC_ENGINE.md"),
        rootProject.layout.projectDirectory.file("docs/CORPUS_ANCHORS.md"),
    ).withPropertyName("corpusFiles").withPathSensitivity(PathSensitivity.RELATIVE)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        // MASTER_BUILD_PROMPT 17: zero warnings, treated as errors.
        allWarningsAsErrors.set(true)
    }
}

/**
 * MASTER_BUILD_PROMPT 2.4 and 17. The privacy policy tells people this app cannot
 * open a network connection and invites them to check it in Android settings. That
 * only stays true if something checks it here, on the merged manifest, every build.
 */
abstract class VerifyNoInternetPermission : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mergedManifest: RegularFileProperty

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun verify() {
        val manifest = mergedManifest.get().asFile.readText()
        val found = NETWORK_PERMISSIONS.filter { manifest.contains(it) }
        val output = report.get().asFile
        output.parentFile.mkdirs()
        if (found.isNotEmpty()) {
            output.writeText(found.joinToString("\n"))
            throw GradleException(
                "the merged manifest declares a network permission, which this app " +
                    "promises it never will: ${found.joinToString()}. Find the dependency " +
                    "that added it and remove the dependency.",
            )
        }
        output.writeText("no network permission in the merged manifest\n")
    }

    private companion object {
        val NETWORK_PERMISSIONS = listOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.CHANGE_NETWORK_STATE",
            "android.permission.CHANGE_WIFI_STATE",
        )
    }
}

/**
 * CLARITY_LOGIC_ENGINE.md 12: the simulator lives in `devtools`, debug builds only.
 *
 * **Putting a file in `src/debug` is the mechanism, not the verification.** Android Gradle
 * compiles that directory into the debug variant and no other, which is exactly why the
 * failure mode is silent: the day somebody moves one simulator class into `src/main` so a
 * screen can reach it, or widens a source set, nothing breaks and the release build quietly
 * grows eleven synthetic personas, a year of generated event logs and a copy of the check
 * suite. So this reads what Gradle actually resolved for each source set and fails if the
 * arrangement is not the one that was promised.
 *
 * Three things, and the first is the one that is usually missing. A verification that only
 * looked for the package where it must not be would pass on a repository where the
 * simulator had been deleted.
 *
 * 1. The devtools package **is** under a debug source directory, and is not empty
 * 2. It is **not** under any source directory the release variant compiles
 * 3. Nothing the release variant compiles **names** the package, which would not link
 */
abstract class VerifyDevtoolsAreDebugOnly : DefaultTask() {

    /** Source directories Gradle resolved for the debug source set. */
    @get:Input
    abstract val debugSourceDirs: ListProperty<String>

    /** Source directories Gradle resolved for the source sets a release build compiles. */
    @get:Input
    abstract val releaseSourceDirs: ListProperty<String>

    @get:Input
    abstract val devtoolsPackage: Property<String>

    /** Every Kotlin file a release build compiles, so the package name can be looked for. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val releaseSources: ConfigurableFileCollection

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun verify() {
        val packageName = devtoolsPackage.get()
        val packagePath = packageName.replace('.', '/')
        val failures = mutableListOf<String>()

        val present = debugSourceDirs.get()
            .map { File(it, packagePath) }
            .filter { directory -> directory.listFiles()?.any { it.extension == "kt" } == true }
        if (present.isEmpty()) {
            failures += "no Kotlin file found in $packagePath under any debug source directory, " +
                "so either the simulator is gone or it has moved somewhere the release build " +
                "can see. CLARITY_LOGIC_ENGINE.md 12 requires it in devtools, debug builds only"
        }

        releaseSourceDirs.get()
            .map { File(it, packagePath) }
            .filter { it.isDirectory }
            .forEach { failures += "$packagePath exists under a release source directory: $it" }

        for (file in releaseSources.files.sortedBy { it.path }) {
            if (!file.isFile) continue
            if (file.readText().contains(packageName)) {
                failures += "${file.path} names $packageName, which is not in the release variant"
            }
        }

        val output = report.get().asFile
        output.parentFile.mkdirs()
        if (failures.isNotEmpty()) {
            output.writeText(failures.joinToString("\n"))
            throw GradleException(
                "the simulator is not debug only:\n" + failures.joinToString("\n") { "  $it" },
            )
        }
        output.writeText(
            "$packageName is present in ${present.size} debug source directory(s) " +
                "and absent from every release one\n",
        )
    }
}

/**
 * The source directories each side of the line.
 *
 * These are the conventional Android layout paths rather than a read of the Gradle
 * model. Reading the model would be better, because a `sourceSets` block that added
 * the debug directory to `main` would then be caught here rather than by inspection,
 * but the source set API does not expose its directories as a property in this AGP
 * version and forcing it produced a build file that did not compile.
 *
 * The gap is covered instead by the task below, which fails if the devtools package
 * appears anywhere in the release sources it is given. If someone reroutes the source
 * sets, this list is what needs updating, and that is stated here rather than assumed.
 */
private val DEVTOOLS_DEBUG_DIRS = listOf("src/debug/java")
private val DEVTOOLS_RELEASE_DIRS = listOf("src/main/java", "src/release/java")

val devtoolsDebugDirs = DEVTOOLS_DEBUG_DIRS.map { layout.projectDirectory.dir(it).asFile.path }
val devtoolsReleaseDirs =
    DEVTOOLS_RELEASE_DIRS.map { layout.projectDirectory.dir(it).asFile.path }

val verifyDevtoolsAreDebugOnly = tasks.register<VerifyDevtoolsAreDebugOnly>("verifyDevtoolsAreDebugOnly") {
    group = "verification"
    description = "Fails if the devtools simulator is reachable from a release build."
    debugSourceDirs.set(devtoolsDebugDirs)
    releaseSourceDirs.set(devtoolsReleaseDirs)
    devtoolsPackage.set("com.kamsiob.claritynow.devtools")
    releaseSources.from(
        DEVTOOLS_RELEASE_DIRS.map { dir ->
            fileTree(layout.projectDirectory.dir(dir)) { include("**/*.kt") }
        },
    )
    report.set(layout.buildDirectory.file("reports/devtools-debug-only.txt"))
}

// Runs inside verifyClarity, which depends on testDebugUnitTest, and blocks a release
// build outright. Matched lazily for the same reason the manifest check is: both task
// names are registered after this script is evaluated.
tasks.matching { it.name == "testDebugUnitTest" || it.name == "assembleRelease" }
    .configureEach { dependsOn(verifyDevtoolsAreDebugOnly) }

/**
 * The three corpus files, copied into the APK assets at build time.
 *
 * The engine has no sentences without them. They live at the repository root because
 * they are specification documents the owner edits and `verifyLanguageHygiene` scans,
 * and they have to be inside the APK because the catalog parses them at runtime.
 *
 * **Copied rather than duplicated.** A second set under `src/main/assets` would be two
 * corpora that drift, and the one the app shipped would not be the one anybody
 * reviewed. That is the whole reason this is a build step.
 *
 * It goes through the Variant API rather than `sourceSets.assets.srcDir`, because AGP
 * refuses a `Provider` there: it cannot tell a generated directory from a static one,
 * and a plain `srcDir` would not carry the task dependency, so a clean build could
 * package an empty assets directory and the app would start with a silent engine.
 */
abstract class CopyCorpora : DefaultTask() {

    @get:InputFiles
    abstract val corpora: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copyThem() {
        val target = outputDir.get().asFile.resolve("corpus")
        target.deleteRecursively()
        target.mkdirs()
        corpora.files.forEach { source ->
            source.copyTo(target.resolve(source.name), overwrite = true)
        }
    }
}

val copyCorpora = tasks.register<CopyCorpora>("copyCorpora") {
    group = "build"
    description = "Copies the corpus files into the APK assets, so the engine has sentences."
    corpora.from(rootProject.layout.projectDirectory.file("CORPUS_1_PULSE.md"))
    corpora.from(rootProject.layout.projectDirectory.file("CORPUS_2_REPORT.md"))
    corpora.from(rootProject.layout.projectDirectory.file("CORPUS_3_MOMENTUM.md"))
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(copyCorpora, CopyCorpora::outputDir)
    }
}

val verifyNoInternetPermission = tasks.register("verifyNoInternetPermission") {
    group = "verification"
    description = "Fails if any variant's merged manifest declares a network permission."
}

androidComponents {
    onVariants { variant ->
        val name = variant.name.replaceFirstChar { it.uppercase() }
        val task = tasks.register<VerifyNoInternetPermission>("verify${name}NoInternetPermission") {
            group = "verification"
            mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            report.set(layout.buildDirectory.file("reports/no-internet-$name.txt"))
        }
        verifyNoInternetPermission.configure { dependsOn(task) }
        // Nothing gets packaged without passing. Matched lazily because the
        // assemble tasks are registered after this callback runs.
        tasks.matching { it.name == "assemble$name" }.configureEach { dependsOn(task) }
    }
}

dependencies {
    // MASTER_BUILD_PROMPT 19, phase 13. The generator module produces
    // src/main/baseline-prof.txt, which is committed so a release can be built on a
    // machine with no phone attached. See baselineprofile/build.gradle.kts.
    baselineProfile(project(":baselineprofile"))

    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.compose.ui.test.manifest)
}
