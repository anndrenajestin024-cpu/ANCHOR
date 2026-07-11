using Anchor.Data;
using Anchor.ViewModels;
using Microsoft.UI.Xaml.Controls;
using Windows.Storage;
using Windows.Storage.Pickers;
using WinRT.Interop;

namespace Anchor.Views;

public sealed partial class SettingsDialog : ContentDialog
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

    private void OnSaveClick(ContentDialog sender, ContentDialogButtonClickEventArgs args)
    {
        var autoLock = AutoLockBox.SelectedIndex switch { 0 => 0, 1 => 1, 2 => 5, 3 => 15, _ => 5 };
        var pct = int.TryParse(BudgetAlertBox.Text, out var p) ? Math.Clamp(p, 70, 100) : _vm.Data.Settings.BudgetAlertPct;
        _vm.SaveSettings(CurrencyBox.SelectedItem as string ?? "AED", _vm.Data.Settings.AppLockEnabled, pct, autoLock, OwnerBox.Text);
    }

    private void LockNow_Click(object sender, Microsoft.UI.Xaml.RoutedEventArgs e)
    {
        _vm.LockNow();
        Hide();
    }

    private nint GetWindowHandle() => WindowNative.GetWindowHandle(App.MainWindowInstance);

    private async void DownloadBackup_Click(object sender, Microsoft.UI.Xaml.RoutedEventArgs e)
    {
        var picker = new FileSavePicker();
        InitializeWithWindow.Initialize(picker, GetWindowHandle());
        picker.SuggestedFileName = "anchor-backup";
        picker.FileTypeChoices.Add("JSON", new List<string> { ".json" });

        var file = await picker.PickSaveFileAsync();
        if (file == null) return;

        await FileIO.WriteTextAsync(file, _vm.ExportBackupJson());
        BackupMsg.Text = "Backup saved.";
    }

    private async void RestoreBackup_Click(object sender, Microsoft.UI.Xaml.RoutedEventArgs e)
    {
        var picker = new FileOpenPicker();
        InitializeWithWindow.Initialize(picker, GetWindowHandle());
        picker.FileTypeFilter.Add(".json");

        var file = await picker.PickSingleFileAsync();
        if (file == null) return;

        var json = await FileIO.ReadTextAsync(file);
        BackupMsg.Text = _vm.RestoreBackup(json);
    }

    private void ResetSample_Click(object sender, Microsoft.UI.Xaml.RoutedEventArgs e) => _vm.ResetToSampleData();

    private void ClearAll_Click(object sender, Microsoft.UI.Xaml.RoutedEventArgs e) => _vm.ClearAllData();
}
