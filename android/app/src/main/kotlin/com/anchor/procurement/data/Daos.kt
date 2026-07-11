package com.anchor.procurement.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name")
    fun observeAll(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: String): SupplierEntity?

    @Query("SELECT * FROM suppliers WHERE lower(name) = lower(:name) LIMIT 1")
    suspend fun findByName(name: String): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(supplier: SupplierEntity)

    @Update
    suspend fun update(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM suppliers")
    suspend fun clear()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clear()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets")
    suspend fun clear()
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases")
    fun observeAll(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchase_docs")
    fun observeAllDocs(): Flow<List<PurchaseDocEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(purchase: PurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocs(docs: List<PurchaseDocEntity>)

    @Query("DELETE FROM purchase_docs WHERE purchaseId = :purchaseId")
    suspend fun clearDocsFor(purchaseId: String)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM purchase_docs WHERE purchaseId = :id")
    suspend fun deleteDocsForPurchase(id: String)

    @Query("DELETE FROM purchases")
    suspend fun clear()

    @Query("DELETE FROM purchase_docs")
    suspend fun clearAllDocs()
}

@Dao
interface QuoteGroupDao {
    @Query("SELECT * FROM quote_groups")
    fun observeAll(): Flow<List<QuoteGroupEntity>>

    @Query("SELECT * FROM quotes")
    fun observeAllQuotes(): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroup(group: QuoteGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuote(quote: QuoteEntity)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Query("DELETE FROM quote_groups")
    suspend fun clear()

    @Query("DELETE FROM quotes")
    suspend fun clearQuotes()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE linkType = :linkType AND linkId = :linkId")
    suspend fun deleteForLink(linkType: String, linkId: String)

    @Query("DELETE FROM reminders")
    suspend fun clear()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 0")
    fun observe(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 0")
    suspend fun getOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)
}
