# SignalSnitch - Product Requirements Document (PRD)

## 1. Overview

**SignalSnitch** is a fun, lightweight Android utility app designed for the Pixel 8 Pro (and other modern Android devices). Its primary purpose is to alert the user immediately when their device loses cellular network connection (Voice/SMS/Data). Unlike standard system behavior which is passive, SignalSnitch actively notifies the user via a push notification, ensuring they are aware they have "gone dark."

## 2. Goals & Success Criteria

### 2.1 Goals

- **Reliability:** Accurately detect when the cellular network is lost, even if the app is in the background or the screen is off.
- **Simplicity:** A clean, single-screen interface that is easy to understand.
- **Fun:** A playful user experience (e.g., "Sad Phone" icon when offline).
- **Automation:** Automatically start monitoring on boot (optional but preferred) or via a simple toggle.
- **Deployable:** Easy to build and install via GitHub Actions.

### 2.2 Success Criteria

- **Detection:** The app triggers an alert within 5-10 seconds of toggling "Airplane Mode" or removing the SIM card (simulated network loss).
- **Notification:** A high-priority notification appears on the lock screen when connection is lost.
- **Background:** The app continues to monitor network state when minimized or when the phone is locked.
- **Logging:** A visible log in the app shows timestamps of "Lost" and "Restored" events.
- **CI/CD:** A GitHub Action successfully builds a debug APK that can be downloaded and installed on a Pixel 8 Pro.

## 3. Technical Requirements

### 3.1 Tech Stack

- **Language:** Kotlin (Latest stable)
- **UI Framework:** Jetpack Compose (Material 3)
- **Minimum SDK:** Android 26 (Oreo) - Ensures good background execution support.
- **Target SDK:** Android 34 (Upside Down Cake) - Optimized for Pixel 8 Pro.
- **Architecture:** MVVM (Model-View-ViewModel) for clean separation of concerns.

### 3.2 Key Components

1.  **NetworkMonitor:**
    - Uses `ConnectivityManager.NetworkCallback` to listen for `onLost` and `onAvailable` events.
    - Specifically monitors `NetworkCapabilities.TRANSPORT_CELLULAR`.
    - Ignores Wi-Fi state (User wants to know about _Mobile Network_ loss specifically).

2.  **Foreground Service:**
    - A `Service` running in the foreground with a persistent notification ("SignalSnitch is watching...").
    - This is required by Android to prevent the system from killing the app in the background.

3.  **UI (MainActivity):**
    - **Status Indicator:** Large Icon/Emoji (e.g., 📶✅ vs 📵❌).
    - **Control:** A big "Start/Stop Monitoring" button.
    - **History:** A lazy column (list) showing recent logs (e.g., "14:05: Lost Connection", "14:07: Restored").

4.  **Notifications:**
    - **Channel:** High Importance channel for "Network Alerts".
    - **Action:** Tapping the notification opens the app.

## 4. CI/CD & Deployment

- **Platform:** GitHub Actions.
- **Trigger:** Push to `main` branch.
- **Steps:**
  1.  Checkout Code.
  2.  Set up JDK 17.
  3.  Run Unit Tests (`./gradlew test`).
  4.  Build Debug APK (`./gradlew assembleDebug`).
  5.  Upload APK as Artifact (retention: 7 days).
- **Play Store:** Configuration placeholders included for future automation (Fastlane).

## 5. Future Scope (Post-MVP)

- Custom alert sounds.
- "Back Online" notification.
- Map view of where signal was lost.
- Automated Play Store release pipeline.
