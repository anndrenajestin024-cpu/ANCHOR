using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Anchor.Data;
using Anchor.ViewModels;

namespace Anchor.Views;

public partial class RemindersView : UserControl
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

            var card = new Border { Background = Brushes.White, CornerRadius = new CornerRadius(10), Padding = new Thickness(14), Margin = new Thickness(0, 0, 0, 8) };
            var row = new Grid();
            row.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
            row.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            row.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });

            var check = new CheckBox { IsChecked = done, VerticalAlignment = VerticalAlignment.Center };
            var id = r.Id;
            check.Click += (_, _) => _vm.ToggleReminderDone(id);
            Grid.SetColumn(check, 0);

            var mid = new StackPanel { Margin = new Thickness(6, 0, 0, 0), VerticalAlignment = VerticalAlignment.Center };
            mid.Children.Add(new TextBlock { Text = r.Title, FontSize = 14, FontWeight = FontWeights.Medium, Foreground = new SolidColorBrush(done ? Color.FromArgb(255, 166, 167, 172) : Color.FromArgb(255, 61, 43, 33)) });
            mid.Children.Add(new TextBlock { Text = $"Due {Format.FDate(r.Due)}", FontSize = 12, Foreground = new SolidColorBrush(Color.FromArgb(255, 166, 167, 172)) });
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
