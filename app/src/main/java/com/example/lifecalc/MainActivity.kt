package com.example.lifecalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import org.koin.android.ext.android.inject
import kotlin.getValue
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.presentation.navigation.ZeitwertNavGraph
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.ZeitwertTheme

class MainActivity : ComponentActivity() {

    // BillingManager via Koin — Lifecycle an Activity gebunden
    private val billingManager: BillingManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Zeichnet hinter Status- und Navigationsleiste (modernes Android)
        enableEdgeToEdge()

        // Billing-Verbindung aufbauen
        billingManager.connect()

        setContent {
            ZeitwertTheme {
                ZeitwertNavGraph(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Verbindung sauber trennen
        billingManager.disconnect()
    }
}


