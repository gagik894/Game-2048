package com.play.game_2048

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Navigation routes
object Routes {
    const val MODE_SELECTION = "mode_selection"
    const val GAME_BOARD = "game_board/{gameMode}"
}

@Composable
fun GameApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as Activity
    
    // Setup ad states
    val interstitialAdState = rememberInterstitialAd("ca-app-pub-3940256099942544/1033173712")
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.MODE_SELECTION,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.MODE_SELECTION) {
                GameModeSelection { gameMode ->
                    navController.navigate("game_board/${gameMode.name}")
                }
            }
            
            composable(
                route = Routes.GAME_BOARD,
                arguments = listOf(navArgument("gameMode") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameModeString = backStackEntry.arguments?.getString("gameMode") ?: GameMode.CLASSIC.name
                val gameMode = GameMode.valueOf(gameModeString)
                
                var gameState by remember(gameMode) { 
                    val emptyState = GameState(plateau = plateauVide(gameMode.size), boardSize = gameMode.size)
                    val initialPlateau = plateauInitial(emptyState)
                    mutableStateOf(GameState(
                        plateau = initialPlateau, 
                        id = emptyState.id,
                        boardSize = gameMode.size
                    ))
                }
                
                // Use a separate state for oldBoard to ensure it doesn't change immediately
                var oldBoard by remember(gameMode) { mutableStateOf<List<List<Tile>>>(emptyList()) }
                
                // Initialize oldBoard if it's empty
                LaunchedEffect(gameMode) {
                    if (oldBoard.isEmpty()) {
                        oldBoard = gameState.plateau
                    }
                }
                
                Board(
                    gameState = gameState,
                    oldBoard = oldBoard,
                    move = { direction ->
                        // Store current board before updating
                        val currentBoard = gameState.plateau.map { row -> row.toList() }
                        
                        // Update game state with new move
                        val newState = deplacement(gameState, direction)
                        
                        // Only update if the board actually changed
                        if (newState.plateau != gameState.plateau) {
                            oldBoard = currentBoard
                            gameState = newState
                        }
                    },
                    replay = {
                        val resetEmptyState = GameState(
                            plateau = plateauVide(gameMode.size),
                            boardSize = gameMode.size
                        )
                        val resetInitialPlateau = plateauInitial(resetEmptyState)
                        
                        // Store current board before resetting
                        oldBoard = gameState.plateau
                        
                        // Update game state with new board
                        gameState = GameState(
                            plateau = resetInitialPlateau,
                            id = resetEmptyState.id,
                            boardSize = gameMode.size
                        )
                    },
                    showAd = {
                        // Use the ad utility from AdUnit.kt
                        showInterstitialAd(
                            interstitialAd = interstitialAdState.interstitialAd,
                            activity = activity,
                            onAdClosed = {
                                interstitialAdState.loadAd()
                            }
                        )
                    },
                    onGameModeSelected = { newMode ->
                        // Navigate to the new game mode
                        navController.navigate("game_board/${newMode.name}") {
                            // Clear back stack to prevent multiple board screens
                            popUpTo(Routes.GAME_BOARD.split("/")[0]) {
                                inclusive = true
                            }
                        }
                        
                        // Show an ad when changing game mode
                        showInterstitialAd(
                            interstitialAd = interstitialAdState.interstitialAd,
                            activity = activity,
                            onAdClosed = {
                                interstitialAdState.loadAd()
                            }
                        )
                    }
                )
            }
        }
    }
}
