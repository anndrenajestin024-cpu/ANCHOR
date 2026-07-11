using System.IO;
using System.Windows;
using Anchor.Data;
using Anchor.Models;
using Anchor.ViewModels;
using Microsoft.Win32;

namespace Anchor.Views;

public partial class SettingsDialog : Window
{
    private readonly AnchorViewModel _vm;

    public SettingsDialog(AnchorViewModel vm)
    {
        InitializeComponent();
        _vm = vm;
        var s = vm.Data.Settings;

        OwnerBox.Text = s.OwnerName;
        CurrencyBox.ItemsSource = Statuses.Currencies;
        CurrencyBox.SelectedItem = s.Currency;
        BudgetAlertBox.Text = s.BudgetAlertPct.ToString();
        AutoLockBox.ItemsSource = new[] { "0 (Never)", "1 minute", "5 minutes", "15 minutes" };
        AutoLockBox.SelectedIndex = s.AutoLockMinutes switch { 0 => 0, 1 => 1, 5 => 2, 15 => 3, _ => 2 };
    }

    private void Save_Click(object sender, RoutedEventArgs e)
    {
        var autoLock = AutoLockBox.SelectedIndex switch { 0 => 0, 1 => 1, 2 => 5, 3 => 15, _ => 5 };
        var pct = int.TryParse(BudgetAlertBox.Text, out var p) ? Math.Clamp(p, 70, 100) : _vm.Data.Settings.BudgetAlertPct;
        _vm.SaveSettings(CurrencyBox.SelectedItem as string ?? "AED", _vm.Data.Settings.AppLockEnabled, pct, autoLock, OwnerBox.Text);
        Close();
    }

    private void Close_Click(object sender, RoutedEventArgs e) => Close();

    private void LockNow_Click(object sender, RoutedEventArgs e)
    {
        _vm.LockNow();
        Close();
    }

    private void DownloadBackup_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new SaveFileDialog { FileName = "anchor-backup", DefaultExt = ".json", Filter = "JSON|*.json" };
        if (dialog.ShowDialog() != true) return;

        File.WriteAllText(dialog.FileName, _vm.ExportBackupJson());
        BackupMsg.Text = "Backup saved.";
    }

    private void RestoreBackup_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new OpenFileDialog { Filter = "JSON|*.json" };
        if (dialog.ShowDialog() != true) return;

        var json = File.ReadAllText(dialog.FileName);
        BackupMsg.Text = _vm.RestoreBackup(json);
    }

    private void ResetSample_Click(object sender, RoutedEventArgs e) => _vm.ResetToSampleData();

    private void ClearAll_Click(object sender, RoutedEventArgs e) => _vm.ClearAllData();
}
