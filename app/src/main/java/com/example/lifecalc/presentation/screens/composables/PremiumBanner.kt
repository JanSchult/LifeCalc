package com.example.lifecalc.presentation.screens.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.R
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary

@Composable
fun PremiumBanner(onUpgrade: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onUpgrade() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1A00)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Primary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text( stringResource(R.string.paywall_title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
                Text( stringResource(R.string.history_premium_subtitle), fontSize = 12.sp, color = OnSurface)
            }
            Text("→", fontSize = 20.sp, color = Primary)
        }
    }
}