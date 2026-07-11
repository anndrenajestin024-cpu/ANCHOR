using Anchor.Data;
using Anchor.ViewModels;
using Microsoft.UI.Text;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;

namespace Anchor.Views;

public sealed partial class SuppliersView : UserControl
{
    private readonly AnchorViewModel _vm;

    public SuppliersView(AnchorViewModel vm)
    {
        InitializeComponent();
        _vm = vm;
        _vm.PropertyChanged += (_, e) =>
        {
            if (e.PropertyName == nameof(AnchorViewModel.Data)) Render();
        };
        Render();
    }

    private void Render()
    {
        var data = _vm.Data;
        var currency = data.Settings.Currency;
        var spendBySupplier = data.Purchases.Where(p => p.IsSpend).GroupBy(p => p.SupplierId).ToDictionary(g => g.Key, g => g.Sum(p => p.Total));

        SuppliersList.Children.Clear();
        foreach (var s in data.Suppliers)
        {
            var count = data.Purchases.Count(p => p.SupplierId == s.Id);
            var amount = spendBySupplier.GetValueOrDefault(s.Id, 0);

            var card = new Border { Background = new SolidColorBrush(Microsoft.UI.Colors.White), CornerRadius = new CornerRadius(10), Padding = new Thickness(14) };
            var grid = new Grid();
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            var left = new StackPanel();
            left.Children.Add(new TextBlock { Text = s.Name, FontSize = 14, FontWeight = FontWeights.Medium });
            left.Children.Add(new TextBlock { Text = $"{count} purchases · {s.Contact.Split('·').FirstOrDefault()?.Trim()}", FontSize = 11, Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 166, 167, 172)) });
            Grid.SetColumn(left, 0);
            var right = new TextBlock { Text = Format.Money(amount, currency, 0), FontSize = 13, FontWeight = FontWeights.SemiBold, VerticalAlignment = VerticalAlignment.Center };
            Grid.SetColumn(right, 1);
            grid.Children.Add(left);
            grid.Children.Add(right);
            card.Child = grid;

            var button = new Button { Content = card, Background = new SolidColorBrush(Microsoft.UI.Colors.Transparent), BorderThickness = new Thickness(0), Padding = new Thickness(0), HorizontalContentAlignment = HorizontalAlignment.Stretch };
            var supplierId = s.Id;
            button.Click += (_, _) =>
            {
                var dialog = new SupplierDetailDialog(_vm, supplierId) { XamlRoot = XamlRoot };
                _ = dialog.ShowAsync();
            };
            SuppliersList.Children.Add(button);
        }
    }
}
