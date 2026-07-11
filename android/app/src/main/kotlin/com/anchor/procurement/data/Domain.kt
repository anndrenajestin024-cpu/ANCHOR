package com.anchor.procurement.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.round

data class Doc(val name: String, val type: String, val date: String)

data class Purchase(
    val id: String,
    val item: String,
    val category: String,
    val supplierId: String,
    val qty: Double,
    val unit: String,
    val price: Double,
    val date: String,
    val status: String,
    val notes: String,
    val basis: Double?,
    val groupId: String?,
    val docs: List<Doc>,
) {
    val total: Double get() = qty * price
    val isSpend: Boolean get() = status in Statuses.spendStatuses
    val savings: Double get() = if (basis != null && basis > total) basis - total else 0.0
}

data class Quote(
    val id: String,
    val groupId: String,
    val supplierId: String,
    val price: Double,
    val validUntil: String,
    val status: String,
)

data class QuoteGroup(
    val id: String,
    val title: String,
    val qty: Double,
    val unit: String,
    val category: String,
    val status: String,
    val selectedQuoteId: String?,
    val purchaseId: String?,
    val quotes: List<Quote>,
)

data class Reminder(
    val id: String,
    val title: String,
    val linkType: String,
    val linkId: String,
    val due: String,
    val status: String,
)

data class Settings(
    val pin: String? = null,
    val autoLockMinutes: Int = 5,
    val appLockEnabled: Boolean = true,
    val currency: String = "AED",
    val budgetAlertPct: Int = 90,
    val ownerName: String = "",
)

data class AnchorData(
    val suppliers: List<SupplierEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val purchases: List<Purchase> = emptyList(),
    val groups: List<QuoteGroup> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val settings: Settings = Settings(),
)

object Format {
    private fun symbol(currency: String) = when (currency) {
        "AED" -> "AED "
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "INR" -> "₹"
        else -> "AED "
    }

    fun money(n: Double?, currency: String, decimals: Int? = null): String {
        if (n == null || n.isNaN()) return "—"
        val d = decimals ?: if (abs(n) < 100 && n % 1.0 != 0.0) 2 else 0
        val rounded = round(n * Math.pow(10.0, d.toDouble())) / Math.pow(10.0, d.toDouble())
        val absVal = abs(rounded)
        val formatted = if (d == 0) {
            "%,.0f".format(absVal)
        } else {
            "%,.${d}f".format(absVal)
        }
        return symbol(currency) + formatted
    }

    private val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    fun fdate(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            val parts = iso.split("-").map { it.toInt() }
            "${monthNames[parts[1] - 1]} ${parts[2]}"
        } catch (e: Exception) {
            "—"
        }
    }

    fun fdateY(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return "${fdate(iso)}, ${iso.take(4)}"
    }

    fun today(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
}

object Palette {
    val categoryColors = listOf(0xFF5B7B9A, 0xFF8FAC78, 0xFFC99B5D, 0xFFA56A5F, 0xFF77787D)
    val supplierColors = categoryColors

    fun categoryColor(category: String, categories: List<String>): Long {
        val idx = categories.indexOf(category)
        return if (idx >= 0) categoryColors[idx % categoryColors.size] else 0xFFB0B1B6
    }

    fun statusColor(status: String): Long = when (status) {
        "Requested" -> 0xFF77787D
        "Quoted" -> 0xFFD0A050
        "Ordered" -> 0xFF5B7B9A
        "Delivered" -> 0xFF7A9BB8
        "Paid" -> 0xFF8FAC78
        "Completed" -> 0xFF5E7A4C
        else -> 0xFF4A4B50
    }
}

/** Business logic mirrors the original Anchor prototype 1:1 (same field names, same formulas). */
object Logic {
    fun rangedPurchases(purchases: List<Purchase>, range: String, today: String = Format.today()): List<Purchase> {
        val cutoff = when (range) {
            "30d" -> LocalDate.parse(today).minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
            "3m" -> LocalDate.parse(today).minusMonths(3).format(DateTimeFormatter.ISO_LOCAL_DATE)
            "6m" -> LocalDate.parse(today).minusMonths(6).format(DateTimeFormatter.ISO_LOCAL_DATE)
            else -> "0000-00-00"
        }
        return purchases.filter { it.date >= cutoff }
    }

    fun filteredPurchases(
        purchases: List<Purchase>,
        supplierName: (String) -> String,
        search: String,
        category: String?,
        supplierId: String?,
        status: String?,
    ): List<Purchase> {
        val q = search.trim().lowercase()
        return purchases.filter { p ->
            if (category != null && p.category != category) return@filter false
            if (supplierId != null && p.supplierId != supplierId) return@filter false
            if (status != null && p.status != status) return@filter false
            if (q.isNotEmpty()) {
                val hay = (p.item + " " + supplierName(p.supplierId) + " " + p.category + " " + p.notes + " " +
                    p.docs.joinToString(" ") { it.name + " " + it.type }).lowercase()
                if (!hay.contains(q)) return@filter false
            }
            true
        }.sortedByDescending { it.date }
    }

    /** Mirrors the prototype's dupCheck: flags a likely-duplicate scan (same supplier, total, and date). */
    fun dupCheck(
        purchases: List<Purchase>,
        supplierNameOf: (String) -> String,
        scannedSupplierName: String,
        qty: Double,
        price: Double,
        date: String,
        currency: String,
    ): String? {
        val tot = qty * price
        val hit = purchases.find { p ->
            supplierNameOf(p.supplierId) == scannedSupplierName && abs(p.total - tot) < 0.01 && p.date == date
        } ?: return null
        return "A purchase from $scannedSupplierName for ${Format.money(tot, currency)} on ${Format.fdateY(date)} is already logged (${hit.item})."
    }
}
