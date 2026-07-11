package com.anchor.procurement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchor.procurement.data.Format
import com.anchor.procurement.data.Statuses
import com.anchor.procurement.ui.AnchorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseSheet(viewModel: AnchorViewModel, categories: List<String>, onDismiss: () -> Unit) {
    var item by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Office Supplies") }
    var supplier by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf(Statuses.units.first()) }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Format.today()) }
    var status by remember { mutableStateOf(Statuses.purchase.first()) }
    var notes by remember { mutableStateOf("") }
    var attach by remember { mutableStateOf(Statuses.attachTypes.first()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Log a purchase", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = item, onValueChange = { item = it }, label = { Text("Item") }, modifier = Modifier.fillMaxWidth())
            Dropdown("Category", categories, category) { category = it }
            OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                Dropdown("Unit", Statuses.units, unit, Modifier.weight(1f)) { unit = it }
            }
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Unit price") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (yyyy-mm-dd)") }, modifier = Modifier.fillMaxWidth())
            Dropdown("Status", Statuses.purchase, status) { status = it }
            Dropdown("Attachment", Statuses.attachTypes, attach) { attach = it }
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    viewModel.addPurchase(item, category, supplier, qty.toDoubleOrNull() ?: 0.0, unit, price.toDoubleOrNull() ?: 0.0, date, status, notes, attach)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save purchase") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(label: String, options: List<String>, value: String, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); expanded = false })
            }
        }
    }
}
