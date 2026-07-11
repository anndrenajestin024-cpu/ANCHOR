using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Anchor.Data;
using Anchor.ViewModels;

namespace Anchor.Views;

public partial class DashboardView : UserControl
{
    private readonly AnchorViewModel _vm;
    private readonly Action<string> _onOpenPurchase;

    public DashboardView(AnchorViewModel vm, Action<string> onOpenPurchase)
    {
        InitializeComponent();
        _vm = vm;
        _onOpenPurchase = onOpenPurchase;
        _vm.PropertyChanged += (_, e) =>
        {
            if (e.PropertyName == nameof(AnchorViewModel.Data) || e.PropertyName == nameof(AnchorViewModel.Range)) Render();
        };
        BuildRangeChips();
        Render();
    }

    private void BuildRangeChips()
    {
        RangeChips.Children.Clear();
        foreach (var (value, label) in new[] { ("all", "All time"), ("6m", "6 months"), ("3m", "3 months"), ("30d", "30 days") })
        {
            var btn = new Button { Content = label, Tag = value, Margin = new Thickness(0, 0, 8, 0) };
            btn.Click += (_, _) => { _vm.SetRange(value); };
            RangeChips.Children.Add(btn);
        }
    }

    private void Render()
    {
        var data = _vm.Data;
        var currency = data.Settings.Currency;
        var ranged = _vm.RangedPurchases();
        var spend = ranged.Where(p => p.IsSpend).ToList();
        var totalSpend = spend.Sum(p => p.Total);
        var totalSavings = ranged.Where(p => p.Savings > 0).Sum(p => p.Savings);
        var pending = ranged.Where(p => p.Status is "Ordered" or "Delivered").ToList();
        var pendingAmt = pending.Sum(p => p.Total);

        var totalBudget = data.Budgets.Sum(b => b.Amount);
        var ytdSpend = data.Purchases.Where(p => p.IsSpend).Sum(p => p.Total);
        var variance = totalBudget - ytdSpend;

        TotalSpendText.Text = Format.Money(totalSpend, currency, 0);
        TotalSpendSub.Text = $"{spend.Count} purchases in range";
        SavingsText.Text = Format.Money(totalSavings, currency, 0);
        VarianceText.Text = variance >= 0 ? $"{Format.Money(variance, currency, 0)} under" : $"{Format.Money(-variance, currency, 0)} over";
        VarianceSub.Text = $"{Format.Money(ytdSpend, currency, 0)} of {Format.Money(totalBudget, currency, 0)}";
        PendingText.Text = Format.Money(pendingAmt, currency, 0);
        PendingSub.Text = $"{pending.Count} orders awaiting payment";

        CategoryList.Children.Clear();
        var byCategory = spend.GroupBy(p => p.Category).Select(g => (g.Key, Amount: g.Sum(p => p.Total))).OrderByDescending(x => x.Amount);
        foreach (var (cat, amt) in byCategory)
        {
            var frac = totalSpend > 0 ? amt / totalSpend : 0;
            var row = new StackPanel { Margin = new Thickness(0, 0, 0, 10) };
            var header = new Grid();
            header.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            header.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            var catText = new TextBlock { Text = cat, FontSize = 13 };
            Grid.SetColumn(catText, 0);
            var amtText = new TextBlock { Text = Format.Money(amt, currency, 0), FontSize = 13, FontWeight = FontWeights.SemiBold };
            Grid.SetColumn(amtText, 1);
            header.Children.Add(catText);
            header.Children.Add(amtText);
            var barBg = new Border { Height = 6, CornerRadius = new CornerRadius(3), Background = new SolidColorBrush(Color.FromArgb(255, 228, 229, 233)), Margin = new Thickness(0, 4, 0, 0) };
            var barFg = new Border { Height = 6, CornerRadius = new CornerRadius(3), Background = new SolidColorBrush(Color.FromArgb(255, 91, 123, 154)), HorizontalAlignment = HorizontalAlignment.Left, Width = Math.Max(frac * 260, 2) };
            var barGrid = new Grid { Width = 260, Margin = new Thickness(0, 4, 0, 0) };
            barGrid.Children.Add(barBg);
            barGrid.Children.Add(barFg);
            row.Children.Add(header);
            row.Children.Add(barGrid);
            CategoryList.Children.Add(row);
        }

        RecentList.Children.Clear();
        foreach (var p in data.Purchases.OrderByDescending(p => p.Date).Take(5))
        {
            var row = new Button
            {
                HorizontalContentAlignment = HorizontalAlignment.Stretch,
                Background = Brushes.Transparent,
                BorderThickness = new Thickness(0),
                Padding = new Thickness(0, 8, 0, 8),
            };
            var grid = new Grid();
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            grid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            var left = new StackPanel();
            left.Children.Add(new TextBlock { Text = p.Item, FontSize = 14 });
            left.Children.Add(new TextBlock { Text = $"{_vm.SupplierName(p.SupplierId)} · {Format.FDate(p.Date)} · {p.Status}", FontSize = 12, Foreground = new SolidColorBrush(Color.FromArgb(255, 166, 167, 172)) });
            Grid.SetColumn(left, 0);
            var right = new TextBlock { Text = Format.Money(p.Total, currency, 0), FontSize = 13, FontWeight = FontWeights.SemiBold, VerticalAlignment = VerticalAlignment.Center };
            Grid.SetColumn(right, 1);
            grid.Children.Add(left);
            grid.Children.Add(right);
            row.Content = grid;
            var id = p.Id;
            row.Click += (_, _) => _onOpenPurchase(id);
            RecentList.Children.Add(row);
        }
    }
}
