# KeySafe - AI Agent Instructions

Welcome, AI Agent! This file contains guidelines and context to help you contribute effectively to the **KeySafe** project.

## Project Overview
KeySafe is an Android application focused on secure credential and password management (a digital vault). Security, data integrity, and privacy are the highest priorities for this project.

### Features
- **App Theme**: Supports Dark, Light, and System default modes.
- **Localization**: Multi-language support (English and Hindi).
- **Edge-to-Edge UI**: Full edge-to-edge layout support, seamlessly drawing behind system bars.
- **Adaptive Layouts**: Full support for Foldable screens, as well as seamless transitions between Portrait and Landscape orientations.
- **Custom Typography**: Support for custom fonts across the application UI.
- **On-the-fly Encryption/Decryption**:
  - The encryption key is **never** saved locally. 
  - Sensitive data is strictly stored as encrypted text. 
  - Whenever encryption or decryption is required, the app must prompt the user via a UI popup to enter their PIN/Master Password to process it on the fly.
  - Encryption logic must be fully supported across all target platforms (Android/iOS/Web).
- **Secure Sharing**: Users can share credentials with others via email, WhatsApp, SMS, or other mediums. The app will first decrypt the selected data, then re-encrypt it using the recipient's key. This generates a protected data file that can be transmitted through any medium but can only be accessed/imported by the KeySafe app.
- **Export/Import**: Users can export and import their encrypted vault data (credentials, passwords, pins, etc.) securely across devices.
- **Centralized Monetization (MainActivity)**: The `MainActivity` must include a dedicated `BannerSpace` (Top/Bottom) and a navigation listener. This allows centralized control of ad monetization across screen changes without duplicating ad logic in every individual screen.

### App Workflow & Lifecycle
1. **Launch & Updates**: On cold start or resume, the app instantly checks for Google Play updates via `AppUpdateManager` and forces an `IMMEDIATE` update if available.
2. **First-Time Setup**: If no authentication method is registered locally, the app displays `SetupAuthScreen` prompting the user to choose between "Device Lock" (Biometrics/System PIN) or creating a custom 4-8 digit "App PIN".
3. **Authentication**:
   - If the app is locked, `MainActivity` intercepts the navigation and shows `AuthScreen` (Biometric prompt) or `PinAuthScreen` (Custom PIN pad).
   - Once successfully authenticated, the UI unlocks and loads the `AppNavGraph`.
4. **Navigation & Ad Routing**:
   - The user lands on `HomeScreen`. The `BottomNavigationBar` is only visible on `Home`, `Checkup`, and `Settings`.
   - The `MainActivity` globally tracks screen changes to display the AdBanner at the **BOTTOM** (for lists/dashboards) or **TOP** (for forms like `AddCredential` where the keyboard opens).
5. **Idle Lock & Backgrounding**: 
   - Managed via `AppLockManager` attached to `ProcessLifecycleOwner`.
   - When the app is minimized to the background, it records a timestamp. Upon returning to the foreground, if the idle time exceeds the user-configured timeout (e.g., Immediately, 5m, 10m, 30m), the app forces a re-authentication.

### Screens(UI interface)
- **Registration/Login**: Authentication screen for verifying user access to the vault (e.g., Master Password, Biometrics).
- **Home/Dashboard**: Main list screen with a top search bar, category chips/filters (e.g., All, Passwords, Passkeys), an "Add password" action, and a list of saved credentials grouped by site/app (displaying account count). Includes a Bottom Navigation Bar (Accounts, Checkup, Settings).
- **Add/Edit Credential**: Form screen to add or edit a password. Features fields for "Site" (with a "Select app" button to pick from installed apps), Username, Password (with eye toggle for visibility), "Suggest strong password" action, and Notes.
- **App Selector (Dialog/BottomSheet)**: A scrollable list of installed apps on the device (with app icons) to quickly select an app when adding a new credential.
- **Search Screen**: Real-time filtering screen where users can search for credentials, showing results matching the search query immediately. The keyboard auto-focuses on entry.
- **Credential Details**: A screen displaying all accounts linked to a specific site/app. Features copy-to-clipboard icons for username and password, a view password toggle, and buttons to Share, Edit, and Delete the credential.
- **Settings Screen**: Contains global toggles for App Lock, Idle Timeout durations, Theme Mode overrides (System/Dark/Light), Font Styles (utilizing Google Downloadable Fonts), and Font Size scaling modifiers.

## Tech Stack & Architecture
*(Note: Technology choices must remain compatible with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP) to support future migration)*
- **Language**: Kotlin
- **Platform**: Android SDK (Structured for future KMP/CMP migration)
- **Architecture**: MVVM / Clean Architecture (Domain and Data layers must be platform-agnostic)
- **UI**: Jetpack Compose only (No XML for screen UI; build with CMP compatibility in mind)
- **Navigation**: Nav3 API (Latest) / KMP-compatible navigation (e.g., JetBrains Navigation Compose)

- **Asynchronous Programming**: Kotlin Coroutines and Flow
- **Dependency Injection**: Koin or Kotlin-Inject (Avoid Hilt/Dagger as they are Android/JVM-specific and break KMP)
- **Network API**: Ktor Client (KMP compatible)
- **Local Storage/Security**: Use KMP-friendly solutions where possible (e.g., Multiplatform Settings, SQLDelight, Room KMP, or abstracted secure storage interfaces)
- **Business Logic**: Core logic must be platform-agnostic and fully supported across Android, iOS, and Web.
- **Data and Domain Modules**: Must remain completely decoupled from platform-specific APIs so they can be reused directly by all targets.


## Agent Guidelines

### 1. Adhere Policies
- **CopyRight**: Don't create and use any copyright content and images in the application. Ensure all assets are open-source, royalty-free, or originally created.
- **Text Content**: Avoid text content for misleading labels and messages in the app. Be clear, transparent, and accurate in all user-facing text.
- **AdMob Policy**: Strictly adhere to AdMob placement policies. Do not place ads that obscure content, encourage accidental clicks, or violate content restrictions.
- **Google Play Console**: Ensure all app features, permissions, and data safety measures comply with Google Play Developer Policies. Do not request unnecessary sensitive permissions.
- **Monetization platform**: Ensure compliance with Google AdMob/AdSense or any other integrated monetization platforms. Avoid fraudulent ad requests, intrusive placements, or artificial impressions.

### 2. Security First
- Never expose sensitive information, keys, or credentials in logs, UI, or unencrypted storage.
- Always use encrypted storage (e.g., EncryptedSharedPreferences, Android Keystore, SQLCipher) for storing user credentials or master passwords.
- Audit any third-party dependencies before adding them to ensure they do not introduce security vulnerabilities.

### 3. Code Quality & Style
- Write idiomatic Kotlin code. Use Kotlin standard library functions (e.g., `let`, `apply`, `map`) where appropriate.
- Follow the official Kotlin coding conventions.
- Ensure all public functions and classes have appropriate KDoc comments.
- Keep functions small and focused on a single responsibility.

### 4. File Modifications
- When modifying files, prefer targeted surgical edits (`replace_file_content` or `multi_replace_file_content`) over rewriting entire files to minimize the risk of destroying unsaved user changes.
- Ensure all imports are correctly managed after adding or removing code.

### 5. Testing
- Ensure that any new business logic is covered by Unit Tests.
- Mock external dependencies where appropriate.
- Verify security-critical logic strictly through tests.

### 6. Build & Context
- Do not run shell scripts that modify files on the disk directly (no `sed`, `awk`, etc.). Rely on the IDE's built-in file management tools.
- Before proposing massive architectural changes, verify the current project structure using `find_files` or `code_search` to align with the existing patterns.

---
*Note to Agents: Always double-check your code for potential memory leaks, Adhere Policies, lifecycle mismatches, and thread-safety issues.*