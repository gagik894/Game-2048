package com.play.game_2048.util

import kotlin.math.floor

/**
 * Formats scores to use K notation (e.g., 10.5K) for large numbers
 */
fun formatScore(score: Int): String {
    return when {
        score < 1000 -> score.toString()
        score < 10000 -> String.format("%.2fK", score / 1000.0)
        score < 1000000 -> {
            val formatted = floor(score / 10.0) / 100.0
            String.format("%.2fK", formatted)
        }
        else -> {
            val formatted = floor(score / 10000.0) / 100.0
            String.format("%.2fM", formatted)
        }
    }
}