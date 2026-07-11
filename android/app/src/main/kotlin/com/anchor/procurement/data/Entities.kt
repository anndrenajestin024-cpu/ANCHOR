package com.anchor.procurement.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    val contact: String,
    val notes: String,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String,
    val sortOrder: Int,
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val amount: Double,
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val item: String,
    val category: String,
    val supplierId: String,
    val qty: Double,
    val unit: String,
    val price: Double,
    val date: String, // ISO yyyy-MM-dd
    val status: String,
    val notes: String,
    val basis: Double?,
    val groupId: String?,
)

@Entity(tableName = "purchase_docs")
data class PurchaseDocEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: String,
    val name: String,
    val type: String,
    val date: String,
)

@Entity(tableName = "quote_groups")
data class QuoteGroupEntity(
    @PrimaryKey val id: String,
    val title: String,
    val qty: Double,
    val unit: String,
    val category: String,
    val status: String, // active | done
    val selectedQuoteId: String?,
    val purchaseId: String?,
)

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val supplierId: String,
    val price: Double,
    val validUntil: String,
    val status: String, // Received | Selected | Rejected
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val linkType: String, // p | g
    val linkId: String,
    val due: String,
    val status: String, // Upcoming | Overdue | Completed
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val pin: String? = null,
    val autoLockMinutes: Int = 5,
    val appLockEnabled: Boolean = true,
    val currency: String = "AED",
    val budgetAlertPct: Int = 90,
    val ownerName: String = "",
)

object Statuses {
    val purchase = listOf("Requested", "Quoted", "Ordered", "Delivered", "Paid", "Completed")
    val spendStatuses = setOf("Ordered", "Delivered", "Paid", "Completed")
    val units = listOf("pcs", "box", "kg", "litre", "roll", "set", "pack")
    val attachTypes = listOf("None", "Invoice", "PO", "Receipt")
    val quote = listOf("Received", "Selected", "Rejected")
    val reminder = listOf("Upcoming", "Overdue", "Completed")
    val currencies = listOf("AED", "USD", "EUR", "GBP", "INR")
}
