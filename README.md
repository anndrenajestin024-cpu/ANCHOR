# Anchor

A native procurement tracker app — track purchases, suppliers, quote comparisons, budgets, and reminders. Data stays on-device (no backend, no account).

## Android

Source lives in `android/`. Built with Kotlin + Jetpack Compose + Room (local database).

Every push builds and lints the app via GitHub Actions. To get an installable APK:

1. Go to the **Actions** tab → **Android** workflow → latest successful run.
2. Download the `anchor-debug-apk` artifact (or `anchor-release-apk-unsigned` for a release build).
3. Unzip it, transfer `app-debug.apk` to your Android phone, and open it to install (you'll need to allow "install from unknown sources" once, since it isn't from the Play Store).

## Windows

Source lives in `windows/Anchor/`. Built with C# + WinUI 3 (same data model, features, and seed content as the Android app). Ships as a framework-dependent unpackaged build: the app itself needs no installer or certificate, but it depends on Microsoft's own **Windows App Runtime**, installed once via Microsoft's official (Microsoft-signed) redistributable.

Every push builds it via GitHub Actions on a real Windows runner (this dev environment can't compile WinUI 3 itself). To install:

1. **One-time setup:** install the [Windows App SDK 1.6 runtime redistributable](https://aka.ms/windowsappsdk/1.6/latest/windowsappruntimeinstall-x64.exe) from Microsoft (signed by Microsoft, so no certificate-trust step needed). If that link doesn't work, search "Windows App SDK runtime download" — you want the 1.6.x x64 installer.
2. Go to the **Actions** tab → **Windows** workflow → latest successful run.
3. Download the `anchor-windows` artifact and unzip it.
4. Run `Anchor.exe` inside the extracted folder. Future updates: repeat steps 2–4 (skip step 1, the runtime stays installed).
