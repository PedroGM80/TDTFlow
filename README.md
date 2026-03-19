# TDTFlow — Spanish TV & Radio Streaming

Android app for streaming Spanish TDT channels and radio stations. Built with Kotlin, Jetpack Compose and Clean Architecture.

## Features

### Channels & Radio
- **TDT**: RTVE (La 1, La 2, 24h, Clan, Teledeporte), regional channels (TV3, ETB1, Canal Sur, Aragón TV) and more
- **Radio**: 40+ stations — LOS40, Cadena SER, COPE, Rock FM, Europa FM, Flaix FM, etc.
- HLS and MP3/AAC streaming via ExoPlayer/Media3

### Smart features
- Category filtering (Generalistas, Informativos, Deportivos, Infantiles, Musicales, Regionales…)
- Real-time search with debounce
- Broken channel detection — auto-marks unplayable streams, allows retry and full revalidation
- Favorites — persist and manage your preferred channels
- Offline fallback — 50+ hardcoded channels when the API is unreachable

### UI
- Jetpack Compose + Material Design 3
- Phone: portrait grid + landscape fullscreen player
- TV (10-foot): focus-based navigation, D-pad friendly
- Light / Dark / System theme
- Multilingual: Spanish, English, Catalan

---

## Architecture

Three-layer Clean Architecture with MVI presentation pattern.

```
┌─────────────────────────────────────────────┐
│  Presentation (app module)                  │
│  ViewModel  →  onIntent()  →  uiState       │
│  Screens: MobileScreen / TvScreen           │
│  Components: ChannelCard, CategoryFilter…   │
└──────────────────┬──────────────────────────┘
                   │ uses
┌──────────────────▼──────────────────────────┐
│  Domain (domain module — pure JVM)          │
│  UseCase: GetChannelsUseCase                │
│  Models: Channel, ChannelCategory           │
│  Repository interfaces                      │
└──────────────────┬──────────────────────────┘
                   │ implements
┌──────────────────▼──────────────────────────┐
│  Data (data module)                         │
│  ChannelRepositoryImpl (network + fallback) │
│  FavoritesRepositoryImpl (in-memory)        │
│  BrokenChannelTracker                       │
└─────────────────────────────────────────────┘
```

### MVI pattern

Every ViewModel exposes exactly two things:

```kotlin
val uiState: StateFlow<*>          // single source of truth for the UI
fun onIntent(intent: SealedClass)  // single entry point for user actions
```

| ViewModel | State | Intent |
|---|---|---|
| `TdtViewModel` | `TdtUiState` | `TdtIntent` |
| `FavoritesViewModel` | `FavoritesUiState` | `FavoritesIntent` |
| `OptionsMenuViewModel` | `OptionsMenuState` | `OptionsMenuIntent` |

All mutation methods are private; the sealed intent class is the exhaustive contract of what the UI can request.

### Other patterns

- **Repository pattern** — data source abstraction
- **Use cases** — `operator fun invoke()` for single-responsibility operations
- **Manual DI** — `DIContainer` object with lazy singletons, no framework needed

---

## Project structure

```
tdtflow/
├── app/
│   └── src/main/java/com/pedrogm/tdtflow/
│       ├── ui/
│       │   ├── TdtViewModel.kt
│       │   ├── TdtUiState.kt
│       │   ├── TdtIntent.kt
│       │   ├── ChannelFilterLogic.kt
│       │   ├── mobile/
│       │   │   └── MobileScreen.kt       # Portrait + landscape fullscreen
│       │   ├── tv/
│       │   │   ├── TvScreen.kt
│       │   │   └── components/           # TvChannelBrowser, TvPlayerFullscreen…
│       │   ├── favorites/
│       │   │   ├── FavoritesViewModel.kt
│       │   │   ├── FavoritesUiState.kt
│       │   │   ├── FavoritesIntent.kt
│       │   │   └── FavoritesScreen.kt
│       │   ├── options/
│       │   │   ├── OptionsMenuViewModel.kt
│       │   │   ├── OptionsMenuState.kt
│       │   │   ├── OptionsMenuIntent.kt  # (file: OptionsMenuEvent.kt)
│       │   │   ├── OptionsMenuScreen.kt
│       │   │   ├── AppTheme.kt
│       │   │   └── AppLanguage.kt
│       │   ├── components/               # ChannelCard, VideoPlayer, SearchBar…
│       │   └── theme/                    # AppColors, TDTFlowTheme
│       ├── player/
│       │   ├── TdtPlayer.kt              # ExoPlayer wrapper
│       │   └── PlayerState.kt
│       ├── di/
│       │   └── DIContainer.kt
│       ├── util/
│       │   ├── TimeConstants.kt
│       │   └── Constants.kt
│       ├── MainActivity.kt
│       └── TvActivity.kt
│
├── domain/
│   └── src/main/java/com/pedrogm/tdtflow/domain/
│       ├── model/
│       │   ├── Channel.kt
│       │   └── ChannelCategory.kt
│       ├── usecase/
│       │   ├── GetChannelsUseCase.kt
│       │   ├── AddFavoriteUseCase.kt
│       │   ├── RemoveFavoriteUseCase.kt
│       │   └── GetFavoritesUseCase.kt
│       └── repository/
│           ├── ChannelRepository.kt
│           └── FavoritesRepository.kt
│
└── data/
    └── src/main/java/com/pedrogm/tdtflow/data/
        ├── repository/
        │   ├── ChannelRepositoryImpl.kt
        │   └── FavoritesRepositoryImpl.kt
        ├── remote/
        │   ├── ChannelService.kt
        │   └── ChannelMapper.kt
        ├── BrokenChannelTracker.kt
        ├── FallbackChannels.kt
        └── OptionsPreferences.kt
```

---

## Getting started

**Requirements:** Android Studio Hedgehog+, SDK 28+, Kotlin 2.x, Gradle 8+

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Install on connected device / emulator
./gradlew installDebug
```

For TV: use a TV emulator (API 28+) and run the `TvActivity` launch configuration.

---

## Key implementation details

### Reactive filter pipeline

Five flows combined with debounced search — recomposition only happens when the result actually changes:

```kotlin
combine(
    _channels, _selectedCategory, debouncedQuery,
    brokenChannelTracker.brokenUrls, _showBrokenChannels
) { channels, category, query, brokenUrls, showBroken ->
    ChannelFilterLogic.applyFilters(...)
}
.flowOn(Dispatchers.Default)   // CPU work off Main
.distinctUntilChanged()        // skip identical lists
.stateIn(viewModelScope, ...)
```

### MVI intent dispatch

```kotlin
// Screen
onCategorySelected = { viewModel.onIntent(TdtIntent.FilterByCategory(it)) }

// ViewModel
fun onIntent(intent: TdtIntent) {
    when (intent) {
        is TdtIntent.FilterByCategory -> filterByCategory(intent.category)
        is TdtIntent.SelectChannel    -> selectChannel(intent.channel)
        is TdtIntent.Retry            -> retry()
        // ...
    }
}
private fun filterByCategory(category: ChannelCategory?) { ... }
```

### Broken channel detection

`BrokenChannelTracker` observes player errors and buffering timeouts via `flatMapLatest`. When a stream fails, its URL is added to a `StateFlow<Set<String>>` which feeds back into the filter pipeline — the channel is visually marked and hidden by default without any explicit refresh.

---

## Dependencies

| Area | Library |
|---|---|
| UI | Jetpack Compose, Material 3, Lucide icons |
| ViewModel / Flow | AndroidX Lifecycle, Kotlin Coroutines |
| Media | AndroidX Media3 (ExoPlayer) |
| Networking | Retrofit 2, OkHttp 3 |
| Images | Coil Compose |
| Testing | JUnit 4, kotlinx-coroutines-test, Turbine |

---

## Testing

Unit tests cover all ViewModels without Android dependencies:

- `FavoritesViewModelTest` — 11 tests, `FakeFavoritesRepository`, verifies MVI intents
- `OptionsMenuViewModelTest` — 12 tests with Turbine, verifies state transitions per intent

```bash
./gradlew :app:test
```
