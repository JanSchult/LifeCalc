package com.example.lifecalc.presentation.screens.composables

import android.text.Layout
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lifecalc.ui.theme.OnSurface

@Composable
 fun SummaryItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    align: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = align) {
        Text(label, fontSize = 11.sp, color = OnSurface)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}