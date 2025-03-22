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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.log2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp

@Composable
fun Board(
    gameState: GameState,
    oldBoard: List<List<Tile>>,
    modifier: Modifier = Modifier,
    move: (Int) -> Unit = {},
    replay: () -> Unit,
    showAd: () -> Unit = {},
) {
    var showDialog by remember { mutableStateOf(false) }
    var showLoseDialog by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(true) }
    val board = gameState.plateau
    
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

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "2048",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            DisplayScore(gameState.score)
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.padding(4.dp)
                    .background(Color.LightGray, MaterialTheme.shapes.medium)
                    .padding(4.dp)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Replay",
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Text(
                text = "Join the numbers and get to the 2048 tile!",
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        // Game board - takes most of the space
        Box(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    handleDragGestures(move)
                }
                .padding(16.dp)
        ) {
            DrawStaticBoard(board)
            AnimateTiles(board, oldBoard)
        }
        
        // Banner ad at the bottom
        BannerAd(adUnitId = "ca-app-pub-3940256099942544/6300978111") // Using test ad unit ID
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
}

@Composable
fun DrawStaticBoard(board: List<List<Tile>>) {
    Surface(
        color = Color.DarkGray,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            for (row in board) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (cell in row) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
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
fun AnimateTiles(board: List<List<Tile>>, oldBoard: List<List<Tile>>) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var tileSize by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .aspectRatio(1f)
            .onGloballyPositioned { coordinates ->
                boxSize = coordinates.size
            }
    ) {
        LaunchedEffect(boxSize) {
            println("Box size: $boxSize")
            if (boxSize.width > 0) {
                val boxWidthDp = with(density) { boxSize.width.toDp() }
                val boxHeightDp = with(density) { boxSize.height.toDp() }
                tileSize = boxWidthDp / board.size
                println("Tile size: $tileSize")
            }
        }

        for (rowIndex in board.indices) {
            for (colIndex in board[rowIndex].indices) {
                val tile = board[rowIndex][colIndex]
                if (tile.number != 0) {
                    val previousPosition = findTilePosition(oldBoard, tile.id)
                    val startX = (previousPosition?.second ?: colIndex) * tileSize
                    val startY = (previousPosition?.first ?: rowIndex) * tileSize

                    val animatedOffsetX = remember { Animatable(startX.value) }
                    val animatedOffsetY = remember { Animatable(startY.value) }

                    LaunchedEffect(colIndex, rowIndex) {
                        if (previousPosition != null) {
                            animatedOffsetX.animateTo(
                                targetValue = colIndex * tileSize.value,
                                animationSpec = tween(
                                    durationMillis = 150,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            animatedOffsetY.animateTo(
                                targetValue = rowIndex * tileSize.value,
                                animationSpec = tween(
                                    durationMillis = 150,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }

                    Tile(
                        tile = tile,
                        isNew = previousPosition == null,
                        oldTile = previousPosition?.let { oldBoard[it.first][it.second] },
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    animatedOffsetX.value.dp.roundToPx(),
                                    animatedOffsetY.value.dp.roundToPx()
                                )
                            }
                            .size(tileSize)
                            .aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DisplayScore(score: Int) {
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
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.Red,
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

@Composable
fun Tile(
    tile: Tile,
    modifier: Modifier = Modifier,
    isNew: Boolean = false,
    oldTile: Tile? = null,
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val color = remember { Animatable(calculateTileColor(tile.number)) }
    var isVisible by remember { mutableStateOf(!isNew) }

    LaunchedEffect(isNew) {
        if (isNew) {
            scale.animateTo(
                targetValue = 0.0f,
                animationSpec = tween(durationMillis = 1)
            )
            delay(250)
            isVisible = true

            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 0, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(tile.id, tile.number) {
        if (oldTile != null && oldTile.number != tile.number) {
            color.animateTo(
                targetValue = calculateTileColor(oldTile.number),
                animationSpec = tween(durationMillis = 0, easing = FastOutSlowInEasing)
            )
            color.animateTo(
                targetValue = calculateTileColor(tile.number),
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        } else {
            color.animateTo(
                targetValue = calculateTileColor(tile.number),
                animationSpec = tween(durationMillis = 0, easing = FastOutSlowInEasing)
            )
        }
    }

    if (isVisible) {
        Surface(
            modifier = modifier
                .aspectRatio(1f)
                .padding(4.dp)
                .graphicsLayer(
                    rotationZ = rotation.value,
                    scaleX = scale.value,
                    scaleY = scale.value
                ),
            color = color.value,
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
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

                val fontSize = when (displayedNumber.toString().length) {
                    1 -> 32.sp
                    2 -> 28.sp
                    3 -> 24.sp
                    else -> 20.sp
                }

                Text(
                    text = displayedNumber.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = fontSize),
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer(alpha = animatedAlpha.value)
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
