package com.play.game_2048

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.log2

data class Tile(var id: Int, var number: Int)

@Composable
fun Tile(
    tile: Tile,
    modifier: Modifier = Modifier,
    isNew: Boolean = false,
    isMergeResult: Boolean = false,
    oldTile: Tile? = null,
    tileSize: Dp = 0.dp,
    scale: Float = 1f,
    glowIntensity: Float = 0f
) {
    val rotation = remember { Animatable(0f) }
    val initialScale = if (isNew) 0f else 1f
    val animatedScale = remember { Animatable(initialScale) }
    val color = remember { androidx.compose.animation.Animatable(calculateTileColor(tile.number)) }
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

@Composable
fun MergingTile(
    tile: Tile,
    tileSize: Dp,
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

fun calculateTileColor(number: Int): Color {
    return if (number == 0) {
        Color.LightGray
    } else {
        val hue = (60 - (log2(number.toDouble()) * 25)) % 360
        val adjustedHue = if (hue < 0) hue + 360 else hue
        Color.hsl(adjustedHue.toFloat(), 0.8f, 0.7f)
    }
}