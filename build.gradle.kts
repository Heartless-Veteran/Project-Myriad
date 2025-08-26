// Top-level build file for Project Myriad - Pure Android Kotlin Application

buildscript {
    extra.apply {
        set("buildToolsVersion", "35.0.0")
        set("minSdkVersion", 21)
        set("compileSdkVersion", 35)
        set("targetSdkVersion", 35)
        set("kotlinVersion", "1.9.25")
        set("composeVersion", "2024.02.00")
        set("hiltVersion", "2.48")
        set("roomVersion", "2.6.1")
        set("retrofitVersion", "2.9.0")
        set("coilVersion", "2.5.0")
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}
