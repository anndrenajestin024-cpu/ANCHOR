using Anchor.ViewModels;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Anchor.Views;

public sealed partial class AddQuoteDialog : ContentDialog
{
    private const string NewComparisonOption = "＋ New comparison…";
    private readonly AnchorViewModel _vm;

    public AddQuoteDialog(AnchorViewModel vm)
    {
        InitializeComponent();
        _vm = vm;

        var activeTitles = vm.Data.Groups.Where(g => g.Status == "active").Select(g => g.Title).ToList();
        activeTitles.Add(NewComparisonOption);
        GroupBox.ItemsSource = activeTitles;
        GroupBox.SelectedIndex = 0;

        CategoryBox.ItemsSource = vm.Data.Categories;
        CategoryBox.SelectedIndex = 0;
    }

    private void GroupBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        var isNew = GroupBox.SelectedItem as string == NewComparisonOption;
        NewTitleBox.Visibility = isNew ? Visibility.Visible : Visibility.Collapsed;
        CategoryBox.Visibility = isNew ? Visibility.Visible : Visibility.Collapsed;
    }

    private void OnPrimaryButtonClick(ContentDialog sender, ContentDialogButtonClickEventArgs args)
    {
        var selected = GroupBox.SelectedItem as string;
        var isNew = selected == NewComparisonOption;
        _vm.AddQuoteToGroup(
            isNew ? null : selected,
            NewTitleBox.Text,
            CategoryBox.SelectedItem as string ?? "Facilities",
            SupplierBox.Text,
            double.TryParse(QtyBox.Text, out var qty) ? qty : 0,
            double.TryParse(PriceBox.Text, out var price) ? price : 0,
            ValidUntilBox.Text);
    }
}
