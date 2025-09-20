package com.heartlessveteran.myriad.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.ui.components.VideoPlayer

/**
 * Demo screen to showcase the video player functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerDemoScreen() {
    var showPlayer by remember { mutableStateOf(false) }
    
    if (showPlayer) {
        // Show video player in fullscreen
        VideoPlayer(
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            modifier = Modifier.fillMaxSize(),
            onVideoComplete = {
                showPlayer = false
            }
        )
    } else {
        // Show demo interface
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopAppBar(
                title = { Text("Video Player Demo") }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Video Playback Features",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✅ ExoPlayer integration")
                    Text("✅ Custom playback controls")
                    Text("✅ Play, pause, seek, volume")
                    Text("✅ Progress tracking")
                    Text("✅ Auto-hiding controls")
                    Text("✅ Episode management")
                    Text("✅ .mp4, .mkv, .avi support")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { showPlayer = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Video Demo")
            }
        }
    }
}