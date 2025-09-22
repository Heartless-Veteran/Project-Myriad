package com.heartlessveteran.myriad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaStatus
import com.heartlessveteran.myriad.core.ui.theme.MyriadTheme
import com.heartlessveteran.myriad.ui.screens.AnimeLibraryScreen
import com.heartlessveteran.myriad.ui.viewmodel.AnimeLibraryViewModel
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Test activity demonstrating the anime and manga functionality
 */
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the DI container from the application
        val app = application as MyriadApplication
        val diContainer = app.diContainer

        setContent {
            MyriadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ProjectMyriadDemo(diContainer)
                }
            }
        }

        // Initialize test data
        lifecycleScope.launch {
            initializeTestData(diContainer)
        }
    }

    private suspend fun initializeTestData(diContainer: DIContainer) {
        try {
            // Create a test manga
            val testManga =
                Manga(
                    title = "Test Manga",
                    author = "Test Author",
                    description = "A test manga for demonstrating database functionality",
                    status = MangaStatus.ONGOING,
                    genres = listOf("Action", "Adventure"),
                    isInLibrary = true,
                    dateAdded = Date(),
                )

            // Save to database
            diContainer.mangaRepository.saveManga(testManga)
        } catch (e: Exception) {
            // Handle error
        }
    }
}

@Composable
fun ProjectMyriadDemo(diContainer: DIContainer) {
    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Project Myriad - Demo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Demo info card
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "🎌 Project Myriad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "The Definitive Manga and Anime Platform",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "✅ Anime functionality implemented",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "✅ Video player with ExoPlayer integration",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "✅ Episode management and progress tracking",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "✅ Local file support (.mp4/.mkv/.avi)",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Anime Library Demo
            val animeLibraryViewModel =
                remember {
                    AnimeLibraryViewModel(
                        getLibraryAnimeUseCase = diContainer.getLibraryAnimeUseCase,
                        getAnimeDetailsUseCase = diContainer.getAnimeDetailsUseCase,
                        addAnimeToLibraryUseCase = diContainer.addAnimeToLibraryUseCase,
                        searchLibraryAnimeUseCase = diContainer.searchLibraryAnimeUseCase,
                    )
                }

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "🎬 Anime Library Demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "This demonstrates the anime library screen with sample data",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Anime Library Screen
            AnimeLibraryScreen(
                modifier = Modifier.weight(1f),
                viewModel = animeLibraryViewModel,
            )
        }
    }
}
