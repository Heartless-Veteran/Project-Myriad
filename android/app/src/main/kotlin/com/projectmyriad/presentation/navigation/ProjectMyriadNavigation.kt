package com.projectmyriad.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Main navigation component for Project Myriad.
 * Handles navigation between different screens using Navigation Compose.
 */
@Composable
fun ProjectMyriadNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        
        composable("library") {
            LibraryScreen(navController = navController)
        }
        
        composable("vault") {
            VaultScreen(navController = navController)
        }
        
        composable("ai_core") {
            AiCoreScreen(navController = navController)
        }
        
        composable("browser") {
            BrowserScreen(navController = navController)
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Project Myriad\nThe Definitive Manga and Anime Platform",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LibraryScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Library Screen\n(Coming Soon)",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun VaultScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "The Vault\nLocal Media Management\n(Coming Soon)",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun AiCoreScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AI Core\nIntelligent Features\n(Coming Soon)",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun BrowserScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "The Browser\nOnline Discovery\n(Coming Soon)",
            style = MaterialTheme.typography.titleLarge
        )
    }
}