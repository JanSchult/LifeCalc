package com.example.lifecalc.presentation.screens.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.Surface

@Composable
 fun PaywallDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⭐", fontSize = 40.sp)
                Text(
                    "Premium freischalten",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    "Mit Premium kannst du unbegrenzte Ausgaben eintragen und dein Budget vollständig im Blick behalten.",
                    fontSize = 14.sp,
                    color = OnSurface,
                    lineHeight = 20.sp
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Background
                    )
                ) {
                    Text("Ab 2,99 €/Monat — Jetzt testen")
                }
                TextButton(onClick = onDismiss) {
                    Text("Später", color = OnSurface)
                }
            }
        }
    }
}
