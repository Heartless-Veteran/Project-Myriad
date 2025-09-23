import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.baseline.profile)
    // Code Quality plugins
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    id("jacoco")
    // Security plugins
    alias(libs.plugins.owasp.dependency.check)
    // Firebase plugins commented out - optional feature
    // id("com.google.gms.google-services")
    // id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.heartlessveteran.myriad"
    compileSdk =
        libs.versions.compile.sdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.heartlessveteran.myriad"
        minSdk =
            libs.versions.min.sdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.target.sdk
                .get()
                .toInt()
        versionCode = 2 // Increment for new release
        versionName = "1.0.1" // Updated to new user-facing version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Phase 5: Localization support - Multi-language support  
    androidResources {
        localeFilters += listOf(
            "en", "es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh-rCN", "zh-rTW", "ar", "hi"
        )
    }

    // Load signing properties from local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    // Signing configurations
    signingConfigs {
        create("release") {
            val keystoreFile = file("myriad-release-key.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = localProperties.getProperty("MYRIAD_RELEASE_STORE_PASSWORD")
                keyAlias = "myriad-key-alias"
                keyPassword = localProperties.getProperty("MYRIAD_RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            // Link the signing configuration for release builds
            signingConfig = signingConfigs.getByName("release")

            // Phase 4: Enhanced release optimizations
            isDebuggable = false
            isPseudoLocalesEnabled = false
            isCrunchPngs = true

            // Phase 5: Performance optimizations for release
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")

            // Enable split APKs by ABI for smaller downloads
            ndk {
                abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            }
        }

        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            
            // Phase 6: Enhanced debug configuration
            isDebuggable = true
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
        }

        // Phase 6: Staging build type for final QA
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
            isDebuggable = false
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
        }
    }

    // Enable App Bundle optimizations
    bundle {
        abi {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        language {
            enableSplit = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion =
            libs.versions.compose.compiler
                .get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)

    // Compose BOM and UI
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.ui)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room Database (needed for DIContainer)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Core modules
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))

    // Feature modules
    implementation(project(":feature:browser"))
    implementation(project(":feature:vault"))
    implementation(project(":feature:ai"))
    implementation(project(":feature:reader"))
    implementation(project(":feature:settings"))

    // Media3 ExoPlayer for video playback
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    // Baseline Profile dependency - commented out for now
    // baselineProfile(project(":baselineprofile"))

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.android.testing)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.bundles.compose.ui.testing)

    debugImplementation(libs.bundles.compose.ui.debug)
}

// Code Quality Configurations
ktlint {
    version.set("1.3.1")
    debug.set(true)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)

    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }

    // Disable some rules that conflict with Compose conventions
    additionalEditorconfig.put("ktlint_standard_function-naming", "disabled")
    additionalEditorconfig.put("ktlint_standard_no-wildcard-imports", "disabled")
}

detekt {
    toolVersion = "1.23.8"
    config.setFrom(file("../config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true

    source.setFrom(files("src/main/kotlin", "src/test/kotlin"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

// JaCoCo Test Coverage Configuration
jacoco {
    toolVersion = "0.8.13"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter =
        listOf(
            "**/R.class",
            "**/R\$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/databinding/**",
            "**/generated/**",
        )

    val debugTree =
        fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
            exclude(fileFilter)
        }

    val mainSrc = "${project.projectDir}/src/main/kotlin"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(
        fileTree(project.layout.buildDirectory.get()) {
            include("**/*.exec", "**/*.ec")
        },
    )
}

// Dokka API Documentation Configuration - temporarily simplified
// Full V2 configuration can be added later when plugin stabilizes

// OWASP Dependency Check Configuration
dependencyCheck {
    // The path to the dependency-check database
    autoUpdate = true
    format = "ALL"

    // Fail build on CVSS score above threshold (set to 7.0 for high-severity vulnerabilities)
    failBuildOnCVSS = 7.0f

    // Reports directory
    outputDirectory = "${project.layout.buildDirectory.get()}/reports/dependency-check"

    // Analyzer configurations
    analyzers {
        experimentalEnabled = false
        archiveEnabled = true
        jarEnabled = true
        nodeEnabled = false
    }
}
