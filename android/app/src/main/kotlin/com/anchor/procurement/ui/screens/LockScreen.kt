package com.anchor.procurement.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.R
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.theme.AnchorColors
import com.anchor.procurement.ui.theme.AvianoSerifFamily
import com.anchor.procurement.ui.theme.GordenFamily

private val LockGradient = Brush.linearGradient(
    0f to Color(0xFF8E8F94),
    0.38f to Color(0xFF55565B),
    0.70f to Color(0xFF2E2F33),
    1f to Color(0xFF0E0F12),
)

@Composable
fun LockScreen(viewModel: AnchorViewModel) {
    val entry by viewModel.pinEntry.collectAsStateWithLifecycle()
    val error by viewModel.pinError.collectAsStateWithLifecycle()
    val mode = viewModel.pinMode()
    var showForgotConfirm by remember { mutableStateOf(false) }

    val title = when (mode) {
        "create" -> "Create a 4-digit PIN"
        "confirm" -> "Confirm your PIN"
        else -> "Enter PIN"
    }
    val subtitle = if (mode == "enter") "Your procurement data is locked" else "Used to unlock this app on this device"

    Box(modifier = Modifier.fillMaxSize().background(LockGradient)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.TopStart).padding(18.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.anchor_wordmark_logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(38.dp).clip(CircleShape),
            )
            Text("Anchor", color = Color.White, fontSize = 18.sp, fontFamily = GordenFamily, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.profile_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(130.dp).clip(CircleShape),
            )
            Text(
                "Rodney Cedrick Rozario", color = Color.White, fontSize = 15.sp, fontFamily = AvianoSerifFamily, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 22.dp))
            Text(subtitle, color = Color(0xFFDDDDDD), fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))

            Row(modifier = Modifier.padding(vertical = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .width(13.dp)
                            .aspectRatio(1f)
                            .background(if (i < entry.length) Color.White else Color.Transparent, CircleShape),
                    )
                }
            }

            if (error.isNotEmpty()) {
                Text(error, color = Color(0xFFE8A18C), fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
            }

            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                keys.chunked(3).forEach { rowKeys ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        rowKeys.forEach { k ->
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(if (k.isNotEmpty()) Color.White.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable(enabled = k.isNotEmpty()) { viewModel.pressPinKey(k) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (k.isNotEmpty()) Text(k, color = Color.White, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            TextButton(onClick = { showForgotConfirm = true }, modifier = Modifier.padding(top = 20.dp)) {
                Text("Forgot PIN?", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            }
        }
    }

    if (showForgotConfirm) {
        AlertDialog(
            onDismissRequest = { showForgotConfirm = false },
            title = { Text("Reset PIN") },
            text = { Text("This clears your saved PIN so you can set a new one. Your purchases, suppliers, and other data stay untouched.") },
            confirmButton = {
                TextButton(onClick = { viewModel.forgotPin(); showForgotConfirm = false }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showForgotConfirm = false }) { Text("Cancel") }
            },
        )
    }
}
