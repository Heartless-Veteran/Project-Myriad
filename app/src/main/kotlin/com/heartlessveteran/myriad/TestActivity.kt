package com.heartlessveteran.myriad

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/**
 * Simple activity for testing release build system
 */
class TestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this)
        textView.text = "Project Myriad Release Build Test\nVersion: ${BuildConfig.VERSION_NAME}\nBuild Type: ${BuildConfig.BUILD_TYPE}\nSigned: Release build created successfully!"
        textView.setPadding(64, 64, 64, 64)
        textView.textSize = 16f
        
        setContentView(textView)
    }
}