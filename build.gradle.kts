// Top-level build file for Project Myriad - Pure Android Kotlin Application

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.baseline.profile) apply false
    // Code Quality plugins
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.owasp.dependency.check) apply false
    // Firebase plugins - optional features
    // id("com.google.gms.google-services") version "4.4.0" apply false
    // id("com.google.firebase.crashlytics") version "2.9.9" apply false
}
