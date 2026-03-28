# 📺 TDTFlow — Spanish TV & Radio Streaming

[![CI](https://github.com/pedrogm/tdtflow/actions/workflows/ci.yml/badge.svg)](https://github.com/pedrogm/tdtflow/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/Gradle-9.4-blue.svg?style=flat&logo=gradle)](https://gradle.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%2F%20Android%20TV-green.svg?logo=android)](https://www.android.com)

**TDTFlow** is a modern Android application designed for streaming Spanish TDT (Television Digital Terrestre) channels and radio stations. Built from the ground up using **Jetpack Compose**, **Clean Architecture**, and the **MVI (Model-View-Intent)** pattern, it offers a seamless experience across both mobile and TV devices.

---

## ✨ Features

### 📡 Content & Streaming
- **Full TDT Access**: Includes national (RTVE: La 1, La 2, 24h, Clan, Teledeporte), regional (TV3, ETB, Canal Sur, Aragón TV), and thematic channels.
- **Radio Stations**: 40+ high-quality stations (LOS40, Cadena SER, COPE, Rock FM, Europa FM, Flaix FM, etc.).
- **High Performance**: Optimized HLS and MP3/AAC streaming powered by **AndroidX Media3 (ExoPlayer)**.

### 🧠 Smart Functionality
- **Dynamic Filtering**: Category-based navigation (General, News, Sports, Kids, Music, Regional).
- **Intelligent Search**: Real-time search with debounce to minimize UI overhead.
- **Broken Channel Detection**: Automatic detection of unplayable streams with auto-marking and easy retry mechanisms.
- **Favorites Management**: Save and manage your preferred channels across sessions.
- **Resilient Design**: Offline fallback with 50+ hardcoded channels for when the API is unreachable.

### 🎨 Modern UI/UX
- **Multi-Platform**: Fully adaptive UI for phones (Portrait/Landscape) and Android TV (D-pad optimized).
- **Material Design 3**: Modern aesthetics with dynamic colors and fluid animations.
- **Theming**: Full support for Light, Dark, and System themes.
- **Multilingual**: Localized in Spanish, English, and Catalan.

---

## 🏗 Architecture

The project follows **Clean Architecture** principles, ensuring a clear separation of concerns across three specialized modules.

### Module Breakdown
- **`:app` (Presentation)**: Jetpack Compose UI, ViewModels implementing MVI, and Hilt DI modules.
- **`:domain` (Business Logic)**: Pure Kotlin/JVM module containing UseCases, Domain Models, and Repository interfaces.
- **`:data` (Data Layer)**: Implementation of repositories, Ktor networking, caching logic, and local preferences.

### MVI (Model-View-Intent)
We use a strict MVI pattern where each ViewModel exposes a single `StateFlow` and handles actions through a single `onIntent()` entry point.

```kotlin
// Example Contract
interface MviViewModel<S, I> {
    val uiState: StateFlow<S>
    fun onIntent(intent: I)
}
```

---

## 🛠 Technical Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin 2.x (K2 Compiler) |
| **UI Framework** | Jetpack Compose (Material 3) |
| **DI** | Hilt |
| **Networking** | Ktor Client |
| **Media** | AndroidX Media3 / ExoPlayer |
| **Image Loading** | Coil |
| **Serialization** | Kotlinx Serialization |
| **Architecture** | Clean Architecture + MVI |
| **Testing** | JUnit 4, Turbine, Coroutines Test |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug or newer)
- **JDK 21** (Required for Gradle 9)
- **Android Device/Emulator**: API 24 (Nougat) or higher

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/pedrogm/tdtflow.git
   ```
2. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run unit tests to verify the setup:
   ```bash
   ./gradlew test
   ```

---

## 🧪 Testing & CI/CD

### Local Testing
The project maintains a high test coverage for business logic and state transitions:
```bash
# Run all module tests
./gradlew :app:test :domain:test :data:test
```

### GitHub Actions (CI)
Our CI pipeline ensures code quality on every push:
- **Linting**: Static analysis across all modules.
- **Unit Tests**: Verification of all UseCases and ViewModels.
- **Build**: Automated debug APK generation.

---

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Developed with ❤️ for the Android & Open Source community.*
