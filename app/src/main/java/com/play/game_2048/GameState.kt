package com.play.game_2048

data class GameState(
    var plateau: Plateau,
    var score: Int = 0,
    var id: Int = 1,
    var isWin: Boolean = false,
    var isLose: Boolean = false
) {
    fun updateState() {
        isWin = estGagnant(plateau)
        isLose = estTermine(plateau)
    }
}