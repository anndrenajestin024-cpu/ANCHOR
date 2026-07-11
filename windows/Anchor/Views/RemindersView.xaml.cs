using Anchor.Data;
using Anchor.ViewModels;
using Microsoft.UI.Text;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;

namespace Anchor.Views;

public sealed partial class RemindersView : UserControl
{
    private readonly AnchorViewModel _vm;

    public RemindersView(AnchorViewModel vm)
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
        RemindersList.Children.Clear();
        foreach (var r in _vm.Data.Reminders.OrderBy(x => x.Due))
        {
            var done = r.Status == "Completed";
            var badge = done ? "Done" : r.Status == "Overdue" ? "Overdue" : "Upcoming";

            var card = new Border { Background = new SolidColorBrush(Microsoft.UI.Colors.White), CornerRadius = new CornerRadius(10), Padding = new Thickness(14) };
            var row = new Grid();
            row.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            row.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            row.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });

            var check = new CheckBox { IsChecked = done };
            var id = r.Id;
            check.Click += (_, _) => _vm.ToggleReminderDone(id);
            Grid.SetColumn(check, 0);

            var mid = new StackPanel { Margin = new Thickness(6, 0, 0, 0) };
            mid.Children.Add(new TextBlock { Text = r.Title, FontSize = 14, FontWeight = FontWeights.Medium, Foreground = new SolidColorBrush(done ? Windows.UI.Color.FromArgb(255, 166, 167, 172) : Windows.UI.Color.FromArgb(255, 61, 43, 33)) });
            mid.Children.Add(new TextBlock { Text = $"Due {Format.FDate(r.Due)}", FontSize = 12, Foreground = new SolidColorBrush(Windows.UI.Color.FromArgb(255, 166, 167, 172)) });
            Grid.SetColumn(mid, 1);

            var badgeText = new TextBlock { Text = badge, FontSize = 11, VerticalAlignment = VerticalAlignment.Center };
            Grid.SetColumn(badgeText, 2);

            row.Children.Add(check);
            row.Children.Add(mid);
            row.Children.Add(badgeText);
            card.Child = row;
            RemindersList.Children.Add(card);
        }
    }
}
