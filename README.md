# Kaikanakku Pattika - The Traditional Kerala Measurement App

**Kaikanakku Pattika** is a modern Android application designed to bridge the gap between traditional Kerala measurement units (Kol, Viral) and the metric system (cm).  
Built for carpenters, architects, students, and anyone working with traditional architectural plans, this app provides a **fast, accurate, and user-friendly tool** for conversions and calculations.

The app is built with a **modern, offline-first architecture** using **Java**, **AndroidX libraries**, and the **MVVM pattern**, ensuring a robust and maintainable codebase.  
It supports both **English** and **Malayalam** languages.

---

## ✨ Features
The app is packed with features designed to streamline your workflow:

### 📐 1. Unit Converter
- **Bidirectional Conversion:** Seamlessly switch between converting Centimeters to Kol/Viral and vice-versa.
- **Quick Conversions:** A recommendation system provides chips for common values (e.g., 100 cm, 5 Kol) for one-tap conversions.
- **Recent History:** The five most recent conversions are displayed directly on the screen for quick reference.

---

### 🧮 2. Unit Calculator
- **Tabbed Interface:** A clean, tab-based UI separates different arithmetic operations.
- **Add & Subtract:** Perform addition and subtraction between two different Kol/Viral/cm measurements.
- **Multiply:** Easily multiply a Kol/Viral/cm measurement by any numerical factor.

---

### 📜 3. Comprehensive History
- **Automatic Saving:** Every calculation and conversion is automatically saved to the history.
    - Duplicates and zero-value entries are intelligently ignored.

**Powerful Filtering & Sorting:**
- **Search:** Instantly find entries by searching for input or output values.
- **Sort:** Organize your history by date (newest first) or by size (ascending/descending).
- **Favorites:** Filter to show only your starred entries.

**Easy Management:**
- **Swipe to Delete:** Quickly remove an entry with a simple swipe. An "Undo" option is provided.
- **Copy & Reuse:** Tap an entry to copy the result, or tap the reuse icon to send the value back to the converter.
- **User-Friendly Timestamps:** All entries display a clearly formatted date and time (e.g., `Sep 24, 2025 at 4:27 PM`).

---

### ⚙️ 4. Advanced Settings
- **Localization:** Switch between English and Malayalam on the fly.

**Conversion Control:**
- **Precision Mode:** Enable or disable decimal values for centimeter remainders.
- **Rounding Mode:** Choose to either round or truncate decimals when precision is off.

**Data Management:**
- **Auto-Delete:** Set a custom duration (e.g., 30 days) after which old history entries are automatically deleted by a background worker.
- **Export:** Export your entire history to a `.csv` file for record-keeping or sharing.
- **Reset:** Restore all settings to their default values.

---

## 🛠️ Technical Stack & Architecture
This application is built using **modern Android development practices and libraries**.

- **Language:** Java
- **Architecture:** MVVM (Model-View-ViewModel) - A robust and scalable architecture that separates UI logic from business logic.
- **UI:** Android XML Layouts with Material Design 3 components for a clean and modern user interface.

**Core Libraries:**
- **AndroidX Libraries:** AppCompat, ViewModel, LiveData, Preference.
- **Navigation Component:** Manages all in-app navigation and argument passing between screens.
- **Room Persistence Library:** Efficient SQLite ORM for storing calculation history.
- **DataStore:** Modern, safe, asynchronous storage for user preferences and settings (replacing SharedPreferences).
- **WorkManager:** Handles background tasks such as daily auto-deletion of old history entries.
- **RxJava3:** Enables reactive programming for smooth and efficient data streams between the database and UI.

---

## 📂 Project Structure
The project follows a **feature-based package structure** for organized and maintainable code.
```
in.udhaya.kaikanakku
├── data # Manages data sources (Repository Pattern)
│ ├── db # Room Database (DAO, Entity, Database class)
│ └── repository # Single source of truth for data (History, Settings)
├── ui # UI layer (Fragments, ViewModels, Adapters)
│ ├── about
│ ├── calculator
│ ├── converter
│ ├── help
│ ├── history
│ └── settings
├── util # Utility classes for conversions, calculations, etc.
└── workers # Background tasks using WorkManager
```
###  👤 Author
*UdhayaChandra*