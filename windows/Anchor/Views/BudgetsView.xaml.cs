using Anchor.Data;
using Anchor.ViewModels;
using Microsoft.UI.Text;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;

namespace Anchor.Views;

public sealed partial class BudgetsView : UserControl
{
    private readonly AnchorViewModel _vm;

    public BudgetsView(AnchorViewModel vm)
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
        var alertFrac = data.Settings.BudgetAlertPct / 100.0;

        BudgetsList.Children.Clear();
        foreach (var b in data.Budgets)
        {
            var spent = data.Purchases.Where(p => p.IsSpend && p.Category == b.Category).Sum(p => p.Total);
            var hasBudget = b.Amount > 0;
            var frac = hasBudget ? spent / b.Amount : 0;
            var over = hasBudget && frac > 1.0;
            var warn = hasBudget && frac >= alertFrac;
            var barColor = over ? Windows.UI.Color.FromArgb(255, 184, 92, 72) : warn ? Windows.UI.Color.FromArgb(255, 208, 160, 80) : Windows.UI.Color.FromArgb(255, 143, 172, 120);

            var card = new Border { Background = new SolidColorBrush(Microsoft.UI.Colors.White), CornerRadius = new CornerRadius(12), Padding = new Thickness(16) };
            var stack = new StackPanel { Spacing = 8 };

            var header = new Grid();
            header.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            header.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            header.Children.Add(new TextBlock { Text = b.Category, FontSize = 15, FontWeight = FontWeights.Medium });
            var varText = !hasBudget ? "no budget set" : over ? $"{Format.Money(spent - b.Amount, currency, 0)} over" : $"{Format.Money(b.Amount - spent, currency, 0)} left";
            var varLabel = new TextBlock { Text = varText, FontSize = 12 };
            Grid.SetColumn(varLabel, 1);
            header.Children.Add(varLabel);
            stack.Children.Add(header);

            var barBg = new Border { Height = 8, CornerRadius = new CornerRadius(4), Background = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 228, 229, 233)) };
            var barFg = new Border { Height = 8, CornerRadius = new CornerRadius(4), Background = new SolidColorBrush(barColor), HorizontalAlignment = HorizontalAlignment.Left, Width = Math.Max(Math.Min(frac, 1.0) * 500, 2) };
            var barGrid = new Grid();
            barGrid.Children.Add(barBg);
            barGrid.Children.Add(barFg);
            stack.Children.Add(barGrid);

            var editRow = new StackPanel { Orientation = Orientation.Horizontal, Spacing = 8 };
            var amountBox = new TextBox { Width = 160, Text = hasBudget ? b.Amount.ToString("0") : "", PlaceholderText = "Budget amount" };
            var saveBtn = new Button { Content = "Save" };
            var category = b.Category;
            saveBtn.Click += (_, _) => _vm.SetBudget(category, double.TryParse(amountBox.Text, out var v) ? v : 0);
            editRow.Children.Add(amountBox);
            editRow.Children.Add(saveBtn);
            stack.Children.Add(editRow);

            card.Child = stack;
            BudgetsList.Children.Add(card);
        }
    }
}
