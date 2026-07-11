package com.anchor.procurement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.data.Format
import com.anchor.procurement.data.Palette
import com.anchor.procurement.data.Statuses
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.components.StatusChip
import com.anchor.procurement.ui.theme.AnchorColors

@Composable
fun PurchasesScreen(viewModel: AnchorViewModel, onOpenPurchase: (String) -> Unit) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val currency = data.settings.currency
    val rows = viewModel.filteredPurchases()

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = filters.search,
            onValueChange = viewModel::setSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search purchases") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
        )

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = filters.status == null && filters.category == null && filters.supplierId == null,
                        onClick = { viewModel.clearFilters() },
                        label = { Text("All", fontSize = 10.sp) },
                    )
                    Statuses.purchase.forEach { s ->
                        FilterChip(
                            selected = filters.status == s,
                            onClick = { viewModel.setStatusFilter(if (filters.status == s) null else s) },
                            label = { Text(s, fontSize = 9.sp) },
                        )
                    }
                }
            }
            item { Text("${rows.size} purchases", fontSize = 10.sp, color = AnchorColors.TextMuted, modifier = Modifier.padding(vertical = 12.dp)) }

            if (rows.isEmpty()) {
                item { Text("No purchases match these filters.", color = AnchorColors.TextMuted, fontSize = 11.sp) }
            }

            items(rows, key = { it.id }) { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onOpenPurchase(p.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(p.item, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            StatusChip(p.status, Color(Palette.statusColor(p.status)))
                        }
                        Text(
                            "${viewModel.supplierName(p.supplierId)} · ${p.qty.toCleanString()} ${p.unit} × ${Format.money(p.price, currency)} · ${Format.fdate(p.date)}",
                            fontSize = 10.sp, color = AnchorColors.TextMuted, modifier = Modifier.padding(top = 4.dp),
                        )
                        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(Format.money(p.total, currency, 0), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            if (p.savings > 0) {
                                Text("saved ${Format.money(p.savings, currency, 0)}", fontSize = 10.sp, color = AnchorColors.SuccessDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.toCleanString(): String = if (this % 1.0 == 0.0) this.toInt().toString() else this.toString()
