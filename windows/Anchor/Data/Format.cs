using System.Globalization;

namespace Anchor.Data;

public static class Format
{
    private static string Symbol(string currency) => currency switch
    {
        "AED" => "AED ",
        "USD" => "$",
        "EUR" => "€",
        "GBP" => "£",
        "INR" => "₹",
        _ => "AED ",
    };

    public static string Money(double? n, string currency, int? decimals = null)
    {
        if (n is null || double.IsNaN(n.Value)) return "—";
        var d = decimals ?? (Math.Abs(n.Value) < 100 && n.Value % 1.0 != 0.0 ? 2 : 0);
        var abs = Math.Abs(n.Value);
        var formatted = abs.ToString(d == 0 ? "N0" : "N" + d, CultureInfo.InvariantCulture);
        return Symbol(currency) + formatted;
    }

    private static readonly string[] MonthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    public static string FDate(string? iso)
    {
        if (string.IsNullOrWhiteSpace(iso)) return "—";
        var parts = iso.Split('-');
        if (parts.Length != 3 || !int.TryParse(parts[1], out var month) || !int.TryParse(parts[2], out var day)) return "—";
        if (month < 1 || month > 12) return "—";
        return $"{MonthNames[month - 1]} {day}";
    }

    public static string FDateY(string? iso)
    {
        if (string.IsNullOrWhiteSpace(iso)) return "—";
        return $"{FDate(iso)}, {iso[..4]}";
    }

    public static string Today() => DateTime.Now.ToString("yyyy-MM-dd");
}
