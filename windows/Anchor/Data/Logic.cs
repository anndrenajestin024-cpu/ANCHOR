using Anchor.Models;

namespace Anchor.Data;

/// <summary>Business logic mirrors the original Anchor prototype 1:1 (same field names, same formulas).</summary>
public static class Logic
{
    public static List<Purchase> RangedPurchases(List<Purchase> purchases, string range)
    {
        var today = DateTime.Now.Date;
        DateTime cutoff = range switch
        {
            "30d" => today.AddDays(-30),
            "3m" => today.AddMonths(-3),
            "6m" => today.AddMonths(-6),
            _ => DateTime.MinValue,
        };
        var cutoffStr = cutoff == DateTime.MinValue ? "0000-00-00" : cutoff.ToString("yyyy-MM-dd");
        return purchases.Where(p => string.CompareOrdinal(p.Date, cutoffStr) >= 0).ToList();
    }

    public static List<Purchase> FilteredPurchases(
        List<Purchase> purchases, Func<string, string> supplierName, string search, string? category, string? supplierId, string? status)
    {
        var q = search.Trim().ToLowerInvariant();
        return purchases.Where(p =>
        {
            if (category != null && p.Category != category) return false;
            if (supplierId != null && p.SupplierId != supplierId) return false;
            if (status != null && p.Status != status) return false;
            if (q.Length > 0)
            {
                var hay = (p.Item + " " + supplierName(p.SupplierId) + " " + p.Category + " " + p.Notes + " " +
                    string.Join(" ", p.Docs.Select(d => d.Name + " " + d.Type))).ToLowerInvariant();
                if (!hay.Contains(q)) return false;
            }
            return true;
        }).OrderByDescending(p => p.Date).ToList();
    }

    /// <summary>Flags a likely-duplicate scan (same supplier, total, and date) — mirrors the prototype's dupCheck.</summary>
    public static string? DupCheck(List<Purchase> purchases, Func<string, string> supplierNameOf, string scannedSupplierName, double qty, double price, string date, string currency)
    {
        var total = qty * price;
        var hit = purchases.FirstOrDefault(p => supplierNameOf(p.SupplierId) == scannedSupplierName && Math.Abs(p.Total - total) < 0.01 && p.Date == date);
        if (hit == null) return null;
        return $"A purchase from {scannedSupplierName} for {Format.Money(total, currency)} on {Format.FDateY(date)} is already logged ({hit.Item}).";
    }
}
