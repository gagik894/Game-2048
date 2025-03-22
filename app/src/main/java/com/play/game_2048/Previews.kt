package com.play.game_2048

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
                (row == 3 && col == 3) -> 65536
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