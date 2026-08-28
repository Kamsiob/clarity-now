import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

/**
 * MASTER_BUILD_PROMPT 19, phase 13. The module that generates
 * `app/src/main/baseline-prof.txt` by driving the real app on a real device.
 *
 * **This module produces a build input, not a test result.** Nothing here runs in
 * `verifyClarity` and nothing here gates a commit. It is run deliberately, on a
 * connected phone, when the startup path has changed:
 *
 * ```
 * ./gradlew :app:generateBaselineProfile
 * ```
 *
 * and the generated file is committed, because the release build has to be able to
 * package a profile on a machine with no device attached.
 *
 * **`com.android.test` rather than a source set inside `:app`.** A macrobenchmark has
 * to install and cold start the app under test as a separate process, which it cannot
 * do from inside that app's own instrumentation. The separate module is the only shape
 * that measures a real cold start rather than a warm one.
 */
android {
    namespace = "com.kamsiob.claritynow.baselineprofile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // A baseline profile is only installed without root from API 33 up. Below that
        // the generator cannot write one, so the floor here is higher than the app's.
        minSdk = 33
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
}

// The generator drives the app the way a person does, so it runs against the variant a
// person installs, on the phone that is plugged in.
//
// Generation stays a deliberate act rather than a step in a release build, because a
// release build on a machine with no phone attached has to succeed. That is the
// plugin's default (`automaticGenerationDuringBuild` is off unless it is turned on in
// the consumer's `baselineProfile` block, not this one), so it is left unset here
// rather than restated, and this comment is the record that it was checked.
baselineProfile {
    useConnectedDevices = true
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        // MASTER_BUILD_PROMPT 17 applies to every module, not only to :app.
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
