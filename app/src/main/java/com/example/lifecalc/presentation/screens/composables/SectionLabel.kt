package com.example.lifecalc.presentation.screens.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lifecalc.ui.theme.PrimaryDim

@Composable
 fun SectionLabel(text: String) = Text(
    text = text,
    fontSize = 10.sp,
    letterSpacing = 3.sp,
    color = PrimaryDim,
    fontWeight = FontWeight.Medium
)