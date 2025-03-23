package com.play.game_2048.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.game_2048.data.model.GameMode

@Composable
fun GameModeSelection(onModeSelected: (GameMode) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "2048",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            text = "Select Game Mode",
            fontSize = 24.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 columns
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
           items(GameMode.entries) { mode ->
               GameModeCard(
                   icon = null,
                   iconComponent = { GameModeIcon(mode, it) },
                   modeName = mode.displayName,
                   cardColor = getModeColor(mode),
                   iconTint = getModeIconTint(mode),
               ) { onModeSelected(mode) }
           }
        }
    }
}


@Composable
private fun GameModeIcon(mode: GameMode, modifier: Modifier = Modifier) {
    val gridSize = mode.size
    val spacing = 2.dp
    val cellColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            repeat(gridSize) { row ->
                Row(
                    modifier = Modifier
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    repeat(gridSize) { col ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    color = cellColor,
                                    shape = RectangleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
private fun getModeColor(mode: GameMode): Color {
    return when (mode) {
        GameMode.MINI -> Color(0xFF90CAF9)    // Light Green
        GameMode.CLASSIC -> Color(0xFFA5D6A7) // Light Blue
        GameMode.LARGE -> Color(0xFFFFD54F)   // Amber
        GameMode.EXTREME -> Color(0xFFF48FB1) // Deep Orange
    }
}

private fun getModeIconTint(mode: GameMode): Color {
    return when (mode) {
        GameMode.MINI -> Color(0xFF2196F3)    // Dark Green
        GameMode.CLASSIC -> Color(0xFF4CAF50) // Dark Blue
        GameMode.LARGE -> Color(0xFFFF9800)   // Dark Amber
        GameMode.EXTREME -> Color(0xFFE91E63) // Dark Orange
    }
}

@Composable
fun GameModeCard(
    icon: ImageVector?,
    iconComponent: @Composable (Modifier) -> Unit,
    modeName: String,
    cardColor: Color,
    iconTint: Color,
    onModeSelected: () -> Unit
) {
    Card (
        onClick = onModeSelected,
        modifier = Modifier.size(150.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = modeName,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 8.dp),
                    tint = iconTint
                )
            } ?: iconComponent(Modifier.size(64.dp).padding(8.dp))
            Text(
                modeName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}