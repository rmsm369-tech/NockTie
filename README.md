# Public Domain Explorer (PDE)

App Name: Nocktie

Public Domain Explorer is a multi-feature Android application focused on note-taking, lightweight AI interactions (via a secure web app hand-off), and small productivity utilities. This repository contains the app source and supporting scripts.

## Features

* **AI Chat:** Seamlessly integrated AI chat experience via external browser hand-off.
* **Note Taking:** Create and manage personal notes locally.
* **Game Center:** Built-in, offline-capable games including "Fino Tap". More games coming soon!
* **Smart Profile:** Track your usage sessions, location, and manage your account.
* **Shareable:** Share the actual APK file with friends directly from the app.

## Tech Stack

- Kotlin
- Jetpack Compose (Material3)
- Firebase Auth & Firestore
- Room (local notes)
- WorkManager (daily quote reminders)
- OkHttp (network calls)

## Quick build

Requirements:
- JDK 17
- Android SDK (compileSdk 34)
- Gradle (wrapper provided)

From the repository root, build the debug APK:

```powershell
.\gradlew :app:clean
.\gradlew :app:assembleDebug
```

The generated APK will be available at:

`app/build/outputs/apk/debug/app-debug.apk`

## App Configuration

- `local.properties` may contain secure worker endpoints (e.g. `WORKER_URL`, `WORKER_URL_PRIMARY`, `WORKER_URL_SECONDARY`). Do NOT commit API keys to source control.
- The app currently hands off AI interactions to: `https://rmsm369-tech.github.io/hoxip.ai/index.html`.

## Updates & Hosting

To support in-app update prompts, place a `version.json` file on your hosting with this structure:

```json
{ "latestVersion": "1.1", "downloadUrl": "https://yourhost/path/to/app-debug.apk" }
```

Implement the update-check in `HomeScreen` (a small fetch + compare against `BuildConfig.VERSION_NAME`) to prompt users when a new release is available.

## Sharing the APK

Two approaches:

- Share a hosted APK link (recommended): upload `app-debug.apk` to GitHub/GitHub Pages or another host and share the direct download URL.
- Share the actual APK file from the device: implement `FileProvider` in `AndroidManifest.xml`, add `res/xml/file_paths.xml`, and use `FileProvider.getUriForFile(...)` from `Profile`.

## Notes & Troubleshooting

- Google Sign-In will not work inside Android `WebView` due to secure-browser policies. The app opens the Hoxip web app in the external browser to avoid this restriction.
- If you see runtime crashes after edits, capture `adb logcat` output and open `crash.log` for diagnosis.

## Contributing

1. Fork the repo
2. Create a new branch
3. Make changes and open a pull request

## License

This project is public domain / MIT-style. Adjust the license file as needed.

---

If you want, I can also add a short `version.json` example to the hosted site or implement the `FileProvider` sharing flow inside the app.
