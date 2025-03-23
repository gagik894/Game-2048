package com.play.game_2048.ui

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
import com.play.game_2048.data.model.GameMode
import com.play.game_2048.data.model.GameState
import com.play.game_2048.data.model.GameStateHistoryEntry
import com.play.game_2048.util.deplacement
import com.play.game_2048.util.plateauInitial
import com.play.game_2048.util.plateauVide

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
    val interstitialAdState = rememberInterstitialAd("ca-app-pub-3940256099942544/1033173712") // Test ID
    val rewardedAdState = rememberRewardedAd("ca-app-pub-3940256099942544/5224354917") // Test ID
    val rewardedInterstitialAdState = rememberRewardedInterstitialAd("ca-app-pub-3940256099942544/5354046379") // Test ID

    // Helper function to navigate to game board while preserving back stack
    fun navigateToGameBoard(gameMode: GameMode) {
        navController.navigate("game_board/${gameMode.name}")
    }

    // Helper function to navigate to home while preserving back stack
    fun navigateToHome() {
        navController.navigate(Routes.MODE_SELECTION)
    }
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.MODE_SELECTION,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.MODE_SELECTION) {
                GameModeSelection { gameMode ->
                    // Navigate first, then try to show ad
                    navigateToGameBoard(gameMode)
                    showInterstitialAd(
                        interstitialAd = interstitialAdState.interstitialAd,
                        activity = activity,
                        onAdClosed = {
                            interstitialAdState.clearAd()  // Clear the current ad instance
                            interstitialAdState.loadAd()   // Load the next ad
                        }
                    )
                }
            }
            
            composable(
                route = Routes.GAME_BOARD,
                arguments = listOf(navArgument("gameMode") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameModeString = backStackEntry.arguments?.getString("gameMode") ?: GameMode.CLASSIC.name
                val gameMode = GameMode.valueOf(gameModeString)
                
                // Use gameMode as the key for remember to ensure state reset on mode change
                var gameState by remember(gameMode) { 
                    val initialScore = if (gameMode == GameMode.CLASSIC) 0 else 100
                    val emptyState = GameState(
                        plateau = plateauVide(gameMode.size),
                        boardSize = gameMode.size,
                        score = initialScore
                    )
                    val initialPlateau = plateauInitial(emptyState)
                    mutableStateOf(
                        GameState(
                            plateau = initialPlateau,
                            id = emptyState.id,
                            boardSize = gameMode.size,
                            score = initialScore
                        )
                    )
                }
                
                var oldBoard by remember(gameMode) { mutableStateOf<List<List<Tile>>>(emptyList()) }
                
                LaunchedEffect(gameMode) {
                    if (oldBoard.isEmpty()) {
                        oldBoard = gameState.plateau
                    }
                }
                
                Board(
                    gameState = gameState,
                    oldBoard = oldBoard,
                    move = { direction ->
                        val currentBoard = gameState.plateau.map { row -> row.toList() }
                        val currentHistory = gameState.moveHistory.toMutableList()
                        if (currentHistory.size >= 5) {
                            currentHistory.removeAt(0)
                        }
                        currentHistory.add(
                            GameStateHistoryEntry(
                                plateau = currentBoard,
                                score = gameState.score,
                                id = gameState.id
                            )
                        )
                        val newState = deplacement(gameState, direction)
                        if (newState.plateau != gameState.plateau) {
                            oldBoard = currentBoard
                            gameState = newState.copy(moveHistory = currentHistory)
                        }
                    },
                    replay = {
                        // Reset game state first, then show rewarded ad
                        val resetEmptyState = GameState(
                            plateau = plateauVide(gameMode.size),
                            boardSize = gameMode.size,
                            score = 100
                        )
                        val resetInitialPlateau = plateauInitial(resetEmptyState)
                        oldBoard = gameState.plateau
                        gameState = GameState(
                            plateau = resetInitialPlateau,
                            id = resetEmptyState.id,
                            boardSize = gameMode.size,
                            score = 100,
                            moveHistory = emptyList()
                        )
                        
                        showRewardedInterstitialAd(
                            rewardedInterstitialAdState.rewardedInterstitialAd,
                            activity,
                            onUserEarnedReward = {},
                            onAdClosed = {
                                rewardedInterstitialAdState.loadAd()
                            }
                        )
                    },
                    showAd = {
                        // Reset game state first, then try to show ad
                        val resetEmptyState = GameState(
                            plateau = plateauVide(gameMode.size),
                            boardSize = gameMode.size
                        )
                        val resetInitialPlateau = plateauInitial(resetEmptyState)
                        oldBoard = gameState.plateau
                        gameState = GameState(
                            plateau = resetInitialPlateau,
                            id = resetEmptyState.id,
                            boardSize = gameMode.size,
                            moveHistory = emptyList()
                        )

                        showInterstitialAd(
                            interstitialAd = interstitialAdState.interstitialAd,
                            activity = activity,
                            onAdClosed = {
                                interstitialAdState.clearAd()  // Clear the current ad instance
                                interstitialAdState.loadAd()   // Load the next ad
                            }
                        )
                    },
                    goBackWithRewardedAd = { onRewardEarned ->
                        showRewardedAd(
                            rewardedAd = rewardedAdState.rewardedAd,
                            activity = activity,
                            onUserEarnedReward = {
                                onRewardEarned()
                            },
                            onAdClosed = {
                                rewardedAdState.loadAd()
                            }
                        )
                    },
                    onGameModeSelected = { newMode ->
                        // Navigate first, then try to show ad
                        navigateToGameBoard(newMode)
                        showInterstitialAd(
                            interstitialAd = interstitialAdState.interstitialAd,
                            activity = activity,
                            onAdClosed = {
                                interstitialAdState.clearAd()  // Clear the current ad instance
                                interstitialAdState.loadAd()   // Load the next ad
                            }
                        )
                    },
                    onGameStateUpdate = { newState, newOldBoard ->
                        gameState = newState
                        oldBoard = newOldBoard
                    },
                    onNavigateToHome = {
                        // Navigate first, then try to show ad
                        navigateToHome()
                        showInterstitialAd(
                            interstitialAd = interstitialAdState.interstitialAd,
                            activity = activity,
                            onAdClosed = {
                                interstitialAdState.clearAd()  // Clear the current ad instance
                                interstitialAdState.loadAd()   // Load the next ad
                            }
                        )
                    }
                )
            }
        }
    }
}
