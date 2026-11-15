# KeySafe - AI Agent Instructions

Welcome, AI Agent! This file contains guidelines and context to help you contribute effectively to the **KeySafe** project.

## Project Overview
KeySafe is a secure Android credential vault application built with Kotlin and Jetpack Compose. Security, data integrity, clean architecture, and privacy are the highest priorities for this project.

### Core Features
- **App Theme**: Supports Dark, Light, and System default modes with custom font styling and font size scaling.
- **Localization & Multi-Language Support**: All user-facing text (titles, labels, buttons, placeholders, dialogs, error messages, navigation items) is strictly externalized into `res/values/strings.xml`. ViewModels use a `UiText` wrapper (`UiText.StringResource`) to deliver localized error messages without holding Android `Context`.
- **Edge-to-Edge UI**: Full edge-to-edge layout support drawing seamlessly behind system bars with keyboard `imePadding()` management on scrollable forms.
- **Adaptive Layouts**: Support for Foldables, tablets, and orientation changes (Portrait/Landscape).
- **On-the-fly Encryption/Decryption**:
  - Encryption keys are **never** saved locally. 
  - Sensitive vault data stored in local SQLite database is encrypted on-the-fly using military-grade **AES-256-GCM + PBKDF2-HMAC-SHA256** key derivation with unique random Salt & IV per entry via `AESDataTransformer` (with automatic fallback support for legacy AES-CBC entries).
  - Whenever encryption or decryption is needed, the app prompts the user via a UI popup to enter their key/PIN to process it on the fly.
- **Offline Favicon Caching**: Domain favicons are fetched via Google Favicon API and aggressively cached offline using a custom Coil `ImageLoaderFactory` (`KeySafeApp.kt` & `ImageLoaderUtils.kt`).
- **Encrypted Vault Backup & Restore (`.ksvault`)**:
  - File Format: Recommended `.ksvault` binary format protected with AES-256-GCM encryption + PBKDF2-HMAC-SHA256 (100,000 iterations) key derivation and authentication tag verification.
  - File Naming Convention: Postfixed with date timestamp `yyyy-MM-dd_HH-mm-ss` (e.g., `keysafe_backup_2026-03-30_14-30-45.ksvault`).
  - Export Channels: Supports local file saving (`CreateDocument`) and cross-app sharing (`Intent.ACTION_SEND` to WhatsApp, Gmail, Drive).
  - Export Options: Export all vault items or user-selected specific credentials.
  - Import Strategies: Support for `Merge` (adds new/updated items into vault) and `Override` (clears vault and replaces completely from backup file).
- **Centralized Monetization (MainActivity)**: `MainActivity` tracks navigation destination changes to show global AdBanners at the **BOTTOM** (dashboard/lists) or **TOP** (form screens when soft keyboard is active).

---

## Architecture & Code Organization

### Technology Stack
- **Language**: Kotlin (Idiomatic, platform-agnostic domain/data structure ready for KMP/CMP)
- **UI Framework**: Jetpack Compose (Material 3 warm peach palette)
- **Navigation**: Jetpack Navigation 3 API (`NavDisplay` with `Screen` key definitions)
- **Dependency Injection**: Koin DI split into segregated modules:
  - `dataModule` (Database, Repository, DataStore Settings, AES Transformer)
  - `domainModule` (AppLockManager, UseCases)
  - `viewModelModule` (Viewmodel declarations)

### Modular Screen & ViewModel Hierarchy (`ui/screens/add/` & `ui/viewmodel/add/`)
To prevent monolithic God classes, the credential creation/editing flow is segregated by category using OOP inheritance:
- **ViewModels**:
  - `BaseAddCredentialViewModel`: Base abstract class managing common state (title, PIN, notes, favorite, security questions) and saving lifecycle.
  - `AddSiteViewModel`: Category-specific validation and `SiteCredential` mapping.
  - `AddCardViewModel`: Category-specific validation, MM/YY date range logic, and `CardCredential` mapping.
  - `AddComputerViewModel`: Category-specific validation and `ComputerCredential` mapping.
- **Screen Composables**:
  - `BaseAddCredentialScreen`: Wraps scaffold, top bar, encryption dialog, and favorite toggle.
  - `SharedFormElements`: Renders optional/collapsible PIN, Note, and Security Questions fields.
  - `AddSiteScreen`, `AddCardScreen`, `AddComputerScreen`: Render category-specific form templates.

---

## App Workflow & Lifecycle

### 1. Launch & Play Store Updates
- On cold start and resume, `MainActivity` checks for updates via `AppUpdateManager` and triggers an `IMMEDIATE` update if available.

### 2. First Launch & Optional App Lock
- **App Lock is disabled by default on first launch**. The user lands directly on `HomeScreen` without being forced to set up a PIN/Biometrics.
- If the user enables **"App Lock"** in `SettingsScreen` (and no auth method is set), `MainActivity` presents `SetupAuthScreen` prompting them to choose between "Device Lock" (Biometrics/System PIN) or creating a custom 4-8 digit "App PIN".
- When App Lock is enabled and locked, `MainActivity` intercepts navigation to present `AuthScreen` (Biometric prompt) or `PinAuthScreen` (Custom PIN pad).
- Idle lock timeouts (Immediately, 1m, 5m, 10m, 15m, 30m) are tracked via `AppLockManager` attached to `ProcessLifecycleOwner`.

### 3. Category Selection & Navigation Flow
- **Home Screen Category Chips** (`All`, `Site`, `Card`, `Computer`):
  - Selecting a category chip filters the credential list.
  - Tapping **"Add credential"** while a specific category chip (`Site`, `Card`, `Computer`) is active routes **directly** to that category's form (`AddSiteScreen`, `AddCardScreen`, or `AddComputerScreen`).
  - Tapping **"Add credential"** while the `All` chip is active opens `CategorySelectionDialog` popup for the user to select the category.

---

## Form Validation & Template Rules

### 1. Category-Specific Mandatory Fields & Visual Indicators
Mandatory fields are visually indicated with a red asterisk (`*`):
- **Site**: Site / Domain Name (`*`), Username (`*`), Password (`*`).
- **Card**: Card Name / Label (`*`), Card Type (Credit/Debit), Bank / Issuer Name (`*`).
- **Computer**: Computer Name / Label (`*`), Operating System (Windows/Mac/Linux/Android), Username (`*`), Password (`*`).

### 2. Dynamic Input Validation Rules
- **Inline Error Clearing**: Error messages clear instantly as the user types/modifies the text in the field. Clearing a field removes inline errors until validation is re-triggered on Save.
- **Domain Validation**: Real-time domain regex validation (`^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(/.*)?$`) on domain field loss of focus.
- **Card Validity Dates (MM/YY)**:
  - Input is capped at 4 digits (`MMYY`) and formatted visually as `MM/YY`.
  - Typing a digit `2-9` for the month automatically prefixes a `0` (e.g., typing `5` becomes `05`).
  - Validates month `MM` range (`01` to `12`).
  - Ensures `Start (MM/YY) < Expiry (MM/YY)` (expiry date must be after start date).

### 3. Collapsible Optional Form Sections
- **Note** and **Security Questions** are optional and **hidden by default** when adding a new credential.
- Tapping `"+ Add Note"` or `"+ Add Security Question"` expands the respective section on demand.
- When editing an existing credential that contains a note or security questions, the sections render expanded automatically.
- **Hidden Select App Feature**: The "Select app" button in `SiteTemplateForm` is hidden/commented out for future releases.

### 4. Delete Confirmation
- Credential deletion cannot occur directly. Tapping **Delete** must present an `AlertDialog` confirmation prompt (`dialog_delete_title`, `dialog_delete_message`). Deletion is only executed if the user explicitly confirms the action in the dialog.

---

## Design & UI Guidelines

### 1. Previews
Every `@Composable` screen and base component (`ui/screens`, `ui/screens/add`, `ui/base/components`, `ui/base/dialogs`, `ui/base/templates`) must include `@Preview(showBackground = true)` with mock harnesses for visual inspection in Android Studio.

### 2. Localization
Never hardcode user-visible strings in Composables or ViewModels. Use `stringResource(R.string.key)` in Composables and `UiText.StringResource(R.string.key)` in ViewModels.

---

## Agent Guidelines

### 1. Policies & Compliance
- **Copyright**: Use open-source, royalty-free, or originally created assets.
- **Play Console & AdMob**: Adhere to AdMob placement policies (do not obscure UI controls or force accidental clicks) and Google Play Developer Policies.

### 2. Security Practices
- Never log sensitive credentials, PINs, or raw database keys.
- Ensure encryption keys are processed on-the-fly and never persisted in memory or local storage.

### 3. File Modification & Safety
- Use built-in file editing tools (`replace_file_content`, `multi_replace_file_content`, `write_file`).
- Do NOT use shell scripts (`sed`, `awk`, redirection) to edit files.
