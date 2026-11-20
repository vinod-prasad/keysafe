# 🔐 KeySafe — Secure Android Credential Vault (v1.0.0)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg?logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material%203-4285F4.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Navigation 3](https://img.shields.io/badge/Navigation-3-3DDC84.svg?logo=android)](https://developer.android.com/guide/navigation)
[![Encryption](https://img.shields.io/badge/Security-AES--256--GCM-red.svg?logo=letsencrypt)](https://en.wikipedia.org/wiki/Galois/Counter_Mode)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**KeySafe** is an ultra-secure, privacy-first Android credential vault application built with 100% Kotlin and Jetpack Compose. Designed around a **Zero-Knowledge Architecture**, sensitive vault entries are encrypted on-the-fly directly in volatile memory using **AES-256-GCM** and **PBKDF2-HMAC-SHA256** key derivation. Encryption keys are **never stored locally** on the device or transmitted to any server.

---

## 🌟 Core Features

### 🛡️ 1. Zero-Knowledge Encryption
- **On-the-Fly Processing**: The Vault Master Key is processed strictly in temporary RAM memory during encryption/decryption and is wiped immediately after use.
- **AES-256-GCM + PBKDF2-HMAC-SHA256**: Sensitive fields (usernames, passwords, PINs, notes, card numbers, CVVs, security answers) are transformed into Base64 ciphertexts with unique random 16-byte Salts and 12-byte IVs per entry.

### 📦 2. Encrypted Vault Backup & Restore (`.ksvault`)
- **Container Encryption**: Export backups into an authenticated, encrypted binary format (`.ksvault`) derived from a custom Backup File Password via **PBKDF2 (100,000 iterations)**.
- **Progressive UI Disclosure**: The Import tab progressively reveals configuration choices only after a valid `.ksvault` file is selected.
- **Merge & Override Strategies**: Safely merge incoming backups with local data or execute a full override (guarded by a data deletion confirmation dialog).

### 🎨 3. Material 3 & M3 Expressive UI
- **Warm Peach Theme**: Fully customized Material 3 color palette supporting Dark, Light, and System modes with font scaling and custom typography (Google Fonts).
- **M3 Expressive Components**: Integrates the latest Compose Material 3 1.5.0 expressive features, including smooth `WavyCircularProgressIndicator` loading animations.
- **Edge-to-Edge Experience**: Full edge-to-edge layout drawing behind system bars with smooth keyboard inset management.

### 🧭 4. Jetpack Navigation 3 Architecture
- Built on Jetpack Navigation 3 (`NavDisplay`) with 5 top-level destinations in the Bottom Navigation Bar:
  1. 🔑 **Accounts** (Home Dashboard with category filters: All, Sites, Cards, Computers).
  2. ⭐ **Favorites** (Quick access starred credentials).
  3. ☁️ **Backup** (Vault Export & Import management).
  4. ⚙️ **Settings** (App Lock, Theme, Font Style, Idle Timeouts, Replay Tour).
  5. ❓ **Support** (Help, FAQs, and Email Customer Support).

### 🖼️ 5. Custom Profile Avatar & Interactive Crop
- Select any photo (JPEG, PNG, WEBP) via Android System Photo Picker.
- Interactive **Profile Image Crop Dialog** featuring a circular viewport frame, touch drag positioning, and zoom slider (`0.5x` to `3.5x`).
- Cropped avatars are saved locally to `context.filesDir/profile_avatar.png` and rendered seamlessly across the app.

### 📱 6. Onboarding Walk-Through & Replay
- 3-step `HorizontalPager` walk-through introducing new users to KeySafe's security model.
- Replay the entire walk-through anytime by selecting **"Take App Tour"** in Settings.

### 💳 7. Category Templates & "Add More Info" Pills
- Dedicated form templates for **Sites**, **Cards**, and **Computers** with real-time domain RegExp checking (`^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(/.*)?$`) and MM/YY card validity date logic.
- Collapsible **"Add more info ˅"** header containing soft warm peach pill chips (`[🔑 Item PIN]`, `[📝 Note]`, `[❓ Security Questions]`) that expand fields on demand.

---

## 🔑 Key & PIN Specification Reference

| Key / PIN Type | Icon | Length Criteria | Storage & Security Rule |
| :--- | :--- | :--- | :--- |
| **Vault Master Key** | `Icons.Default.Key` | Min 4 chars *(8+ recommended)*, Unlimited Max | Processed strictly in memory for DB & `.ksvault` operations. **Never saved locally**. |
| **App Unlock PIN** | `Icons.Default.Lock` | 4 to 8 numeric digits | Optional local PIN used to unlock the app UI on cold start or idle timeout. |
| **Backup File Password** | `Icons.Default.Lock` | Min 4 chars *(8+ recommended)*, Unlimited Max | Password set during export to encrypt the `.ksvault` backup container. |
| **Item Security PIN** | `label_item_pin` | Optional, User-defined | Saved value stored inside a credential record (e.g. 4-digit ATM card PIN). |

---

## 🏗️ Architecture & Code Organization

KeySafe follows **Clean Architecture** principles decoupled into segregated packages:

```
com.appnest.safe.credentials.vault
├── data
│   ├── db (KeySafeDatabaseHelper SQLite OpenHelper)
│   ├── dto (BackupPayload JSON Data Transfer Objects)
│   ├── repository (CredentialRepositoryImpl)
│   ├── security (AESDataTransformer, VaultBackupManager)
│   └── SettingsManager (DataStore Preferences)
├── domain
│   ├── model (Credential, SiteCredential, CardCredential, ComputerCredential)
│   ├── repository (CredentialRepository Interface)
│   └── usecase (SaveCredentialUseCase, DecryptCredentialUseCase, etc.)
├── navigation
│   ├── NavGraph (AppNavDisplay)
│   ├── NavigationState (Navigation 3 backstack revision tracking)
│   └── Screen (Serializable NavKey definitions & BottomNavItem)
└── ui
    ├── base (components, dialogs, templates)
    ├── screens (Home, Favorites, Backup, Settings, Support, Search, Details)
    │   ├── add (BaseAddCredentialScreen, SharedFormElements)
    │   └── onboarding (OnboardingScreen)
    ├── theme (Color, Theme, Type)
    └── viewmodel (HomeViewModel, BackupViewModel, AddCredentialViewModels)
```

---

## 🗄️ Database Schema & Encryption Mapping

### `TABLE_CREDENTIALS`
| Column Name | Storage Format | Encryption Status |
| :--- | :--- | :--- |
| `id` | String (UUID) | Plaintext (Primary Key) |
| `category` | String | Plaintext (`Site`, `Card`, `Computer`) |
| `site_or_app` | String | Plaintext (Title / Domain) |
| `site_logo` | String | Plaintext (Logo URL) |
| `username` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2) |
| `password` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2) |
| `pin` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2, Nullable) |
| `note` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2, Nullable) |
| `is_favorite` | Integer | Plaintext (0 or 1) |
| `card_type` | String | Plaintext (`Credit`, `Debit`, Nullable) |
| `card_issuer` | String | Plaintext (Bank Name, Nullable) |
| `card_name_on_card` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2, Nullable) |
| `card_number` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2, Nullable) |
| `card_validity_start` | String | Plaintext (MM/YY, Nullable) |
| `card_validity_end` | String | Plaintext (MM/YY, Nullable) |
| `card_cvv` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2, Nullable) |
| `os_type` | String | Plaintext (`Windows`, `Mac`, `Linux`, `Android`, Nullable) |
| `created_at` / `updated_at` | Long | Plaintext (Epoch Timestamps) |

### `TABLE_SECURITY_QUESTIONS`
| Column Name | Storage Format | Encryption Status |
| :--- | :--- | :--- |
| `id` | String (UUID) | Plaintext (Primary Key) |
| `credential_id` | String (UUID) | Plaintext (Foreign Key to `credentials.id` ON DELETE CASCADE) |
| `question` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2) |
| `answer` | String | **Base64 Encrypted** (AES-256-GCM + PBKDF2) |

---

## 🚀 Building & Running

### Prerequisites
- **Android Studio**: Ladybug / 2024.2.1 or newer
- **JDK**: Java 17+
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 37 (Android 15/16)

### Building via Terminal
```bash
# Clone the repository
git clone https://github.com/your-username/KeySafe.git
cd KeySafe

# Build Debug APK
./gradlew assembleDebug

# Build Optimized Release APK (R8 Minified)
./gradlew assembleRelease
```

### ProGuard / R8 Optimization & APK Size
- **Debug APK**: `~26 MB` (Includes un-shrunk debug symbols and full extended icon set for live debugging).
- **Release APK**: `~5 - 8 MB` (R8 code minification & `shrinkResources` strip 99% of unused vector icons and classes).
- **Play Store Bundle (`.aab`)**: `~3 - 5 MB` (Splits binaries per device CPU architecture).

---

## 📄 License

```
Copyright (c) 2026 KeySafe

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction rights.
```
