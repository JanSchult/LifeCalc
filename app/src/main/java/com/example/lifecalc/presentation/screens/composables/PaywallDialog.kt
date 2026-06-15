package com.example.lifecalc.presentation.screens.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lifecalc.R
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.Surface

@Composable
 fun PaywallDialog(
    onDismiss: () -> Unit,
    onMonthly: () -> Unit,   // ← neu
    onYearly: () -> Unit     // ← neu
) {
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
                    stringResource(R.string.history_premium_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    stringResource(R.string.paywall_description),
                    fontSize = 14.sp,
                    color = OnSurface,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                // Jahres-Abo — prominent
                Button(
                    onClick = onYearly,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Background
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.paywall_yearly),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            stringResource(R.string.paywall_yearly_hint),
                            fontSize = 11.sp
                        )
                    }
                }

                // Monats-Abo — sekundär
                OutlinedButton(
                    onClick = onMonthly,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Primary)
                ) {
                    Text( stringResource(R.string.paywall_monthly), color = Primary)
                }

                TextButton(onClick = onDismiss) {
                    Text( stringResource(R.string.paywall_later), color = OnSurface)
                }
            }
        }
    }
}