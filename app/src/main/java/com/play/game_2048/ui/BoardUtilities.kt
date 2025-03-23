package com.play.game_2048.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs

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
        },
        onDrag = { change, dragAmount ->
            totalDragAmount += dragAmount
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

data class MergeAnimation(
    val sourceId: Int, 
    val targetId: Int,
    val position: Pair<Int, Int>
)