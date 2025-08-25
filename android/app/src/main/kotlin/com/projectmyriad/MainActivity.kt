package com.projectmyriad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.projectmyriad.presentation.navigation.ProjectMyriadNavigation
import com.projectmyriad.presentation.theme.ProjectMyriadTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Project Myriad Android app.
 * Entry point for Jetpack Compose UI with Navigation and Material 3.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ProjectMyriadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProjectMyriadApp()
                }
            }
        }
    }
}

@Composable
fun ProjectMyriadApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        ProjectMyriadNavigation(
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectMyriadAppPreview() {
    ProjectMyriadTheme {
        ProjectMyriadApp()
    }
}