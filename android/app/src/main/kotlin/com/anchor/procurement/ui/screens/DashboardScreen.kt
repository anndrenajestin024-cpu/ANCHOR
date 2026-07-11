package com.anchor.procurement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.data.Format
import com.anchor.procurement.data.Palette
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.components.SectionLabel
import com.anchor.procurement.ui.theme.AnchorColors

@Composable
fun DashboardScreen(viewModel: AnchorViewModel, onOpenPurchase: (String) -> Unit) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val range by viewModel.range.collectAsStateWithLifecycle()
    val currency = data.settings.currency

    val ranged = viewModel.rangedPurchases()
    val spend = ranged.filter { it.isSpend }
    val totalSpend = spend.sumOf { it.total }
    val savingsPurchases = ranged.filter { it.savings > 0 }
    val totalSavings = savingsPurchases.sumOf { it.savings }
    val pending = ranged.filter { it.status in listOf("Ordered", "Delivered") }
    val pendingAmt = pending.sumOf { it.total }

    val totalBudget = data.budgets.sumOf { it.amount }
    val ytdSpend = data.purchases.filter { it.isSpend }.sumOf { it.total }
    val variance = totalBudget - ytdSpend

    val byCategory = spend.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.total } }
        .toList().sortedByDescending { it.second }

    val recent = data.purchases.sortedByDescending { it.date }.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "All time", "6m" to "6 months", "3m" to "3 months", "30d" to "30 days").forEach { (v, label) ->
                    FilterChip(selected = range == v, onClick = { viewModel.setRange(v) }, label = { Text(label, fontSize = 12.sp) })
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Total spend", Format.money(totalSpend, currency, 0), "${spend.size} purchases", Modifier.weight(1f))
                KpiCard("Savings", Format.money(totalSavings, currency, 0), "vs highest quotes", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    "Budget variance",
                    if (variance >= 0) Format.money(variance, currency, 0) + " under" else Format.money(-variance, currency, 0) + " over",
                    "${Format.money(ytdSpend, currency, 0)} of ${Format.money(totalBudget, currency, 0)}",
                    Modifier.weight(1f),
                )
                KpiCard("Pending", Format.money(pendingAmt, currency, 0), "${pending.size} orders awaiting payment", Modifier.weight(1f))
            }
        }

        if (byCategory.isNotEmpty()) {
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface)) {
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel("Spend by category")
                        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            byCategory.forEach { (cat, amt) ->
                                val color = Color(Palette.categoryColor(cat, data.categories))
                                val frac = if (totalSpend > 0) (amt / totalSpend).toFloat() else 0f
                                Column {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(cat, fontSize = 13.sp, color = AnchorColors.CharcoalMid)
                                        Text(Format.money(amt, currency, 0), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .padding(top = 4.dp)
                                            .background(AnchorColors.OutlineFaint, RoundedCornerShape(3.dp)),
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth(frac.coerceIn(0f, 1f))
                                                .height(6.dp)
                                                .background(color, RoundedCornerShape(3.dp)),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface)) {
                Column(Modifier.padding(16.dp)) {
                    SectionLabel("Recent purchases")
                    Column(Modifier.padding(top = 8.dp)) {
                        if (recent.isEmpty()) {
                            Text("No purchases yet", color = AnchorColors.TextMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        recent.forEachIndexed { i, p ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenPurchase(p.id) }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(p.item, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${viewModel.supplierName(p.supplierId)} · ${Format.fdate(p.date)} · ${p.status}",
                                        fontSize = 12.sp, color = AnchorColors.TextMuted,
                                    )
                                }
                                Text(Format.money(p.total, currency, 0), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (i < recent.lastIndex) HorizontalDivider(color = AnchorColors.OutlineFaint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface)) {
        Column(Modifier.padding(14.dp)) {
            Text(label.uppercase(), fontSize = 10.sp, color = AnchorColors.TextMuted, letterSpacing = 1.sp)
            Text(value, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = AnchorColors.TextWarm, modifier = Modifier.padding(top = 6.dp))
            Text(sub, fontSize = 11.sp, color = AnchorColors.TextMuted, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
