package com.anchor.procurement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.anchor.procurement.ui.theme.AnchorColors

@Composable
fun SuppliersScreen(viewModel: AnchorViewModel, onOpenSupplier: (String) -> Unit) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val currency = data.settings.currency

    val spendBySupplier = data.purchases.filter { it.isSpend }
        .groupBy { it.supplierId }.mapValues { (_, list) -> list.sumOf { it.total } }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (data.suppliers.isEmpty()) {
            item { Text("No suppliers yet.", color = AnchorColors.TextMuted, fontSize = 11.sp) }
        }
        items(data.suppliers, key = { it.id }) { s ->
            val count = data.purchases.count { it.supplierId == s.id }
            val amount = spendBySupplier[s.id] ?: 0.0
            val color = Color(Palette.categoryColor(s.name, data.suppliers.map { it.name }))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSupplier(s.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier
                            .background(color, CircleShape),
                    ) {
                        Text(
                            s.name.split(" ").take(2).mapNotNull { it.firstOrNull() }.joinToString(""),
                            color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(s.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("$count purchases · ${s.contact.substringBefore("·").trim()}", fontSize = 9.sp, color = AnchorColors.TextMuted)
                    }
                    Text(Format.money(amount, currency, 0), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
