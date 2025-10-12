# ExpenseShareApp

## Overview

**ExpenseShareApp** is an Android application designed for travelers to manage shared expenses
during trips. It allows users to create expense groups, track expenses in multiple currencies,
calculate who owes whom, and export reports. Built with modern Android practices using Kotlin,
Jetpack Compose, and Firebase, the app emphasizes modularity, scalability, and robust testing.

### Features

- User authentication (email/password, Google Sign-In) via Firebase Authentication.
- Create/join expense groups to track shared expenses.
- Add expenses with customizable split strategies (equal, percentage, custom).
- Real-time currency conversion for multi-currency support.
- Calculate balances within groups to determine who owes whom.
- Offline support for expense tracking using Firestore persistence.
- Push notifications for expense updates via Firebase Cloud Messaging.
- Export expense reports as CSV or PDF, stored in Firebase Storage.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Clean Architecture
- **Backend**: Firebase (Authentication, Firestore, Storage, Cloud Messaging)
- **Dependency Injection**: Koin
- **Asynchronous Programming**: Kotlin Coroutines, Flow/StateFlow
- **Navigation**: Jetpack Navigation Component for Compose
- **Testing**: JUnit 5, MockK, Compose Testing, Robolectric
- **Other Libraries**: Coil (image loading), Timber (logging), LeakCanary (memory leak detection)

## Project Structure

The app follows a multi-module architecture for scalability and maintainability:

- `:app`: Entry point and navigation setup.
- `:core`: Shared utilities, themes, and configurations.
- `:data`: Data layer with Firebase integration and repositories.
- `:domain`: Business logic and use cases.
- `:ui`: Feature-specific modules (e.g., `:ui:auth`, `:ui:expense`).

## Setup Instructions

### Prerequisites

- Android Studio (latest stable version)
- Firebase project with Authentication, Firestore, Storage, and Cloud Messaging enabled
- Kotlin 1.9.24 or higher
- Gradle 8.0 or higher

### Steps

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/pedrazamiguez/expense-share-app.git
   cd expense-share-app
   ```
2. **Set Up Firebase**:
    - Create a Firebase project
      at [console.firebase.google.com](https://console.firebase.google.com).
    - Download the `google-services.json` file and place it in the `:app` directory.
    - Enable Authentication (email/password, Google Sign-In), Firestore, Storage, and Cloud
      Messaging.
3. **Configure Gradle**:
    - Ensure `libs.versions.toml` is updated with the latest dependency versions.
    - Sync the project with Gradle in Android Studio.
4. **Run the App**:
    - Build and run the app on an emulator or physical device (API 24+).
    - Use Firebase emulators for local testing (optional).

## Running Tests

- **Unit Tests**: Run with `./gradlew test` to execute JUnit 5 tests for use cases and repositories.
- **UI Tests**: Run with `./gradlew connectedAndroidTest` to test Compose screens and navigation.
- **Emulator Tests**: Use Firebase Test Lab or local emulators for integration tests.

## Architecture and Design Patterns

- **MVVM with Clean Architecture**:
    - Presentation: Jetpack Compose + ViewModels
    - Domain: Use cases and domain models
    - Data: Repositories with Firebase integration
- **Design Patterns**:
    - Repository: Abstracts data sources (Firestore, local cache).
    - Strategy: Handles different expense split strategies.
    - Factory: Creates expense objects with currency conversion.
    - Singleton: Manages Firebase instances via Koin.
    - Observer: Uses StateFlow for reactive UI updates.

## Contributing

1. Fork the repository and create a feature branch (`git checkout -b feature/your-feature`).
2. Follow Kotlin coding standards and use KtLint for code style consistency.
3. Write unit and UI tests for new features.
4. Submit a pull request with a clear description of changes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For questions or feedback, reach out
via [GitHub Issues](https://github.com/<your-username>/ExpenseShareApp/issues).