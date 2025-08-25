package com.projectmyriad

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Project Myriad Application class.
 * Entry point for the Android application with Hilt dependency injection.
 */
@HiltAndroidApp
class ProjectMyriadApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide components here
    }
}