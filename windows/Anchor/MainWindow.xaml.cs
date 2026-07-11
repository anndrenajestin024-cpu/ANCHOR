using System.Windows;
using System.Windows.Controls;
using Anchor.Data;
using Anchor.ViewModels;
using Anchor.Views;

namespace Anchor;

public partial class MainWindow : Window
{
    public AnchorViewModel ViewModel { get; }

    private DashboardView? _dashboard;
    private PurchasesView? _purchases;
    private QuotesView? _quotes;
    private SuppliersView? _suppliers;
    private BudgetsView? _budgets;
    private RemindersView? _reminders;

    private readonly (string Tag, string Label)[] _navItems =
    {
        ("dashboard", "Dashboard"),
        ("purchases", "Purchases"),
        ("quotes", "Quotes"),
        ("suppliers", "Suppliers"),
        ("budgets", "Budgets"),
        ("reminders", "Reminders"),
    };

    public MainWindow()
    {
        InitializeComponent();
        ViewModel = new AnchorViewModel(new Store());

        BuildNav();

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

    private void BuildNav()
    {
        foreach (var (tag, label) in _navItems)
        {
            var btn = new Button
            {
                Content = label,
                Tag = tag,
                HorizontalAlignment = HorizontalAlignment.Stretch,
                HorizontalContentAlignment = HorizontalAlignment.Left,
                Padding = new Thickness(10, 8, 10, 8),
                Margin = new Thickness(0, 2, 0, 2),
                Background = System.Windows.Media.Brushes.Transparent,
                Foreground = System.Windows.Media.Brushes.White,
                BorderThickness = new Thickness(0),
            };
            btn.Click += (_, _) => NavigateTo(tag);
            NavPanel.Children.Add(btn);
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

    private void OnUnlocked()
    {
        UpdateLockVisibility();
    }

    private void SettingsButton_Click(object sender, RoutedEventArgs e) => OpenSettings();

    private void NavigateTo(string tag)
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

    private void OpenSettings()
    {
        var dialog = new SettingsDialog(ViewModel) { Owner = this };
        dialog.ShowDialog();
    }
}
