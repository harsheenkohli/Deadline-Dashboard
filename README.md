# Deadline Dashboard

This is an Android application designed to help students manage their assignment deadlines effectively. It provides a clear, organized, and visually intuitive interface to track upcoming assignments, monitor progress, and stay on top of their workload.

## Features

*   **Dashboard:** A central hub to view all your assignments, sorted by urgency, deadline, subject, or weightage.
*   **Weekly Overview:** A comprehensive view of your upcoming week, including a timeline, priority heatmap, and subject-wise load.
*   **Weekly Summary:** A summary of your performance, including a completed vs. pending breakdown, subject difficulty heatmap, and more.
*   **Add/Edit Assignments:** Easily add new assignments or edit existing ones with a clean and intuitive user interface.
*   **Customizable Themes:** Personalize your experience with a variety of color themes.
*   **Notifications:** Get timely reminders for your upcoming deadlines.

## Tech Stack

*   **Kotlin:** The primary programming language for the application.
*   **Jetpack Compose:** The modern UI toolkit for building native Android UI.
*   **Room:** A persistence library to manage the app's local database.
*   **ViewModel:** A component of the Android Architecture Components to manage UI-related data.
*   **Navigation Component:** To handle in-app navigation.
*   **Coroutines:** For managing background threads and asynchronous operations.

## Permissions

This app requires the following permissions to function correctly:

*   `POST_NOTIFICATIONS`: To display reminder notifications for your assignments.
*   `SCHEDULE_EXACT_ALARM`: To schedule and deliver these reminders at the correct time.

## Libraries Used

This project utilizes a variety of libraries to provide a robust and modern user experience. Key libraries include:

*   **AndroidX Libraries:**
    *   `core-ktx`: Core Kotlin extensions.
    *   `appcompat`: Provides backward-compatible versions of Android UI components.
    *   `lifecycle-runtime-ktx`: Manages app lifecycle and processes.
    *   `activity-compose`: For integrating Jetpack Compose with Android Activities.
    *   `datastore-preferences`: For simple, asynchronous key-value storage.
    *   `room`: For robust, local database storage.
    *   `glance-appwidget`: For creating modern home screen widgets.

*   **Jetpack Compose:**
    *   `compose-bom`: Bill of Materials to manage Compose library versions.
    *   `ui`, `ui-graphics`, `ui-tooling-preview`: Foundational UI components.
    *   `material3`, `material-icons-extended`: Implements Material Design 3.
    *   `navigation-compose`: For navigating between screens.
    *   `lifecycle-viewmodel-compose`: For integrating ViewModels with Compose.
    *   `runtime-livedata`: For observing `LiveData` objects in Compose.

*   **Other Libraries:**
    *   `MPAndroidChart`: For creating beautiful and informative charts.
    *   `kotlinx-coroutines`: For managing concurrency and asynchronous code.

This list provides an overview of the core dependencies but is not exhaustive.

## How to Build an APK

You can build an APK of the project using either Android Studio or the command line.

### Using Android Studio

1.  Go to **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
2.  Android Studio will build the project, and a notification will appear when the build is complete.
3.  Click the **locate** link in the notification to find the generated APK file.

### Using the Command Line

1.  Open a terminal in the root directory of the project.
2.  Run the following command to build a debug APK:

    ```
    ./gradlew app:assembleDebug
    ```

3.  The generated APK will be located in `app/build/outputs/apk/debug/`.
