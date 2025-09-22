package com.heartlessveteran.myriad

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Project Myriad.
 * Uses Hilt dependency injection for clean architecture and testability.
 */
@HiltAndroidApp
class MyriadApplication : Application()
