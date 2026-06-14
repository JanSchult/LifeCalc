package com.example.lifecalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import org.koin.android.ext.android.inject
import kotlin.getValue
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.data.preference.UserPreferences
import com.example.lifecalc.presentation.navigation.Screen
import com.example.lifecalc.presentation.navigation.ZeitwertNavGraph
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.ZeitwertTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val billingManager: BillingManager by inject()

    @Volatile
    private var onboardingDone: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        billingManager.connect()

        val userPreferences = UserPreferences(this)

        splashScreen.setKeepOnScreenCondition { onboardingDone == null }

        lifecycleScope.launch {
            // Erst lesen...
            val done = userPreferences.onboardingDoneFlow.first()

            android.util.Log.d("ONBOARDING", "onboardingDone aus DataStore: $done")

            onboardingDone = done

            val startDestination = if (done) Screen.Input.route
            else Screen.Onboarding.route

            android.util.Log.d("ONBOARDING", "startDestination: $startDestination")

            // ...dann setContent aufrufen
            setContent {
                ZeitwertTheme {
                    ZeitwertNavGraph(
                        startDestination = startDestination,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Background)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.disconnect()
    }
}