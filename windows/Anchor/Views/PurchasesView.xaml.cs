using Anchor.Data;
using Anchor.Models;
using Anchor.ViewModels;
using Microsoft.UI.Text;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;

namespace Anchor.Views;

public sealed partial class PurchasesView : UserControl
{
    private readonly AnchorViewModel _vm;

    public PurchasesView(AnchorViewModel vm)
    {
        InitializeComponent();
        _vm = vm;
        _vm.PropertyChanged += (_, e) =>
        {
            if (e.PropertyName == nameof(AnchorViewModel.Data)) Render();
        };
        BuildStatusChips();
        Render();
    }

    private void BuildStatusChips()
    {
        StatusChips.Children.Clear();
        var all = new Button { Content = "All" };
        all.Click += (_, _) => { _vm.ClearFilters(); Render(); };
        StatusChips.Children.Add(all);
        foreach (var s in Statuses.Purchase)
        {
            var btn = new Button { Content = s };
            btn.Click += (_, _) => { _vm.SetStatusFilter(_vm.Filters.Status == s ? null : s); Render(); };
            StatusChips.Children.Add(btn);
        }
    }

    private void SearchBox_TextChanged(object sender, TextChangedEventArgs e)
    {
        _vm.Filters.Search = SearchBox.Text;
        Render();
    }

    private void AddButton_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new AddPurchaseDialog(_vm) { XamlRoot = XamlRoot };
        _ = dialog.ShowAsync();
    }

    public void OpenDetail(string purchaseId)
    {
        var p = _vm.Data.Purchases.FirstOrDefault(x => x.Id == purchaseId);
        if (p == null) return;
        var dialog = new PurchaseDetailDialog(_vm, p) { XamlRoot = XamlRoot };
        _ = dialog.ShowAsync();
    }

    private void Render()
    {
        var currency = _vm.Data.Settings.Currency;
        var rows = _vm.FilteredPurchases();
        ResultCount.Text = $"{rows.Count} purchase{(rows.Count == 1 ? "" : "s")}";

        RowsList.Children.Clear();
        foreach (var p in rows)
        {
            var card = new Border
            {
                Background = new SolidColorBrush(Microsoft.UI.Colors.White),
                CornerRadius = new CornerRadius(10),
                Padding = new Thickness(14),
            };
            var stack = new StackPanel { Spacing = 4 };

            var header = new Grid();
            header.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            header.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            var itemText = new TextBlock { Text = p.Item, FontSize = 15, FontWeight = FontWeights.Medium };
            Grid.SetColumn(itemText, 0);
            var statusText = new TextBlock { Text = p.Status, FontSize = 11, Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 85, 86, 91)) };
            Grid.SetColumn(statusText, 1);
            header.Children.Add(itemText);
            header.Children.Add(statusText);

            var sub = new TextBlock
            {
                Text = $"{_vm.SupplierName(p.SupplierId)} · {p.Qty} {p.Unit} × {Format.Money(p.Price, currency)} · {Format.FDate(p.Date)}",
                FontSize = 12, Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 166, 167, 172)),
            };

            var footer = new Grid();
            footer.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            footer.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            var totalText = new TextBlock { Text = Format.Money(p.Total, currency, 0), FontSize = 14, FontWeight = FontWeights.SemiBold };
            Grid.SetColumn(totalText, 0);
            footer.Children.Add(totalText);
            if (p.Savings > 0)
            {
                var savText = new TextBlock { Text = $"saved {Format.Money(p.Savings, currency, 0)}", FontSize = 12, Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 94, 122, 76)) };
                Grid.SetColumn(savText, 1);
                footer.Children.Add(savText);
            }

            stack.Children.Add(header);
            stack.Children.Add(sub);
            stack.Children.Add(footer);
            card.Child = stack;

            var button = new Button
            {
                Content = card,
                Background = new SolidColorBrush(Microsoft.UI.Colors.Transparent),
                BorderThickness = new Thickness(0),
                Padding = new Thickness(0),
                HorizontalContentAlignment = HorizontalAlignment.Stretch,
            };
            var id = p.Id;
            button.Click += (_, _) => OpenDetail(id);
            RowsList.Children.Add(button);
        }
    }
}
