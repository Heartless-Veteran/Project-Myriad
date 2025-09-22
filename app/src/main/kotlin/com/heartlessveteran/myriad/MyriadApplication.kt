package com.heartlessveteran.myriad

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Project Myriad.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class MyriadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
