package com.anchor.procurement.data

object Seed {
    fun categories() = listOf("IT Equipment", "Office Supplies", "Facilities", "Packaging")
        .mapIndexed { i, name -> CategoryEntity(name, i) }

    fun blankBudgets() = categories().map { BudgetEntity(it.name, 0.0) }

    fun suppliers() = listOf(
        SupplierEntity("s1", "Meridian Office Supply", "Dana Ruiz · dana@meridianos.com · +1 415 220 8841", "Net-30 terms. Reliable on paper goods; slower on furniture."),
        SupplierEntity("s2", "Corelink IT Distribution", "Priya Nair · priya@corelink.io · +1 628 314 0952", "Best laptop pricing when bundled. Ask for Priya directly."),
        SupplierEntity("s3", "Atlas Facilities Group", "Tom Becker · tbecker@atlasfg.com · +1 510 883 2210", "Strong install crew. Quotes expire fast — act within 2 weeks."),
        SupplierEntity("s4", "Northpine Packaging", "Elsa Marin · elsa@northpine.co · +1 415 992 7743", "Volume price breaks at 500+ units."),
        SupplierEntity("s5", "Brightway Industrial", "Sam Okafor · sam@brightway.com · +1 408 771 5526", "Competitive on facilities & MRO categories."),
    )

    fun budgets() = listOf(
        BudgetEntity("IT Equipment", 15000.0),
        BudgetEntity("Office Supplies", 6000.0),
        BudgetEntity("Facilities", 8000.0),
        BudgetEntity("Packaging", 2500.0),
    )

    data class SeedPurchase(val p: PurchaseEntity, val docs: List<PurchaseDocEntity>)

    fun purchases(): List<SeedPurchase> {
        fun p(
            id: String, item: String, category: String, sup: String, qty: Double, unit: String,
            price: Double, date: String, status: String, notes: String = "", basis: Double? = null,
            groupId: String? = null, docs: List<Triple<String, String, String>> = emptyList(),
        ) = SeedPurchase(
            PurchaseEntity(id, item, category, sup, qty, unit, price, date, status, notes, basis, groupId),
            docs.map { (name, type, d) -> PurchaseDocEntity(purchaseId = id, name = name, type = type, date = d) },
        )

        return listOf(
            p("p1", "A4 Copy Paper 80gsm", "Office Supplies", "s1", 50.0, "box", 22.90, "2026-01-15", "Completed",
                docs = listOf(Triple("INV-1088.pdf", "Invoice", "2026-01-15"))),
            p("p2", "14\" Business Laptops", "IT Equipment", "s2", 8.0, "pcs", 1120.0, "2026-02-12", "Completed",
                notes = "Negotiated as a bundle with the dock station pipeline.", basis = 9920.0, groupId = "g4",
                docs = listOf(Triple("PO-2026-004.pdf", "PO", "2026-02-10"), Triple("INV-2044.pdf", "Invoice", "2026-02-12"))),
            p("p3", "Toner Cartridges 26X", "Office Supplies", "s1", 20.0, "pcs", 58.0, "2026-02-25", "Completed",
                docs = listOf(Triple("INV-1104.pdf", "Invoice", "2026-02-25"))),
            p("p4", "Ergonomic Task Chairs", "Facilities", "s3", 12.0, "pcs", 289.0, "2026-03-03", "Completed", basis = 3840.0,
                docs = listOf(Triple("INV-3310.pdf", "Invoice", "2026-03-03"))),
            p("p5", "A4 Copy Paper 80gsm", "Office Supplies", "s1", 60.0, "box", 24.50, "2026-03-18", "Paid",
                docs = listOf(Triple("INV-1121.pdf", "Invoice", "2026-03-18"))),
            p("p6", "LED Panel Retrofit", "Facilities", "s3", 40.0, "pcs", 68.0, "2026-04-07", "Completed", basis = 3200.0,
                docs = listOf(Triple("PO-2026-008.pdf", "PO", "2026-04-01"), Triple("INV-3388.pdf", "Invoice", "2026-04-07"))),
            p("p7", "Corrugated Shipping Boxes", "Packaging", "s4", 500.0, "pcs", 1.82, "2026-04-22", "Paid", basis = 1100.0,
                docs = listOf(Triple("INV-5507.pdf", "Invoice", "2026-04-22"))),
            p("p8", "27\" QHD Monitors", "IT Equipment", "s2", 10.0, "pcs", 235.0, "2026-05-09", "Delivered",
                notes = "From comparison — Corelink beat Brightway by \$35/unit.", basis = 2700.0, groupId = "g1",
                docs = listOf(Triple("PO-2026-011.pdf", "PO", "2026-05-05"), Triple("INV-2231.pdf", "Invoice", "2026-05-09"))),
            p("p9", "A4 Copy Paper 80gsm", "Office Supplies", "s1", 40.0, "box", 25.40, "2026-05-21", "Paid",
                docs = listOf(Triple("INV-1187.pdf", "Invoice", "2026-05-21"))),
            p("p10", "Toner Cartridges 26X", "Office Supplies", "s1", 24.0, "pcs", 61.0, "2026-05-27", "Paid",
                docs = listOf(Triple("INV-1201.pdf", "Invoice", "2026-05-27"))),
            p("p11", "HVAC Filter Sets MERV-13", "Facilities", "s5", 30.0, "pcs", 19.50, "2026-06-04", "Delivered",
                docs = listOf(Triple("PO-2026-014.pdf", "PO", "2026-06-01"))),
            p("p12", "Stretch Wrap Film 20µm", "Packaging", "s4", 80.0, "roll", 7.40, "2026-06-19", "Ordered",
                docs = listOf(Triple("PO-2026-017.pdf", "PO", "2026-06-17"))),
            p("p13", "USB-C Dock Stations", "IT Equipment", "s2", 15.0, "pcs", 148.0, "2026-06-30", "Ordered", basis = 2550.0, groupId = "g3",
                docs = listOf(Triple("PO-2026-019.pdf", "PO", "2026-06-28"))),
            p("p14", "Breakroom Coffee Supplies", "Office Supplies", "s1", 10.0, "box", 42.0, "2026-07-06", "Quoted"),
            p("p15", "Warehouse Pallet Jacks", "Facilities", "s5", 2.0, "pcs", 1240.0, "2026-07-09", "Requested",
                notes = "Awaiting quotes from Atlas & Brightway before ordering."),
        )
    }

    data class SeedGroup(val group: QuoteGroupEntity, val quotes: List<QuoteEntity>)

    fun groups(): List<SeedGroup> = listOf(
        SeedGroup(
            QuoteGroupEntity("g1", "27\" QHD Monitors", 10.0, "pcs", "IT Equipment", "done", "q1", "p8"),
            listOf(
                QuoteEntity("q1", "g1", "s2", 235.0, "2026-05-15", "Selected"),
                QuoteEntity("q2", "g1", "s5", 270.0, "2026-05-20", "Rejected"),
                QuoteEntity("q3", "g1", "s1", 262.0, "2026-05-12", "Rejected"),
            ),
        ),
        SeedGroup(
            QuoteGroupEntity("g2", "Standing Desks", 6.0, "pcs", "Facilities", "active", null, null),
            listOf(
                QuoteEntity("q4", "g2", "s3", 545.0, "2026-07-25", "Received"),
                QuoteEntity("q5", "g2", "s1", 585.0, "2026-07-31", "Received"),
                QuoteEntity("q6", "g2", "s5", 529.0, "2026-07-18", "Received"),
            ),
        ),
        SeedGroup(
            QuoteGroupEntity("g3", "USB-C Dock Stations", 15.0, "pcs", "IT Equipment", "done", "q7", "p13"),
            listOf(
                QuoteEntity("q7", "g3", "s2", 148.0, "2026-07-05", "Selected"),
                QuoteEntity("q8", "g3", "s1", 170.0, "2026-07-02", "Rejected"),
                QuoteEntity("q9", "g3", "s5", 156.0, "2026-07-08", "Rejected"),
            ),
        ),
        SeedGroup(
            QuoteGroupEntity("g4", "14\" Business Laptops", 8.0, "pcs", "IT Equipment", "done", "q10", "p2"),
            listOf(
                QuoteEntity("q10", "g4", "s2", 1120.0, "2026-02-01", "Selected"),
                QuoteEntity("q11", "g4", "s1", 1240.0, "2026-02-05", "Rejected"),
                QuoteEntity("q12", "g4", "s5", 1180.0, "2026-01-30", "Rejected"),
            ),
        ),
    )

    fun reminders() = listOf(
        ReminderEntity("r1", "Pay Corelink invoice INV-2231", "p", "p8", "2026-07-15", "Upcoming"),
        ReminderEntity("r2", "Brightway desk quote expires", "g", "g2", "2026-07-18", "Upcoming"),
        ReminderEntity("r3", "Chase stretch-wrap delivery", "p", "p12", "2026-07-08", "Overdue"),
        ReminderEntity("r4", "Confirm HVAC filter receipt", "p", "p11", "2026-07-02", "Completed"),
    )
}
