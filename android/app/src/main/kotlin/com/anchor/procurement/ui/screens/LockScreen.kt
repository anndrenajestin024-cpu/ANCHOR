package com.anchor.procurement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.theme.AnchorColors

@Composable
fun LockScreen(viewModel: AnchorViewModel) {
    val entry by viewModel.pinEntry.collectAsStateWithLifecycle()
    val error by viewModel.pinError.collectAsStateWithLifecycle()
    val mode = viewModel.pinMode()

    val title = when (mode) {
        "create" -> "Create a 4-digit PIN"
        "confirm" -> "Confirm your PIN"
        else -> "Enter PIN"
    }
    val subtitle = if (mode == "enter") "Your procurement data is locked" else "Used to unlock this app on this device"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AnchorColors.Charcoal)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Anchor", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(top = 24.dp))
        Text(subtitle, color = AnchorColors.TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))

        Row(modifier = Modifier.padding(vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .aspectRatio(1f)
                        .background(if (i < entry.length) Color.White else Color.Transparent, CircleShape),
                )
            }
        }

        if (error.isNotEmpty()) {
            Text(error, color = AnchorColors.Danger, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
        }

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            keys.chunked(3).forEach { rowKeys ->
                Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    rowKeys.forEach { k ->
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .aspectRatio(1f)
                                .background(if (k.isNotEmpty()) Color(0xFF232428) else Color.Transparent, CircleShape)
                                .clickable(enabled = k.isNotEmpty()) { viewModel.pressPinKey(k) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (k.isNotEmpty()) Text(k, color = Color.White, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}
