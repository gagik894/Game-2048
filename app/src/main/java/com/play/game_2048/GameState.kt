package com.play.game_2048

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