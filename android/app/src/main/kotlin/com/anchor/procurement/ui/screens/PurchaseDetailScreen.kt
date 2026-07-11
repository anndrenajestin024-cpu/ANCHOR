package com.anchor.procurement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.data.Format
import com.anchor.procurement.data.Statuses
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.components.StatusChip
import com.anchor.procurement.ui.theme.AnchorColors

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailScreen(viewModel: AnchorViewModel, purchaseId: String, onBack: () -> Unit, onOpenQuotes: () -> Unit) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val currency = data.settings.currency
    val p = data.purchases.find { it.id == purchaseId }

    LaunchedEffect(p) {
        if (p == null) onBack()
    }
    if (p == null) return

    val idx = Statuses.purchase.indexOf(p.status)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(p.item) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { viewModel.deletePurchase(p.id); onBack() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(Format.money(p.total, currency, 0), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                StatusChip(p.status, AnchorColors.Primary)
            }
            Text("${p.qty} ${p.unit} × ${Format.money(p.price, currency)} · ${Format.fdateY(p.date)}", fontSize = 13.sp, color = AnchorColors.TextMuted, modifier = Modifier.padding(top = 4.dp))

            if (p.savings > 0) {
                Text(
                    "Saved ${Format.money(p.savings, currency, 0)}" + (p.basis?.let { " (${Math.round(p.savings / it * 1000) / 10.0}%)" } ?: ""),
                    fontSize = 13.sp, color = AnchorColors.SuccessDark, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AnchorColors.OutlineFaint)
            Spacer(Modifier.height(16.dp))

            listOf(
                "Category" to p.category,
                "Supplier" to viewModel.supplierName(p.supplierId),
                "Quantity" to "${p.qty} ${p.unit}",
                "Unit price" to Format.money(p.price, currency),
                "Currency" to currency,
                "Purchase date" to Format.fdateY(p.date),
            ).forEach { (k, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k, fontSize = 13.sp, color = AnchorColors.TextMuted)
                    Text(v, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("STATUS TIMELINE", fontSize = 11.sp, color = AnchorColors.Primary, letterSpacing = 1.sp)
            Column(Modifier.padding(top = 8.dp)) {
                Statuses.purchase.forEachIndexed { i, s ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(if (i <= idx) "●" else "○", color = if (i <= idx) AnchorColors.Primary else AnchorColors.Outline)
                        Text(s, fontSize = 13.sp, color = if (i <= idx) AnchorColors.TextWarm else AnchorColors.TextMuted, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            if (p.docs.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("DOCUMENTS", fontSize = 11.sp, color = AnchorColors.Primary, letterSpacing = 1.sp)
                Column(Modifier.padding(top = 8.dp)) {
                    p.docs.forEach { d ->
                        Text("${d.name} — ${d.type} · added ${Format.fdate(d.date)}", fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }

            if (p.notes.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text("NOTES", fontSize = 11.sp, color = AnchorColors.Primary, letterSpacing = 1.sp)
                Text(p.notes, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            }

            if (p.groupId != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "View originating quote comparison →",
                    fontSize = 13.sp, color = AnchorColors.Primary, fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onOpenQuotes() },
                )
            }
        }
    }
}
