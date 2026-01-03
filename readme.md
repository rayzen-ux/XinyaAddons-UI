Rianixia Settings Overlay

A powerful, system-level Android tweaking utility built with Kotlin and Jetpack Compose. This application provides granular control over hardware parameters, system performance, and device integrity settings, designed for advanced users and custom ROM environments.

Description

Rianixia Settings Overlay is a comprehensive dashboard and control center for Android devices. It leverages root access to modify kernel and system parameters, offering features ranging from CPU frequency control to integrity spoofing. The application follows modern Android development practices, utilizing the MVVM architecture and Kotlin Coroutines for asynchronous system operations.

Key Features:

System Dashboard: Real-time monitoring of CPU load, RAM usage, storage, and battery statistics.

CPU & Thermal Control: Adjust governors, frequencies, and thermal profiles.

I/O Scheduler: Configure internal and external storage schedulers and read-ahead buffers.

Undervolt Utility: Fine-tune voltage settings for power efficiency (kernel dependent).

Integrity & Spoofing: Tools to manage Play Integrity verdicts and device fingerprint spoofing.

Battery Center: Enforce Doze modes and manage background services for improved battery life.

Game Optimization: Manage game-specific properties and performance profiles.

Tech Stack

Language: Kotlin

UI Framework: Jetpack Compose (Material Design 3)

Architecture: MVVM (Model-View-ViewModel)

Concurrency: Kotlin Coroutines & Flow

Android Components: Services, BroadcastReceivers, Activities

Build System: Gradle

Project Structure

The project source code (src/main/java/com/rianixia/settings/overlay) is organized as follows:

ui/: Contains all Jetpack Compose UI code.

screens/: Individual feature screens (e.g., CPUControlScreen, ThermalControlScreen, HomeScreen).

components/: Reusable UI elements (e.g., GlassComponents, BouncyLazyColumn).

theme/: Application theming and color definitions.

viewmodel/: State holders for the UI, handling business logic and communicating with the data layer (e.g., CPUViewModel, IntegritySpoofViewModel).

data/: Repository layer responsible for fetching system data and executing shell commands (e.g., CPURepository, DeviceRepository).

services/: Background services, such as the EnforceDozeService.

security/: Modules related to device integrity and security management (IntegrityManager).

assets/: Configuration files for SoCs, game lists, and device properties (soclist.json, gamelist.txt).

Prerequisites

To build and run this project, you need:

JDK: Version 11 or 17.

Android Studio: Koala or newer recommended.

Android SDK: Target SDK 34 (Android 14) / Min SDK 26 (Android 8.0).

Root Access: The compiled application requires a rooted device (Magisk/KernelSU) to function correctly, as it modifies system files and kernel parameters.

Installation

Clone the repository:

git clone [https://github.com/yourusername/rianixia-settings-overlay.git](https://github.com/yourusername/rianixia-settings-overlay.git)
cd rianixia-settings-overlay


Open in Android Studio:
Open Android Studio and select "Open an existing project," pointing to the cloned directory.

Sync Gradle:
Allow Android Studio to download dependencies and sync the project.

Building

You can build the project using the Gradle wrapper included in the root directory.

Build Debug APK:

./gradlew assembleDebug


Run Tests:

./gradlew test
./gradlew connectedAndroidTest


Usage

After installing the APK on a rooted device:

Grant Root Permissions when prompted upon first launch.

Navigate through the Home Dashboard to view system status.

Use the Performance tab to adjust CPU, I/O, and Thermal settings.

Access the System tab for Integrity Spoofing and Battery optimizations.

Warning: This application modifies system-level configurations. Improper use of voltage control or CPU settings may cause system instability or boot loops. Use with caution.
