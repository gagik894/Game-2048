package com.play.game_2048

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlin.invoke

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
    onGameStateUpdate: (GameState, List<List<Tile>>) -> Unit = { _, _ -> }
) {
    var showDialog by remember { mutableStateOf(false) }
    var showLoseDialog by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(true) }
    var showModeMenu by remember { mutableStateOf(false) }
    var showModeConfirmation by remember { mutableStateOf<GameMode?>(null) }
    val board = gameState.plateau

    // Get screen configuration for responsive layout
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp }
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp }

    // Calculate appropriate sizes based on screen dimensions
    val maxBoardSize = min(screenWidth, screenHeight) * 0.85f
    val titleSize = (min(screenWidth, screenHeight) * 0.08f).coerceIn(32.dp, 56.dp)
    val subtitleSize = (min(screenWidth, screenHeight) * 0.035f).coerceIn(14.dp, 20.dp)

    // Check for score milestones to show ads
    val previousScore = remember { mutableStateOf(0) }
    LaunchedEffect(gameState.score) {
        // Show an ad when player reaches score milestones (every 1000 points)
        if (gameState.score > 0 && previousScore.value / 1000 < gameState.score / 1000) {
            showAd()
        }
        previousScore.value = gameState.score
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
            // Get the last state from history (3 moves back or the oldest available)
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
                    fontSize = titleSize.value.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
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
                DisplayScore(gameState.score)

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
            BannerAd(adUnitId = "ca-app-pub-3940256099942544/6300978111") // Using test ad unit ID
        }
    } else {
        // Portrait layout
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "2048",
                fontSize = titleSize.value.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                color = Color.DarkGray,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            GameMode.values().forEach { mode ->
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

                DisplayScore(gameState.score)
            }

            Text(
                text = "Join the numbers and get to the 2048 tile!",
                fontSize = subtitleSize.value.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
            BannerAd(adUnitId = "ca-app-pub-3940256099942544/6300978111") // Using test ad unit ID
        }
    }

    // Dialogs
    if (showDialog) {
        ReplayConfirmationDialog(
            onConfirm = {
                replay()
                showDialog = false
                showAd() // Show ad on game restart
            },
            onDismiss = { showDialog = false }
        )
    }

    if (gameState.isLose && showLoseDialog) {
        // Only show rewind option if history exists and goBackWithRewardedAd function is provided
        val canRewind = gameState.moveHistory.isNotEmpty() && goBackWithRewardedAd != null

        // Add state to track ad loading
        var isLoading by remember { mutableStateOf(false) }

        GameOverDialog(
            canRewind = canRewind,
            onWatchAd = {
                isLoading = true
                goBackWithRewardedAd?.invoke {
                    isLoading = false
                    goBackToPreviousState()
                }
            },
            onNewGame = {
                showLoseDialog = false
                showAd() // Show ad only when starting new game
                replay()
            },
            onDismiss = { showLoseDialog = false }
        )
    }

    if (gameState.isWin && showWinDialog) {
        YouWonDialog(
            onPlayAgain = { showWinDialog = false },
            onBack = { showWinDialog = false }
        )
    }

    // Mode change confirmation dialog
    showModeConfirmation?.let { mode ->
        GameModeChangeDialog(
            onConfirm = {
                onGameModeSelected(mode)
                showModeConfirmation = null
            },
            onDismiss = { showModeConfirmation = null }
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
                style = MaterialTheme.typography.headlineSmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        text = message,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmButtonText, style = MaterialTheme.typography.bodyMedium)
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(
                        dismissButtonText,
                        style = MaterialTheme.typography.bodyMedium
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
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val buttonScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        // Subtle pulsing animation to draw attention
        buttonScale.animateTo(
            1.05f,
            animationSpec = repeatable(
                iterations = RepeatMode.Reverse.ordinal,
                animation = tween(800, easing = FastOutLinearInEasing)
            )
        )
    }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                if (!isLoading) {
                                    scaleX = buttonScale.value
                                    scaleY = buttonScale.value
                                }
                            },
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
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButtonText = "New Game",
        dismissButtonText = "Close",
        onConfirm = onNewGame,
        onDismiss = onDismiss,
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