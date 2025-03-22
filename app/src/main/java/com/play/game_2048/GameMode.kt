package com.play.game_2048

enum class GameMode(val size: Int, val displayName: String) {
    CLASSIC(4, "Classic 4x4"),
    MINI(3, "Mini 3x3"),
    LARGE(5, "Large 5x5"),
    EXTREME(6, "Extreme 6x6")
}