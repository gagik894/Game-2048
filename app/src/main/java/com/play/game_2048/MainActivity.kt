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
    // Interstitial ad reference
    private var interstitialAd: InterstitialAd? = null
    private val adUnitId = "ca-app-pub-3940256099942544/1033173712" // Test interstitial ad unit ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) { initStatus ->
            val statusMap = initStatus.adapterStatusMap
            for (adapter in statusMap.keys) {
                val status = statusMap[adapter]
                Log.d("AdMob", "Adapter: $adapter, Status: ${status?.initializationState}")
            }
        }
        
        // Set up test device ID
        val testDeviceIds = listOf("D88961EAEF99FFD783871BE31FD76D95")
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        
        // Load the first interstitial ad when the app starts
        loadInterstitialAd()
        
        setContent {
            Game2048Theme {
                // Create an empty GameState first to avoid initialization issues
                val emptyState = GameState(plateau = plateauVide())
                // Initialize the plateau
                val initialPlateau = plateauInitial(emptyState)
                // Create initial GameState with the plateau and the ID
                var gameState by remember { 
                    mutableStateOf(GameState(plateau = initialPlateau, id = emptyState.id))
                }
                var oldBoard by remember { mutableStateOf(gameState.plateau) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Board(
                        gameState = gameState,
                        oldBoard = oldBoard,
                        modifier = Modifier.padding(innerPadding),
                        move = { direction ->
                            oldBoard = gameState.plateau
                            gameState = deplacement(gameState, direction)
                        },
                        replay = {
                            // Reset the game with a new initial state
                            val resetEmptyState = GameState(plateau = plateauVide())
                            val resetInitialPlateau = plateauInitial(resetEmptyState)
                            gameState = GameState(plateau = resetInitialPlateau, id = resetEmptyState.id)
                            oldBoard = gameState.plateau
                        },
                        showAd = {
                            // Show interstitial ad if available
                            showInterstitialAd()
                        }
                    )
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
                    
                    // Set the fullscreen callback
                    interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdMob", "Ad dismissed")
                            // Load a new ad when the current one is dismissed
                            loadInterstitialAd()
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d("AdMob", "Ad showed fullscreen content")
                            // Set to null once shown
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
        // Reload ad if needed when returning to the app
        if (interstitialAd == null) {
            loadInterstitialAd()
        }
    }
}