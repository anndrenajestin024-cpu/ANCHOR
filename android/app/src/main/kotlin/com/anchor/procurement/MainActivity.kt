package com.anchor.procurement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anchor.procurement.ui.AnchorRoot
import com.anchor.procurement.ui.AnchorViewModel
import com.anchor.procurement.ui.theme.AnchorTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as AnchorApp).repository

        setContent {
            val viewModel: AnchorViewModel = viewModel(factory = AnchorViewModel.Factory(repository))

            LaunchedEffect(Unit) {
                while (true) {
                    delay(15_000)
                    viewModel.checkAutoLock()
                }
            }

            AnchorTheme {
                AnchorRoot(viewModel = viewModel)
            }
        }
    }
}
