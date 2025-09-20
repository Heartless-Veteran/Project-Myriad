package com.heartlessveteran.myriad.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoPlayerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun videoPlayer_showsPlayButton_whenInitialized() {
        // Given
        val testVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

        // When
        composeTestRule.setContent {
            VideoPlayer(
                videoUrl = testVideoUrl,
                autoPlay = false
            )
        }

        // Then - verify play button exists in controls
        composeTestRule.onNodeWithContentDescription("Play").assertExists()
    }
}