// Top-level build file for Project Myriad - Pure Android Kotlin Application

buildscript {
    extra.apply {
        set("buildToolsVersion", "35.0.0")
        set("minSdkVersion", 24)
        set("compileSdkVersion", 35)
        set("targetSdkVersion", 35)
        set("kotlinVersion", "2.1.0")
        set("composeVersion", "2024.02.00")
        set("hiltVersion", "2.48")
        set("roomVersion", "2.6.1")
        set("retrofitVersion", "2.9.0")
        set("coilVersion", "2.5.0")
    }
}

plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    // KSP and Hilt temporarily disabled due to Kotlin 2.2.20 compatibility
    // id("com.google.devtools.ksp") version "2.2.20-1.0.30" apply false
    // id("com.google.dagger.hilt.android") version "2.57.1" apply false
    // Code Quality plugins
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
    // Firebase plugins - optional features
    // id("com.google.gms.google-services") version "4.4.0" apply false
    // id("com.google.firebase.crashlytics") version "2.9.9" apply false
}
