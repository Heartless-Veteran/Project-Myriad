package com.heartlessveteran.myriad

import android.app.Application

/**
 * Application class for Project Myriad.
 * Uses manual dependency injection temporarily due to KSP compatibility issues.
 */
class MyriadApplication : Application() {
    lateinit var diContainer: DIContainer
        private set

    override fun onCreate() {
        super.onCreate()
        diContainer = DIContainer(this)
    }
}
