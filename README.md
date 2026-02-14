# SignalSnitch

A fun, lightweight Android utility app that actively alerts you when your device loses cellular network connection. Unlike standard system behavior, SignalSnitch gives you immediate push notifications when you "go dark."

## Features

- 📶 **Real-time Monitoring**: Detects cellular network loss within 5-10 seconds
- 🔔 **High-Priority Alerts**: Lock screen notifications when connection is lost
- 📝 **Event History**: Visual log showing timestamps of network events
- 🎨 **Material 3 UI**: Clean, modern interface with emoji status indicators
- ⚡ **Background Service**: Continues monitoring even when app is minimized or phone is locked
- 🛠️ **Simple Toggle**: One-button start/stop control

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Min SDK**: Android 26 (Oreo)
- **Target SDK**: Android 34 (Upside Down Cake)

## Building

### Prerequisites

- JDK 17
- Android SDK
- Gradle (wrapper included)

### Build Instructions

```bash
# Clone the repository
git clone <your-repo-url>
cd SignalSnitch

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

The debug APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`

## CI/CD

GitHub Actions automatically builds a debug APK on every push to the `main` branch. The APK is available as a workflow artifact for 7 days.

## Permissions

The app requires the following permissions:

- `ACCESS_NETWORK_STATE`: Monitor network connectivity
- `FOREGROUND_SERVICE`: Run background monitoring service
- `POST_NOTIFICATIONS`: Display alert notifications (Android 13+)

## How It Works

1. **NetworkMonitor Service**: Uses `ConnectivityManager.NetworkCallback` to listen for cellular network events
2. **Foreground Service**: Keeps the app alive in the background with a persistent notification
3. **TRANSPORT_CELLULAR**: Specifically monitors mobile network (ignores Wi-Fi)
4. **High-Priority Notifications**: Alerts appear on lock screen when signal is lost

## Usage

1. Launch the app
2. Tap "Start Monitoring"
3. Grant notification permission if prompted
4. The app will now monitor your cellular connection
5. Toggle Airplane Mode or lose signal to test
6. Receive instant notification when connection is lost
7. View event history in the app

## Future Enhancements

- Custom alert sounds
- "Back Online" notifications
- Map view showing where signal was lost
- Automated Play Store release pipeline

## License

See PRD.md for product requirements and specifications.
