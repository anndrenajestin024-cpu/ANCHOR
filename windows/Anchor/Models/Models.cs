namespace Anchor.Models;

public class Doc
{
    public string Name { get; set; } = "";
    public string Type { get; set; } = "";
    public string Date { get; set; } = "";
}

public class Purchase
{
    public string Id { get; set; } = "";
    public string Item { get; set; } = "";
    public string Category { get; set; } = "";
    public string SupplierId { get; set; } = "";
    public double Qty { get; set; }
    public string Unit { get; set; } = "pcs";
    public double Price { get; set; }
    public string Date { get; set; } = "";
    public string Status { get; set; } = "Requested";
    public string Notes { get; set; } = "";
    public double? Basis { get; set; }
    public string? GroupId { get; set; }
    public List<Doc> Docs { get; set; } = new();

    public double Total => Qty * Price;
    public bool IsSpend => Statuses.SpendStatuses.Contains(Status);
    public double Savings => (Basis.HasValue && Basis.Value > Total) ? Basis.Value - Total : 0.0;
}

public class Supplier
{
    public string Id { get; set; } = "";
    public string Name { get; set; } = "";
    public string Contact { get; set; } = "";
    public string Notes { get; set; } = "";
}

public class Budget
{
    public string Category { get; set; } = "";
    public double Amount { get; set; }
}

public class Quote
{
    public string Id { get; set; } = "";
    public string GroupId { get; set; } = "";
    public string SupplierId { get; set; } = "";
    public double Price { get; set; }
    public string ValidUntil { get; set; } = "";
    public string Status { get; set; } = "Received";
}

public class QuoteGroup
{
    public string Id { get; set; } = "";
    public string Title { get; set; } = "";
    public double Qty { get; set; }
    public string Unit { get; set; } = "pcs";
    public string Category { get; set; } = "";
    public string Status { get; set; } = "active";
    public string? SelectedQuoteId { get; set; }
    public string? PurchaseId { get; set; }
    public List<Quote> Quotes { get; set; } = new();
}

public class Reminder
{
    public string Id { get; set; } = "";
    public string Title { get; set; } = "";
    public string LinkType { get; set; } = "p";
    public string LinkId { get; set; } = "";
    public string Due { get; set; } = "";
    public string Status { get; set; } = "Upcoming";
}

public class Settings
{
    public string? Pin { get; set; }
    public int AutoLockMinutes { get; set; } = 5;
    public bool AppLockEnabled { get; set; } = true;
    public string Currency { get; set; } = "AED";
    public int BudgetAlertPct { get; set; } = 90;
    public string OwnerName { get; set; } = "";
}

public class AppData
{
    public List<Supplier> Suppliers { get; set; } = new();
    public List<string> Categories { get; set; } = new();
    public List<Budget> Budgets { get; set; } = new();
    public List<Purchase> Purchases { get; set; } = new();
    public List<QuoteGroup> Groups { get; set; } = new();
    public List<Reminder> Reminders { get; set; } = new();
    public Settings Settings { get; set; } = new();
}

public static class Statuses
{
    public static readonly List<string> Purchase = new() { "Requested", "Quoted", "Ordered", "Delivered", "Paid", "Completed" };
    public static readonly HashSet<string> SpendStatuses = new() { "Ordered", "Delivered", "Paid", "Completed" };
    public static readonly List<string> Units = new() { "pcs", "box", "kg", "litre", "roll", "set", "pack" };
    public static readonly List<string> AttachTypes = new() { "None", "Invoice", "PO", "Receipt" };
    public static readonly List<string> Currencies = new() { "AED", "USD", "EUR", "GBP", "INR" };
}
