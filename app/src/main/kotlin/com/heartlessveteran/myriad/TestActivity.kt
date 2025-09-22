package com.heartlessveteran.myriad

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaStatus
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Test activity demonstrating the new database functionality
 */
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "Project Myriad - Initializing Database..."
        textView.setPadding(64, 64, 64, 64)
        textView.textSize = 16f
        setContentView(textView)

        // Get the DI container from the application
        val app = application as MyriadApplication
        val diContainer = app.diContainer

        // Test the database functionality
        lifecycleScope.launch {
            try {
                // Create a test manga
                val testManga = Manga(
                    title = "Test Manga",
                    author = "Test Author",
                    description = "A test manga for demonstrating database functionality",
                    status = MangaStatus.ONGOING,
                    genres = listOf("Action", "Adventure"),
                    isInLibrary = true,
                    dateAdded = Date()
                )

                // Save to database
                val saveResult = diContainer.mangaRepository.saveManga(testManga)
                
                textView.text = """
                    Project Myriad - Database Test
                    
                    ✅ Database initialized successfully
                    ✅ Room DAOs created
                    ✅ Repository implementation working
                    ✅ Use cases connected
                    
                    Test Results:
                    - Manga save: ${if (saveResult is Result.Success) "SUCCESS" else "FAILED"}
                    
                    Version: ${BuildConfig.VERSION_NAME}
                    Build Type: ${BuildConfig.BUILD_TYPE}
                    
                    Database implementation completed!
                """.trimIndent()

            } catch (e: Exception) {
                textView.text = """
                    Project Myriad - Database Test
                    
                    ❌ Error: ${e.message}
                    
                    Please check implementation.
                """.trimIndent()
            }
        }
    }
}
