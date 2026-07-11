package com.anchor.procurement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.data.Format
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.theme.AnchorColors

@Composable
fun BudgetsScreen(viewModel: AnchorViewModel) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val currency = data.settings.currency
    val alertFrac = data.settings.budgetAlertPct / 100.0

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(data.budgets, key = { it.category }) { b ->
            val spent = data.purchases.filter { it.isSpend && it.category == b.category }.sumOf { it.total }
            val hasBudget = b.amount > 0
            val frac = if (hasBudget) spent / b.amount else 0.0
            val over = hasBudget && frac > 1.0
            val warn = hasBudget && frac >= alertFrac
            val barColor = if (over) AnchorColors.Danger else if (warn) AnchorColors.Warn else AnchorColors.Success

            var text by remember(b.category, b.amount) { mutableStateOf(if (hasBudget) b.amount.toInt().toString() else "") }

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(b.category, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            if (!hasBudget) "no budget set" else if (over) "${Format.money(spent - b.amount, currency, 0)} over" else "${Format.money(b.amount - spent, currency, 0)} left",
                            fontSize = 10.sp,
                            color = if (over) AnchorColors.Danger else AnchorColors.TextMuted,
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(top = 10.dp)
                            .background(AnchorColors.OutlineFaint, RoundedCornerShape(4.dp)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(frac.coerceIn(0.0, 1.0).toFloat())
                                .height(8.dp)
                                .background(barColor, RoundedCornerShape(4.dp)),
                        )
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Budget amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .width(160.dp),
                        singleLine = true,
                    )
                    TextButton(onClick = { viewModel.setBudget(b.category, text.toDoubleOrNull() ?: 0.0) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
