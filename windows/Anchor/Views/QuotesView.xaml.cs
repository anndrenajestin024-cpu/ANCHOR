using Anchor.Data;
using Anchor.ViewModels;
using Microsoft.UI.Text;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;

namespace Anchor.Views;

public sealed partial class QuotesView : UserControl
{
    private readonly AnchorViewModel _vm;

    public QuotesView(AnchorViewModel vm)
    {
        InitializeComponent();
        _vm = vm;
        _vm.PropertyChanged += (_, e) =>
        {
            if (e.PropertyName == nameof(AnchorViewModel.Data)) Render();
        };
        Render();
    }

    private void AddButton_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new AddQuoteDialog(_vm) { XamlRoot = XamlRoot };
        _ = dialog.ShowAsync();
    }

    private void Render()
    {
        var currency = _vm.Data.Settings.Currency;
        var groups = _vm.Data.Groups.OrderBy(g => g.Status == "active" ? 0 : 1).ToList();

        GroupsList.Children.Clear();
        foreach (var g in groups)
        {
            var active = g.Status == "active";
            var totals = g.Quotes.Select(q => q.Price * g.Qty).ToList();
            var minTotal = totals.Count > 0 ? totals.Min() : 0;

            var card = new Border { Background = new SolidColorBrush(Microsoft.UI.Colors.White), CornerRadius = new CornerRadius(12), Padding = new Thickness(16) };
            var stack = new StackPanel { Spacing = 6 };

            var header = new Grid();
            header.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            header.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            var titleText = new TextBlock { Text = g.Title, FontSize = 16, FontWeight = FontWeights.SemiBold };
            Grid.SetColumn(titleText, 0);
            var stLabel = active ? (g.SelectedQuoteId != null ? "Selected" : "Comparing") : "Converted";
            var stText = new TextBlock { Text = stLabel, FontSize = 11 };
            Grid.SetColumn(stText, 1);
            header.Children.Add(titleText);
            header.Children.Add(stText);
            stack.Children.Add(header);
            stack.Children.Add(new TextBlock { Text = $"{g.Qty} {g.Unit} · {g.Category} · {g.Quotes.Count} quotes", FontSize = 12, Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 166, 167, 172)) });

            foreach (var q in g.Quotes.OrderBy(x => x.Price))
            {
                var total = q.Price * g.Qty;
                var cheapest = Math.Abs(total - minTotal) < 0.001;
                var selected = g.SelectedQuoteId == q.Id;

                var row = new Grid { Margin = new Thickness(0, 6, 0, 0) };
                row.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
                row.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
                var left = new StackPanel();
                left.Children.Add(new TextBlock { Text = _vm.SupplierName(q.SupplierId), FontSize = 13, FontWeight = FontWeights.Medium });
                left.Children.Add(new TextBlock { Text = $"{Format.Money(q.Price, currency)} / unit · valid {Format.FDate(q.ValidUntil)}", FontSize = 11, Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 166, 167, 172)) });
                Grid.SetColumn(left, 0);

                var right = new StackPanel { Orientation = Orientation.Horizontal, Spacing = 6, HorizontalAlignment = HorizontalAlignment.Right };
                right.Children.Add(new TextBlock { Text = Format.Money(total, currency, 0), FontSize = 13, FontWeight = FontWeights.SemiBold });
                if (cheapest || selected)
                {
                    right.Children.Add(new TextBlock { Text = selected ? "Selected" : "Lowest", FontSize = 11, Foreground = new SolidColorBrush(selected ? Windows.UI.Color.FromArgb(255, 85, 86, 91) : Windows.UI.Color.FromArgb(255, 94, 122, 76)) });
                }
                Grid.SetColumn(right, 1);
                row.Children.Add(left);
                row.Children.Add(right);
                stack.Children.Add(row);

                if (active && !selected)
                {
                    var selectBtn = new Button { Content = "Select this quote", HorizontalAlignment = HorizontalAlignment.Stretch, Margin = new Thickness(0, 4, 0, 0) };
                    var gid = g.Id; var qid = q.Id;
                    selectBtn.Click += (_, _) => _vm.SelectQuote(gid, qid);
                    stack.Children.Add(selectBtn);
                }
            }

            if (active && g.SelectedQuoteId != null)
            {
                var selQ = g.Quotes.First(x => x.Id == g.SelectedQuoteId);
                var savings = (totals.Count > 0 ? totals.Max() : 0) - selQ.Price * g.Qty;
                if (savings > 0)
                {
                    stack.Children.Add(new TextBlock { Text = $"Savings: {Format.Money(savings, currency, 0)}", FontSize = 13, FontWeight = FontWeights.SemiBold, Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 94, 122, 76)), Margin = new Thickness(0, 8, 0, 0) });
                }
                var convertBtn = new Button { Content = "Convert to purchase", HorizontalAlignment = HorizontalAlignment.Stretch, Margin = new Thickness(0, 8, 0, 0) };
                var gid2 = g.Id;
                convertBtn.Click += (_, _) => _vm.ConvertGroupToPurchase(gid2);
                stack.Children.Add(convertBtn);
            }

            card.Child = stack;
            GroupsList.Children.Add(card);
        }
    }
}
