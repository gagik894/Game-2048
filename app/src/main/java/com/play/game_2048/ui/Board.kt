package com.play.game_2048.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.play.game_2048.util.Plateau
import com.play.game_2048.data.model.GameMode
import com.play.game_2048.data.model.GameState
import com.play.game_2048.util.plateauInitial
import com.play.game_2048.util.plateauVide
// Add import for HighScoreManager
import com.play.game_2048.data.model.HighScoreManager

@Composable
fun Board(
    gameState: GameState,
    oldBoard: List<List<Tile>>,
    modifier: Modifier = Modifier,
    move: (Int) -> Unit = {},
    replay: () -> Unit,
    showAd: () -> Unit = {},
    goBackWithRewardedAd: ((onRewardEarned: () -> Unit) -> Unit)? = null,
    onGameModeSelected: (GameMode) -> Unit = {},
    onGameStateUpdate: (GameState, List<List<Tile>>) -> Unit = { _, _ -> },
    onNavigateToHome: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var showLoseDialog by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(true) }
    var showModeMenu by remember { mutableStateOf(false) }
    var showModeConfirmation by remember { mutableStateOf<GameMode?>(null) }
    var showHighScoreDialog by remember { mutableStateOf(false) }
    val board = gameState.plateau

    // Initialize the high score manager
    val context = LocalContext.current
    val highScoreManager = remember { HighScoreManager(context) }

    // Determine the current game mode based on board size
    val currentGameMode = remember(gameState.boardSize) {
        GameMode.entries.find { it.size == gameState.boardSize } ?: GameMode.CLASSIC
    }

    // Get the current high score for this mode - key this off the gameState.id to refresh when the game resets
    val highScore = remember(currentGameMode, gameState.id) {
        highScoreManager.getHighScore(currentGameMode)
    }

    // Track if the current score is a new high score
    var isNewHighScore by remember { mutableStateOf(false) }

    // Reset high score flags when game resets
    LaunchedEffect(gameState.id) {
        isNewHighScore = false
    }

    // Get screen configuration for responsive layout
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp }
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp }

    // Calculate appropriate sizes based on screen dimensions
    val maxBoardSize = min(screenWidth, screenHeight) * 0.85f
    val titleSize = (min(screenWidth, screenHeight) * 0.08f).coerceIn(32.dp, 56.dp)
    val subtitleSize = (min(screenWidth, screenHeight) * 0.035f).coerceIn(14.dp, 20.dp)

    // Check for score milestones and high score updates
    val previousScore = remember { mutableIntStateOf(0) }
    LaunchedEffect(gameState.score) {
        // Change base interval to 4000 to show first ad at 4000
        val baseInterval = 4000
        val currentScore = gameState.score

        // Calculate appropriate milestone based on score range
        val interval = when {
            currentScore < 15000 -> baseInterval  // First milestone at 4000
            currentScore < 30000 -> baseInterval * 2  // Then at 8000, 12000, 16000
            currentScore < 50000 -> baseInterval * 4  // Then at 32000, 36000...
            else -> baseInterval * 6  // Very large intervals for end game
        }

        // Calculate current and previous milestones using dynamic interval
        val currentMilestone = (currentScore / interval) * interval
        val previousMilestone = (previousScore.intValue / interval) * interval

        // Show ad when crossing milestone
        if (currentScore > 0 && currentMilestone > previousMilestone) {
            showAd()
        }

        // High score logic remains the same
        if (gameState.score > highScore) {
            if (previousScore.intValue <= highScore && gameState.score > highScore) {
                isNewHighScore = true
                highScoreManager.updateHighScore(currentGameMode, gameState.score)
                if (gameState.score >= highScore + 50) {
                    showHighScoreDialog = true
                }
            } else {
                highScoreManager.updateHighScore(currentGameMode, gameState.score)
            }
        }

        previousScore.intValue = gameState.score
    }

    // Show game over dialog when the game is lost
    LaunchedEffect(gameState.isLose) {
        if (gameState.isLose) {
            showLoseDialog = true
        }
    }

    // Function to handle going back to previous state
    fun goBackToPreviousState() {
        if (gameState.moveHistory.isNotEmpty()) {
            // Get the last state from history (5 moves back or the oldest available)
            val stepsBack = 5
            val historyIndex = (gameState.moveHistory.size - stepsBack).coerceAtLeast(0)
            val previousState = gameState.moveHistory[historyIndex]

            // Update the game state with the previous state
            val newState = gameState.copy(
                plateau = previousState.plateau as Plateau,
                score = previousState.score,
                id = previousState.id,
                isLose = false,  // Reset game over flag
                moveHistory = gameState.moveHistory.take(historyIndex)  // Keep only older history
            )

            // Update the board display through callback
            onGameStateUpdate(newState, gameState.plateau)

            // Close the dialog
            showLoseDialog = false
        }
    }

    if (isLandscape) {
        // Landscape layout
        Row(
            modifier = modifier.fillMaxSize()
        ) {
            // Left side: Title & Controls
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "2048",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .background(Color.LightGray, MaterialTheme.shapes.medium)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replay",
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showModeMenu = true },
                            modifier = Modifier
                                .background(Color.LightGray, MaterialTheme.shapes.medium)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Game Mode",
                            )
                        }

                        DropdownMenu(
                            expanded = showModeMenu,
                            onDismissRequest = { showModeMenu = false }
                        ) {
                            GameMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    onClick = {
                                        showModeMenu = false
                                        showModeConfirmation = mode
                                    },
                                    text = { Text(mode.displayName) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                ScoreDisplay(
                    currentScore = gameState.score,
                    highScore = highScore,
                    isNewHighScore = isNewHighScore,
                    isLandscape = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Join the numbers and get to the 2048 tile!",
                    fontSize = subtitleSize.value.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Right side: Game board
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
                    .padding(16.dp)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .pointerInput(Unit) {
                        handleDragGestures(move)
                    }
            ) {
                DrawStaticBoard(board)
                AnimateTiles(board, oldBoard, gameState)
            }
        }

        // Banner ad at the bottom
        Box(modifier = Modifier.fillMaxWidth()) {
            BannerAd(adUnitId = "ca-app-pub-2523891738770793/7272797422")
        }
    } else {
        // Portrait layout
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = CenterHorizontally
        ) {
            Text(
                text = "2048",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Combined row with controls and scores
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Controls
                    // Common button size and styling
                    val buttonModifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)

                    IconButton(
                        onClick = { showDialog = true },
                        modifier = buttonModifier
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replay",
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        IconButton(
                            onClick = { showModeMenu = true },
                            modifier = buttonModifier
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Game Mode",
                            )
                        }

                        DropdownMenu(
                            expanded = showModeMenu,
                            onDismissRequest = { showModeMenu = false }
                        ) {
                            GameMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    onClick = {
                                        showModeMenu = false
                                        showModeConfirmation = mode
                                    },
                                    text = { Text(mode.displayName) }
                                )
                            }
                        }
                    }


                // Add spacer between buttons and scores
                Spacer(modifier = Modifier.width(12.dp))

                // Scores - using the new compact mode
                ScoreDisplay(
                    currentScore = gameState.score,
                    highScore = highScore,
                    isNewHighScore = isNewHighScore,
                    compactMode = true,
                    modifier = Modifier.weight(0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Join the numbers and get to the 2048 tile!",
                fontSize = subtitleSize.value.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            // Game board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        handleDragGestures(move)
                    }
            ) {
                DrawStaticBoard(board)
                AnimateTiles(board, oldBoard, gameState)
            }

            // Banner ad at the bottom
            BannerAd(adUnitId = "ca-app-pub-2523891738770793/7272797422") // Using test ad unit ID
        }
    }

    // Dialogs
    if (showDialog) {
        ReplayConfirmationDialog(
            onConfirm = {
                showDialog = false
                replay()  // This will show rewarded interstitial
            },
            onDismiss = { showDialog = false }
        )
    }

    if (gameState.isLose && showLoseDialog) {
        val canRewind = gameState.moveHistory.isNotEmpty() && goBackWithRewardedAd != null
        var isLoading by remember { mutableStateOf(false) }

        GameOverDialog(
            canRewind = canRewind,
            onWatchAd = {
                isLoading = true
                goBackWithRewardedAd?.invoke {
                    isLoading = false
                    goBackToPreviousState()
                    showLoseDialog = false
                }
            },
            onNewGame = {
                showLoseDialog = false

                // Save the final score before reset to check for high score
                val finalScore = gameState.score
                val oldHighScore = highScoreManager.getHighScore(currentGameMode)
                val isHighScoreAchieved = finalScore > oldHighScore

                // Reset game state
                val resetEmptyState = GameState(
                    plateau = plateauVide(gameState.boardSize),
                    boardSize = gameState.boardSize
                )
                val resetInitialPlateau = plateauInitial(resetEmptyState)
                onGameStateUpdate(
                    GameState(
                        plateau = resetInitialPlateau,
                        id = resetEmptyState.id,
                        boardSize = gameState.boardSize,
                        moveHistory = emptyList()
                    ),
                    gameState.plateau
                )

                // If high score was achieved, update and show dialog after game reset
                if (isHighScoreAchieved) {
                    highScoreManager.updateHighScore(currentGameMode, finalScore)
                    // Show high score dialog with a slight delay to ensure game reset is complete
                    showHighScoreDialog = true
                }

                // Show ad after resetting game state - this is intentional for new game
                showAd()
            },
            onDismiss = { showLoseDialog = false },
            onBack = {
                showLoseDialog = false
                onNavigateToHome()
            }
        )
    }

    if (gameState.isWin && showWinDialog) {
        YouWonDialog(
            onPlayAgain = {
                showWinDialog = false
                showAd() // Just show the ad without resetting game state
            },
            onBack = {
                showWinDialog = false
                onNavigateToHome()
            }
        )
    }

    // Mode change confirmation dialog
    showModeConfirmation?.let { mode ->
        GameModeChangeDialog(
            onConfirm = {
                showModeConfirmation = null
                onGameModeSelected(mode)
                // Show interstitial ad after mode change
                showAd()
            },
            onDismiss = { showModeConfirmation = null }
        )
    }

    // High score dialog
    if (showHighScoreDialog) {
        NewHighScoreDialog(
            score = gameState.score,
            gameMode = currentGameMode.displayName,
            onDismiss = { showHighScoreDialog = false }
        )
    }
}

@Composable
fun StyledAlertDialog(
    title: String,
    message: @Composable () -> Unit,
    confirmButtonText: String,
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    isError: Boolean = true
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        text = message,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(
                        dismissButtonText,
                    )
                }
            }
        }
    )
}

@Composable
fun YouWonDialog(
    onPlayAgain: () -> Unit,
    onBack: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(true) }
    if (!showDialog) return
    StyledAlertDialog(
        title = "Congratulations!",
        message = {
            Column {
                Text(
                    "You win! Would you like to continue playing?",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButtonText = "Continue",
        dismissButtonText = "Play Again",
        onConfirm = onPlayAgain,
        onDismiss = {
            showDialog = false
            onBack()
        },
        isError = false
    )
}

@Composable
fun ReplayConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    StyledAlertDialog(
        title = "Confirm Replay",
        message = {
            Text(
                "Are you sure you want to replay? You will lose your progress.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButtonText = "Yes",
        dismissButtonText = "No",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isError = false
    )
}

@Composable
fun GameOverDialog(
    canRewind: Boolean,
    onWatchAd: () -> Unit,
    onNewGame: () -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    StyledAlertDialog(
        title = "Game Over",
        message = {
            Column {
                Text(
                    "You lose! Choose an option below:",
                    style = MaterialTheme.typography.bodyLarge
                )
                if (canRewind) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Watch an ad to go back a few steps and continue playing!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            onWatchAd()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green,
                            contentColor = Color.Black
                        ),
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading Ad...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Watch Ad",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Watch Ad & Rewind",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButtonText = "New Game",
        dismissButtonText = "Back to Menu",
        onConfirm = onNewGame,
        onDismiss = onBack,
        isError = true
    )
}

@Composable
fun GameModeChangeDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    StyledAlertDialog(
        title = "Change Game Mode",
        message = {
            Text(
                "Changing game mode will restart your current game. Are you sure?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButtonText = "Continue",
        dismissButtonText = "Cancel",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isError = false
    )
}

@Composable
fun HighScoreDialog(
    newHighScore: Int,
    onDismiss: () -> Unit
) {
    StyledAlertDialog(
        title = "New High Score!",
        message = {
            Text(
                "Congratulations! You've achieved a new high score of $newHighScore points!",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButtonText = "OK",
        onConfirm = onDismiss,
        isError = false
    )
}