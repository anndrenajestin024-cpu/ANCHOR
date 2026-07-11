using System.Collections.ObjectModel;
using System.Text.Json;
using Anchor.Data;
using Anchor.Models;
using CommunityToolkit.Mvvm.ComponentModel;

namespace Anchor.ViewModels;

public partial class PurchaseFilters : ObservableObject
{
    [ObservableProperty] private string search = "";
    [ObservableProperty] private string? category;
    [ObservableProperty] private string? supplierId;
    [ObservableProperty] private string? status;
}

public partial class AnchorViewModel : ObservableObject
{
    private readonly Store _store;

    [ObservableProperty] private AppData data;
    [ObservableProperty] private bool locked = true;
    [ObservableProperty] private string pinEntry = "";
    [ObservableProperty] private string pinError = "";
    [ObservableProperty] private string range = "all";
    [ObservableProperty] private string toast = "";
    [ObservableProperty] private bool showGreet = false;

    public PurchaseFilters Filters { get; } = new();

    public string GreetText => $"Welcome back, {Data.Settings.OwnerName} — Your wife loves you ♥";

    private string? _pinFirstEntry;
    private DateTime _lastActivity = DateTime.Now;
    private readonly Microsoft.UI.Dispatching.DispatcherQueueTimer _greetTimer;

    public AnchorViewModel(Store store)
    {
        _store = store;
        data = _store.Load();
        _greetTimer = Microsoft.UI.Dispatching.DispatcherQueue.GetForCurrentThread().CreateTimer();
        _greetTimer.Interval = TimeSpan.FromSeconds(8);
        _greetTimer.IsRepeating = false;
        _greetTimer.Tick += (_, _) => ShowGreet = false;
    }

    private void TriggerGreetIfNamed()
    {
        if (string.IsNullOrWhiteSpace(Data.Settings.OwnerName)) return;
        ShowGreet = true;
        _greetTimer.Stop();
        _greetTimer.Start();
    }

    private void Persist() => _store.Save(Data);

    public string SupplierName(string id) => Data.Suppliers.FirstOrDefault(s => s.Id == id)?.Name ?? id;

    public string Money(double? n, int? decimals = null) => Format.Money(n, Data.Settings.Currency, decimals);

    public void ShowToast(string msg) => Toast = msg;
    public void ClearToast() => Toast = "";

    public void RecordActivity() => _lastActivity = DateTime.Now;

    public void CheckAutoLock()
    {
        var minutes = Data.Settings.AutoLockMinutes;
        if (minutes > 0 && !Locked && (DateTime.Now - _lastActivity).TotalMinutes > minutes)
        {
            Locked = true;
            PinEntry = "";
        }
    }

    public void LockNow()
    {
        Locked = true;
        PinEntry = "";
    }

    public string PinMode() => Data.Settings.Pin == null ? (_pinFirstEntry == null ? "create" : "confirm") : "enter";

    public void PressPinKey(string key)
    {
        var mode = PinMode();
        var entry = key == "⌫" ? (PinEntry.Length > 0 ? PinEntry[..^1] : PinEntry) : (PinEntry + key);
        if (entry.Length > 4) entry = entry[..4];

        if (entry.Length == 4)
        {
            switch (mode)
            {
                case "create":
                    _pinFirstEntry = entry;
                    PinEntry = "";
                    PinError = "";
                    break;
                case "confirm":
                    if (entry == _pinFirstEntry)
                    {
                        Data.Settings.Pin = entry;
                        Persist();
                        Locked = false;
                        PinEntry = "";
                        _pinFirstEntry = null;
                        PinError = "";
                        TriggerGreetIfNamed();
                    }
                    else
                    {
                        PinEntry = "";
                        _pinFirstEntry = null;
                        PinError = "PINs did not match — start over";
                    }
                    break;
                default:
                    if (entry == Data.Settings.Pin)
                    {
                        Locked = false;
                        PinEntry = "";
                        PinError = "";
                        RecordActivity();
                        TriggerGreetIfNamed();
                    }
                    else
                    {
                        PinEntry = "";
                        PinError = "Wrong PIN — try again";
                    }
                    break;
            }
        }
        else
        {
            PinEntry = entry;
            PinError = "";
        }
    }

    // ----- filters / range -----
    public void SetRange(string r) => Range = r;

    public void SetStatusFilter(string? status)
    {
        Filters.Status = status;
        Filters.Category = null;
        Filters.SupplierId = null;
    }

    public void ClearFilters()
    {
        Filters.Search = "";
        Filters.Category = null;
        Filters.SupplierId = null;
        Filters.Status = null;
    }

    public List<Purchase> RangedPurchases() => Logic.RangedPurchases(Data.Purchases, Range);

    public List<Purchase> FilteredPurchases() => Logic.FilteredPurchases(
        Data.Purchases, SupplierName, Filters.Search, Filters.Category, Filters.SupplierId, Filters.Status);

    // ----- purchases -----
    public void AddPurchase(string item, string category, string supplierName, double qty, string unit, double price, string date, string status, string notes, string attach)
    {
        if (string.IsNullOrWhiteSpace(item)) { ShowToast("Item name is required."); return; }
        if (qty <= 0) { ShowToast("Quantity must be greater than zero."); return; }
        if (price <= 0) { ShowToast("Unit price must be greater than zero."); return; }
        if (string.IsNullOrWhiteSpace(supplierName)) { ShowToast("Supplier name is required."); return; }

        var supplier = FindOrCreateSupplier(supplierName.Trim());
        var id = "p" + DateTimeOffset.Now.ToUnixTimeMilliseconds();
        var docs = attach == "None" ? new List<Doc>() : new List<Doc> { new() { Name = $"{attach}-{item.Split(' ')[0]}.pdf", Type = attach, Date = date } };
        Data.Purchases.Add(new Purchase
        {
            Id = id, Item = item.Trim(), Category = category, SupplierId = supplier.Id, Qty = qty, Unit = unit,
            Price = price, Date = date, Status = status, Notes = notes.Trim(), Basis = null, GroupId = null, Docs = docs,
        });
        Persist();
        OnPropertyChanged(nameof(Data));
        ShowToast($"Purchase logged — {Money(qty * price, 0)}");
    }

    public void DeletePurchase(string id)
    {
        Data.Purchases.RemoveAll(p => p.Id == id);
        Data.Reminders.RemoveAll(r => r.LinkType == "p" && r.LinkId == id);
        foreach (var g in Data.Groups) if (g.PurchaseId == id) g.PurchaseId = null;
        Persist();
        OnPropertyChanged(nameof(Data));
        ShowToast("Purchase deleted");
    }

    private Supplier FindOrCreateSupplier(string name)
    {
        var existing = Data.Suppliers.FirstOrDefault(s => string.Equals(s.Name, name, StringComparison.OrdinalIgnoreCase));
        if (existing != null) return existing;
        var fresh = new Supplier { Id = "s" + DateTimeOffset.Now.ToUnixTimeMilliseconds(), Name = name, Contact = "", Notes = "" };
        Data.Suppliers.Add(fresh);
        return fresh;
    }

    // ----- quotes -----
    public void AddQuoteToGroup(string? groupTitle, string newTitle, string category, string supplierName, double qty, double price, string validUntil)
    {
        if (groupTitle == null && string.IsNullOrWhiteSpace(newTitle)) { ShowToast("Name the new comparison."); return; }
        if (qty <= 0 || price <= 0) { ShowToast("Quantity and unit price must be greater than zero."); return; }
        if (string.IsNullOrWhiteSpace(supplierName)) { ShowToast("Supplier name is required."); return; }

        var supplier = FindOrCreateSupplier(supplierName.Trim());
        QuoteGroup group;
        if (groupTitle == null)
        {
            group = new QuoteGroup { Id = "g" + DateTimeOffset.Now.ToUnixTimeMilliseconds(), Title = newTitle.Trim(), Qty = qty, Unit = "pcs", Category = category, Status = "active" };
            Data.Groups.Add(group);
        }
        else
        {
            group = Data.Groups.First(g => g.Title == groupTitle);
        }
        group.Quotes.Add(new Quote { Id = "q" + DateTimeOffset.Now.ToUnixTimeMilliseconds(), GroupId = group.Id, SupplierId = supplier.Id, Price = price, ValidUntil = string.IsNullOrWhiteSpace(validUntil) ? "2026-07-31" : validUntil, Status = "Received" });
        Persist();
        OnPropertyChanged(nameof(Data));
        ShowToast("Quote added");
    }

    public void SelectQuote(string groupId, string quoteId)
    {
        var group = Data.Groups.First(g => g.Id == groupId);
        group.SelectedQuoteId = quoteId;
        foreach (var q in group.Quotes) q.Status = q.Id == quoteId ? "Selected" : "Rejected";
        Persist();
        OnPropertyChanged(nameof(Data));
        ShowToast("Quote selected — savings locked in");
    }

    public void ConvertGroupToPurchase(string groupId)
    {
        var group = Data.Groups.First(g => g.Id == groupId);
        var quote = group.Quotes.First(q => q.Id == group.SelectedQuoteId);
        var highest = group.Quotes.Max(q => q.Price) * group.Qty;
        var id = "p" + DateTimeOffset.Now.ToUnixTimeMilliseconds();
        Data.Purchases.Add(new Purchase
        {
            Id = id, Item = group.Title, Category = group.Category, SupplierId = quote.SupplierId, Qty = group.Qty, Unit = group.Unit,
            Price = quote.Price, Date = Format.Today(), Status = "Ordered", Notes = "Created from quote comparison.", Basis = highest, GroupId = groupId,
            Docs = new List<Doc> { new() { Name = $"Quote-{SupplierName(quote.SupplierId).Split(' ')[0]}.pdf", Type = "Quote", Date = quote.ValidUntil } },
        });
        group.Status = "done";
        group.PurchaseId = id;
        Persist();
        OnPropertyChanged(nameof(Data));
        ShowToast("Converted to purchase");
    }

    // ----- budgets -----
    public void SetBudget(string category, double amount)
    {
        var b = Data.Budgets.FirstOrDefault(x => x.Category == category);
        if (b != null) b.Amount = amount;
        Persist();
        OnPropertyChanged(nameof(Data));
    }

    // ----- reminders -----
    public void ToggleReminderDone(string reminderId)
    {
        var r = Data.Reminders.FirstOrDefault(x => x.Id == reminderId);
        if (r == null) return;
        r.Status = r.Status == "Completed" ? "Upcoming" : "Completed";
        Persist();
        OnPropertyChanged(nameof(Data));
    }

    // ----- scan (demo) -----
    public void SaveScannedPurchase(string item, string category, string supplierName, double qty, double price, string date, string invNo, string tax)
    {
        if (string.IsNullOrWhiteSpace(item)) { ShowToast("Line item is required."); return; }
        if (qty <= 0 || price <= 0) { ShowToast("Quantity and unit price must be greater than zero."); return; }
        if (string.IsNullOrWhiteSpace(supplierName)) { ShowToast("Supplier name is required."); return; }

        var supplier = FindOrCreateSupplier(supplierName.Trim());
        var id = "p" + DateTimeOffset.Now.ToUnixTimeMilliseconds();
        var notes = !string.IsNullOrWhiteSpace(invNo)
            ? $"Scanned from {invNo}" + (!string.IsNullOrWhiteSpace(tax) && double.TryParse(tax, out var taxVal) ? $" · tax {Money(taxVal)}" : "")
            : "Scanned document";
        Data.Purchases.Add(new Purchase
        {
            Id = id, Item = item.Trim(), Category = string.IsNullOrWhiteSpace(category) ? "Office Supplies" : category, SupplierId = supplier.Id,
            Qty = qty, Unit = "pcs", Price = price, Date = date, Status = "Paid", Notes = notes,
            Docs = new List<Doc> { new() { Name = $"{(string.IsNullOrWhiteSpace(invNo) ? "scan" : invNo)}.pdf", Type = "Invoice", Date = date } },
        });
        Persist();
        OnPropertyChanged(nameof(Data));
        ShowToast("Scanned purchase saved");
    }

    public string? DupCheckForScan(string scannedSupplierName, double qty, double price, string date) =>
        Logic.DupCheck(Data.Purchases, SupplierName, scannedSupplierName, qty, price, date, Data.Settings.Currency);

    // ----- settings -----
    public void SaveSettings(string currency, bool appLockEnabled, int budgetAlertPct, int autoLockMinutes, string ownerName)
    {
        Data.Settings.Currency = currency;
        Data.Settings.AppLockEnabled = appLockEnabled;
        Data.Settings.BudgetAlertPct = budgetAlertPct;
        Data.Settings.AutoLockMinutes = autoLockMinutes;
        Data.Settings.OwnerName = ownerName;
        Persist();
        OnPropertyChanged(nameof(Data));
    }

    public string ExportBackupJson() => Backup.Export(Data);

    public string RestoreBackup(string json)
    {
        var result = Backup.Restore(json);
        if (result == null) return "Restore failed — file could not be read as a backup. Your current data is untouched.";
        Data = result;
        Persist();
        OnPropertyChanged(nameof(Data));
        return $"Backup restored — {result.Purchases.Count} purchases";
    }

    public void ResetToSampleData()
    {
        var fresh = Seed.Full();
        fresh.Settings.Pin = Data.Settings.Pin;
        Data = fresh;
        Persist();
        OnPropertyChanged(nameof(Data));
        ShowToast("Sample data loaded");
    }

    public void ClearAllData()
    {
        var fresh = Seed.Blank();
        fresh.Settings = Data.Settings;
        Data = fresh;
        Persist();
        OnPropertyChanged(nameof(Data));
        ShowToast("All data cleared — fresh start");
    }
}
