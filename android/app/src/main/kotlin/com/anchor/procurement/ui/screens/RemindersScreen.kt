package com.anchor.procurement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
fun RemindersScreen(viewModel: AnchorViewModel) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val reminders = data.reminders.sortedBy { it.due }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (reminders.isEmpty()) {
            item { Text("No reminders yet.", color = AnchorColors.TextMuted, fontSize = 13.sp) }
        }
        items(reminders, key = { it.id }) { r ->
            val done = r.status == "Completed"
            val (badgeLabel, badgeColor) = when {
                done -> "Done" to AnchorColors.SuccessDark
                r.status == "Overdue" -> "Overdue" to AnchorColors.Danger
                else -> "Upcoming" to AnchorColors.Primary
            }
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = AnchorColors.Surface)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleReminderDone(r.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = done, onCheckedChange = { viewModel.toggleReminderDone(r.id) })
                    Column(Modifier.weight(1f).padding(start = 6.dp)) {
                        Text(
                            r.title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                            color = if (done) AnchorColors.TextMuted else AnchorColors.TextWarm,
                        )
                        Text("Due ${Format.fdate(r.due)}", fontSize = 12.sp, color = AnchorColors.TextMuted)
                    }
                    StatusChip(badgeLabel, badgeColor)
                }
            }
        }
    }
}
