using System.Windows;
using System.Windows.Controls;
using Anchor.Data;
using Anchor.Models;
using Anchor.ViewModels;

namespace Anchor.Views;

public partial class PurchaseDetailDialog : Window
{
    private readonly AnchorViewModel _vm;
    private readonly Purchase _purchase;

    public PurchaseDetailDialog(AnchorViewModel vm, Purchase purchase)
    {
        InitializeComponent();
        _vm = vm;
        _purchase = purchase;
        Title = purchase.Item;
        Build();
    }

    private void Build()
    {
        var currency = _vm.Data.Settings.Currency;
        var p = _purchase;

        DetailPanel.Children.Add(new TextBlock { Text = Format.Money(p.Total, currency, 0), FontSize = 22, FontWeight = FontWeights.SemiBold, Margin = new Thickness(0, 0, 0, 6) });
        DetailPanel.Children.Add(new TextBlock { Text = $"{p.Qty} {p.Unit} × {Format.Money(p.Price, currency)} · {Format.FDateY(p.Date)}", FontSize = 13, Margin = new Thickness(0, 0, 0, 6) });

        if (p.Savings > 0)
        {
            DetailPanel.Children.Add(new TextBlock { Text = $"Saved {Format.Money(p.Savings, currency, 0)}", FontSize = 13, FontWeight = FontWeights.SemiBold, Margin = new Thickness(0, 0, 0, 6) });
        }

        foreach (var (label, value) in new[]
        {
            ("Category", p.Category), ("Supplier", _vm.SupplierName(p.SupplierId)),
            ("Quantity", $"{p.Qty} {p.Unit}"), ("Unit price", Format.Money(p.Price, currency)),
            ("Currency", currency), ("Purchase date", Format.FDateY(p.Date)),
        })
        {
            DetailPanel.Children.Add(new TextBlock { Text = $"{label}: {value}", FontSize = 13, Margin = new Thickness(0, 0, 0, 4) });
        }

        if (p.Docs.Count > 0)
        {
            DetailPanel.Children.Add(new TextBlock { Text = "DOCUMENTS", FontSize = 11, FontWeight = FontWeights.SemiBold, Margin = new Thickness(0, 8, 0, 4) });
            foreach (var d in p.Docs)
            {
                DetailPanel.Children.Add(new TextBlock { Text = $"{d.Name} — {d.Type} · added {Format.FDate(d.Date)}", FontSize = 12, Margin = new Thickness(0, 0, 0, 2) });
            }
        }

        if (!string.IsNullOrWhiteSpace(p.Notes))
        {
            DetailPanel.Children.Add(new TextBlock { Text = "NOTES", FontSize = 11, FontWeight = FontWeights.SemiBold, Margin = new Thickness(0, 8, 0, 4) });
            DetailPanel.Children.Add(new TextBlock { Text = p.Notes, FontSize = 13, TextWrapping = TextWrapping.Wrap });
        }
    }

    private void OnDeleteClick(object sender, RoutedEventArgs e)
    {
        _vm.DeletePurchase(_purchase.Id);
        Close();
    }

    private void Close_Click(object sender, RoutedEventArgs e) => Close();
}
