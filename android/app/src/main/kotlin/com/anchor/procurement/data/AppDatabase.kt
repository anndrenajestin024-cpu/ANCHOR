package com.anchor.procurement.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SupplierEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        PurchaseEntity::class,
        PurchaseDocEntity::class,
        QuoteGroupEntity::class,
        QuoteEntity::class,
        ReminderEntity::class,
        SettingsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun supplierDao(): SupplierDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun quoteGroupDao(): QuoteGroupDao
    abstract fun reminderDao(): ReminderDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anchor.db",
                ).build().also { instance = it }
            }
    }
}
