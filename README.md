# 📺 TDTFlow — Spanish TV & Radio Streaming

[![CI](https://github.com/pedrogm/tdtflow/actions/workflows/ci.yml/badge.svg)](https://github.com/pedrogm/tdtflow/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-9.1.0-blue.svg?style=flat&logo=gradle)](https://developer.android.com/build)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Phone%20·%20Tablet%20·%20TV%20·%20Auto-green.svg?logo=android)](https://www.android.com)

**TDTFlow** is a modern Android application for streaming Spanish free-to-air television (TDT) and radio stations — no subscription, no sign-up required. Built with **Jetpack Compose**, **Clean Architecture**, and **MVI**, it delivers a consistent, fluid experience across every Android form factor.

---

## 📸 Screenshots

| Phone | Tablet | Android TV |
|:---:|:---:|:---:|
| ![Phone](app/screens/phone_portrait.png) | ![Tablet](app/screens/tablet.png) | ![TV](app/screens/tv.png) |

---

## ✨ Features

### 📡 Content & Streaming
- **100+ TDT channels** — national (La 1, La 2, TRECE, 24h, Clan, Teledeporte), regional (TV3, ETB, Canal Sur, Aragón TV) and thematic channels.
- **40+ radio stations** — LOS40, Cadena SER, COPE, Rock FM, Europa FM, Flaix FM and more.
- **HLS & MP3/AAC streaming** powered by **AndroidX Media3 / ExoPlayer**.
- **Offline resilience** — 50+ hardcoded fallback channels when the remote source is unreachable.

### 🧠 Smart Functionality
- **Category filtering** — General, News, Sports, Kids, Entertainment, Regional, Music.
- **Real-time search** with debounce to minimise recompositions.
- **Broken channel detection** — automatic marking of unplayable streams, easy retry.
- **Persistent favourites** — saved with DataStore, restored across sessions.
- **Persistent preferences** — theme (Light / Dark / System) and language (ES / EN / CA) backed by DataStore.

### 🖥 Multi-Platform UI
- **Phone** — portrait & landscape layouts, fullscreen player with brightness/volume swipe gestures.
- **Tablet** — adaptive grid, optimised for larger screens.
- **Android TV** — D-pad/remote optimised with TV Material 3 (focus glow, scale animations, Leanback launcher).
- **Android Auto** — music & radio channels exposed via `MediaLibraryService`; artwork, title and category shown on the car's display; steering-wheel controls via MediaSession.
- **Automotive OS** — native car UI (`CarAppService`) with channel list, `NowPlayingScreen` and real-time player state.

### 🎨 Design
- **Material Design 3** with dynamic colour on Android 12+.
- **Consistent artwork** across all surfaces — Coil loads channel logos everywhere; letter-avatar fallback when no logo is available.
- **Smooth transitions** — `AnimatedContent` / `AnimatedVisibility` throughout.
- **Multilingual** — Spanish, English, Catalan.

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────┐
│  :app  (Presentation)                           │
│  Compose UI · ViewModels · Hilt · Car services  │
├─────────────────────────────────────────────────┤
│  :domain  (Business Logic — pure Kotlin/JVM)    │
│  UseCases · Domain Models · Repository interfaces│
├─────────────────────────────────────────────────┤
│  :data  (Data Layer)                            │
│  Ktor · RepositoryImpl · DataStore · Fallback   │
└─────────────────────────────────────────────────┘
```

**MVI pattern** — every ViewModel exposes a single `StateFlow<UiState>` and accepts actions through `onIntent()`:

```kotlin
viewModel.onIntent(TdtIntent.SelectChannel(channel))
viewModel.onIntent(TdtIntent.FilterByCategory(ChannelCategory.MUSIC))
viewModel.onIntent(TdtIntent.Search("cope"))
```

**Android Auto / Automotive OS** — a `MediaLibraryService` bridges the shared `ExoPlayer` instance to the car host. A separate `CarAppService` provides the native Automotive OS UI using Car App Library templates.

```
TdtPlayer (singleton)
    ├── Phone / TV  →  Compose PlayerView
    ├── Android Auto  →  TdtMediaService (MediaLibrarySession)
    └── Automotive OS →  TdtCarScreen / NowPlayingScreen (CarAppService)
```

---

## 🛠 Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin 2.3.20 (K2 compiler) |
| UI | Jetpack Compose · Material 3 · TV Material 3 |
| DI | Hilt 2.59 |
| Networking | Ktor 3 |
| Media | AndroidX Media3 1.10 / ExoPlayer |
| Car | AndroidX Car App Library 1.7 |
| Image loading | Coil 2 |
| Persistence | DataStore Preferences |
| Serialization | Kotlinx Serialization |
| Architecture | Clean Architecture · MVI |
| Testing | JUnit 4 · Turbine · Coroutines Test |
| CI | GitHub Actions |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug or newer
- **JDK 21** (required by Gradle 9)
- **Android device / emulator**: API 24+

### Build & run
```bash
git clone https://github.com/pedrogm/tdtflow.git
cd tdtflow
./gradlew assembleDebug        # build
./gradlew installDebug         # install on connected device
./gradlew test                 # unit tests
```

### Testing Android Auto
Use the **Desktop Head Unit (DHU)** included in the Android SDK:
```bash
# 1. Enable Developer options in the Android Auto phone app
# 2. Connect phone via USB
# 3. Launch DHU from Android Studio's "Running Devices" or:
$ANDROID_HOME/extras/google/auto/desktop-head-unit
```

---

## 🧪 Testing & CI

```bash
./gradlew :app:test            # ViewModel + UI logic tests
./gradlew :domain:test         # UseCase tests
./gradlew :data:test           # Repository tests
./gradlew lint                 # static analysis
```

The GitHub Actions pipeline runs on every push: lint → unit tests → debug build.

---

## 📝 License

MIT License — see [LICENSE](LICENSE) for details.

---

*Developed with ❤️ for the Android & open-source community.*
