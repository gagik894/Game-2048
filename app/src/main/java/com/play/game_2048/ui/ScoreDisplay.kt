package com.play.game_2048.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp

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
        color = MaterialTheme.colorScheme.surfaceVariant,
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