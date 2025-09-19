package com.heartlessveteran.myriad

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for Project Myriad
 * The Definitive Manga and Anime Platform - Kotlin Android Edition
 */
@HiltAndroidApp
class MyriadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
