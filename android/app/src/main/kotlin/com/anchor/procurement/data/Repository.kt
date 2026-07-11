package com.anchor.procurement.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONObject

class Repository(private val db: AppDatabase) {

    val data: Flow<AnchorData> = combine(
        db.supplierDao().observeAll(),
        db.categoryDao().observeAll(),
        db.budgetDao().observeAll(),
        db.purchaseDao().observeAll(),
        db.purchaseDao().observeAllDocs(),
        db.quoteGroupDao().observeAll(),
        db.quoteGroupDao().observeAllQuotes(),
        db.reminderDao().observeAll(),
        db.settingsDao().observe(),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val suppliers = values[0] as List<SupplierEntity>
        @Suppress("UNCHECKED_CAST")
        val categories = values[1] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val budgets = values[2] as List<BudgetEntity>
        @Suppress("UNCHECKED_CAST")
        val purchaseEntities = values[3] as List<PurchaseEntity>
        @Suppress("UNCHECKED_CAST")
        val docs = values[4] as List<PurchaseDocEntity>
        @Suppress("UNCHECKED_CAST")
        val groupEntities = values[5] as List<QuoteGroupEntity>
        @Suppress("UNCHECKED_CAST")
        val quoteEntities = values[6] as List<QuoteEntity>
        @Suppress("UNCHECKED_CAST")
        val reminderEntities = values[7] as List<ReminderEntity>
        val settingsEntity = values[8] as SettingsEntity?

        val docsByPurchase = docs.groupBy { it.purchaseId }
        val purchases = purchaseEntities.map { pe ->
            Purchase(
                id = pe.id, item = pe.item, category = pe.category, supplierId = pe.supplierId,
                qty = pe.qty, unit = pe.unit, price = pe.price, date = pe.date, status = pe.status,
                notes = pe.notes, basis = pe.basis, groupId = pe.groupId,
                docs = (docsByPurchase[pe.id] ?: emptyList()).map { Doc(it.name, it.type, it.date) },
            )
        }
        val quotesByGroup = quoteEntities.groupBy { it.groupId }
        val groups = groupEntities.map { ge ->
            QuoteGroup(
                id = ge.id, title = ge.title, qty = ge.qty, unit = ge.unit, category = ge.category,
                status = ge.status, selectedQuoteId = ge.selectedQuoteId, purchaseId = ge.purchaseId,
                quotes = (quotesByGroup[ge.id] ?: emptyList()).map {
                    Quote(it.id, it.groupId, it.supplierId, it.price, it.validUntil, it.status)
                },
            )
        }
        val reminders = reminderEntities.map { Reminder(it.id, it.title, it.linkType, it.linkId, it.due, it.status) }
        val settings = settingsEntity?.let {
            Settings(it.pin, it.autoLockMinutes, it.appLockEnabled, it.currency, it.budgetAlertPct, it.ownerName)
        } ?: Settings()

        AnchorData(
            suppliers = suppliers.sortedBy { it.name },
            categories = categories.sortedBy { it.sortOrder }.map { it.name },
            budgets = budgets,
            purchases = purchases,
            groups = groups,
            reminders = reminders,
            settings = settings,
        )
    }

    /** Called once on app startup: creates default settings and loads sample data on a fresh install. */
    suspend fun ensureSeeded() {
        if (db.settingsDao().getOnce() == null) {
            db.settingsDao().upsert(SettingsEntity())
        }
        if (db.supplierDao().getById("s1") == null) {
            loadSampleData()
        }
    }

    suspend fun loadSampleData() {
        db.categoryDao().upsertAll(Seed.categories())
        db.supplierDao().clear()
        Seed.suppliers().forEach { db.supplierDao().upsert(it) }
        db.budgetDao().clear()
        Seed.budgets().forEach { db.budgetDao().upsert(it) }
        db.purchaseDao().clear()
        db.purchaseDao().clearAllDocs()
        Seed.purchases().forEach { sp ->
            db.purchaseDao().upsert(sp.p)
            if (sp.docs.isNotEmpty()) db.purchaseDao().upsertDocs(sp.docs)
        }
        db.quoteGroupDao().clear()
        db.quoteGroupDao().clearQuotes()
        Seed.groups().forEach { sg ->
            db.quoteGroupDao().upsertGroup(sg.group)
            sg.quotes.forEach { db.quoteGroupDao().upsertQuote(it) }
        }
        db.reminderDao().clear()
        Seed.reminders().forEach { db.reminderDao().upsert(it) }
    }

    suspend fun clearAllData() {
        db.categoryDao().upsertAll(Seed.categories())
        db.supplierDao().clear()
        db.budgetDao().clear()
        Seed.categories().forEach { db.budgetDao().upsert(BudgetEntity(it.name, 0.0)) }
        db.purchaseDao().clear()
        db.purchaseDao().clearAllDocs()
        db.quoteGroupDao().clear()
        db.quoteGroupDao().clearQuotes()
        db.reminderDao().clear()
    }

    // ----- mutations -----

    suspend fun upsertSupplier(supplier: SupplierEntity) = db.supplierDao().upsert(supplier)

    suspend fun findOrCreateSupplier(name: String): SupplierEntity {
        db.supplierDao().findByName(name)?.let { return it }
        val fresh = SupplierEntity(id = "s${System.currentTimeMillis()}", name = name, contact = "", notes = "")
        db.supplierDao().upsert(fresh)
        return fresh
    }

    suspend fun addPurchase(purchase: PurchaseEntity, docs: List<PurchaseDocEntity>) {
        db.purchaseDao().upsert(purchase)
        if (docs.isNotEmpty()) db.purchaseDao().upsertDocs(docs)
    }

    suspend fun deletePurchase(id: String) {
        db.purchaseDao().deleteDocsForPurchase(id)
        db.purchaseDao().delete(id)
        db.reminderDao().deleteForLink("p", id)
    }

    suspend fun setBudgetAmount(category: String, amount: Double) = db.budgetDao().upsert(BudgetEntity(category, amount))

    suspend fun addQuoteGroup(group: QuoteGroupEntity) = db.quoteGroupDao().upsertGroup(group)

    suspend fun addQuote(quote: QuoteEntity) = db.quoteGroupDao().upsertQuote(quote)

    suspend fun updateQuoteGroup(group: QuoteGroupEntity) = db.quoteGroupDao().upsertGroup(group)

    suspend fun updateQuote(quote: QuoteEntity) = db.quoteGroupDao().updateQuote(quote)

    suspend fun toggleReminder(reminder: ReminderEntity) = db.reminderDao().update(reminder)

    suspend fun addReminder(reminder: ReminderEntity) = db.reminderDao().upsert(reminder)

    suspend fun saveSettings(settings: SettingsEntity) = db.settingsDao().upsert(settings)

    // ----- restore (export lives in Backup.kt, operating on already-loaded AnchorData) -----

    suspend fun restoreFromBackup(json: String): Result<Int> = runCatching {
        val root = JSONObject(json)
        if (root.optString("app") != "anchor-procurement") error("not a valid Anchor backup")
        val data = root.getJSONObject("data")

        val suppliers = data.getJSONArray("suppliers")
        db.supplierDao().clear()
        for (i in 0 until suppliers.length()) {
            val s = suppliers.getJSONObject(i)
            db.supplierDao().upsert(SupplierEntity(s.getString("id"), s.getString("name"), s.optString("contact"), s.optString("notes")))
        }

        val categories = data.getJSONArray("categories")
        val catEntities = (0 until categories.length()).map { CategoryEntity(categories.getString(it), it) }
        db.categoryDao().clear()
        db.categoryDao().upsertAll(catEntities)

        val budgets = data.getJSONArray("budgets")
        db.budgetDao().clear()
        for (i in 0 until budgets.length()) {
            val b = budgets.getJSONObject(i)
            db.budgetDao().upsert(BudgetEntity(b.getString("category"), b.getDouble("amount")))
        }

        val purchases = data.getJSONArray("purchases")
        db.purchaseDao().clear()
        db.purchaseDao().clearAllDocs()
        for (i in 0 until purchases.length()) {
            val p = purchases.getJSONObject(i)
            val id = p.getString("id")
            db.purchaseDao().upsert(
                PurchaseEntity(
                    id = id, item = p.getString("item"), category = p.getString("category"),
                    supplierId = p.getString("sup"), qty = p.getDouble("qty"), unit = p.getString("unit"),
                    price = p.getDouble("price"), date = p.getString("date"), status = p.getString("status"),
                    notes = p.optString("notes"), basis = if (p.isNull("basis")) null else p.optDouble("basis"),
                    groupId = if (p.isNull("groupId")) null else p.optString("groupId"),
                ),
            )
            val docs = p.optJSONArray("docs")
            if (docs != null) {
                val docEntities = (0 until docs.length()).map { j ->
                    val d = docs.getJSONObject(j)
                    PurchaseDocEntity(purchaseId = id, name = d.getString("name"), type = d.getString("type"), date = d.getString("date"))
                }
                if (docEntities.isNotEmpty()) db.purchaseDao().upsertDocs(docEntities)
            }
        }

        val groups = data.getJSONArray("groups")
        db.quoteGroupDao().clear()
        db.quoteGroupDao().clearQuotes()
        for (i in 0 until groups.length()) {
            val g = groups.getJSONObject(i)
            db.quoteGroupDao().upsertGroup(
                QuoteGroupEntity(
                    id = g.getString("id"), title = g.getString("title"), qty = g.getDouble("qty"),
                    unit = g.getString("unit"), category = g.getString("category"), status = g.getString("status"),
                    selectedQuoteId = if (g.isNull("selected")) null else g.optString("selected"),
                    purchaseId = if (g.isNull("purchaseId")) null else g.optString("purchaseId"),
                ),
            )
            val quotes = g.getJSONArray("quotes")
            for (j in 0 until quotes.length()) {
                val q = quotes.getJSONObject(j)
                db.quoteGroupDao().upsertQuote(
                    QuoteEntity(q.getString("id"), g.getString("id"), q.getString("sup"), q.getDouble("price"), q.getString("valid"), q.getString("status")),
                )
            }
        }

        val reminders = data.getJSONArray("reminders")
        db.reminderDao().clear()
        for (i in 0 until reminders.length()) {
            val r = reminders.getJSONObject(i)
            val link = r.getJSONObject("link")
            db.reminderDao().upsert(ReminderEntity(r.getString("id"), r.getString("title"), link.getString("t"), link.getString("id"), r.getString("due"), r.getString("status")))
        }

        purchases.length()
    }
}
