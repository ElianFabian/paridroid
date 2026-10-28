# PariDroid

PariDroid provides [Pari/GP](https://pari.math.u-bordeaux.fr/) for the Android system.

## About This Project

This is a modernized fork of the original [PariDroid](https://pari.math.u-bordeaux.fr/paridroid/index.html) app. The original app was built with legacy Android Views and Java. This version has been completely refactored to use modern Android development practices:

### Key Modernization Efforts

- **🎨 Migrated to Jetpack Compose** - The entire UI has been rewritten using Jetpack Compose and Material 3, replacing the legacy XML layouts and View-based UI
- **📱 Kotlin-First** - The entire codebase is now written in Kotlin, replacing the original Java implementation
- **🏗️ Modular Architecture** - The Pari/GP native logic has been extracted into a separate library module (`:paridroidgp`) for better separation of concerns
- **🎯 MVVM with ViewModels** - Implemented modern architecture using `ViewModel`, `StateFlow`, and Compose state observation for reactive UI
- **⚡ Modern Build System** - Updated to use Gradle Kotlin DSL (`.kts`), Android Gradle Plugin 8+, and modern dependency management with version catalogs
- **🎪 Native Integration** - The `paridroidgp` module handles JNI/NDK integration with Pari/GP and GMP libraries using modern NDK build configuration

## Project Structure

```
paridroid-2026/
├── app/                    # Main application module (Compose UI + ViewModels)
│   └── src/main/java/fr/ubordeaux/math/paridroid/
│       ├── PariActivity.kt         # Main Activity (Compose entry point)
│       ├── PariScreen.kt           # Main Compose UI screen
│       ├── PariViewModel.kt        # ViewModel with StateFlow state management
│       ├── PariSettingsScreen.kt   # Settings screen (Compose)
│       └── PariSettingsActivity.kt # Settings Activity
│
├── paridroidgp/            # Library module: Pari/GP native engine
│   └── src/main/
│       ├── java/fr/ubordeaux/math/paridroidgp/  # JNI bridge (Kotlin)
│       └── jni/                                 # Native C code + NDK build scripts
│
├── gradle/                 # Gradle version catalogs & build logic
└── fastlane/               # Fastlane configuration for Play Store / F-Droid deployments
```

## Architecture Highlights

### UI Layer (app module)
- **PariScreen.kt** - Main composable with text input, autocomplete, command history, results display
- **PariViewModel.kt** - Manages all UI state via `StateFlow`: input text, output history, custom scripts, command autocomplete
- **PariSettingsScreen.kt** - Compose-based settings screen with Material 3 components

### Native Engine (paridroidgp module)
- **JNI Bridge** - Kotlin wrapper (`ParidroidGP.kt`) exposing `eval()`, `init()`, `setDefault()`, `getSupportedCommands()`
- **Native Layer** - C implementation linking against Pari/GP 2.15+ and mini-GMP
- **NDK Build** - Configured via `Android.mk` / `Application.mk` with `ndkBuild` in Gradle

## Building

### Prerequisites
- Android Studio (latest stable)
- JDK 17+
- Android SDK 36 (compileSdk) / minSdk 21
- NDK 28.2.13676358 (configured in `paridroidgp/build.gradle.kts`)

### Build Commands
```bash
# Debug build
./gradlew app:assembleDebug

# Release build
./gradlew app:assembleRelease

# Run tests
./gradlew app:test
```

## License

PariDroid is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

See the [GNU General Public License](http://www.gnu.org/licenses/gpl.txt) for more details.

### Third-Party Licenses
- **Pari/GP** - GPL v2+
- **GMP / mini-GMP** - LGPL v3+
- **Material Icons** - Apache License 2.0 (used in app icons)
- **F-Droid metadata translations** - GNU AGPL v3 (in `fastlane/metadata/android/`)

## Distribution

Prebuilt binaries are available through:
- [F-Droid](https://f-droid.org/)
- Google Play Store
- [PARI/GP website downloads](https://pari.math.u-bordeaux.fr/download.html#paridroid)

## Documentation

- [PARI/GP User Manual](https://pari.math.u-bordeaux.fr/doc.html)
- [PariDroid Build Instructions](https://pari.math.u-bordeaux.fr/paridroid/index.html)
