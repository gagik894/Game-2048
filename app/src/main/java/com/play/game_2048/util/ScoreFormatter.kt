package com.play.game_2048.util

import kotlin.math.floor

/**
 * Formats scores to use K notation (e.g., 10.5K) for large numbers
 */
fun formatScore(score: Int): String {
    return when {
        score < 1000 -> score.toString()
        score < 10000 -> String.format("%.1fK", score / 1000.0).replace(".0K", "K")
        score < 1000000 -> {
            val formatted = floor(score / 100.0) / 10.0
            if (formatted == formatted.toInt().toDouble()) {
                "${formatted.toInt()}K"
            } else {
                "${formatted}K"
            }
        }
        else -> {
            val formatted = floor(score / 100000.0) / 10.0
            if (formatted == formatted.toInt().toDouble()) {
                "${formatted.toInt()}M"
            } else {
                "${formatted}M"
            }
        }
    }
}