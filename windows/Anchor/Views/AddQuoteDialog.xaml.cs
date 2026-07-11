using System.Windows;
using System.Windows.Controls;
using Anchor.ViewModels;

namespace Anchor.Views;

public partial class AddQuoteDialog : Window
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
        NewTitlePanel.Visibility = isNew ? Visibility.Visible : Visibility.Collapsed;
        CategoryPanel.Visibility = isNew ? Visibility.Visible : Visibility.Collapsed;
    }

    private void Save_Click(object sender, RoutedEventArgs e)
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
        Close();
    }

    private void Cancel_Click(object sender, RoutedEventArgs e) => Close();
}
