# 📺 TDTFlow - Spanish TV & Radio Streaming App

A modern Android streaming application for watching Spanish TV channels and listening to radio stations. Built with **Kotlin**, **Jetpack Compose**, and **Clean Architecture** principles.

## ✨ Features

### 📺 TV Channels
- **RTVE Channels**: La 1, La 2, 24 Horas, Clan, Teledeporte
- **Regional Broadcasts**: TV3 (Cataluña), ETB 1 (Euskadi), Canal Sur (Andalucía), Aragón TV
- Full HLS streaming support with ExoPlayer

### 📻 Radio Stations
- **40+ Musical Stations**: LOS40, Cadena Dial, Rock FM, Europa FM, Flaix FM, and more
- **News & General**: Cadena SER, COPE, Onda Cero, Radio Nacional, esRadio
- MP3/AAC streaming via StreamTheWorld and custom servers

### 🎯 Smart Features
- **Category Filtering**: TV, News, Sports, Kids, Music, Regional
- **Search & Discovery**: Real-time search with debounced queries
- **Broken Channel Detection**: Auto-detect and manage unplayable streams
- **Channel Validation**: Manual revalidation to keep channel list fresh
- **Responsive Design**: Optimized for both phone (portrait/landscape) and TV

### 🎨 UI/UX
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Latest Material design system
- **Adaptive Layouts** - Phone and TV form factors
- **Smooth Animations** - Professional transitions and overlays
- **Dark Theme** - Easy on the eyes streaming experience

## 🏗️ Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────┐
│  Presentation (UI)                  │
│  - TdtViewModel                     │
│  - MobileScreen / TvChannelBrowser  │
│  - Components (ChannelCard, Filter) │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│  Domain (Business Logic)            │
│  - UseCase (GetChannelsUseCase)     │
│  - Models (Channel, Category)       │
│  - Repository Interface             │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│  Data (Sources & Repositories)      │
│  - ChannelRepositoryImpl             │
│  - Remote (API via Retrofit)        │
│  - Local Fallback (FallbackChannels)│
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│  Infrastructure                     │
│  - Networking (Retrofit, OkHttp)    │
│  - Media Player (ExoPlayer/Media3)  │
│  - Dependencies (DIContainer)       │
└─────────────────────────────────────┘
```

### Key Design Patterns

- **MVVM** - ViewModel with Kotlin Flow reactive state
- **Manual DI** - Simple, non-intrusive dependency injection
- **Repository Pattern** - Data abstraction layer
- **Use Cases** - Single responsibility business operations
- **Composable Pattern** - Modular, reusable UI components

## 📂 Project Structure

```
tdtflow/
├── app/                          # Android application module
│   ├── src/main/java/com/pedrogm/tdtflow/
│   │   ├── ui/
│   │   │   ├── mobile/          # Phone UI (portrait/landscape)
│   │   │   ├── tv/              # TV UI (10-foot interface)
│   │   │   ├── components/      # Reusable UI components
│   │   │   ├── theme/           # Material Design 3 theme & colors
│   │   │   ├── TdtViewModel.kt  # Main view model
│   │   │   └── TdtUiState.kt    # UI state data class
│   │   ├── player/              # Media playback layer
│   │   │   ├── TdtPlayer.kt     # ExoPlayer wrapper
│   │   │   └── PlayerState.kt   # Playback state enum
│   │   ├── util/                # Utilities
│   │   │   ├── TimeConstants.kt # Timeout/delay values
│   │   │   └── Constants.kt     # App-wide constants
│   │   ├── MainActivity.kt      # Phone entry point
│   │   └── TvActivity.kt        # TV entry point
│   └── AndroidManifest.xml
│
├── domain/                       # Domain/business logic module
│   ├── src/main/java/com/pedrogm/tdtflow/domain/
│   │   ├── model/               # Business entities
│   │   │   ├── Channel.kt       # Channel data model
│   │   │   └── ChannelCategory.kt
│   │   ├── usecase/             # Business operations
│   │   │   └── GetChannelsUseCase.kt
│   │   └── repository/          # Repository interfaces
│   │       └── ChannelRepository.kt
│
├── data/                        # Data access module
│   ├── src/main/java/com/pedrogm/tdtflow/data/
│   │   ├── remote/              # API calls
│   │   │   ├── ChannelService.kt
│   │   │   ├── ChannelMapper.kt
│   │   │   └── AmbitConstants.kt
│   │   ├── repository/          # Repository implementation
│   │   │   ├── ChannelRepositoryImpl.kt
│   │   │   └── FallbackChannels.kt (40+ channels)
│   │   ├── BrokenChannelTracker.kt
│   │   └── DIContainer.kt       # Dependency container
│
└── build.gradle.kts             # Build configuration
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (Giraffe or newer)
- Android SDK 28+
- Gradle 8.1+
- Kotlin 1.9+

### Building

```bash
# Clone repository
git clone <repository-url>
cd tdtflow

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run on emulator/device
./gradlew installDebug
adb shell am start -n com.pedrogm.tdtflow/.MainActivity
```

### Development Setup

1. Open project in Android Studio
2. Android Studio will sync Gradle automatically
3. Select target device (phone emulator or TV emulator)
4. Run `app` configuration

## 📱 Architecture Highlights

### Reactive State Management
```kotlin
// TdtViewModel uses Kotlin Flow for reactive updates
private val _filteredChannels: StateFlow<List<Channel>> = combine(
    _channels,
    _selectedCategory,
    debouncedQuery,
    brokenChannelTracker.brokenUrls,
    _showBrokenChannels
) { channels, category, query, brokenUrls, showBroken ->
    ChannelFilterLogic.applyFilters(
        channels = channels,
        category = category,
        query = query,
        brokenUrls = brokenUrls,
        showBroken = showBroken
    )
}
```

### Manual Dependency Injection
```kotlin
// Simple, explicit DI without frameworks
fun provideViewModelFactory(activity: Activity): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = activity.application as TdtFlowApp
            return TdtViewModel(app, getChannelsUseCase) as T
        }
    }
}
```

### Composable UI Components
```kotlin
// Reusable, testable UI components
@Composable
fun ChannelCard(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) { /* ... */ }

@Composable
fun CategoryFilter(
    selectedCategory: ChannelCategory?,
    onCategorySelected: (ChannelCategory?) -> Unit
) { /* ... */ }
```

## 🔌 Dependencies

### Core
- **androidx.lifecycle** - MVVM ViewModels
- **kotlinx.coroutines** - Async/reactive programming
- **androidx.compose** - Modern UI toolkit

### Media
- **androidx.media3** - ExoPlayer for HLS streaming
- **com.google.android.exoplayer2** - Media playback

### Networking
- **com.squareup.retrofit2** - HTTP client
- **com.squareup.okhttp3** - HTTP interceptor & caching

### Images
- **io.coil-kt:coil-compose** - Image loading & caching

### UI/Icons
- **androidx.compose.material3** - Material Design 3
- **com.composables:icons-lucide** - Icon library

## 📊 Channel Data

### API Integration
- Primary source: TDTChannels API (remote channels)
- Fallback: Hardcoded 50+ channels (no network)
- Logo merging: Uses fallback logos when API returns empty

### Supported Categories
- **Generalistas** - Main TV channels
- **Informativos** - News channels
- **Deportivos** - Sports channels
- **Infantiles** - Kids content
- **Musicales** - Radio & music
- **Regionales** - Regional/autonomous broadcasts
- **Populares** - Popular channels
- **Streaming** - OTT services

## 🎮 Multi-Device Support

### Phone UI (Portrait)
- Landscape fullscreen player with gesture controls
- Grid channel layout with search
- Bottom app bar with playback controls
- Adaptive category filtering

### TV UI (10-foot)
- Focus-based navigation
- Large cards for remote control
- Full-screen player with overlay controls
- D-pad friendly filtering system

## 🐛 Error Handling

### Graceful Degradation
- **Network Failure** → Uses fallback channels
- **Invalid Streams** → Marks as "broken", allows retry
- **Missing Logos** → Falls back to category icon
- **Buffering Timeout** → User can skip or retry

### Monitoring
- Broken channel tracker with auto-recovery
- Player error logging
- Detailed analytics for stream failures

## 🔄 Recent Refactoring

### Code Quality Improvements (6 commits)
1. **Color Constants** - Centralized theme colors (AppColors)
2. **Component Extraction** - UI modularization
3. **Filter Logic** - Atomic filtering pipeline (ChannelFilterLogic)
4. **Overlay Separation** - Landscape UI components
5. **Color Consolidation** - Player colors grouped semantically
6. **Category Refactoring** - Filter component modularization

**Results**: ~150+ lines refactored, 5 new reusable components, improved SOLID compliance

## 🛠️ Development Workflow

### Making Changes
1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes following SOLID principles
3. Build and test: `./gradlew build test`
4. Commit with clear messages: `git commit -m "feat: description"`
5. Push and create pull request

### Code Standards
- **Kotlin Style Guide** - Official Kotlin conventions
- **Clean Architecture** - Layered, decoupled design
- **SOLID Principles** - Single Responsibility emphasis
- **Meaningful Names** - Self-documenting code

## 📝 License

[Add your license here]

## 👤 Author

[Your name/contact]

## 🤝 Contributing

Contributions welcome! Please follow the development workflow above and ensure:
- ✅ Code compiles without errors
- ✅ Tests pass locally
- ✅ Architecture principles are maintained
- ✅ Code is well-documented

## 🙏 Acknowledgments

- **ExoPlayer/Media3** - Professional media playback
- **TDTChannels** - Channel data API
- **Jetpack Compose** - Modern Android UI
- **Kotlin** - Language and ecosystem
