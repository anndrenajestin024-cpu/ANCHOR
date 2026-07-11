using Anchor.Data;
using Microsoft.UI.Text;
using Microsoft.UI.Xaml.Controls;
using Anchor.ViewModels;

namespace Anchor.Views;

public sealed partial class SupplierDetailDialog : ContentDialog
{
    public SupplierDetailDialog(AnchorViewModel vm, string supplierId)
    {
        InitializeComponent();
        var supplier = vm.Data.Suppliers.FirstOrDefault(s => s.Id == supplierId);
        if (supplier == null) return;

        Title = supplier.Name;
        var currency = vm.Data.Settings.Currency;
        var purchases = vm.Data.Purchases.Where(p => p.SupplierId == supplierId).OrderByDescending(p => p.Date).ToList();
        var spend = purchases.Where(p => p.IsSpend).Sum(p => p.Total);

        DetailPanel.Children.Add(new TextBlock { Text = Format.Money(spend, currency, 0), FontSize = 20, FontWeight = FontWeights.SemiBold });
        DetailPanel.Children.Add(new TextBlock { Text = $"{purchases.Count} purchases total", FontSize = 12 });

        if (!string.IsNullOrWhiteSpace(supplier.Contact))
        {
            DetailPanel.Children.Add(new TextBlock { Text = supplier.Contact, FontSize = 13, TextWrapping = Microsoft.UI.Xaml.TextWrapping.Wrap });
        }
        if (!string.IsNullOrWhiteSpace(supplier.Notes))
        {
            DetailPanel.Children.Add(new TextBlock { Text = supplier.Notes, FontSize = 13, TextWrapping = Microsoft.UI.Xaml.TextWrapping.Wrap });
        }

        DetailPanel.Children.Add(new TextBlock { Text = "PURCHASE HISTORY", FontSize = 11, FontWeight = FontWeights.SemiBold, Margin = new Microsoft.UI.Xaml.Thickness(0, 8, 0, 0) });
        foreach (var p in purchases)
        {
            DetailPanel.Children.Add(new TextBlock
            {
                Text = $"{p.Item} — {Format.FDateY(p.Date)} · {p.Qty} {p.Unit} × {Format.Money(p.Price, currency)} · {p.Status} · {Format.Money(p.Total, currency, 0)}",
                FontSize = 12,
                TextWrapping = Microsoft.UI.Xaml.TextWrapping.Wrap,
            });
        }
    }
}
