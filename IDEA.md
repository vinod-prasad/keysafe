# 💡 KeySafe — Future Product Roadmap & Feature Backlog

This document outlines the planned feature enhancements, new categories, and interactive capabilities scheduled for upcoming releases of **KeySafe**.

---

## 🎮 1. Interactive Encryption Mini-Game (Immersive Experience)
- **Goal**: Transform the full-screen immersive view into an educational, interactive encryption-based mini-game.
- **User Flow**: Tapping the KeySafe logo icon on the Home dashboard launches `ImmersiveVaultActivity` (Full-Screen Immersive Mode).
- **Game Mechanics**:
  - Educational puzzles based on cryptography concepts (e.g., Substitution Cipher breaking, AES round matching, Key-Pair matching).
  - Score tracking and zero-knowledge trivia challenges.

---

## 💳 2. Extended Banking & Card Details
- **Goal**: Provide comprehensive banking metadata storage inside the **Card** credential template.
- **New Fields To Add**:
  - **IFSC Code** (Indian Financial System Code) with auto-uppercase formatting.
  - **Bank Branch Code** & **Branch Name**.
  - **Bank Branch Address**.
  - **Primary Account Number** linked to the card.
  - **Account Type** (Savings, Current, Salary, NRI).
- **Security**: Sensitive banking numbers and codes will be encrypted using `AESDataTransformer` (AES-256-GCM + PBKDF2).

---

## 🖼️ 3. Encrypted Photo Vault (New Category: Photos / Documents)
- **Goal**: Enable users to capture and securely store sensitive physical documents (Passports, ID Cards, Driver's Licenses, Tax Documents).
- **User Flow**:
  - Add new category: **Photos / Documents**.
  - CameraX capture interface or System Image Picker.
- **Security & Storage**:
  - Image byte streams are encrypted on-the-fly via AES-256-GCM before writing to local internal storage.
  - Decrypted in memory only when viewed by the user after entering their Vault Master Key.

---

## 🔑 4. Backup Codes Category with ML Kit OCR Tool
- **Goal**: Store long 2FA recovery and backup codes for accounts (Google, GitHub, AWS, Email, Operating System recovery keys).
- **OCR Text Recognition Scanner**:
  - Integrated CameraX + ML Kit Text Recognition tool.
  - Users can point their camera at paper/screen backup codes to automatically scan, extract, and copy alphanumeric code lists into the form.
- **Security**: Backup code payloads are encrypted on-the-fly and stored in `TABLE_CREDENTIALS`.

---

## 📌 Implementation Milestones

| Milestone | Feature Module | Target Release |
| :--- | :--- | :--- |
| **Phase 1** | Card Section Banking Fields (IFSC, Branch, Account) | `v1.1.0` |
| **Phase 2** | Encryption Mini-Game inside `ImmersiveVaultActivity` | `v1.2.0` |
| **Phase 3** | Encrypted Photo & Document Category | `v1.3.0` |
| **Phase 4** | Backup Codes Category & CameraX ML Kit OCR Scanner | `v1.4.0` |
