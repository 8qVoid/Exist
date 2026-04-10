# Exist

Exist is a Kotlin Android app built with Jetpack Compose focused on daily memory capture: "Proof you existed today."

## Built With

### Android stack
- Kotlin
- Jetpack Compose (Material 3)
- MVVM architecture
- Navigation Compose
- Room (local database)
- DataStore Preferences (local settings + local auth persistence)
- CameraX (photo capture + optional video challenge capture)
- Coil (image loading)
- WorkManager (random local prompt notifications)

### Tooling
- Android Gradle Plugin `9.1.0`
- Kotlin `2.2.10`
- Gradle `9.3.1`
- Java 17 (Android Studio JBR)

### Key libraries
- `androidx.compose` BOM `2025.02.00`
- `androidx.navigation:navigation-compose:2.9.4`
- `androidx.room:room-runtime/ktx:2.7.2`
- `androidx.work:work-runtime-ktx:2.10.3`
- `androidx.datastore:datastore-preferences:1.1.7`
- `androidx.camera:*:1.4.2` (core, camera2, lifecycle, view, video)
- `io.coil-kt:coil-compose:2.7.0`

## Features
- Local email/password auth (no OTP required)
- Profile onboarding (full name, birthday, profile photo URI)
- Daily proof capture (minimum one, supports multiple photos per day)
- Emotion tagging and optional caption
- Temporary memory mode with expiry filtering
- Day detail, edit, delete
- Recap slideshow
- Archive/timeline
- Tabs: Dashboard, Highlights, Analytics, Profile
- Analytics: pie chart + trend charts (`7d` / `30d`)
- Camera: flip, flash, tap-to-focus, shutter sound
- Optional random 10-second video challenge
- Optional random daily prompt notification

## User Flow (How To Use)
1. Open app and create account (local on device).
2. Complete onboarding profile.
3. On Dashboard, tap `Take Today's Proof`.
4. Capture a photo, add emotion/caption, then:
   - `Save + Add Another` to keep capturing today
   - `Save + Finish` to return home
5. Open:
   - `Today` for full day detail
   - `Recap` for slideshow
   - `Archive` for past days
6. Use `Profile` / `Settings` to control reminders, temporary duration, and challenge mode.
7. Check `Analytics` for activity and emotion trends.

## How It Works (Technical Flow)
1. `CameraScreen` captures media via CameraX.
2. `CaptureViewModel` prepares metadata (emotion, caption, temporary flag).
3. `MemoryRepository` saves `MemoryPhoto` to Room.
4. Room flows update Home/Archive/Day/Recap/Analytics screens reactively.
5. Expired temporary memories are filtered from queries.
6. App settings and auth session/account data are persisted with DataStore.

## Run (Android)
1. Open in Android Studio.
2. Build debug APK:
   ```bash
   ./gradlew :app:assembleDebug
   ```
   On Windows:
   ```bat
   .\gradlew.bat :app:assembleDebug
   ```
3. APK output:
   - `app/build/outputs/apk/debug/app-debug.apk`

## GitHub

If folder is not a git repo yet:
```bash
git init
git add .
git commit -m "Initial Exist app commit"
git branch -M main
git remote add origin https://github.com/<your-username>/<repo-name>.git
git push -u origin main
```

If folder is already a git repo:
```bash
git add .
git commit -m "Update app and README"
git push
```

## Recommended .gitignore
Do not commit:
- `local.properties`
- `.gradle/`
- `.gradle-user-home/`
- `app/build/`
- `backend/node_modules/`
- secret files like `backend/.env`
