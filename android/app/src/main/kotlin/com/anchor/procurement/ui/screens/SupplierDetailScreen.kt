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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.data.Format
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.theme.AnchorColors

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(viewModel: AnchorViewModel, supplierId: String, onBack: () -> Unit, onOpenPurchase: (String) -> Unit) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val currency = data.settings.currency
    val supplier = data.suppliers.find { it.id == supplierId }

    LaunchedEffect(supplier) {
        if (supplier == null) onBack()
    }
    if (supplier == null) return

    val purchases = data.purchases.filter { it.supplierId == supplierId }.sortedByDescending { it.date }
    val spend = purchases.filter { it.isSpend }.sumOf { it.total }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(supplier.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
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
            Text(Format.money(spend, currency, 0), fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text("${purchases.size} purchases total", fontSize = 10.sp, color = AnchorColors.TextMuted)

            if (supplier.contact.isNotBlank()) {
                Text(supplier.contact, fontSize = 11.sp, color = AnchorColors.TextMuted, modifier = Modifier.padding(top = 12.dp))
            }
            if (supplier.notes.isNotBlank()) {
                Text(supplier.notes, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AnchorColors.OutlineFaint)
            Spacer(Modifier.height(12.dp))
            Text("PURCHASE HISTORY", fontSize = 9.sp, color = AnchorColors.Primary, letterSpacing = 1.sp)

            purchases.forEach { p ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPurchase(p.id) }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(p.item, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("${Format.fdateY(p.date)} · ${p.qty} ${p.unit} × ${Format.money(p.price, currency)} · ${p.status}", fontSize = 9.sp, color = AnchorColors.TextMuted)
                    }
                    Text(Format.money(p.total, currency, 0), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider(color = AnchorColors.OutlineFaint)
            }
        }
    }
}
