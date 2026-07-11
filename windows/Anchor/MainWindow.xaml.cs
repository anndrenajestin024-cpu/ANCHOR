using Anchor.Data;
using Anchor.ViewModels;
using Anchor.Views;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Anchor;

public sealed partial class MainWindow : Window
{
    public AnchorViewModel ViewModel { get; }

    private DashboardView? _dashboard;
    private PurchasesView? _purchases;
    private QuotesView? _quotes;
    private SuppliersView? _suppliers;
    private BudgetsView? _budgets;
    private RemindersView? _reminders;

    public MainWindow()
    {
        InitializeComponent();
        ViewModel = new AnchorViewModel(new Store());

        LockViewControl.Initialize(ViewModel, OnUnlocked);
        ViewModel.PropertyChanged += (_, e) =>
        {
            if (e.PropertyName == nameof(AnchorViewModel.Locked))
            {
                UpdateLockVisibility();
            }
            else if (e.PropertyName == nameof(AnchorViewModel.ShowGreet))
            {
                UpdateGreetVisibility();
            }
        };

        UpdateLockVisibility();
        ShowDashboard();
    }

    private void UpdateGreetVisibility()
    {
        if (ViewModel.ShowGreet)
        {
            GreetText.Text = ViewModel.GreetText;
            GreetBar.Visibility = Visibility.Visible;
        }
        else
        {
            GreetBar.Visibility = Visibility.Collapsed;
        }
    }

    private void UpdateLockVisibility()
    {
        if (ViewModel.Locked)
        {
            LockRoot.Visibility = Visibility.Visible;
            ShellRoot.Visibility = Visibility.Collapsed;
            LockViewControl.RefreshState();
        }
        else
        {
            LockRoot.Visibility = Visibility.Collapsed;
            ShellRoot.Visibility = Visibility.Visible;
        }
    }

    private void OnUnlocked()
    {
        UpdateLockVisibility();
    }

    private void Nav_ItemInvoked(NavigationView sender, NavigationViewItemInvokedEventArgs args)
    {
        if (args.IsSettingsInvoked)
        {
            OpenSettings();
            return;
        }
        var tag = (args.InvokedItemContainer as NavigationViewItem)?.Tag as string;
        NavigateTo(tag);
    }

    private void Nav_SelectionChanged(NavigationView sender, NavigationViewSelectionChangedEventArgs args)
    {
        if (args.IsSettingsSelected)
        {
            OpenSettings();
        }
    }

    private void NavigateTo(string? tag)
    {
        switch (tag)
        {
            case "purchases": ShowPurchases(); break;
            case "quotes": ShowQuotes(); break;
            case "suppliers": ShowSuppliers(); break;
            case "budgets": ShowBudgets(); break;
            case "reminders": ShowReminders(); break;
            default: ShowDashboard(); break;
        }
    }

    private void ShowDashboard()
    {
        _dashboard ??= new DashboardView(ViewModel, id => { ShowPurchases(); _purchases!.OpenDetail(id); });
        ContentHost.Content = _dashboard;
    }

    private void ShowPurchases()
    {
        _purchases ??= new PurchasesView(ViewModel);
        ContentHost.Content = _purchases;
    }

    private void ShowQuotes()
    {
        _quotes ??= new QuotesView(ViewModel);
        ContentHost.Content = _quotes;
    }

    private void ShowSuppliers()
    {
        _suppliers ??= new SuppliersView(ViewModel);
        ContentHost.Content = _suppliers;
    }

    private void ShowBudgets()
    {
        _budgets ??= new BudgetsView(ViewModel);
        ContentHost.Content = _budgets;
    }

    private void ShowReminders()
    {
        _reminders ??= new RemindersView(ViewModel);
        ContentHost.Content = _reminders;
    }

    private async void OpenSettings()
    {
        var dialog = new SettingsDialog(ViewModel) { XamlRoot = Content.XamlRoot };
        await dialog.ShowAsync();
    }
}
