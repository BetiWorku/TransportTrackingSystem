# 📦 Release APK Folder

Place the compiled `.apk` file here before pushing to GitHub.

## How to Build the APK

1. Open the project in **Android Studio**.
2. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
3. Wait for the build to finish.
4. Click **"locate"** in the notification at the bottom right, or find the file at:
   `app/build/outputs/apk/debug/app-debug.apk`
5. Copy that `.apk` file into **this folder** (`release/`).
6. Rename it to something descriptive, e.g.:
   `TransportTrackingSystem-v1.0.apk`
7. Run:
   ```bash
   git add release/TransportTrackingSystem-v1.0.apk
   git commit -m "feat: add compiled release APK v1.0"
   git push
   ```
