using System.Windows;
using System.Windows.Controls;
using Anchor.ViewModels;

namespace Anchor.Views;

public partial class LockView : UserControl
{
    private AnchorViewModel? _vm;
    private Action? _onUnlocked;

    public LockView()
    {
        InitializeComponent();
    }

    public void Initialize(AnchorViewModel vm, Action onUnlocked)
    {
        _vm = vm;
        _onUnlocked = onUnlocked;
        RefreshState();
    }

    public void RefreshState()
    {
        if (_vm == null) return;
        var mode = _vm.PinMode();
        TitleText.Text = mode switch
        {
            "create" => "Create a 4-digit PIN",
            "confirm" => "Confirm your PIN",
            _ => "Enter PIN",
        };
        SubtitleText.Text = mode == "enter" ? "Your procurement data is locked" : "Used to unlock this app on this device";
        ErrorText.Text = _vm.PinError;
    }

    private void DigitClick(object sender, RoutedEventArgs e)
    {
        if (_vm == null) return;
        var digit = ((Button)sender).Content?.ToString() ?? "";
        var wasLocked = _vm.Locked;
        _vm.PressPinKey(digit);
        RefreshState();
        if (wasLocked && !_vm.Locked) _onUnlocked?.Invoke();
    }

    private void BackClick(object sender, RoutedEventArgs e)
    {
        if (_vm == null) return;
        _vm.PressPinKey("⌫");
        RefreshState();
    }
}
