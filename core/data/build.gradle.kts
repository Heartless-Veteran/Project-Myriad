plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.heartlessveteran.myriad.core.data"
    compileSdk = libs.versions.compile.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core:domain"))
    
    // Core Android
    implementation(libs.androidx.core.ktx)
    
    // Room Database
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    
    // Network
    implementation(libs.bundles.networking)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
}