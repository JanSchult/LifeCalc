package com.example.lifecalc.presentation.screens.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifecalc.domain.model.ExpenseCategory
import com.example.lifecalc.ui.theme.Background
import com.example.lifecalc.ui.theme.OnSurface
import com.example.lifecalc.ui.theme.Primary
import com.example.lifecalc.ui.theme.Surface
import com.example.lifecalc.ui.theme.SurfaceAlt

@Composable
fun CategoryGrid(
    selected: ExpenseCategory,
    onSelect: (ExpenseCategory) -> Unit
) {
    val categories = ExpenseCategory.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(4).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { cat ->
                    val isSelected = cat == selected
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(cat) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Primary else SurfaceAlt
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(cat.emoji, fontSize = 18.sp)
                            Text(
                                cat.label,
                                fontSize = 9.sp,
                                color = if (isSelected) Background else OnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
