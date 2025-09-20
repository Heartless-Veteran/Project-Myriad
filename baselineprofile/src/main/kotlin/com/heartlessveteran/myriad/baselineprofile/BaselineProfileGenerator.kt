package com.heartlessveteran.myriad.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates baseline profiles for Project Myriad.
 * These profiles improve app startup time and reduce jank during critical user flows.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = "com.heartlessveteran.myriad",
            // Maximum iteration to collect baseline profile
            maxIterations = 5,
            // Clears the code cache between iterations to ensure we capture
            // a clean baseline profile.
            includeInStartupProfile = true
        ) {
            // Start the app and wait for it to be fully loaded
            pressHome()
            startActivityAndWait()

            // Wait for the app to be idle
            device.wait(Until.findObject(By.pkg(packageName)), 5000)

            // Navigate through key user flows
            
            // 1. App startup and main screen
            device.waitForIdle()
            
            // 2. Navigate to library (if available)
            val libraryButton = device.findObject(By.textContains("Library"))
            if (libraryButton != null) {
                libraryButton.click()
                device.waitForIdle()
            }
            
            // 3. Navigate to browse section (if available)
            val browseButton = device.findObject(By.textContains("Browse"))
            if (browseButton != null) {
                browseButton.click()
                device.waitForIdle()
            }
            
            // 4. Scroll through content lists
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                // Scroll down and up to trigger lazy loading
                scrollable.scroll(Direction.DOWN, 0.8f)
                device.waitForIdle()
                scrollable.scroll(Direction.UP, 0.8f)
                device.waitForIdle()
            }
            
            // 5. Navigate to settings (if available)
            val settingsButton = device.findObject(By.textContains("Settings"))
            if (settingsButton != null) {
                settingsButton.click()
                device.waitForIdle()
            }
        }
    }
}