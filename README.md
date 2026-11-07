# NexaMusic Air

NexaMusic Air is a lightweight Android music player built with Kotlin + Jetpack Compose + Media3, designed to preserve the NexaMusic feel while reducing runtime overhead.

## Stack

- Kotlin
- Jetpack Compose + Material 3
- Android Media3 (ExoPlayer + MediaSessionService)
- Coroutines + Flow
- Room (minimal playlist schema)
- DataStore Preferences
- Gradle Kotlin DSL

## Core modules

```
app/src/main/java/com/nexamusic/air/
  data/
  network/innertube/
  playback/
  ui/
  navigation/
```

## Included features

- YouTube Music search via lightweight InnerTube client
- Stream URL resolution and playback
- Local music playback via MediaStore
- Background playback through `MediaSessionService`
- Lock-screen / notification-compatible playback controls
- Mini player + full-screen player
- Queue playback controls (play/pause, next/prev, shuffle, repeat)
- Synced LRC lyrics parser and active-line selection
- Basic settings screen

## Build instructions

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

Generated APKs:

- Debug: `app/build/outputs/apk/debug/`
- Release (arm64-v8a split): `app/build/outputs/apk/release/`

## Optimization choices (Air)

- Release minification + resource shrinking enabled
- arm64-v8a ABI split enabled
- Minimal dependency set (no non-essential integrations)
- Compose-first UI with simple recomposition-friendly state flows
- Small Room schema and lightweight DataStore usage

## APK size and performance comparison

Fill after first successful build/profile run:

| Metric | NexaMusic (base) | NexaMusic Air |
|---|---:|---:|
| Debug APK size | - | - |
| Release APK size | - | - |
| Cold start (ms) | - | - |
| Idle RAM (MB) | - | - |

## License

This project follows GPL-3.0 compatibility expectations from the original NexaMusic lineage. Keep attribution and license obligations when distributing derivatives.
