## ✨ Project Overview

Addis Transport is a comprehensive, dual-platform ecosystem designed to revolutionize public commuting in Addis Ababa. It bridges the gap between daily commuters and transit authorities by providing accurate, real-time bus tracking and a powerful centralized management dashboard.

<div align="center">
  
| 📱 Passenger App (Android/Kotlin) | 💻 Admin Dashboard (React.js) |
| :--- | :--- |
| **Live Map Tracking:** Watch buses move in real-time. | **Enterprise Fleet Control:** Add, update, or remove buses. |
| **Smart Trip Planner:** Find the fastest routes to destinations. | **Terminal Config:** Geolocation-based route ordering. |
| **Instant ETA:** Get accurate arrival times based on speed. | **Command Center:** Monitor driver info & live capacity. |
| **Alerts & News:** Receive push notifications for delays. | **Broadcast Hub:** Send system-wide alerts instantly. |

</div>

---

## 🚀 Today's Major Updates (May 17, 2026)

*   📧 **Secure Email OTP Pipeline**: Re-implemented the OTP email delivery using standard **JavaMail SSL (Port 465)**, fixing the "Could not convert socket to TLS" error. Enabled secure end-to-end delivery of 6-digit codes to user inboxes.
*   🔒 **Secure OTP Flow**: Removed the on-screen helper code from the verification screen to ensure the security flow is authentic and the code is obtained exclusively from the user's inbox.
*   👥 **Admin User List Fixed**: Removed `limit(2)` restriction in the admin dashboard query to ensure **all registered users** show up in real-time.
*   🗑️ **Robust User Deletion**: Reorganized React hooks and fully repaired the 🗑️ **Delete User** action with prompt confirmation and direct Firestore synchronization. Secured administrator profiles from accidental deletion using case-insensitive check guards.

---

## 🛠️ Architecture & Tech Stack

### 📱 1. Mobile Passenger App (Native Android)
Built for speed, reliability, and smooth animations even on lower-end devices.
*   **Kotlin**: Google's official, highly-secure language preventing common crashes.
*   **MVVM Architecture**: Separates UI from logic, ensuring the app never crashes during screen rotations.
*   **Coroutines**: Handles heavy background tasks (like GPS polling) without freezing the UI.
*   **Google Maps SDK**: Renders custom dynamic markers seamlessly.

### 🌐 2. Web Admin Dashboard (React.js)
A robust command center built to handle thousands of live data points.
*   **React.js**: Component-based architecture for extremely fast map and data rendering without page reloads.
*   **Tailwind CSS**: Utility-first styling for a beautiful, responsive, "Glassmorphism" enterprise aesthetic.
*   **Context API**: Manages complex global states (like authenticated admin profiles).

### ☁️ 3. Shared Backend
*   **Firebase Firestore (NoSQL)**: Ultra-fast real-time database syncing across Web and Mobile simultaneously.
*   **Firebase Auth**: Secure, role-based access control.

---

## 📂 Complete Project Folder Blueprint

Here is the exact, comprehensive file-level folder blueprint of both the **Android Passenger App** and the **React Admin Panel**:

```text
TransportTrackingSystem/
├── 📱 app/ (Native Android Passenger App)
│   ├── src/main/java/com/example/transporttrackingsystem/
│   │   ├── 📺 activities/ (Screen UI Controllers)
│   │   │   ├── 🎬 SplashActivity.kt (Initial splash loader)
│   │   │   ├── 🚪 WelcomeActivity.kt (Onboarding flow entry)
│   │   │   ├── 🔑 LoginActivity.kt (Secure passenger login)
│   │   │   ├── 📝 RegisterActivity.kt (Passenger registration form)
│   │   │   ├── 📧 OtpVerificationActivity.kt (Secure 6-digit email OTP checking)
│   │   │   ├── 🏛️ MainActivity.kt (Passenger home navigation & terminal lists)
│   │   │   ├── 📍 BusTrackerActivity.kt (Real-time Google Maps bus tracking screen)
│   │   │   ├── ℹ️ BusDetailsActivity.kt (Trip metrics, occupancy & speed statistics)
│   │   │   ├── 💬 ComplaintActivity.kt (User ticket/feedback submission)
│   │   │   ├── 📰 UserNewsActivity.kt (Broadcasting announcements list)
│   │   │   ├── ⚙️ SettingsActivity.kt (Commuter profile management)
│   │   │   ├── 👤 UserDashboardActivity.kt (Ticket and session analytics)
│   │   │   ├── 🔒 ForgotPasswordActivity.kt (SMTP-based credential recovery request)
│   │   │   └── ✏️ ResetPasswordActivity.kt (Secure code confirmation and new pass entry)
│   │   ├── 🔌 adapters/ (Data RecyclerView Bridges)
│   │   │   ├── 🚍 BusAdapter.kt (Lists active buses and ETA details)
│   │   │   └── 🤝 SharedAdapters.kt (Reusable components for News & Complaints)
│   │   ├── 📦 models/ (Data blueprint structures)
│   │   │   └── 📐 Models.kt (Firebase structures: User, Bus, Route, News, Complaint)
│   │   └── 🔧 utils/ (Asynchronous helpers)
│   │       ├── 📨 EmailHelper.kt (SMTP client wrapper for secure SSL mailings)
│   │       └── 🔔 NotificationActionReceiver.kt (Dynamic system notifications trigger)
│   └── src/main/res/ (Material Design Assets & XML layouts)
│       ├── drawable/ (App icons and button vector assets)
│       ├── layout/ (XML layouts for all screens)
│       └── values/ (Color tokens, premium typography & strings configurations)
│
├── 💻 admin-dashboard-web/ (React Command Center Web Panel)
│   ├── src/
│   │   ├── 📺 main.jsx (React bootstrap initializer)
│   │   ├── 🚪 App.jsx (Routes manager & real-time background sync)
│   │   ├── 🤝 firebase.js (Web Firestore configuration)
│   │   ├── 🎨 index.css (Tailwind components & custom glassmorphism)
│   │   ├── 🧩 components/ (Web Widgets & Panels)
│   │   │   ├── 🔒 AdminLogin.jsx (Security check and authorization gateway)
│   │   │   └── 🎛️ DashboardComponents.jsx (Interactive panels for fleet, complaints & news)
│   │   └── 🖼️ assets/ (Branding SVG/PNG assets)
│   ├── package.json (Web dependencies & scripts)
│   └── vite.config.js (Vite compiler settings)
│
├── 🎥 Transport App Demo.mp4 (Live interactive demo video file at root)
├── build.gradle.kts (Kotlin Gradle root compilation config)
└── README.md (Comprehensive documentation hub)
```

---

## ⚙️ Setup & Installation

### Running the Web Dashboard
```bash
cd admin-dashboard-web
npm install
npm run dev
```
*(Runs securely on localhost with hot-module reloading)*

### Build Instructions
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/BetiWorku/TransportTrackingSystem.git
    ```
2.  **Open in Android Studio**:
    *   File > Open > Select `TransportTrackingSystem` folder.
3.  **Configure Firebase**:
    *   Place your `google-services.json` file in the `app/` directory.
    *   Ensure Firestore and Authentication are enabled in your Firebase Console.
4.  **Configure Maps API**:
    *   Add your API Key in `AndroidManifest.xml` under:
        ```xml
        <meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR_KEY_HERE"/>
        ```
5.  **Sync Gradle**:
    *   Click "Sync Project with Gradle Files" in the top bar.
6.  **Run**:
    *   Select your emulator or physical device and click the **Run** button.

## 🎥 Application Demo Video
https://github.com/user-attachments/assets/20018d8d-1fc8-40fc-b6e8-c327955cfc4a
