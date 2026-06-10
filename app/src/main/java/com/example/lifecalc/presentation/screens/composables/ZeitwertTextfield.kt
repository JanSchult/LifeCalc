package com.example.lifecalc.presentation.screens.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.ui.theme.Danger
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
    keyboardType: KeyboardType = KeyboardType.Decimal,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 12.sp) },
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) Danger else Primary,
                unfocusedBorderColor = if (isError) Danger else SurfaceAlt,
                focusedLabelColor = if (isError) Danger else Primary,
                errorBorderColor = Danger,
                errorLabelColor = Danger,
                errorCursorColor = Danger,
                unfocusedTextColor = OnBackground,
                focusedTextColor = OnBackground,
                cursorColor = Primary,
                unfocusedContainerColor = Surface,
                focusedContainerColor = Surface
            )
        )
        // Fehlermeldung unter dem Feld
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Danger,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}