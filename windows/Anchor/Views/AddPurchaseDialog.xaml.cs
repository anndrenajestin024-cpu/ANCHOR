using System.Windows;
using Anchor.Data;
using Anchor.ViewModels;

namespace Anchor.Views;

public partial class AddPurchaseDialog : Window
{
    private readonly AnchorViewModel _vm;

    public AddPurchaseDialog(AnchorViewModel vm)
    {
        InitializeComponent();
        _vm = vm;

        CategoryBox.ItemsSource = vm.Data.Categories;
        CategoryBox.SelectedIndex = 0;
        UnitBox.ItemsSource = Statuses.Units;
        UnitBox.SelectedIndex = 0;
        StatusBox.ItemsSource = Statuses.Purchase;
        StatusBox.SelectedIndex = 0;
        AttachBox.ItemsSource = Statuses.AttachTypes;
        AttachBox.SelectedIndex = 0;
        DateBox.Text = Format.Today();
    }

    private void Save_Click(object sender, RoutedEventArgs e)
    {
        _vm.AddPurchase(
            ItemBox.Text,
            CategoryBox.SelectedItem as string ?? "",
            SupplierBox.Text,
            double.TryParse(QtyBox.Text, out var qty) ? qty : 0,
            UnitBox.SelectedItem as string ?? "pcs",
            double.TryParse(PriceBox.Text, out var price) ? price : 0,
            DateBox.Text,
            StatusBox.SelectedItem as string ?? "Requested",
            NotesBox.Text,
            AttachBox.SelectedItem as string ?? "None");
        Close();
    }

    private void Cancel_Click(object sender, RoutedEventArgs e) => Close();
}
