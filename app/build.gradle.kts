import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    // Temporarily disabled due to Kotlin 2.0 KAPT compatibility - using manual DI
    // id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
    // Temporarily disabled for simple test app
    // id("org.jetbrains.kotlin.plugin.compose")
    // id("androidx.baselineprofile")
    // Code Quality plugins
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt") // Re-enabled for code quality checks
    id("org.jetbrains.dokka")
    id("jacoco")
    // Firebase plugins commented out - optional feature
    // id("com.google.gms.google-services")
    // id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.heartlessveteran.myriad"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.heartlessveteran.myriad"
        minSdk = 24
        targetSdk = 35
        versionCode = 2 // Increment for new release
        versionName = "1.0.1" // Updated to new user-facing version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Load Gemini API key from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("geminiApiKey", "")}\"")
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

            // Enable split APKs by ABI for smaller downloads
            ndk {
                abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            }
        }

        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
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
        // Compose disabled for simple test
        // compose = true
        buildConfig = true
    }

    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.15"
    // }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android - minimal dependencies for testing release build
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.activity:activity:1.11.0")

    // Baseline Profile dependency - commented out for now
    // baselineProfile(project(":baselineprofile"))

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
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
