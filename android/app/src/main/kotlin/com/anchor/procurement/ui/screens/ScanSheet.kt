package com.anchor.procurement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.theme.AnchorColors
import kotlinx.coroutines.delay

private data class ScanSample(val fileName: String, val sub: String, val supplier: String, val invNo: String, val date: String, val item: String, val qty: String, val price: String, val category: String)

private val scanSamples = listOf(
    ScanSample("invoice_corelink_2231.pdf", "PDF · uploaded just now", "Corelink IT Distribution", "INV-2231", "2026-05-09", "27\" QHD Monitors", "10", "235", "IT Equipment"),
    ScanSample("invoice_brightway_8817.jpg", "Photo · uploaded just now", "Brightway Industrial", "INV-8817", "2026-07-08", "Safety Gloves Nitrile", "60", "10.21", "Facilities"),
    ScanSample("receipt_meridian_0710.jpg", "Photo · uploaded just now", "Meridian Office Supply", "", "2026-07-10", "Breakroom Coffee Supplies", "10", "42", "Office Supplies"),
)

/** Demo scan flow — mirrors the prototype's canned OCR simulation (not real OCR). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanSheet(viewModel: AnchorViewModel, categories: List<String>, onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    var picked by remember { mutableStateOf<ScanSample?>(null) }

    var item by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Office Supplies") }
    var supplier by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var invNo by remember { mutableStateOf("") }

    if (step == 2) {
        LaunchedEffect(picked) {
            delay(1700)
            picked?.let {
                item = it.item; category = it.category; supplier = it.supplier
                qty = it.qty; price = it.price; date = it.date; invNo = it.invNo
            }
            step = 3
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Scan a document", style = MaterialTheme.typography.titleLarge)

            when (step) {
                1 -> {
                    Text("Demo mode — pick a sample document to simulate a scan.", fontSize = 13.sp, color = AnchorColors.TextMuted)
                    scanSamples.forEach { s ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { picked = s; step = 2 },
                            colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface),
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(s.fileName, fontWeight = FontWeight.Medium)
                                Text(s.sub, fontSize = 12.sp, color = AnchorColors.TextMuted)
                            }
                        }
                    }
                }
                2 -> {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text("Reading document…", modifier = Modifier.padding(top = 12.dp))
                    }
                }
                else -> {
                    val dup = viewModel.dupCheckForScan(supplier, qty.toDoubleOrNull() ?: 0.0, price.toDoubleOrNull() ?: 0.0, date)
                    if (dup != null) {
                        Text(dup, color = AnchorColors.Warn, fontSize = 12.sp)
                    }
                    OutlinedTextField(value = item, onValueChange = { item = it }, label = { Text("Item") }, modifier = Modifier.fillMaxWidth())
                    Dropdown("Category", categories, category) { category = it }
                    OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Qty") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Unit price") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            viewModel.saveScannedPurchase(item, category, supplier, qty.toDoubleOrNull() ?: 0.0, price.toDoubleOrNull() ?: 0.0, date, invNo, "")
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Save scanned purchase") }
                }
            }
        }
    }
}
