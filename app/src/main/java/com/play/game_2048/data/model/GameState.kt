package com.play.game_2048.data.model

import com.play.game_2048.util.MergedTile
import com.play.game_2048.util.Plateau
import com.play.game_2048.ui.Tile
import com.play.game_2048.util.estGagnant
import com.play.game_2048.util.estTermine

data class GameState(
    var plateau: Plateau,
    var score: Int = 0,
    var id: Int = 1,
    var isWin: Boolean = false,
    var isLose: Boolean = false,
    var boardSize: Int = 4,  // Default size is 4x4
    var lastMergedTiles: List<MergedTile> = emptyList(),
    var moveHistory: List<GameStateHistoryEntry> = emptyList()
) {
    fun updateState() {
        isWin = estGagnant(plateau)
        isLose = estTermine(plateau)
    }
}

data class GameStateHistoryEntry(
    val plateau: List<List<Tile>>,
    val score: Int,
    val id: Int
)