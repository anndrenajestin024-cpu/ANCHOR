package com.anchor.procurement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.data.Format
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.components.StatusChip
import com.anchor.procurement.ui.theme.AnchorColors

@Composable
fun QuotesScreen(viewModel: AnchorViewModel) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val currency = data.settings.currency
    val groups = data.groups.sortedBy { if (it.status == "active") 0 else 1 }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (groups.isEmpty()) {
            item { Text("No quote comparisons yet.", color = AnchorColors.TextMuted, fontSize = 11.sp) }
        }
        items(groups, key = { it.id }) { g ->
            val active = g.status == "active"
            val totals = g.quotes.map { it.price * g.qty }
            val minTotal = totals.minOrNull() ?: 0.0
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(g.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        StatusChip(
                            label = if (active) (if (g.selectedQuoteId != null) "Selected" else "Comparing") else "Converted",
                            color = if (active) AnchorColors.Primary else AnchorColors.SuccessDark,
                        )
                    }
                    Text("${g.qty.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }} ${g.unit} · ${g.category} · ${g.quotes.size} quotes", fontSize = 10.sp, color = AnchorColors.TextMuted, modifier = Modifier.padding(top = 4.dp))

                    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        g.quotes.sortedBy { it.price }.forEach { q ->
                            val total = q.price * g.qty
                            val cheapest = total == minTotal
                            val selected = g.selectedQuoteId == q.id
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(viewModel.supplierName(q.supplierId), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("${Format.money(q.price, currency)} / unit · valid ${Format.fdate(q.validUntil)}", fontSize = 9.sp, color = AnchorColors.TextMuted)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(Format.money(total, currency, 0), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    if (cheapest || selected) {
                                        Text(
                                            if (selected) " · Selected" else " · Lowest",
                                            fontSize = 9.sp,
                                            color = if (selected) AnchorColors.Primary else AnchorColors.SuccessDark,
                                        )
                                    }
                                }
                            }
                            if (active && !selected) {
                                Button(onClick = { viewModel.selectQuote(g.id, q.id) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Select this quote")
                                }
                            }
                            HorizontalDivider(color = AnchorColors.OutlineFaint)
                        }
                    }

                    if (active && g.selectedQuoteId != null) {
                        val selQ = g.quotes.first { it.id == g.selectedQuoteId }
                        val savings = (totals.maxOrNull() ?: 0.0) - selQ.price * g.qty
                        if (savings > 0) {
                            Text(
                                "Savings: ${Format.money(savings, currency, 0)}",
                                fontSize = 11.sp, color = AnchorColors.SuccessDark, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        Button(onClick = { viewModel.convertGroupToPurchase(g.id) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text("Convert to purchase")
                        }
                    }
                }
            }
        }
    }
}
