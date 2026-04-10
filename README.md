# Exist

Exist is a Kotlin Android app built with Jetpack Compose focused on daily memory capture: “Proof you existed today.”

## Built With

### Android stack
- Kotlin
- Jetpack Compose (Material 3)
- MVVM architecture
- Navigation Compose
- Room (local database)
- DataStore Preferences (local app settings + local auth persistence)
- CameraX (photo capture, video challenge capture)
- Coil (image loading)
- WorkManager (random local prompt notifications)

### Tooling
- Android Gradle Plugin `9.1.0`
- Kotlin `2.2.10`
- Gradle `9.3.1`
- Java 17 (Android Studio JBR)

### Key libraries (current project)
- `androidx.compose` BOM `2025.02.00`
- `androidx.navigation:navigation-compose:2.9.4`
- `androidx.room:room-runtime/ktx:2.7.2`
- `androidx.work:work-runtime-ktx:2.10.3`
- `androidx.datastore:datastore-preferences:1.1.7`
- `androidx.camera:*:1.4.2` (core, camera2, lifecycle, view, video)
- `io.coil-kt:coil-compose:2.7.0`
- `com.squareup.retrofit2:retrofit:2.11.0` (kept in project)
- `com.squareup.okhttp3:logging-interceptor:4.12.0` (kept in project)

## Current App Features
- Email/password local auth (no OTP required)
- Profile onboarding (name, birthday, profile photo URI)
- Daily proof photo capture
- Multiple photos per day
- Emotion tagging + optional captions
- Temporary memory mode with expiry filtering
- Day detail and memory edit/delete
- Recap slideshow view
- Archive/timeline view
- Tabbed main UI: Dashboard, Highlights, Analytics, Profile
- Analytics charts (emotion and activity trends)
- Camera controls: flip, flash, tap-to-focus, shutter sound
- Optional random 10-second video challenge
- Optional random daily notification prompt

## Run (Android)
1. Open the project in Android Studio.
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

## Upload To GitHub

If this folder is **not** a git repo yet:
```bash
git init
git add .
git commit -m "Initial Exist app commit"
git branch -M main
git remote add origin https://github.com/<your-username>/<repo-name>.git
git push -u origin main
```

If this folder is already a git repo:
```bash
git add .
git commit -m "Update Exist app and README"
git push
```

## Recommended .gitignore notes
Do not commit:
- `local.properties`
- `.gradle/`
- `.gradle-user-home/`
- `app/build/`, `backend/node_modules/`
- secret env files like `backend/.env`
