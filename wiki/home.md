# Welcome to the SplitTrip Wiki

**SplitTrip** is a modular Android application designed for travelers to manage shared expenses efficiently. It focuses on Clean Architecture, modularity, and modern Android development practices.

## 🎯 Project Goal

To provide a seamless experience for creating expense groups, tracking spending in multiple currencies, and calculating debts/balances between users, with offline support and cloud synchronization.

## 🏗️ Architecture Overview

The project follows **Clean Architecture** with **MVVM** and a **multi-module** structure:

* **`:app`**: Entry point and dependency injection setup.
* **`:core`**: Shared components including:
* **`:core:design-system`**: UI components, themes, and the `ScreenUiProvider` system.
* **`:core:common`**: Shared utilities and constants.


* **`:domain`**: Pure Kotlin business logic (Models, UseCases, Repository Interfaces).
* **`:data`**: Data layer implementation split into specific data sources:
* **`:data:firebase`**: Cloud synchronization and authentication.
* **`:data:local`**: Local persistence using Room.
* **`:data:remote`**: Network operations (e.g., currency exchange rates).


* **`:features`**: Feature-specific modules containing UI and presentation logic (e.g., `:features:expenses`, `:features:settings`, `:features:balances`, `:features:groups`).

## 🛠️ Key Technologies

* **Language**: Kotlin
* **UI**: Jetpack Compose (Material 3)
* **DI**: Koin
* **Async**: Coroutines & Flow
* **Navigation**: Jetpack Navigation Compose
* **Backend**: Firebase (Auth, Firestore, Messaging) + Room (Local Cache)
* **Network**: Retrofit

## 💡 Domain Concepts

* **[Subunits & Group Composition](sub-units-and-group-composition.md)**: How travel groups are composed of couples, families, and solo travelers — and how this affects contributions, withdrawals, expense splitting, and balance calculation.

## 📚 Service & Component Catalog

* **[Core Services & Components Catalog](core-services-catalog.md)**: Comprehensive reference of all reusable UI components, formatters, domain services, and preview utilities. **Check here before creating new code** to avoid duplication.

## 🚀 Getting Started

Check the sidebar to navigate through specific architectural concepts like our **ScreenUiProvider** system, Navigation strategies, and UI patterns.
