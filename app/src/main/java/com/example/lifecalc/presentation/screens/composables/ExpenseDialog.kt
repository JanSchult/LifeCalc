package com.example.lifecalc.presentation.screens.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lifecalc.domain.model.ExpenseCategory
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.OnBackground
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.Surface
import com.example.lifecalc.ui.theme.SurfaceAlt

@Composable
 fun ExpenseDialog(
    isEditing: Boolean,
    name: String,
    amount: String,
    category: ExpenseCategory,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Ausgabe bearbeiten" else "Ausgabe hinzufügen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Bezeichnung") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("Betrag (€/Monat)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                // Kategorie-Auswahl
                Text("Kategorie", fontSize = 12.sp, color = OnSurface)
                CategoryGrid(
                    selected = category,
                    onSelect = onCategoryChange
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abbrechen", color = OnSurface)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Background
                        )
                    ) {
                        Text(if (isEditing) "Speichern" else "Hinzufügen")
                    }
                }
            }
        }
    }
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = SurfaceAlt,
    focusedLabelColor = Primary,
    focusedTextColor = OnBackground,
    unfocusedTextColor = OnBackground,
    cursorColor = Primary,
    unfocusedContainerColor = SurfaceAlt,
    focusedContainerColor = SurfaceAlt
)