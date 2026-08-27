import com.android.build.api.artifact.SingleArtifact
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
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
val versionMajor = 0
val versionMinor = 5
val versionPatch = 0

android {
    namespace = "com.kamsiob.claritynow"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.kamsiob.claritynow"
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
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["appLabel"] = "Clarity Now debug"
        }
        release {
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
