package com.play.game_2048

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.play.game_2048.ui.GameApp
import com.play.game_2048.ui.theme.Game2048Theme

class MainActivity : ComponentActivity() {
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
        
        setContent {
            Game2048Theme {
                GameApp()
            }
        }
    }
}