# ExpenseShareApp

## Overview

**ExpenseShareApp** is a modular Android application designed for travelers to manage shared
expenses efficiently. It allows users to create expense groups, track spending in multiple
currencies, calculate debts, and sync data across devices.

Built with modern Android practices—including **Jetpack Compose**, **Clean Architecture**, and *
*Offline-First** principles—the app serves as a reference for scalable, multi-module Android
development.

### ✨ Key Features

* **User Authentication**: Secure login (Email/Password) via Firebase Authentication.
* **Group Management**: Create and join expense groups with unique invite codes.
* **Smart Expense Tracking**: Support for custom split strategies (Equal, Percentage, Shares).
* **Multi-Currency Support**: Real-time currency conversion using Open Exchange Rates.
* **Debt Simplification**: Automatically calculates "Who owes Whom" balances.
* **Offline-First**: Full functionality without internet; syncs automatically when online.
* **Push Notifications**: Instant updates when members add or modify expenses.

## 🛠️ Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/) (100%)
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3 Expressive
  Design)
* **Architecture**: Clean Architecture + MVVM + MVI patterns
* **Dependency Injection**: [Koin](https://insert-koin.io/)
* **Asynchronous Programming**: Coroutines & Flow
* **Local Data**: [Room Database](https://developer.android.com/training/data-storage/room) (Single
  Source of Truth)
* **Remote Data**: [Retrofit](https://square.github.io/retrofit/) (Currency APIs)
* **Backend (BaaS)**: Firebase (Auth, Firestore, Cloud Messaging)
* **Navigation**: Jetpack Navigation Compose (Feature-based modular navigation)

## 📂 Project Structure

The app follows a strict **Multi-Module** architecture to ensure separation of concerns and faster
build times:

* **`:app`**: The application entry point. Wiring and DI setup.
* **`:core`**: Shared foundational components.
* **`:core:common`**: Utilities, Constants, and DataStore preferences.
* **`:core:design-system`**: Reusable UI components (`ExpressiveFab`, `DynamicTopAppBar`), Themes,
  and the `ScreenUiProvider` system.


* **`:data`**: The Data Layer implementation.
* **`:data:local`**: Room Database entities and DAOs.
* **`:data:remote`**: Retrofit services for external APIs.
* **`:data:firebase`**: Firestore and Auth implementations.


* **`:domain`**: Pure Kotlin business logic (Use Cases, Models, Repository Interfaces).
* **`:features`**: Standalone feature modules containing UI and ViewModels.

## 🚀 Setup Instructions

### Prerequisites

* Android Studio Koala or newer.
* JDK 17.
* A Firebase Project with Authentication (Email) and Firestore enabled.
* An API Key from [Open Exchange Rates](https://openexchangerates.org/) (optional, for currency
  features).

### Steps

1. **Clone the Repository**:

```bash
git clone https://github.com/pedrazamiguez/expense-share-app.git
cd expense-share-app

```

2. **Firebase Configuration**:

* Go to the [Firebase Console](https://console.firebase.google.com).
* Create a project and add an Android app (package: `es.pedrazamiguez.expenseshareapp`).
* Download `google-services.json` and place it in the `app/` directory.


3. **API Keys (Secrets)**:

* Add your Open Exchange Rates key to your `local.properties`:

```properties
OPEN_EXCHANGE_RATES_APP_ID="your_api_key_here"

```

4. **Build & Run**:

* Sync Gradle files.
* Select the `app` configuration and run on an Emulator (API 26+ recommended).

## 🧪 Testing

The project uses a comprehensive testing strategy:

* **Unit Tests**: JUnit 5 & MockK for Domain and Data layers.

```bash
./gradlew test

```

* **UI Tests**: Compose Testing for Screens and Navigation flows.

```bash
./gradlew connectedAndroidTest

```

## 📐 Architecture & Patterns

This project adheres to the **"Strict Visibility"** principle:

1. **Features** cannot see other **Features** (they communicate via `:domain`).
2. **Features** cannot see **Data** implementation details (only Domain interfaces).
3. **App** module is the only one that sees everything to wire up the Dependency Injection.

**Key Patterns:**

* **Navigation Discovery**: Features expose `NavigationProvider` interfaces so the App module can "
  plug them in" dynamically.
* **Repository Pattern**: Mediates between Local (Room) and Cloud (Firestore) data sources.
* **ScreenUiProvider**: Decouples the Main Activity's Scaffold (TopBar/FAB) from individual screens.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
