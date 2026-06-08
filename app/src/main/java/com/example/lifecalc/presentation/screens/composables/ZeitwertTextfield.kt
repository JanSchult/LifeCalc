package com.example.lifecalc.presentation.screens.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.lifecalc.ui.theme.OnBackground
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.Surface
import com.example.lifecalc.ui.theme.SurfaceAlt

@Composable
 fun ZeitwertTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = SurfaceAlt,
            focusedLabelColor = Primary,
            unfocusedTextColor = OnBackground,
            focusedTextColor = OnBackground,
            cursorColor = Primary,
            unfocusedContainerColor = Surface,
            focusedContainerColor = Surface
        )
    )
}