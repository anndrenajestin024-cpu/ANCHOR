# Anchor

A native procurement tracker app — track purchases, suppliers, quote comparisons, budgets, and reminders. Data stays on-device (no backend, no account).

## Android

Source lives in `android/`. Built with Kotlin + Jetpack Compose + Room (local database).

Every push builds and lints the app via GitHub Actions. To get an installable APK:

1. Go to the **Actions** tab → **Android** workflow → latest successful run.
2. Download the `anchor-debug-apk` artifact (or `anchor-release-apk-unsigned` for a release build).
3. Unzip it, transfer `app-debug.apk` to your Android phone, and open it to install (you'll need to allow "install from unknown sources" once, since it isn't from the Play Store).

## Windows

Source lives in `windows/Anchor/`. Built with C# + WPF (same data model, features, and seed content as the Android app) — plain .NET desktop, not WinUI3/WindowsAppSDK, so there's no MSIX packaging, no Windows App Runtime dependency, and no certificate to trust.

Every push builds it via GitHub Actions on a real Windows runner (this dev environment can't compile a Windows GUI app itself), producing a proper installer (via Inno Setup — no relation to MSIX/WindowsAppSDK). To install:

1. Go to the **Actions** tab → **Windows** workflow → latest successful run.
2. Download the `anchor-windows-installer` artifact, unzip it, and run `AnchorSetup.exe`.
3. Follow the wizard — it installs to Program Files, adds a Start Menu entry, optionally a desktop shortcut, and a normal uninstaller (Settings → Apps).

A portable no-installer build is also available as the `anchor-windows` artifact if you'd rather just unzip and run `Anchor.exe` directly.
