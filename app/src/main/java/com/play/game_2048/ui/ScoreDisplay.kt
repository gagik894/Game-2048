package com.play.game_2048.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.play.game_2048.util.formatScore

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScoreDisplay(
    currentScore: Int,
    highScore: Int,
    isNewHighScore: Boolean = false,
    isLandscape: Boolean = false,
    compactMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Get screen configuration to adjust score display size
    val configuration = LocalConfiguration.current
    val minScreenDim = min(
        configuration.screenWidthDp.dp,
        configuration.screenHeightDp.dp
    )
    val scoreFontSize = (minScreenDim * 0.05f).coerceIn(16.dp, 28.dp)
    val compactScoreFontSize = (minScreenDim * 0.045f).coerceIn(14.dp, 24.dp)
    
    if (compactMode) {
        // Compact horizontal layout for portrait mode 
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current Score - compact design
            Surface(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SCORE",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        AnimatedContent(
                            targetState = currentScore,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                            }, label = ""
                        ) { targetScore ->
                            Text(
                                text = formatScore(targetScore),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.Red,
                                    fontSize = compactScoreFontSize.value.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // High Score - compact design
            Surface(
                modifier = Modifier.weight(1f),
                color = if (isNewHighScore) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "BEST",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        AnimatedContent(
                            targetState = highScore,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                            }, label = ""
                        ) { targetScore ->
                            Text(
                                text = formatScore(targetScore),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = if (isNewHighScore) Color.Green else Color(0xFF2196F3), // Nice blue
                                    fontSize = compactScoreFontSize.value.sp,
                                    fontWeight = if (isNewHighScore) FontWeight.ExtraBold else FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    } else if (isLandscape) {
        // Vertical layout for landscape mode (stacks the scores)
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current Score
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    AnimatedContent(
                        targetState = currentScore,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                        }, label = ""
                    ) { targetScore ->
                        Text(
                            text = formatScore(targetScore),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.Red,
                                fontSize = scoreFontSize.value.sp
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // High Score
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isNewHighScore) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "BEST",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    AnimatedContent(
                        targetState = highScore,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                        }, label = ""
                    ) { targetScore ->
                        Text(
                            text = formatScore(targetScore),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = if (isNewHighScore) Color.Green else Color.Blue,
                                fontSize = scoreFontSize.value.sp,
                                fontWeight = if (isNewHighScore) FontWeight.ExtraBold else FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    } else {
        // Original horizontal layout for portrait mode
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Current Score
            Surface(
                modifier = Modifier,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    AnimatedContent(
                        targetState = currentScore,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                        }, label = ""
                    ) { targetScore ->
                        Text(
                            text = formatScore(targetScore),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.Red,
                                fontSize = scoreFontSize.value.sp
                            )
                        )
                    }
                }
            }
            
            // High Score
            Surface(
                modifier = Modifier,
                color = if (isNewHighScore) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "BEST",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    AnimatedContent(
                        targetState = highScore,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                        }, label = ""
                    ) { targetScore ->
                        Text(
                            text = formatScore(targetScore),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = if (isNewHighScore) Color.Green else Color.Blue,
                                fontSize = scoreFontSize.value.sp,
                                fontWeight = if (isNewHighScore) FontWeight.ExtraBold else FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

// Keep the original DisplayScore for backward compatibility
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
                text = "Score: ${formatScore(targetScore)}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.Red,
                    fontSize = scoreFontSize.value.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

fun formatScore(score: Int): String {
    return if (score >= 1000) {
        String.format("%.1fK", score / 1000.0)
    } else {
        score.toString()
    }
}