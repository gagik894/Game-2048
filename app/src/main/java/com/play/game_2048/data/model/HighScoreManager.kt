package com.play.game_2048.data.model

import android.content.Context
import android.content.SharedPreferences
import com.play.game_2048.data.model.GameMode

/**
 * Manages high scores for different game modes in the 2048 game
 */
class HighScoreManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Get the high score for a specific game mode
     */
    fun getHighScore(gameMode: GameMode): Int {
        val key = getKeyForMode(gameMode)
        return prefs.getInt(key, 0)
    }
    
    /**
     * Update the high score for a game mode if the new score is higher
     * Returns true if a new high score was set
     */
    fun updateHighScore(gameMode: GameMode, score: Int): Boolean {
        val key = getKeyForMode(gameMode)
        val currentHighScore = getHighScore(gameMode)
        
        if (score > currentHighScore) {
            prefs.edit().putInt(key, score).apply()
            return true
        }
        return false
    }
    
    /**
     * Reset the high score for a specific game mode
     */
    fun resetHighScore(gameMode: GameMode) {
        val key = getKeyForMode(gameMode)
        prefs.edit().putInt(key, 0).apply()
    }
    
    /**
     * Reset all high scores across all game modes
     */
    fun resetAllHighScores() {
        GameMode.entries.forEach { resetHighScore(it) }
    }
    
    /**
     * Get all high scores as a map of game mode to score
     */
    fun getAllHighScores(): Map<GameMode, Int> {
        return GameMode.entries.associateWith { getHighScore(it) }
    }
    
    /**
     * Create a preference key for the given game mode
     */
    private fun getKeyForMode(gameMode: GameMode): String {
        return HIGH_SCORE_PREFIX + gameMode.name
    }
    
    companion object {
        private const val PREF_NAME = "high_scores"
        private const val HIGH_SCORE_PREFIX = "high_score_"
    }
}