package com.play.game_2048

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp

@Composable
fun Board(
    gameState: GameState,
    oldBoard: List<List<Tile>>,
    modifier: Modifier = Modifier,
    move: (Int) -> Unit = {},
    replay: () -> Unit,
    showAd: () -> Unit = {},
    onGameModeSelected: (GameMode) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var showLoseDialog by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(true) }
    var showModeMenu by remember { mutableStateOf(false) }
    var showModeConfirmation by remember { mutableStateOf<GameMode?>(null) }
    val board = gameState.plateau
    
    // Get screen configuration for responsive layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp }
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp }
    
    // Calculate appropriate sizes based on screen dimensions
    val maxBoardSize = min(screenWidth, screenHeight) * 0.85f
    val titleSize = (min(screenWidth, screenHeight) * 0.08f).coerceIn(32.dp, 56.dp)
    val subtitleSize = (min(screenWidth, screenHeight) * 0.035f).coerceIn(14.dp, 20.dp)
    
    // Check for game over to show ads
    LaunchedEffect(gameState.isLose, gameState.isWin) {
        if (gameState.isLose || gameState.isWin) {
            showAd()
        }
    }
    
    // Check for score milestones to show ads
    val previousScore = remember { mutableStateOf(0) }
    LaunchedEffect(gameState.score) {
        // Show an ad when player reaches score milestones (every 1000 points)
        if (gameState.score > 0 && previousScore.value / 1000 < gameState.score / 1000) {
            showAd()
        }
        previousScore.value = gameState.score
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
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Replay") },
            text = { Text("Are you sure you want to replay? You will lose your progress.") },
            confirmButton = {
                TextButton(onClick = {
                    replay()
                    showDialog = false
                    showAd() // Show ad on game restart
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (gameState.isLose && !showLoseDialog) {
        showLoseDialog = true
        AlertDialog(
            onDismissRequest = { showLoseDialog = false },
            title = { Text("Game Over") },
            text = { Text("You lose! Would you like to try again?") },
            confirmButton = {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        replay()
                        showLoseDialog = false
                    }) {
                    Text("Replay")
                }
            }
        )
    }

    if (gameState.isWin && showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = { Text("Congratulations!") },
            text = { Text("You win! Would you like to continue playing?") },
            confirmButton = {
                TextButton(onClick = {
                    showWinDialog = false
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWinDialog = false }) {
                    Text("Stop")
                }
            }
        )
    }

    // Mode change confirmation dialog
    showModeConfirmation?.let { mode ->
        AlertDialog(
            onDismissRequest = { showModeConfirmation = null },
            title = { Text("Change Game Mode") },
            text = { Text("Changing game mode will restart your current game. Are you sure?") },
            confirmButton = {
                TextButton(onClick = { 
                    onGameModeSelected(mode)
                    showModeConfirmation = null
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModeConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
