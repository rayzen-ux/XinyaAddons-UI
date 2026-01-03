# 🎯 Rianixia Settings Overlay

<div align="center">

**A powerful, system-level Android tweaking utility for advanced users**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5+-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0+-3DDC84?style=flat&logo=android&logoColor=white)](https://www.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📖 Overview

Rianixia Settings Overlay is a comprehensive dashboard and control center that provides granular control over hardware parameters, system performance, and device integrity settings. Built with modern Android development practices, it leverages root access to modify kernel and system parameters, offering a sleek Jetpack Compose interface for advanced users and custom ROM environments.

## ✨ Key Features

### 📊 **System Dashboard**
Real-time monitoring of CPU load, RAM usage, storage metrics, and battery statistics at a glance.

### ⚡ **CPU & Thermal Control**
Adjust governors, frequencies, and thermal profiles to optimize performance or battery life.

### 💾 **I/O Scheduler Management**
Configure internal and external storage schedulers with customizable read-ahead buffers.

### 🔋 **Undervolt Utility**
Fine-tune voltage settings for enhanced power efficiency (kernel support required).

### 🛡️ **Integrity & Spoofing**
Advanced tools to manage Play Integrity verdicts and device fingerprint spoofing.

### 🔌 **Battery Center**
Enforce aggressive Doze modes and manage background services for maximum battery longevity.

### 🎮 **Game Optimization**
Manage game-specific properties and performance profiles for enhanced gaming experience.

---

## 🏗️ Architecture

**Built on solid foundations:**

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3
- **Architecture Pattern:** MVVM (Model-View-ViewModel)
- **Concurrency:** Kotlin Coroutines & Flow
- **Build System:** Gradle

---

## 📁 Project Structure

```
src/main/java/com/rianixia/settings/overlay/
│
├── 🎨 ui/
│   ├── screens/          # Feature screens (CPU, Thermal, Home, etc.)
│   ├── components/       # Reusable UI components
│   └── theme/            # Application theming & colors
│
├── 🧠 viewmodel/         # State management & business logic
│
├── 📦 data/              # Repository layer for system operations
│
├── ⚙️ services/          # Background services (EnforceDozeService, etc.)
│
├── 🔐 security/          # Integrity & security management
│
└── 📄 assets/            # Configuration files (SoC, games, properties)
```

---

## 🔧 Prerequisites

- **JDK:** Version 11 or 17
- **Android Studio:** Koala or newer (recommended)
- **Target SDK:** 34 (Android 14)
- **Minimum SDK:** 26 (Android 8.0)
- **Root Access:** Required (Magisk/KernelSU)

> ⚠️ **Important:** This application requires a rooted device to function, as it modifies system files and kernel parameters.

---

## 🚀 Getting Started

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/rianixia-settings-overlay.git
cd rianixia-settings-overlay
```

### Building

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

---

## ⚠️ Safety Warning

> **CAUTION:** This application modifies system-level configurations. Improper use of voltage control or CPU settings may cause system instability, thermal issues, or boot loops. 
> 
> **Always:**
> - Create a backup before making changes
> - Test settings incrementally
> - Know how to access recovery mode
> - Use at your own risk

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues, fork the repository, and create pull requests.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

</div>
```