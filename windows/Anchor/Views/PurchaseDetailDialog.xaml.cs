using Anchor.Data;
using Anchor.Models;
using Anchor.ViewModels;
using Microsoft.UI.Text;
using Microsoft.UI.Xaml.Controls;

namespace Anchor.Views;

public sealed partial class PurchaseDetailDialog : ContentDialog
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

        DetailPanel.Children.Add(new TextBlock { Text = Format.Money(p.Total, currency, 0), FontSize = 22, FontWeight = FontWeights.SemiBold });
        DetailPanel.Children.Add(new TextBlock { Text = $"{p.Qty} {p.Unit} × {Format.Money(p.Price, currency)} · {Format.FDateY(p.Date)}", FontSize = 13 });

        if (p.Savings > 0)
        {
            DetailPanel.Children.Add(new TextBlock { Text = $"Saved {Format.Money(p.Savings, currency, 0)}", FontSize = 13, FontWeight = FontWeights.SemiBold });
        }

        foreach (var (label, value) in new[]
        {
            ("Category", p.Category), ("Supplier", _vm.SupplierName(p.SupplierId)),
            ("Quantity", $"{p.Qty} {p.Unit}"), ("Unit price", Format.Money(p.Price, currency)),
            ("Currency", currency), ("Purchase date", Format.FDateY(p.Date)),
        })
        {
            DetailPanel.Children.Add(new TextBlock { Text = $"{label}: {value}", FontSize = 13 });
        }

        if (p.Docs.Count > 0)
        {
            DetailPanel.Children.Add(new TextBlock { Text = "DOCUMENTS", FontSize = 11, FontWeight = FontWeights.SemiBold });
            foreach (var d in p.Docs)
            {
                DetailPanel.Children.Add(new TextBlock { Text = $"{d.Name} — {d.Type} · added {Format.FDate(d.Date)}", FontSize = 12 });
            }
        }

        if (!string.IsNullOrWhiteSpace(p.Notes))
        {
            DetailPanel.Children.Add(new TextBlock { Text = "NOTES", FontSize = 11, FontWeight = FontWeights.SemiBold });
            DetailPanel.Children.Add(new TextBlock { Text = p.Notes, FontSize = 13, TextWrapping = Microsoft.UI.Xaml.TextWrapping.Wrap });
        }
    }

    private void OnDeleteClick(ContentDialog sender, ContentDialogButtonClickEventArgs args)
    {
        _vm.DeletePurchase(_purchase.Id);
    }
}
