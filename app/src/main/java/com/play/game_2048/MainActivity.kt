package com.play.game_2048

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.play.game_2048.ui.theme.Game2048Theme

class MainActivity : ComponentActivity() {
    private var interstitialAd: InterstitialAd? = null
    private val adUnitId = "ca-app-pub-3940256099942544/1033173712"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        MobileAds.initialize(this) { initStatus ->
            val statusMap = initStatus.adapterStatusMap
            for (adapter in statusMap.keys) {
                val status = statusMap[adapter]
                Log.d("AdMob", "Adapter: $adapter, Status: ${status?.initializationState}")
            }
        }
        
        val testDeviceIds = listOf("D88961EAEF99FFD783871BE31FD76D95")
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        
        loadInterstitialAd()
        
        setContent {
            Game2048Theme {
                var hasSelectedMode by remember { mutableStateOf(false) }
                var currentGameMode by remember { mutableStateOf(GameMode.CLASSIC) }
                var gameState by remember(currentGameMode) { 
                    val emptyState = GameState(plateau = plateauVide(currentGameMode.size), boardSize = currentGameMode.size)
                    val initialPlateau = plateauInitial(emptyState)
                    mutableStateOf(GameState(
                        plateau = initialPlateau, 
                        id = emptyState.id,
                        boardSize = currentGameMode.size
                    ))
                }
                var oldBoard by remember { mutableStateOf(gameState.plateau) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (!hasSelectedMode) {
                        GameModeSelection { mode ->
                            currentGameMode = mode
                            hasSelectedMode = true
                            val resetEmptyState = GameState(
                                plateau = plateauVide(mode.size),
                                boardSize = mode.size
                            )
                            val resetInitialPlateau = plateauInitial(resetEmptyState)
                            gameState = GameState(
                                plateau = resetInitialPlateau,
                                id = resetEmptyState.id,
                                boardSize = mode.size
                            )
                            oldBoard = gameState.plateau
                        }
                    } else {
                        Board(
                            gameState = gameState,
                            oldBoard = oldBoard,
                            modifier = Modifier.padding(innerPadding),
                            move = { direction ->
                                oldBoard = gameState.plateau
                                gameState = deplacement(gameState, direction)
                            },
                            replay = {
                                val resetEmptyState = GameState(
                                    plateau = plateauVide(currentGameMode.size),
                                    boardSize = currentGameMode.size
                                )
                                val resetInitialPlateau = plateauInitial(resetEmptyState)
                                gameState = GameState(
                                    plateau = resetInitialPlateau,
                                    id = resetEmptyState.id,
                                    boardSize = currentGameMode.size
                                )
                                oldBoard = gameState.plateau
                            },
                            showAd = {
                                showInterstitialAd()
                            },
                            onGameModeSelected = { newMode ->
                                currentGameMode = newMode
                                val resetEmptyState = GameState(
                                    plateau = plateauVide(newMode.size),
                                    boardSize = newMode.size
                                )
                                val resetInitialPlateau = plateauInitial(resetEmptyState)
                                gameState = GameState(
                                    plateau = resetInitialPlateau,
                                    id = resetEmptyState.id,
                                    boardSize = newMode.size
                                )
                                oldBoard = gameState.plateau
                                showInterstitialAd()
                            }
                        )
                    }
                }
            }
        }
    }
    
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            this, 
            adUnitId, 
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("AdMob", "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    
                    interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdMob", "Ad dismissed")
                            loadInterstitialAd()
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d("AdMob", "Ad showed fullscreen content")
                            interstitialAd = null
                        }
                    }
                }
                
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdMob", "Interstitial ad failed to load: ${adError.message}")
                    interstitialAd = null
                }
            }
        )
    }
    
    private fun showInterstitialAd() {
        if (interstitialAd != null) {
            Log.d("AdMob", "Showing interstitial ad")
            interstitialAd?.show(this)
        } else {
            Log.d("AdMob", "Interstitial ad not ready yet, loading a new one")
            loadInterstitialAd()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (interstitialAd == null) {
            loadInterstitialAd()
        }
    }
}