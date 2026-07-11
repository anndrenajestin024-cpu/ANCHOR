package com.anchor.procurement.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anchor.procurement.data.AnchorData
import com.anchor.procurement.data.Backup
import com.anchor.procurement.data.BudgetEntity
import com.anchor.procurement.data.Format
import com.anchor.procurement.data.Logic
import com.anchor.procurement.data.Purchase
import com.anchor.procurement.data.PurchaseDocEntity
import com.anchor.procurement.data.PurchaseEntity
import com.anchor.procurement.data.QuoteEntity
import com.anchor.procurement.data.QuoteGroupEntity
import com.anchor.procurement.data.ReminderEntity
import com.anchor.procurement.data.Repository
import com.anchor.procurement.data.SettingsEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PurchaseFilters(
    val search: String = "",
    val category: String? = null,
    val supplierId: String? = null,
    val status: String? = null,
)

class AnchorViewModel(private val repository: Repository) : ViewModel() {

    val data: StateFlow<AnchorData> = repository.data.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AnchorData(),
    )

    private val _locked = MutableStateFlow(true)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private val _pinEntry = MutableStateFlow("")
    val pinEntry: StateFlow<String> = _pinEntry.asStateFlow()

    private val _pinFirstEntry = MutableStateFlow<String?>(null)
    private val _pinError = MutableStateFlow("")
    val pinError: StateFlow<String> = _pinError.asStateFlow()

    private val _range = MutableStateFlow("all")
    val range: StateFlow<String> = _range.asStateFlow()

    private val _filters = MutableStateFlow(PurchaseFilters())
    val filters: StateFlow<PurchaseFilters> = _filters.asStateFlow()

    private val _toast = MutableStateFlow("")
    val toast: StateFlow<String> = _toast.asStateFlow()

    private val _showGreet = MutableStateFlow(false)
    val showGreet: StateFlow<Boolean> = _showGreet.asStateFlow()
    private var greetJob: Job? = null

    val greetText: String
        get() = "Welcome back, ${data.value.settings.ownerName} — Your wife loves you ♥"

    private var lastActivityAt = System.currentTimeMillis()

    private fun triggerGreetIfNamed() {
        if (data.value.settings.ownerName.isBlank()) return
        _showGreet.value = true
        greetJob?.cancel()
        greetJob = viewModelScope.launch {
            delay(8000)
            _showGreet.value = false
        }
    }

    fun supplierName(id: String): String = data.value.suppliers.find { it.id == id }?.name ?: id

    fun money(n: Double?, decimals: Int? = null) = Format.money(n, data.value.settings.currency, decimals)

    fun showToast(msg: String) {
        _toast.value = msg
    }

    fun clearToast() {
        _toast.value = ""
    }

    fun recordActivity() {
        lastActivityAt = System.currentTimeMillis()
    }

    fun checkAutoLock() {
        val minutes = data.value.settings.autoLockMinutes
        if (minutes > 0 && !_locked.value && System.currentTimeMillis() - lastActivityAt > minutes * 60_000L) {
            _locked.value = true
            _pinEntry.value = ""
        }
    }

    fun lockNow() {
        _locked.value = true
        _pinEntry.value = ""
    }

    /** Clears the saved PIN (data stays intact) so the lock screen re-enters "create" mode. */
    fun forgotPin() {
        viewModelScope.launch {
            repository.saveSettings(currentSettingsEntity().copy(pin = null))
        }
        _pinFirstEntry.value = null
        _pinEntry.value = ""
        _pinError.value = ""
    }

    /** Mirrors the prototype's pinPress: builds up to 4 digits, then creates/confirms/verifies. */
    fun pressPinKey(key: String) {
        val pin = data.value.settings.pin
        val mode = if (pin == null) (if (_pinFirstEntry.value == null) "create" else "confirm") else "enter"
        var entry = _pinEntry.value
        entry = if (key == "⌫") entry.dropLast(1) else (entry + key).take(4)

        if (entry.length == 4) {
            when (mode) {
                "create" -> {
                    _pinFirstEntry.value = entry
                    _pinEntry.value = ""
                    _pinError.value = ""
                }
                "confirm" -> {
                    if (entry == _pinFirstEntry.value) {
                        viewModelScope.launch {
                            repository.saveSettings(currentSettingsEntity().copy(pin = entry))
                        }
                        _locked.value = false
                        _pinEntry.value = ""
                        _pinFirstEntry.value = null
                        _pinError.value = ""
                        triggerGreetIfNamed()
                    } else {
                        _pinEntry.value = ""
                        _pinFirstEntry.value = null
                        _pinError.value = "PINs did not match — start over"
                    }
                }
                else -> {
                    if (entry == pin) {
                        _locked.value = false
                        _pinEntry.value = ""
                        _pinError.value = ""
                        recordActivity()
                        triggerGreetIfNamed()
                    } else {
                        _pinEntry.value = ""
                        _pinError.value = "Wrong PIN — try again"
                    }
                }
            }
        } else {
            _pinEntry.value = entry
            _pinError.value = ""
        }
    }

    fun pinMode(): String {
        val pin = data.value.settings.pin
        return if (pin == null) (if (_pinFirstEntry.value == null) "create" else "confirm") else "enter"
    }

    private fun currentSettingsEntity(): SettingsEntity {
        val s = data.value.settings
        return SettingsEntity(0, s.pin, s.autoLockMinutes, s.appLockEnabled, s.currency, s.budgetAlertPct, s.ownerName)
    }

    // ----- filters / range -----
    fun setRange(r: String) { _range.value = r }
    fun setSearch(q: String) { _filters.value = _filters.value.copy(search = q) }
    fun setStatusFilter(status: String?) { _filters.value = _filters.value.copy(status = status, category = null, supplierId = null) }
    fun setCategoryFilter(category: String?) { _filters.value = _filters.value.copy(category = category) }
    fun setSupplierFilter(supplierId: String?) { _filters.value = _filters.value.copy(supplierId = supplierId) }
    fun clearFilters() { _filters.value = PurchaseFilters() }

    fun rangedPurchases(): List<Purchase> = Logic.rangedPurchases(data.value.purchases, _range.value)
    fun filteredPurchases(): List<Purchase> {
        val f = _filters.value
        return Logic.filteredPurchases(data.value.purchases, ::supplierName, f.search, f.category, f.supplierId, f.status)
    }

    // ----- purchases -----
    fun addPurchase(
        item: String, category: String, supplierName: String, qty: Double, unit: String,
        price: Double, date: String, status: String, notes: String, attach: String,
    ) {
        if (item.isBlank()) { showToast("Item name is required."); return }
        if (qty <= 0) { showToast("Quantity must be greater than zero."); return }
        if (price <= 0) { showToast("Unit price must be greater than zero."); return }
        if (supplierName.isBlank()) { showToast("Supplier name is required."); return }
        viewModelScope.launch {
            val supplier = repository.findOrCreateSupplier(supplierName.trim())
            val id = "p${System.currentTimeMillis()}"
            val docs = if (attach == "None") emptyList() else listOf(
                PurchaseDocEntity(purchaseId = id, name = "$attach-${item.split(" ").first()}.pdf", type = attach, date = date),
            )
            repository.addPurchase(
                PurchaseEntity(id, item.trim(), category, supplier.id, qty, unit, price, date, status, notes.trim(), null, null),
                docs,
            )
            showToast("Purchase logged — ${money(qty * price, 0)}")
        }
    }

    fun deletePurchase(id: String) {
        viewModelScope.launch {
            repository.deletePurchase(id)
            showToast("Purchase deleted")
        }
    }

    // ----- quotes -----
    fun addQuoteToGroup(groupTitle: String?, newTitle: String, category: String, supplierName: String, qty: Double, price: Double, validUntil: String) {
        if (groupTitle == null && newTitle.isBlank()) { showToast("Name the new comparison."); return }
        if (qty <= 0 || price <= 0) { showToast("Quantity and unit price must be greater than zero."); return }
        if (supplierName.isBlank()) { showToast("Supplier name is required."); return }
        viewModelScope.launch {
            val supplier = repository.findOrCreateSupplier(supplierName.trim())
            val group = if (groupTitle == null) {
                val g = QuoteGroupEntity("g${System.currentTimeMillis()}", newTitle.trim(), qty, "pcs", category, "active", null, null)
                repository.addQuoteGroup(g)
                g
            } else {
                data.value.groups.first { it.title == groupTitle }.let {
                    QuoteGroupEntity(it.id, it.title, it.qty, it.unit, it.category, it.status, it.selectedQuoteId, it.purchaseId)
                }
            }
            repository.addQuote(QuoteEntity("q${System.currentTimeMillis()}", group.id, supplier.id, price, validUntil.ifBlank { "2026-07-31" }, "Received"))
            showToast("Quote added")
        }
    }

    fun selectQuote(groupId: String, quoteId: String) {
        viewModelScope.launch {
            val group = data.value.groups.first { it.id == groupId }
            repository.updateQuoteGroup(
                QuoteGroupEntity(group.id, group.title, group.qty, group.unit, group.category, group.status, quoteId, group.purchaseId),
            )
            group.quotes.forEach { q ->
                repository.updateQuote(QuoteEntity(q.id, q.groupId, q.supplierId, q.price, q.validUntil, if (q.id == quoteId) "Selected" else "Rejected"))
            }
            showToast("Quote selected — savings locked in")
        }
    }

    fun convertGroupToPurchase(groupId: String) {
        viewModelScope.launch {
            val group = data.value.groups.first { it.id == groupId }
            val quote = group.quotes.first { it.id == group.selectedQuoteId }
            val highest = (group.quotes.maxOfOrNull { it.price } ?: 0.0) * group.qty
            val id = "p${System.currentTimeMillis()}"
            repository.addPurchase(
                PurchaseEntity(
                    id, group.title, group.category, quote.supplierId, group.qty, group.unit,
                    quote.price, Format.today(), "Ordered", "Created from quote comparison.", highest, groupId,
                ),
                listOf(PurchaseDocEntity(purchaseId = id, name = "Quote-${supplierName(quote.supplierId).split(" ").first()}.pdf", type = "Quote", date = quote.validUntil)),
            )
            repository.updateQuoteGroup(QuoteGroupEntity(group.id, group.title, group.qty, group.unit, group.category, "done", group.selectedQuoteId, id))
            showToast("Converted to purchase")
        }
    }

    // ----- budgets -----
    fun setBudget(category: String, amount: Double) {
        viewModelScope.launch { repository.setBudgetAmount(category, amount) }
    }

    // ----- reminders -----
    fun toggleReminderDone(reminderId: String) {
        viewModelScope.launch {
            val r = data.value.reminders.first { it.id == reminderId }
            val newStatus = if (r.status == "Completed") "Upcoming" else "Completed"
            repository.toggleReminder(ReminderEntity(r.id, r.title, r.linkType, r.linkId, r.due, newStatus))
        }
    }

    // ----- scan (demo) -----
    fun saveScannedPurchase(item: String, category: String, supplierName: String, qty: Double, price: Double, date: String, invNo: String, tax: String) {
        if (item.isBlank()) { showToast("Line item is required."); return }
        if (qty <= 0 || price <= 0) { showToast("Quantity and unit price must be greater than zero."); return }
        if (supplierName.isBlank()) { showToast("Supplier name is required."); return }
        viewModelScope.launch {
            val supplier = repository.findOrCreateSupplier(supplierName.trim())
            val id = "p${System.currentTimeMillis()}"
            val notes = if (invNo.isNotBlank()) "Scanned from $invNo" + (if (tax.isNotBlank()) " · tax ${money(tax.toDoubleOrNull())}" else "") else "Scanned document"
            repository.addPurchase(
                PurchaseEntity(id, item.trim(), category.ifBlank { "Office Supplies" }, supplier.id, qty, "pcs", price, date, "Paid", notes, null, null),
                listOf(PurchaseDocEntity(purchaseId = id, name = "${invNo.ifBlank { "scan" }}.pdf", type = "Invoice", date = date)),
            )
            showToast("Scanned purchase saved")
        }
    }

    fun dupCheckForScan(scannedSupplierName: String, qty: Double, price: Double, date: String): String? =
        Logic.dupCheck(data.value.purchases, ::supplierName, scannedSupplierName, qty, price, date, data.value.settings.currency)

    // ----- settings -----
    fun saveSettings(currency: String, appLockEnabled: Boolean, budgetAlertPct: Int, autoLockMinutes: Int, ownerName: String) {
        viewModelScope.launch {
            repository.saveSettings(currentSettingsEntity().copy(currency = currency, appLockEnabled = appLockEnabled, budgetAlertPct = budgetAlertPct, autoLockMinutes = autoLockMinutes, ownerName = ownerName))
        }
    }

    fun setAutoLockMinutes(minutes: Int) {
        viewModelScope.launch { repository.saveSettings(currentSettingsEntity().copy(autoLockMinutes = minutes)) }
    }

    fun exportBackupJson(): String = Backup.export(data.value)

    fun restoreBackup(json: String, onDone: (String) -> Unit) {
        viewModelScope.launch {
            repository.restoreFromBackup(json).fold(
                onSuccess = { count -> onDone("Backup restored — $count purchases") },
                onFailure = { onDone("Restore failed — file could not be read as a backup. Your current data is untouched.") },
            )
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.loadSampleData()
            showToast("Sample data loaded")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            showToast("All data cleared — fresh start")
        }
    }

    class Factory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AnchorViewModel(repository) as T
        }
    }
}
