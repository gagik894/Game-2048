package com.play.game_2048

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay

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
                val totalGapSize = gapCount * cellPadding.value
                tileSize = (availableSize - totalGapSize.dp) / board.size
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