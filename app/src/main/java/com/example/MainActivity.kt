package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.ui.theme.KaomiKormoTheme

class MainActivity : ComponentActivity() {
    private val deepLinkRouteState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("AuthDebug", "Firebase init error: ${e.message}")
        }
        
        // Initialize repositories and settings
        AppSettingsManager.init(this)
        ProfileAndInboxRepository.init(this)
        
        deepLinkRouteState.value = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_ROUTE)
        enableEdgeToEdge()
        
        setContent {
            val isDark by AppSettingsManager.isDarkMode.collectAsState()
            KaomiKormoTheme(darkTheme = isDark) {
                val pendingRoute by deepLinkRouteState
                AppNavigation(
                    deepLinkRoute = pendingRoute,
                    onConsumeDeepLink = { deepLinkRouteState.value = null }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ProfileAndInboxRepository.setOnlinePresence(true)
    }

    override fun onStop() {
        super.onStop()
        ProfileAndInboxRepository.setOnlinePresence(
            isOnline = ProfileAndInboxRepository.currentUserProfile.value.isOnline
        )
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(NotificationHelper.EXTRA_TARGET_ROUTE)?.let {
            deepLinkRouteState.value = it
        }
    }
}
