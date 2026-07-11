package com.anchor.procurement.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.data.Statuses
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.theme.AnchorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(viewModel: AnchorViewModel, onDismiss: () -> Unit) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val settings = data.settings
    val context = LocalContext.current

    var currency by remember(settings.currency) { mutableStateOf(settings.currency) }
    var budgetAlertPct by remember(settings.budgetAlertPct) { mutableStateOf(settings.budgetAlertPct.toString()) }
    var autoLock by remember(settings.autoLockMinutes) { mutableStateOf(settings.autoLockMinutes.toString()) }
    var ownerName by remember(settings.ownerName) { mutableStateOf(settings.ownerName) }
    var backupMsg by remember {
        mutableStateOf("Backup is a single file with all purchases, quotes, suppliers, budgets and reminders. Save it anywhere, including cloud storage.")
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        writeTextToUri(context, uri, viewModel.exportBackupJson())
        backupMsg = "Backup saved."
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val json = readTextFromUri(context, uri)
        if (json == null) {
            backupMsg = "Restore failed — file could not be read. Your current data is untouched."
        } else {
            viewModel.restoreBackup(json) { backupMsg = it }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Your name") }, modifier = Modifier.fillMaxWidth())
            Dropdown("Currency", Statuses.currencies, currency) { currency = it }
            OutlinedTextField(value = budgetAlertPct, onValueChange = { budgetAlertPct = it }, label = { Text("Budget alert % (70-100)") }, modifier = Modifier.fillMaxWidth())
            Dropdown("Auto-lock", listOf("0", "1", "5", "15"), autoLock) { autoLock = it }

            Button(
                onClick = {
                    viewModel.saveSettings(
                        currency, settings.appLockEnabled,
                        budgetAlertPct.toIntOrNull()?.coerceIn(70, 100) ?: settings.budgetAlertPct,
                        autoLock.toIntOrNull() ?: settings.autoLockMinutes,
                        ownerName,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save settings") }

            OutlinedButton(onClick = { viewModel.lockNow(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                Text("Lock now")
            }

            HorizontalDivider(color = AnchorColors.OutlineFaint, modifier = Modifier.padding(vertical = 8.dp))

            Text("BACKUP & RESTORE", style = MaterialTheme.typography.labelLarge)
            Text(backupMsg, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { exportLauncher.launch("anchor-backup.json") }, modifier = Modifier.fillMaxWidth()) {
                Text("Download backup")
            }
            OutlinedButton(onClick = { restoreLauncher.launch("application/json") }, modifier = Modifier.fillMaxWidth()) {
                Text("Restore from backup")
            }

            HorizontalDivider(color = AnchorColors.OutlineFaint, modifier = Modifier.padding(vertical = 8.dp))

            Text("DATA", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(onClick = { viewModel.resetToSampleData() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reset to sample data")
            }
            OutlinedButton(onClick = { viewModel.clearAllData() }, modifier = Modifier.fillMaxWidth()) {
                Text("Clear all data")
            }
        }
    }
}

private fun writeTextToUri(context: Context, uri: android.net.Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
}

private fun readTextFromUri(context: Context, uri: android.net.Uri): String? =
    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
