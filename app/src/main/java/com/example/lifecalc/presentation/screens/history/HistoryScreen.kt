package com.example.lifecalc.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.Viewmodel.HistoryViewModel
import com.example.lifecalc.billing.PremiumStatus
import com.example.lifecalc.presentation.screens.composables.HistoryItem
import com.example.lifecalc.presentation.screens.composables.LockedHistoryItem
import com.example.lifecalc.presentation.screens.composables.PremiumBanner
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val history by viewModel.history.collectAsState(initial = emptyList())
    val premiumStatus by viewModel.premiumStatus.collectAsState()
    val isPremium = premiumStatus is PremiumStatus.Premium

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = OnSurface)
            }
            Text(
                "HISTORIE",
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
        }

        // Premium-Banner (nur Free)
        if (!isPremium) {
            PremiumBanner(onUpgrade = viewModel::launchPremium)
        }

        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Noch keine Berechnungen", color = OnSurface, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { entry ->
                    HistoryItem(entry)
                }
                if (!isPremium) {
                    item {
                        LockedHistoryItem()
                    }
                }
            }
        }
    }
}

