# Auditoría Técnica — TDTFlow

> Actualizado: 2026-04-22 · Rama: `master`

---

## Índice

1. [Estado actual del proyecto](#1-estado-actual-del-proyecto)
2. [Arquitectura](#2-arquitectura)
3. [Tests rotos — acción inmediata](#3-tests-rotos--acción-inmediata)
4. [Calidad de código](#4-calidad-de-código)
5. [Seguridad y build](#5-seguridad-y-build)
6. [UI / UX](#6-ui--ux)
7. [Resumen priorizado](#7-resumen-priorizado)

---

## 1. Estado actual del proyecto

### Módulos

| Módulo | Tipo | Responsabilidad |
|--------|------|-----------------|
| `:domain` | JVM puro | Modelos (`Channel`, `Program`), interfaces de repositorio, casos de uso |
| `:data` | Android library | Room, Ktor, DataStore, implementaciones de repositorio |
| `:app` | Android application | UI Compose, ViewModels, player, Cast, navegación |

### Nuevas funcionalidades añadidas (Gemini)

| Funcionalidad | Archivos principales |
|--------------|----------------------|
| **Persistencia Room** | `ChannelEntity`, `ChannelDao`, `TdtDatabase`, `DatabaseModule` |
| **EPG / Now Playing** | `Program`, `EpgRepository`, `GetNowPlayingUseCase`, `MockEpgRepositoryImpl` |
| **Google Cast** | `CastOptionsProvider`, `CastButton`, `media3-cast`, `mediarouter` |
| **Buffer configurable** | `AppBuffer` (FAST/BALANCED/STABLE), `bufferFlow` en `IOptionsPreferences` |
| **Import/Export favoritos** | `ImportFavoritesUseCase`, `ClearFavoritesUseCase`, JSON serialization |
| **Picture in Picture** | `onUserLeaveHint()` en `MainActivity` con `PictureInPictureParams` |
| **Navegación TV mejorada** | DPAD_UP/DOWN → `PreviousChannel`/`NextChannel` en `TvActivity` |

### Stack de versiones

| Dependencia | Versión |
|-------------|---------|
| AGP | 9.2.0 |
| Kotlin | 2.3.20 |
| Compose BOM | 2026.03.01 |
| Media3 | 1.10.0 |
| Room | 2.8.4 |
| Ktor | 3.4.2 |
| Hilt | 2.59.2 |
| `tv-material` | 1.1.0-rc01 ⚠️ pre-release |

---

## 2. Arquitectura

### Flujo de datos principal

```
API TDTChannels (Ktor)
        ↓ fetch paralelo TV + Radio
ChannelRepositoryImpl
    ├── ChannelCache (memoria, TTL 30 min)
    ├── Room (ChannelDao) ← fallback offline
    └── FallbackChannels ← fallback sin red
        ↓
GetChannelsUseCase → TdtViewModel
    ├── combine(channels, category, query, brokenUrls, showBroken)
    │       → ChannelFilterLogic.applyFilters()
    ├── PlayerController → TdtPlayer (ExoPlayer) → PlaybackService
    └── getNowPlayingUseCase(channelUrl) → MockEpgRepositoryImpl
```

### Gestión de preferencias (corregida en esta sesión)

```
OptionsDataStore (DataStore)
    ├── themeFlow / languageFlow / bufferFlow
    └── saveTheme / saveLanguage / saveBuffer (suspend)
        ↑
OptionsMenuViewModel (combine 3 flows, viewModelScope.launch para saves)
        ↑
MainActivity / TvActivity
    └── LaunchedEffect(language) → AppCompatDelegate.setApplicationLocales()
```

### Problemas de arquitectura abiertos

**[ARCH-1] `TdtFlowApp.appContext` estático**
`TdtFlowApp.kt` expone `applicationContext` como `lateinit var appContext` estático. No se usa en ningún sitio del código actual — es código muerto y un anti-pattern. Eliminar.

**[ARCH-2] `TdtPlayer` usa `runBlocking` para leer buffer**
`TdtPlayer` inicializa `DefaultLoadControl` con `runBlocking { prefs.bufferFlow.first() }`. Bloquea el hilo al crear el player por primera vez. Solución: recibir el `AppBuffer` como parámetro ya resuelto desde el ViewModel o lanzar en un scope dedicado.

**[ARCH-3] `NetworkModule` es un `object` Kotlin, no un módulo Hilt**
No es swappeable en tests. Mejora deseable, no urgente.

**[ARCH-4] `MockEpgRepositoryImpl` en producción**
EPG es mock en producción. Adecuado mientras no haya API real, pero debería estar claramente marcado y ser fácil de sustituir.

---

## 3. Tests rotos — acción inmediata

El CI va a romper en el job `unit-tests` porque los tests no se han actualizado tras los cambios de interfaz. Son 4 ficheros con errores de compilación garantizados:

### [TEST-ROTO-1] `ChannelRepositoryImplTest` — falta `channelDao`

`ChannelRepositoryImpl` ahora requiere `channelDao: ChannelDao` como primer parámetro (sin default). El test lo instancia sin él.

```kotlin
// ROTO — data/src/test/.../ChannelRepositoryImplTest.kt
ChannelRepositoryImpl(cache = cache)                  // falta channelDao
ChannelRepositoryImpl(cache = cache, onError = ...)   // falta channelDao
```

**Fix**: inyectar un `ChannelDao` in-memory o mover los tests a androidTest con Room in-memory (como ya hace `ChannelDaoTest`).

### [TEST-ROTO-2] `FakeFavoritesRepository` — falta `addAll` y `clearAll`

`FavoritesRepository` ahora tiene `addAll(urls: Set<String>)` y `clearAll()`. Los fakes en `app/src/test` y `app/src/androidTest` no los implementan.

```kotlin
// ROTO — FakeFavoritesRepository.kt (ambas copias)
// Falta:
override suspend fun addAll(urls: Set<String>) { ... }
override suspend fun clearAll() { ... }
```

### [TEST-ROTO-3] `FavoritesViewModelTest` — constructor desactualizado

`FavoritesViewModel` ahora requiere `ImportFavoritesUseCase` y `ClearFavoritesUseCase`. El test no los pasa.

```kotlin
// ROTO — FavoritesViewModelTest.kt
FavoritesViewModel(addFavorite, removeFavorite, getFavorites)
// Falta: importFavorites, clearFavorites
```

### [TEST-ROTO-4] `TdtViewModelTest` — constructor desactualizado

`TdtViewModel` ahora requiere `getNowPlayingUseCase: GetNowPlayingUseCase`. El test no lo pasa.

```kotlin
// ROTO — TdtViewModelTest.kt
TdtViewModel(getChannelsUseCase=..., brokenChannelTracker=..., ...)
// Falta: getNowPlayingUseCase
```

---

## 4. Calidad de código

### Bien hecho ✅

- Clean Architecture respetada: `:domain` sin deps Android.
- MVI con `TdtIntent` / `UiState` coherente.
- `OptionsMenuViewModel` con `combine(3 flows)` + saves no bloqueantes.
- `AppCompatDelegate.setApplicationLocales()` para locale (API moderna).
- `LaunchedEffect` para aplicar locale sin `attachBaseContext` manual.
- `network_security_config.xml` en lugar de `usesCleartextTraffic` global.
- `locales_config.xml` declarado para Android 13+.
- Triple fuente en `ChannelRepositoryImpl`: cache → Room → red.
- `ChannelDao` con `@Insert(onConflict = REPLACE)` — correcto para upsert.
- Fakes en lugar de mocks en tests unitarios.

### Pendiente de resolver

**[QUALITY-1] `TdtFlowApp.appContext` — código muerto y anti-pattern**

Nadie lo usa. Eliminarlo.

**[QUALITY-2] `TdtPlayer.runBlocking` para buffer**

```kotlin
// TdtPlayer.kt — MALO
private val loadControl by lazy {
    runBlocking { prefs.bufferFlow.first() } // bloquea hilo
    ...
}
```

**[QUALITY-3] Fakes duplicados entre `test/` y `androidTest/`**

`FakeChannelsRepository`, `FakeFavoritesRepository`, `FakeBrokenChannelTracker`, `TestChannels` existen en ambos source sets. Consolidar en un módulo `:testutils` o source set `sharedTest`.

**[QUALITY-4] `OptionsMenuScreen` duplica secciones Mobile/TV**

Las secciones de tema, idioma, buffer y canales rotos están implementadas dos veces. Extraer a composables compartidos parametrizando solo el estilo TV.

**[QUALITY-5] Strings hardcoded en UI tests**

```kotlin
// MobileScreenTest, FavoritesScreenTest — MALO
assertIsDisplayed("Aún no tienes canales favoritos")
// Correcto:
assertIsDisplayed(composeTestRule.activity.getString(R.string.empty_favorites))
```

**[QUALITY-6] Magic numbers en `TdtPlayer`**

Los valores de buffer (1500, 3000, 500...) están hardcoded dentro del `when`. Moverlos a constantes en `AppBuffer` o a un fichero de constantes.

**[QUALITY-7] Lint XML en raíz sin `.gitignore`**

30+ ficheros `AndroidLint*.xml`, `UnusedSymbol.xml`, etc. en la raíz del repo sin estar ignorados. Añadir a `.gitignore`.

---

## 5. Seguridad y build

### Resuelto en esta sesión ✅

- `usesCleartextTraffic="true"` → `network_security_config.xml` explícito.
- `*.jks` y `*.keystore` añadidos a `.gitignore`.
- `PlaybackService` revisado: `exported=true` con intent-filter es correcto para Media3.

### Abierto

**[SEC-1] `android.r8.strictFullModeForKeepRules=false`**

En `gradle.properties`. Silencia errores de ProGuard en R8 AGP 9. Reactivar a `true` e investigar los keep rules que fallen.

**[SEC-2] `release-key.jks` vacío en disco**

Está en `.gitignore` ahora, pero el fichero vacío sigue en disco. El build release usa `RELEASE_KEYSTORE_PATH` de env vars — correcto.

### CI/CD

- 3 jobs: `lint` → `unit-tests` → `build-release`. Correcto.
- `unit-tests` va a fallar hasta que se arreglen los [TEST-ROTO-*].
- androidTests NO se ejecutan en CI (no hay emulador). Los tests de Compose UI son letra muerta en CI.

---

## 6. UI / UX

### Bien hecho ✅

- Skeleton de carga (`ChannelGridSkeleton`).
- `EmptyState`, `ErrorState` con Lottie + Retry.
- `AnimatedVisibility` en SearchBar.
- `LiveIndicator` con pulse.
- Soporte Dark/Light/System con Material You en Android S+.
- PiP automático al salir con reproducción activa.
- Cast button en `VideoPlayer`.

### Pendiente

**[UI-1] Colores hardcoded fuera de `AppColors`**

| Valor | Ubicación |
|-------|-----------|
| `Color(0xFF121212)` | `VideoPlayer`, `TvPlayerFullscreen` |
| `Color(0xFFFF5252)` | `ErrorState` |
| `Color(0xFFEF5350)` | `BrokenChannelCard` |
| `Color.Red` | `LiveIndicator` |

**[UI-2] Sin `AnimatedContent` entre estados loading/content/error**

La transición entre estados aparece/desaparece sin crossfade. `AnimatedContent` añadiría pulido.

**[UI-3] Sin `semantics(mergeDescendants=true)` en `ChannelCard`**

TalkBack lee nombre, categoría y logo por separado. Agrupar con `Modifier.semantics(mergeDescendants = true)`.

**[UI-4] Locale por defecto**

`values/strings.xml` contiene español como locale por defecto global. Si un usuario tiene un idioma no soportado, recibe español en lugar del inglés (que debería ser el fallback internacional). Mover español a `values-es/` e inglés a `values/`.

**[UI-5] `tv-material` en versión pre-release**

`1.1.0-rc01` — monitorear release GA.

---

## 7. Resumen priorizado

### 🔴 Urgente — el CI está roto

| ID | Acción | Fichero |
|----|--------|---------|
| TEST-ROTO-1 | Añadir `ChannelDao` in-memory a `ChannelRepositoryImplTest` | `ChannelRepositoryImplTest.kt` |
| TEST-ROTO-2 | Implementar `addAll` y `clearAll` en `FakeFavoritesRepository` (×2) | `test/` + `androidTest/` |
| TEST-ROTO-3 | Pasar `importFavorites` y `clearFavorites` en `FavoritesViewModelTest` | `FavoritesViewModelTest.kt` |
| TEST-ROTO-4 | Pasar `getNowPlayingUseCase` en `TdtViewModelTest` | `TdtViewModelTest.kt` |

### 🟠 Alta — bugs y deuda técnica

| ID | Acción | Fichero |
|----|--------|---------|
| ARCH-2 | Eliminar `runBlocking` de `TdtPlayer` para buffer | `TdtPlayer.kt` |
| ARCH-1 | Eliminar `TdtFlowApp.appContext` estático | `TdtFlowApp.kt` |
| SEC-1 | Reactivar `r8.strictFullModeForKeepRules=true` | `gradle.properties` |
| QUALITY-7 | Añadir lint XMLs a `.gitignore` | `.gitignore` |

### 🟡 Media — calidad y mantenibilidad

| ID | Acción |
|----|--------|
| QUALITY-3 | Consolidar fakes en `sharedTest` o `:testutils` |
| QUALITY-5 | Strings hardcoded → `R.string.*` en UI tests |
| QUALITY-4 | Extraer secciones duplicadas de `OptionsMenuScreen` |
| QUALITY-6 | Magic numbers de buffer → constantes en `AppBuffer` |

### 🟢 Baja — polish

| ID | Acción |
|----|--------|
| UI-1 | Mover colores hardcoded a `AppColors` |
| UI-2 | `AnimatedContent` entre estados loading/content/error |
| UI-3 | `semantics(mergeDescendants=true)` en `ChannelCard` |
| UI-4 | Español a `values-es/`, inglés como default en `values/` |

---

*Última revisión: 2026-04-22. Verificar contra estado actual del código antes de actuar.*
