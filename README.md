# Anchor

A native procurement tracker app — track purchases, suppliers, quote comparisons, budgets, and reminders. Data stays on-device (no backend, no account).

## Android

Source lives in `android/`. Built with Kotlin + Jetpack Compose + Room (local database).

Every push builds and lints the app via GitHub Actions. To get an installable APK:

1. Go to the **Actions** tab → **Android** workflow → latest successful run.
2. Download the `anchor-debug-apk` artifact (or `anchor-release-apk-unsigned` for a release build).
3. Unzip it, transfer `app-debug.apk` to your Android phone, and open it to install (you'll need to allow "install from unknown sources" once, since it isn't from the Play Store).

## Windows

Source lives in `windows/Anchor/`. Built with C# + WinUI 3 (same data model, features, and seed content as the Android app), packaged as a signed MSIX for reliable install/update instead of a loose unpackaged .exe.

Every push builds it via GitHub Actions on a real Windows runner (this dev environment can't compile WinUI 3 itself). To install:

1. Go to the **Actions** tab → **Windows** workflow → latest successful run.
2. Download the `anchor-windows-msix` artifact and unzip it — it contains `Anchor_1.0.0.0_x64.msix` and `AnchorSigning.cer`.
3. **One-time setup:** double-click `AnchorSigning.cer` → **Install Certificate** → **Local Machine** → **Place all certificates in the following store** → **Trusted People** → Finish. (This is a self-signed cert since the app isn't going through the Microsoft Store; installing it once tells Windows to trust builds signed with it.)
4. Double-click the `.msix` file to install. Future updates: repeat steps 1–2 and 4 (skip step 3, the cert stays trusted) — it installs in place over the old version.
