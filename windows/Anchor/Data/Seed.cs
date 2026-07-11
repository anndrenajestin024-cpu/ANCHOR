using Anchor.Models;

namespace Anchor.Data;

public static class Seed
{
    public static List<string> Categories() => new() { "IT Equipment", "Office Supplies", "Facilities", "Packaging" };

    public static List<Supplier> Suppliers() => new()
    {
        new Supplier { Id = "s1", Name = "Meridian Office Supply", Contact = "Dana Ruiz · dana@meridianos.com · +1 415 220 8841", Notes = "Net-30 terms. Reliable on paper goods; slower on furniture." },
        new Supplier { Id = "s2", Name = "Corelink IT Distribution", Contact = "Priya Nair · priya@corelink.io · +1 628 314 0952", Notes = "Best laptop pricing when bundled. Ask for Priya directly." },
        new Supplier { Id = "s3", Name = "Atlas Facilities Group", Contact = "Tom Becker · tbecker@atlasfg.com · +1 510 883 2210", Notes = "Strong install crew. Quotes expire fast — act within 2 weeks." },
        new Supplier { Id = "s4", Name = "Northpine Packaging", Contact = "Elsa Marin · elsa@northpine.co · +1 415 992 7743", Notes = "Volume price breaks at 500+ units." },
        new Supplier { Id = "s5", Name = "Brightway Industrial", Contact = "Sam Okafor · sam@brightway.com · +1 408 771 5526", Notes = "Competitive on facilities & MRO categories." },
    };

    public static List<Budget> Budgets() => new()
    {
        new Budget { Category = "IT Equipment", Amount = 15000 },
        new Budget { Category = "Office Supplies", Amount = 6000 },
        new Budget { Category = "Facilities", Amount = 8000 },
        new Budget { Category = "Packaging", Amount = 2500 },
    };

    private static Purchase P(string id, string item, string category, string sup, double qty, string unit, double price, string date, string status,
        string notes = "", double? basis = null, string? groupId = null, List<Doc>? docs = null) => new()
    {
        Id = id, Item = item, Category = category, SupplierId = sup, Qty = qty, Unit = unit, Price = price, Date = date, Status = status,
        Notes = notes, Basis = basis, GroupId = groupId, Docs = docs ?? new List<Doc>(),
    };

    public static List<Purchase> Purchases() => new()
    {
        P("p1", "A4 Copy Paper 80gsm", "Office Supplies", "s1", 50, "box", 22.90, "2026-01-15", "Completed",
            docs: new() { new Doc { Name = "INV-1088.pdf", Type = "Invoice", Date = "2026-01-15" } }),
        P("p2", "14\" Business Laptops", "IT Equipment", "s2", 8, "pcs", 1120, "2026-02-12", "Completed",
            notes: "Negotiated as a bundle with the dock station pipeline.", basis: 9920, groupId: "g4",
            docs: new() { new Doc { Name = "PO-2026-004.pdf", Type = "PO", Date = "2026-02-10" }, new Doc { Name = "INV-2044.pdf", Type = "Invoice", Date = "2026-02-12" } }),
        P("p3", "Toner Cartridges 26X", "Office Supplies", "s1", 20, "pcs", 58, "2026-02-25", "Completed",
            docs: new() { new Doc { Name = "INV-1104.pdf", Type = "Invoice", Date = "2026-02-25" } }),
        P("p4", "Ergonomic Task Chairs", "Facilities", "s3", 12, "pcs", 289, "2026-03-03", "Completed", basis: 3840,
            docs: new() { new Doc { Name = "INV-3310.pdf", Type = "Invoice", Date = "2026-03-03" } }),
        P("p5", "A4 Copy Paper 80gsm", "Office Supplies", "s1", 60, "box", 24.50, "2026-03-18", "Paid",
            docs: new() { new Doc { Name = "INV-1121.pdf", Type = "Invoice", Date = "2026-03-18" } }),
        P("p6", "LED Panel Retrofit", "Facilities", "s3", 40, "pcs", 68, "2026-04-07", "Completed", basis: 3200,
            docs: new() { new Doc { Name = "PO-2026-008.pdf", Type = "PO", Date = "2026-04-01" }, new Doc { Name = "INV-3388.pdf", Type = "Invoice", Date = "2026-04-07" } }),
        P("p7", "Corrugated Shipping Boxes", "Packaging", "s4", 500, "pcs", 1.82, "2026-04-22", "Paid", basis: 1100,
            docs: new() { new Doc { Name = "INV-5507.pdf", Type = "Invoice", Date = "2026-04-22" } }),
        P("p8", "27\" QHD Monitors", "IT Equipment", "s2", 10, "pcs", 235, "2026-05-09", "Delivered",
            notes: "From comparison — Corelink beat Brightway by $35/unit.", basis: 2700, groupId: "g1",
            docs: new() { new Doc { Name = "PO-2026-011.pdf", Type = "PO", Date = "2026-05-05" }, new Doc { Name = "INV-2231.pdf", Type = "Invoice", Date = "2026-05-09" } }),
        P("p9", "A4 Copy Paper 80gsm", "Office Supplies", "s1", 40, "box", 25.40, "2026-05-21", "Paid",
            docs: new() { new Doc { Name = "INV-1187.pdf", Type = "Invoice", Date = "2026-05-21" } }),
        P("p10", "Toner Cartridges 26X", "Office Supplies", "s1", 24, "pcs", 61, "2026-05-27", "Paid",
            docs: new() { new Doc { Name = "INV-1201.pdf", Type = "Invoice", Date = "2026-05-27" } }),
        P("p11", "HVAC Filter Sets MERV-13", "Facilities", "s5", 30, "pcs", 19.50, "2026-06-04", "Delivered",
            docs: new() { new Doc { Name = "PO-2026-014.pdf", Type = "PO", Date = "2026-06-01" } }),
        P("p12", "Stretch Wrap Film 20µm", "Packaging", "s4", 80, "roll", 7.40, "2026-06-19", "Ordered",
            docs: new() { new Doc { Name = "PO-2026-017.pdf", Type = "PO", Date = "2026-06-17" } }),
        P("p13", "USB-C Dock Stations", "IT Equipment", "s2", 15, "pcs", 148, "2026-06-30", "Ordered", basis: 2550, groupId: "g3",
            docs: new() { new Doc { Name = "PO-2026-019.pdf", Type = "PO", Date = "2026-06-28" } }),
        P("p14", "Breakroom Coffee Supplies", "Office Supplies", "s1", 10, "box", 42, "2026-07-06", "Quoted"),
        P("p15", "Warehouse Pallet Jacks", "Facilities", "s5", 2, "pcs", 1240, "2026-07-09", "Requested",
            notes: "Awaiting quotes from Atlas & Brightway before ordering."),
    };

    public static List<QuoteGroup> Groups() => new()
    {
        new QuoteGroup
        {
            Id = "g1", Title = "27\" QHD Monitors", Qty = 10, Unit = "pcs", Category = "IT Equipment", Status = "done", SelectedQuoteId = "q1", PurchaseId = "p8",
            Quotes = new()
            {
                new Quote { Id = "q1", GroupId = "g1", SupplierId = "s2", Price = 235, ValidUntil = "2026-05-15", Status = "Selected" },
                new Quote { Id = "q2", GroupId = "g1", SupplierId = "s5", Price = 270, ValidUntil = "2026-05-20", Status = "Rejected" },
                new Quote { Id = "q3", GroupId = "g1", SupplierId = "s1", Price = 262, ValidUntil = "2026-05-12", Status = "Rejected" },
            },
        },
        new QuoteGroup
        {
            Id = "g2", Title = "Standing Desks", Qty = 6, Unit = "pcs", Category = "Facilities", Status = "active", SelectedQuoteId = null, PurchaseId = null,
            Quotes = new()
            {
                new Quote { Id = "q4", GroupId = "g2", SupplierId = "s3", Price = 545, ValidUntil = "2026-07-25", Status = "Received" },
                new Quote { Id = "q5", GroupId = "g2", SupplierId = "s1", Price = 585, ValidUntil = "2026-07-31", Status = "Received" },
                new Quote { Id = "q6", GroupId = "g2", SupplierId = "s5", Price = 529, ValidUntil = "2026-07-18", Status = "Received" },
            },
        },
        new QuoteGroup
        {
            Id = "g3", Title = "USB-C Dock Stations", Qty = 15, Unit = "pcs", Category = "IT Equipment", Status = "done", SelectedQuoteId = "q7", PurchaseId = "p13",
            Quotes = new()
            {
                new Quote { Id = "q7", GroupId = "g3", SupplierId = "s2", Price = 148, ValidUntil = "2026-07-05", Status = "Selected" },
                new Quote { Id = "q8", GroupId = "g3", SupplierId = "s1", Price = 170, ValidUntil = "2026-07-02", Status = "Rejected" },
                new Quote { Id = "q9", GroupId = "g3", SupplierId = "s5", Price = 156, ValidUntil = "2026-07-08", Status = "Rejected" },
            },
        },
        new QuoteGroup
        {
            Id = "g4", Title = "14\" Business Laptops", Qty = 8, Unit = "pcs", Category = "IT Equipment", Status = "done", SelectedQuoteId = "q10", PurchaseId = "p2",
            Quotes = new()
            {
                new Quote { Id = "q10", GroupId = "g4", SupplierId = "s2", Price = 1120, ValidUntil = "2026-02-01", Status = "Selected" },
                new Quote { Id = "q11", GroupId = "g4", SupplierId = "s1", Price = 1240, ValidUntil = "2026-02-05", Status = "Rejected" },
                new Quote { Id = "q12", GroupId = "g4", SupplierId = "s5", Price = 1180, ValidUntil = "2026-01-30", Status = "Rejected" },
            },
        },
    };

    public static List<Reminder> Reminders() => new()
    {
        new Reminder { Id = "r1", Title = "Pay Corelink invoice INV-2231", LinkType = "p", LinkId = "p8", Due = "2026-07-15", Status = "Upcoming" },
        new Reminder { Id = "r2", Title = "Brightway desk quote expires", LinkType = "g", LinkId = "g2", Due = "2026-07-18", Status = "Upcoming" },
        new Reminder { Id = "r3", Title = "Chase stretch-wrap delivery", LinkType = "p", LinkId = "p12", Due = "2026-07-08", Status = "Overdue" },
        new Reminder { Id = "r4", Title = "Confirm HVAC filter receipt", LinkType = "p", LinkId = "p11", Due = "2026-07-02", Status = "Completed" },
    };

    public static AppData Full() => new()
    {
        Suppliers = Suppliers(),
        Categories = Categories(),
        Budgets = Budgets(),
        Purchases = Purchases(),
        Groups = Groups(),
        Reminders = Reminders(),
        Settings = new Settings(),
    };

    public static AppData Blank() => new()
    {
        Suppliers = new(),
        Categories = Categories(),
        Budgets = Categories().Select(c => new Budget { Category = c, Amount = 0 }).ToList(),
        Purchases = new(),
        Groups = new(),
        Reminders = new(),
        Settings = new Settings(),
    };
}
