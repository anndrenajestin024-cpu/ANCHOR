package com.anchor.procurement.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anchor.procurement.R
import com.anchor.procurement.ui.theme.GordenFamily
import com.anchor.procurement.ui.screens.AddPurchaseSheet
import com.anchor.procurement.ui.screens.AddQuoteSheet
import com.anchor.procurement.ui.screens.BudgetsScreen
import com.anchor.procurement.ui.screens.DashboardScreen
import com.anchor.procurement.ui.screens.LockScreen
import com.anchor.procurement.ui.screens.PurchaseDetailScreen
import com.anchor.procurement.ui.screens.PurchasesScreen
import com.anchor.procurement.ui.screens.QuotesScreen
import com.anchor.procurement.ui.screens.RemindersScreen
import com.anchor.procurement.ui.screens.ScanSheet
import com.anchor.procurement.ui.screens.SettingsSheet
import com.anchor.procurement.ui.screens.SupplierDetailScreen
import com.anchor.procurement.ui.screens.SuppliersScreen

private enum class Tab(val label: String) { Dashboard("Dashboard"), Purchases("Purchases"), Quotes("Quotes"), Suppliers("Suppliers"), Budgets("Budgets"), Reminders("Reminders") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchorRoot(viewModel: AnchorViewModel) {
    val locked by viewModel.locked.collectAsStateWithLifecycle()

    if (locked) {
        LockScreen(viewModel)
        return
    }

    val data by viewModel.data.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()

    var tab by remember { mutableStateOf(Tab.Dashboard) }
    var purchaseDetailId by remember { mutableStateOf<String?>(null) }
    var supplierDetailId by remember { mutableStateOf<String?>(null) }
    var showAddPurchase by remember { mutableStateOf(false) }
    var showAddQuote by remember { mutableStateOf(false) }
    var showScan by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var fabOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        if (toast.isNotEmpty()) {
            snackbarHostState.showSnackbar(toast)
            viewModel.clearToast()
        }
    }

    if (purchaseDetailId != null) {
        PurchaseDetailScreen(
            viewModel = viewModel,
            purchaseId = purchaseDetailId!!,
            onBack = { purchaseDetailId = null },
            onOpenQuotes = { purchaseDetailId = null; tab = Tab.Quotes },
        )
        return
    }
    if (supplierDetailId != null) {
        SupplierDetailScreen(
            viewModel = viewModel,
            supplierId = supplierDetailId!!,
            onBack = { supplierDetailId = null },
            onOpenPurchase = { supplierDetailId = null; purchaseDetailId = it },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.profile_photo),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                        )
                        Spacer(Modifier.width(10.dp))
                        androidx.compose.foundation.layout.Column {
                            Text("Anchor", fontFamily = GordenFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(tab.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                actions = {
                    androidx.compose.material3.IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                // Six items in a tracked-out caps display font is tight on phone width —
                // force a single line so labels truncate instead of wrapping and colliding.
                val navLabelStyle = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp, fontSize = 8.sp)
                @Composable
                fun navLabel(text: String) = Text(text, style = navLabelStyle, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, softWrap = false)
                NavigationBarItem(selected = tab == Tab.Dashboard, onClick = { tab = Tab.Dashboard }, icon = { Icon(Icons.Filled.Dashboard, null) }, label = { navLabel("Dashboard") })
                NavigationBarItem(selected = tab == Tab.Purchases, onClick = { tab = Tab.Purchases }, icon = { Icon(Icons.Filled.Inventory2, null) }, label = { navLabel("Purchases") })
                NavigationBarItem(selected = tab == Tab.Quotes, onClick = { tab = Tab.Quotes }, icon = { Icon(Icons.Filled.RequestQuote, null) }, label = { navLabel("Quotes") })
                NavigationBarItem(selected = tab == Tab.Suppliers, onClick = { tab = Tab.Suppliers }, icon = { Icon(Icons.Filled.People, null) }, label = { navLabel("Suppliers") })
                NavigationBarItem(selected = tab == Tab.Budgets, onClick = { tab = Tab.Budgets }, icon = { Icon(Icons.Filled.PieChart, null) }, label = { navLabel("Budgets") })
                NavigationBarItem(selected = tab == Tab.Reminders, onClick = { tab = Tab.Reminders }, icon = { Icon(Icons.Filled.Notifications, null) }, label = { navLabel("Reminders") })
            }
        },
        floatingActionButton = {
            if (!fabOpen) {
                FloatingActionButton(onClick = { fabOpen = true }) { Icon(Icons.Filled.Add, contentDescription = "Add") }
            } else {
                androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    androidx.compose.material3.ExtendedFloatingActionButton(onClick = { fabOpen = false; showScan = true }, icon = { Icon(Icons.Filled.CameraAlt, null) }, text = { Text("Scan document") }, modifier = Modifier.padding(bottom = 8.dp))
                    androidx.compose.material3.ExtendedFloatingActionButton(onClick = { fabOpen = false; showAddQuote = true }, icon = { Icon(Icons.Filled.RequestQuote, null) }, text = { Text("Add quote") }, modifier = Modifier.padding(bottom = 8.dp))
                    androidx.compose.material3.ExtendedFloatingActionButton(onClick = { fabOpen = false; showAddPurchase = true }, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Add purchase") }, modifier = Modifier.padding(bottom = 8.dp))
                    FloatingActionButton(onClick = { fabOpen = false }) { Icon(Icons.Filled.Add, contentDescription = "Close") }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.Dashboard -> DashboardScreen(viewModel) { purchaseDetailId = it }
                Tab.Purchases -> PurchasesScreen(viewModel) { purchaseDetailId = it }
                Tab.Quotes -> QuotesScreen(viewModel)
                Tab.Suppliers -> SuppliersScreen(viewModel) { supplierDetailId = it }
                Tab.Budgets -> BudgetsScreen(viewModel)
                Tab.Reminders -> RemindersScreen(viewModel)
            }
        }
    }

    if (showAddPurchase) AddPurchaseSheet(viewModel, data.categories) { showAddPurchase = false }
    if (showAddQuote) AddQuoteSheet(viewModel, data) { showAddQuote = false }
    if (showScan) ScanSheet(viewModel, data.categories) { showScan = false }
    if (showSettings) SettingsSheet(viewModel) { showSettings = false }
}
