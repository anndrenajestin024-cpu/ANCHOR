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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.anchor.procurement.data.AnchorData
import com.anchor.procurement.ui.AnchorViewModel

private const val NEW_COMPARISON = "＋ New comparison…"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuoteSheet(viewModel: AnchorViewModel, data: AnchorData, onDismiss: () -> Unit) {
    val activeGroups = data.groups.filter { it.status == "active" }
    val groupOptions = activeGroups.map { it.title } + NEW_COMPARISON

    var groupTitle by remember { mutableStateOf(groupOptions.first()) }
    var newTitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(data.categories.firstOrNull() ?: "Facilities") }
    var supplier by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var validUntil by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add a quote", style = MaterialTheme.typography.titleLarge)
            Dropdown("Comparison", groupOptions, groupTitle) { groupTitle = it }
            if (groupTitle == NEW_COMPARISON) {
                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("New comparison name") }, modifier = Modifier.fillMaxWidth())
                Dropdown("Category", data.categories, category) { category = it }
            }
            OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Unit price") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = validUntil, onValueChange = { validUntil = it }, label = { Text("Valid until (yyyy-mm-dd)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    viewModel.addQuoteToGroup(
                        groupTitle = if (groupTitle == NEW_COMPARISON) null else groupTitle,
                        newTitle = newTitle, category = category, supplierName = supplier,
                        qty = qty.toDoubleOrNull() ?: 0.0, price = price.toDoubleOrNull() ?: 0.0, validUntil = validUntil,
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save quote") }
        }
    }
}
