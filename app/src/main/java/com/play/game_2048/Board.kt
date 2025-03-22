package com.play.game_2048

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.times
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.log2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import kotlin.collections.MutableList

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
            
            // Right side: Game board - replace BoxWithConstraints with Box
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
            
            // Game board - replace BoxWithConstraints with Box
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

@Composable
fun DrawStaticBoard(board: List<List<Tile>>) {
    Surface(
        color = Color.DarkGray,
        modifier = Modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium
    ) {
        // Use same padding for all directions to ensure alignment
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in board) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (cell in row) {
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(Color.LightGray)
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimateTiles(board: List<List<Tile>>, oldBoard: List<List<Tile>>, gameState: GameState) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var tileSize by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val cellPadding = 4.dp
    val boardPadding = 4.dp
    
    // Keep track of the merges that need to be animated
    val mergedTiles = gameState.lastMergedTiles
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.size
            }
    ) {
        LaunchedEffect(boxSize) {
            if (boxSize.width > 0) {
                val boxWidthDp = with(density) { boxSize.width.toDp() }
                val boxHeightDp = with(density) { boxSize.height.toDp() }
                val availableWidth = boxWidthDp - (boardPadding * 2)
                val availableHeight = boxHeightDp - (boardPadding * 2)
                val availableSize = min(availableWidth, availableHeight)
                
                val gapCount = board.size - 1
                val totalGapSize = gapCount * cellPadding
                tileSize = (availableSize - totalGapSize) / board.size
            }
        }

        if (tileSize.value > 0) {
            // Calculate position accounting for padding
            val calcPosition = { index: Int ->
                boardPadding.value + (index * (tileSize.value + cellPadding.value))
            }
            
            // 1. Draw "ghost" tiles that will animate merging
            for (merge in mergedTiles) {
                val sourcePosition = findTilePosition(oldBoard, merge.sourceId)
                val targetPosition = findTilePosition(oldBoard, merge.targetId)
                
                if (sourcePosition != null && targetPosition != null) {
                    val sourceX = calcPosition(sourcePosition.second)
                    val sourceY = calcPosition(sourcePosition.first)
                    val targetX = calcPosition(merge.position.second)
                    val targetY = calcPosition(merge.position.first)
                    
                    val animatedOffsetX = remember { Animatable(sourceX) }
                    val animatedOffsetY = remember { Animatable(sourceY) }
                    val animatedScale = remember { Animatable(1f) }
                    val animatedAlpha = remember { Animatable(1f) }
                    
                    LaunchedEffect(merge.sourceId) {
                        // First move to target position
                        animatedOffsetX.animateTo(
                            targetValue = targetX,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        )
                        animatedOffsetY.animateTo(
                            targetValue = targetY,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        )
                        
                        // Then fade out with scale down
                        animatedScale.animateTo(
                            targetValue = 0.8f,
                            animationSpec = tween(
                                durationMillis = 150,
                                easing = FastOutSlowInEasing
                            )
                        )
                        animatedAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = 100,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                    
                    // Draw the merging tile animation
                    val sourceTile = oldBoard[sourcePosition.first][sourcePosition.second]
                    val targetTile = oldBoard[targetPosition.first][targetPosition.second]
                    
                    MergingTile(
                        tile = sourceTile,
                        tileSize = tileSize,
                        animatedScale = animatedScale.value,
                        animatedAlpha = animatedAlpha.value,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    animatedOffsetX.value.dp.roundToPx(),
                                    animatedOffsetY.value.dp.roundToPx()
                                )
                            }
                            .size(tileSize)
                    )
                }
            }
            
            // 2. Draw the main tiles (including the ones that receive the merged value)
            for (rowIndex in board.indices) {
                for (colIndex in board[rowIndex].indices) {
                    val tile = board[rowIndex][colIndex]
                    if (tile.number != 0) {
                        val previousPosition = findTilePosition(oldBoard, tile.id)
                        
                        // Check if this tile is a result of a merge
                        val isMerged = mergedTiles.any { it.targetId == tile.id }
                        
                        // If tile existed in previous board, animate it
                        if (previousPosition != null) {
                            val isMoving = previousPosition.first != rowIndex || 
                                         previousPosition.second != colIndex
                            
                            val startX = calcPosition(previousPosition.second)
                            val startY = calcPosition(previousPosition.first)
                            val targetX = calcPosition(colIndex)
                            val targetY = calcPosition(rowIndex)
                            
                            // Use a key that changes when the tile's position changes
                            val animationKey = "${tile.id}-${rowIndex}-${colIndex}"
                            val animatedOffsetX = remember(animationKey) { Animatable(startX) }
                            val animatedOffsetY = remember(animationKey) { Animatable(startY) }
                            val glowIntensity = remember(animationKey) { Animatable(0f) }
                            
                            LaunchedEffect(animationKey) {
                                if (isMoving) {
                                    // For moved tiles, start from previous position
                                    animatedOffsetX.snapTo(startX)
                                    animatedOffsetY.snapTo(startY)
                                    
                                    // Animate to the new position
                                    animatedOffsetX.animateTo(
                                        targetValue = targetX,
                                        animationSpec = tween(
                                            durationMillis = 200,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    animatedOffsetY.animateTo(
                                        targetValue = targetY,
                                        animationSpec = tween(
                                            durationMillis = 200,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    
                                    // Add glow animation for merged tiles instead of scaling
                                    if (isMerged) {
                                        // Wait for merge animation to complete
                                        delay(250)
                                        // Glow up and down animation
                                        glowIntensity.animateTo(
                                            targetValue = 0.7f,
                                            animationSpec = tween(
                                                durationMillis = 150,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        glowIntensity.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(
                                                durationMillis = 150,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    }
                                } else {
                                    // For stationary tiles, just snap to position
                                    animatedOffsetX.snapTo(targetX)
                                    animatedOffsetY.snapTo(targetY)
                                    
                                    // Add glow animation for merged tiles even if position didn't change
                                    if (isMerged) {
                                        // Wait for merge animation to complete
                                        delay(250)
                                        // Glow up and down animation
                                        glowIntensity.animateTo(
                                            targetValue = 0.7f,
                                            animationSpec = tween(
                                                durationMillis = 150,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        glowIntensity.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(
                                                durationMillis = 150,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    }
                                }
                            }
                            
                            Tile(
                                tile = tile,
                                isNew = false,
                                isMergeResult = isMerged,
                                oldTile = oldBoard[previousPosition.first][previousPosition.second],
                                tileSize = tileSize,
                                glowIntensity = glowIntensity.value,
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            animatedOffsetX.value.dp.roundToPx(),
                                            animatedOffsetY.value.dp.roundToPx()
                                        )
                                    }
                                    .size(tileSize)
                            )
                        } else {
                            // Handle new tiles with appearance animation
                            val targetX = calcPosition(colIndex)
                            val targetY = calcPosition(rowIndex)
                            
                            Tile(
                                tile = tile,
                                isNew = true,
                                isMergeResult = false,
                                oldTile = null,
                                tileSize = tileSize,
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            targetX.dp.roundToPx(),
                                            targetY.dp.roundToPx()
                                        )
                                    }
                                    .size(tileSize)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MergingTile(
    tile: Tile,
    tileSize: androidx.compose.ui.unit.Dp,
    animatedScale: Float,
    animatedAlpha: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                alpha = animatedAlpha
            ),
        color = calculateTileColor(tile.number),
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Responsive font sizing based on tile size and number length
            val fontSize = when (tile.number.toString().length) {
                1 -> (tileSize / 2.5f).coerceAtLeast(12.dp).coerceAtMost(48.dp)
                2 -> (tileSize / 3f).coerceAtLeast(10.dp).coerceAtMost(40.dp)
                3 -> (tileSize / 3.5f).coerceAtLeast(9.dp).coerceAtMost(36.dp)
                else -> (tileSize / 4f).coerceAtLeast(8.dp).coerceAtMost(32.dp)
            }

            Text(
                text = tile.number.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = fontSize.value.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun Tile(
    tile: Tile,
    modifier: Modifier = Modifier,
    isNew: Boolean = false,
    isMergeResult: Boolean = false,
    oldTile: Tile? = null,
    tileSize: androidx.compose.ui.unit.Dp = 0.dp,
    scale: Float = 1f,
    glowIntensity: Float = 0f
) {
    val rotation = remember { Animatable(0f) }
    val initialScale = if (isNew) 0f else 1f
    val animatedScale = remember { Animatable(initialScale) }
    val color = remember { Animatable(calculateTileColor(tile.number)) }
    var isVisible by remember { mutableStateOf(!isNew) }

    LaunchedEffect(isNew, isMergeResult) {
        if (isNew) {
            // New tile animation
            if (!isVisible) {
                animatedScale.snapTo(0f)
                delay(250)
                isVisible = true
            }

            animatedScale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
            animatedScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
        } else {
            // Regular tile - no scaling animation for merge anymore
            animatedScale.animateTo(
                targetValue = 1f,  // Always keep at 1f for merged tiles
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(tile.id, tile.number) {
        if (oldTile != null && oldTile.number != tile.number) {
            // Color transition for changing numbers
            color.animateTo(
                targetValue = calculateTileColor(oldTile.number),
                animationSpec = tween(durationMillis = 0, easing = FastOutSlowInEasing)
            )
            color.animateTo(
                targetValue = calculateTileColor(tile.number),
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        } else {
            // Set the color immediately
            color.snapTo(calculateTileColor(tile.number))
        }
    }

    if (isVisible) {
        Surface(
            modifier = modifier
                .aspectRatio(1f)
                .graphicsLayer(
                    rotationZ = rotation.value,
                    scaleX = animatedScale.value,
                    scaleY = animatedScale.value
                ),
            color = color.value,
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val animatedAlpha = remember { Animatable(1f) }
                var displayedNumber by remember { mutableIntStateOf(tile.number) }

                LaunchedEffect(tile.number) {
                    if (oldTile != null && oldTile.number != tile.number) {
                        animatedAlpha.animateTo(
                            targetValue = 0.6f,
                            animationSpec = tween(durationMillis = 250)
                        )
                        displayedNumber = tile.number
                        animatedAlpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 300)
                        )
                    } else {
                        displayedNumber = tile.number
                    }
                }

                // Responsive font sizing based on tile size and number length
                val fontSize = when (displayedNumber.toString().length) {
                    1 -> (tileSize / 2.5f).coerceAtLeast(12.dp).coerceAtMost(48.dp)
                    2 -> (tileSize / 3f).coerceAtLeast(10.dp).coerceAtMost(40.dp)
                    3 -> (tileSize / 3.5f).coerceAtLeast(9.dp).coerceAtMost(36.dp)
                    else -> (tileSize / 4f).coerceAtLeast(8.dp).coerceAtMost(32.dp)
                }

                Text(
                    text = displayedNumber.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = fontSize.value.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .graphicsLayer(alpha = animatedAlpha.value),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun calculateTileColor(number: Int): Color {
    return if (number == 0) {
        Color.LightGray
    } else {
        val hue = (60 - (log2(number.toDouble()) * 25)) % 360
        val adjustedHue = if (hue < 0) hue + 360 else hue
        Color.hsl(adjustedHue.toFloat(), 0.8f, 0.7f)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DisplayScore(score: Int) {
    // Get screen configuration to adjust score display size
    val configuration = LocalConfiguration.current
    val minScreenDim = min(
        configuration.screenWidthDp.dp,
        configuration.screenHeightDp.dp
    )
    val scoreFontSize = (minScreenDim * 0.05f).coerceIn(16.dp, 28.dp)
    
    Surface(
        modifier = Modifier,
        color = Color.LightGray,
        shape = MaterialTheme.shapes.medium
    ) {
        AnimatedContent(
            targetState = score,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
            }, label = ""
        ) { targetScore ->
            Text(
                text = "Score: $targetScore",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.Red,
                    fontSize = scoreFontSize.value.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

suspend fun PointerInputScope.handleDragGestures(move: (Int) -> Unit) {
    var totalDragAmount = Offset.Zero
    detectDragGestures(
        onDragEnd = {
            val (dx, dy) = totalDragAmount
            if (abs(dx) > abs(dy)) {
                when {
                    dx > 0 -> move(6)  // right
                    dx < 0 -> move(4)  // left
                }
            } else {
                when {
                    dy > 0 -> move(2)  // down
                    dy < 0 -> move(8)  // up
                }
            }
            totalDragAmount = Offset.Zero
            println("Drag ended")
        },
        onDrag = { change, dragAmount ->
            totalDragAmount += dragAmount
            println("Drag amount: $dragAmount")
            change.consume()
        }
    )
}

fun printBoard(board: Array<Array<Tile>>) {
    for (row in board) {
        for (cell in row) {
            print("${cell.number} id ${cell.id} | ")
        }
        println()
    }
}

fun findTilePosition(board: List<List<Tile>>, id: Int): Pair<Int, Int>? {
    for (rowIndex in board.indices) {
        for (colIndex in board[rowIndex].indices) {
            if (board[rowIndex][colIndex].id == id) {
                return rowIndex to colIndex
            }
        }
    }
    return null
}

fun findTileById(board: Array<Array<Tile>>, id: Int): Tile? {
    for (row in board) {
        for (cell in row) {
            if (cell.id == id) {
                return cell
            }
        }
    }
    return null
}

@Preview(name = "4x4 Board", showBackground = true)
@Composable
fun Preview4x4Board() {
    val board = List(4) { row ->
        MutableList(4) { col ->
            // Create tiles with different powers of 2
            val value = when {
                (row == 0 && col == 0) -> 2
                (row == 0 && col == 1) -> 4
                (row == 0 && col == 2) -> 8
                (row == 0 && col == 3) -> 16
                (row == 1 && col == 0) -> 32
                (row == 1 && col == 1) -> 64
                (row == 1 && col == 2) -> 128
                (row == 1 && col == 3) -> 256
                (row == 2 && col == 0) -> 512
                (row == 2 && col == 1) -> 1024
                (row == 2 && col == 2) -> 2048
                (row == 2 && col == 3) -> 4096
                (row == 3 && col == 0) -> 8192
                (row == 3 && col == 1) -> 16384
                (row == 3 && col == 2) -> 32768
                (row == 3 && col == 3) -> 6553655
                else -> 0
            }
            Tile(id = row * 4 + col, number = value)
        }
    }
    
    val gameState = GameState(
        plateau = board,
        score = 16384,
        isLose = false, 
        isWin = false
    )
    
    MaterialTheme {
        Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
            Board(
                gameState = gameState,
                oldBoard = board,
                replay = {}
            )
        }
    }
}

@Preview(name = "5x5 Board", showBackground = true)
@Composable
fun Preview5x5Board() {
    val board = List(5) { row ->
        MutableList(5) { col ->
            // Create tiles with different powers of 2
            val value = when {
                (row == 0 && col < 4) -> 1 shl (col + 1)  // 2, 4, 8, 16
                (row == 1 && col < 4) -> 1 shl (col + 5)  // 32, 64, 128, 256
                (row == 2 && col < 3) -> 1 shl (col + 9)  // 512, 1024, 2048
                else -> 0
            }
            Tile(id = row * 5 + col, number = value)
        }
    }
    
    val gameState = GameState(
        plateau = board,
        score = 4096,
        isLose = false, 
        isWin = false
    )
    
    MaterialTheme {
        Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
            Board(
                gameState = gameState,
                oldBoard = board,
                replay = {}
            )
        }
    }
}

@Preview(name = "6x6 Board", showBackground = true)
@Composable
fun Preview6x6Board() {
    val board = List(6) { row ->
        MutableList(6) { col ->
            // Create a more sparse board with various values
            val value = when {
                (row == 0 && col == 0) -> 2
                (row == 0 && col == 3) -> 4
                (row == 1 && col == 1) -> 8
                (row == 1 && col == 4) -> 16
                (row == 2 && col == 2) -> 32
                (row == 2 && col == 5) -> 64
                (row == 3 && col == 0) -> 128
                (row == 3 && col == 3) -> 256
                (row == 4 && col == 1) -> 512
                (row == 4 && col == 4) -> 1024
                (row == 5 && col == 2) -> 2048
                (row == 5 && col == 5) -> 4096
                else -> 0
            }
            Tile(id = row * 6 + col, number = value)
        }
    }
    
    val gameState = GameState(
        plateau = board,
        score = 8192,
        isLose = false, 
        isWin = false
    )
    
    MaterialTheme {
        Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
            Board(
                gameState = gameState,
                oldBoard = board,
                replay = {}
            )
        }
    }
}

// Single tile previews
@Preview(name = "Tile 2", showBackground = true)
@Composable
fun PreviewTile2() {
    MaterialTheme {
        Box(modifier = Modifier.size(100.dp)) {
            Tile(
                tile = Tile(id = 1, number = 2),
                tileSize = 100.dp
            )
        }
    }
}

@Preview(name = "Tile 1024", showBackground = true)
@Composable
fun PreviewTile1024() {
    MaterialTheme {
        Box(modifier = Modifier.size(100.dp)) {
            Tile(
                tile = Tile(id = 1, number = 1024),
                tileSize = 100.dp
            )
        }
    }
}

@Preview(name = "Tile 2048", showBackground = true)
@Composable
fun PreviewTile2048() {
    MaterialTheme {
        Box(modifier = Modifier.size(100.dp)) {
            Tile(
                tile = Tile(id = 1, number = 2048),
                tileSize = 100.dp
            )
        }
    }
}
