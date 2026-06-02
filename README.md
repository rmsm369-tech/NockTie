# Public Domain Explorer (PDE)
App Name: Nocktie
Public Domain Explorer is a multi-feature Android application focused on note-taking, lightweight AI interactions (via a secure web app hand-off), and small productivity utilities. This repository contains the app source and supporting scripts.
## Features
 * **AI Chat:** Seamlessly integrated AI chat experience via external browser hand-off.
 * **Note Taking:** Create and manage personal notes locally.
 * **Game Center:** Built-in, offline-capable games including "Fino Tap". More games coming soon!
 * **Smart Profile:** Track your usage sessions, location, and manage your account.
 * **Shareable:** Share the actual APK file with friends directly from the app.
 * **Telegram Bot Sync (v1.1+):** Sync your application account to a secure Telegram companion bot using an account handshake (/start <token>) to chat with the AI chain directly with active session and quota tracking.
## Tech Stack
 * Kotlin
 * Jetpack Compose (Material3)
 * Firebase Auth & Firestore
 * Room (local notes)
 * WorkManager (daily quote reminders)
 * OkHttp (network calls)
 * Cloudflare Workers (Edge routing, caching, and autonomous multi-model API fallback pipelines)
## Quick build
Requirements:
 * JDK 17
 * Android SDK (compileSdk 34)
 * Gradle (wrapper provided)
From the repository root, build the debug APK:
```powershell
.\gradlew :app:clean
.\gradlew :app:assembleDebug

```
The generated APK will be available at:
app/build/outputs/apk/debug/app-debug.apk
To compile a signed production release build:
```powershell
.\gradlew :app:assembleRelease

```
The production APK will be available at:
app/build/outputs/apk/release/app-release.apk
## App Configuration
 * local.properties may contain secure worker endpoints (e.g. WORKER_URL, WORKER_URL_PRIMARY, WORKER_URL_SECONDARY). Do NOT commit API keys to source control.
 * The app currently hands off AI interactions to: [https://rmsm369-tech.github.io/hoxip.ai/index.html](https://rmsm369-tech.github.io/hoxip.ai/index.html).
 * **Backend Architecture:** Multi-model fallback execution utilizes an array routing matrix through Google Gemini (gemini-1.5-flash), Mistral AI (mistral-tiny), NVIDIA NIM (llama-3-1-8b-instruct), and SiliconFlow (DeepSeek-V3).
## Updates & Hosting
To support in-app update prompts, place a version.json file on your hosting with this structure:
```json
{ "latestVersion": "1.1", "downloadUrl": "https://yourhost/path/to/app-debug.apk" }

```
Implement the update-check in HomeScreen (a small fetch + compare against BuildConfig.VERSION_NAME) to prompt users when a new release is available.
## Sharing the APK
Two approaches:
 * Share a hosted APK link (recommended): upload app-debug.apk to GitHub/GitHub Pages or another host and share the direct download URL.
 * Share the actual APK file from the device: implement FileProvider in AndroidManifest.xml, add res/xml/file_paths.xml, and use FileProvider.getUriForFile(...) from Profile.
## Notes & Troubleshooting
 * Google Sign-In will not work inside Android WebView due to secure-browser policies. The app opens the Hoxip web app in the external browser to avoid this restriction.
 * If you see runtime crashes after edits, capture adb logcat output and open crash.log for diagnosis.
 * **Windows File System Locks:** If gradle clean fails with an IOException folder access lock, kill the background compiler runtimes via PowerShell using taskkill /F /IM java.exe and ./gradlew --stop before rebuilding.
## Contributing
 1. Fork the repo
 2. Create a new branch
 3. Make changes and open a pull request
## License
This project is public domain / MIT-style. Adjust the license file as needed.
## 🛠️ Changelog & Version History
### v1.1
 * **Telegram Companion Integration:** Engineered cross-platform account mapping logic using secure short-code links inside Firestore.
 * **Edge Routing Performance:** Replaced redundant cryptographic token operations in the backend worker with an in-memory global token caching mechanism, dropping routing latency to milliseconds.
 * **Quota Indicators:** Added visible usage tracking boxes (📱 [Quota Remaining: X messages]) directly inside egress chat blocks.
 * **Autonomous Pipeline Guardrails:** Deployed strict timeout controls on open interaction semantic data components (Wikipedia, ArXiv, Open-Meteo) to protect worker runtime cycles.
