# Anchor

A native procurement tracker app — track purchases, suppliers, quote comparisons, budgets, and reminders. Data stays on-device (no backend, no account).

## Android

Source lives in `android/`. Built with Kotlin + Jetpack Compose + Room (local database).

Every push builds and lints the app via GitHub Actions. To get an installable APK:

1. Go to the **Actions** tab → **Android** workflow → latest successful run.
2. Download the `anchor-debug-apk` artifact (or `anchor-release-apk-unsigned` for a release build).
3. Unzip it, transfer `app-debug.apk` to your Android phone, and open it to install (you'll need to allow "install from unknown sources" once, since it isn't from the Play Store).

## Windows

Source lives in `windows/Anchor/`. Built with C# + WinUI 3 (same data model, features, and seed content as the Android app).

Every push builds it via GitHub Actions on a real Windows runner (this dev environment can't compile WinUI 3 itself). To get a runnable build:

1. Go to the **Actions** tab → **Windows** workflow → latest successful run.
2. Download the `anchor-windows` artifact and unzip it.
3. Run `Anchor.exe` inside the extracted folder — it's a self-contained build, no separate .NET install needed.
