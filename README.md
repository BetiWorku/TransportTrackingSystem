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

---

## 🔒 DevOps, Database Architecture & Systems Security (Member 4 Specs)

As the Backend Architect, Database Specialist & DevOps Lead, the core database models, release configurations, and database rules have been finalized to ensure high performance, secure communication, and optimized application compilation.

### 🗄️ 1. Database Collections & Architecture
Below is the optimized Firestore database collections structure. Each collection matches a shared Kotlin data model in `Models.kt` to ensure seamless mobile serialization, as well as React component rendering:

| Collection | Firestore Document ID | Core Data Fields | Description |
| :--- | :--- | :--- | :--- |
| **`users`** | Firebase Auth `UID` | `name` (Str), `email` (Str), `role` (Str), `isVerified` (Bool), `otp` (Str), `createdAt` (Timestamp) | Manages commuter profiles, authentication roles, and secure OTP verification status. |
| **`buses`** | Vehicle ID (e.g., `Sheger-01`) | `busId` (Str), `busNumber` (Str), `busName` (Str), `busType` (Str), `routeId` (Str), `terminal` (Str), `capacity` (Int), `passengers` (Int), `latitude` (Double), `longitude` (Double), `currentStop` (Str), `nextStop` (Str), `speed` (Double), `driverName` (Str), `driverPhone` (Str), `status` (Str) | Holds live coordinates, speed metrics, load status, and driver contacts for active vehicles. |
| **`routes`** | Route UUID | `routeId` (Str), `routeName` (Str), `busNumber` (Str) | Configures the transit routes running across Addis Ababa. |
| **`stops`** | Stop UUID | `stopId` (Str), `stopName` (Str), `latitude` (Double), `longitude` (Double), `routeId` (Str), `stopOrder` (Int) | Holds geolocations of specific terminals ordered chronologically for path tracing. |
| **`trips`** | Trip UUID | `tripId` (Str), `userId` (Str), `busNumber` (Str), `entryStop` (Str), `exitStop` (Str?), `status` (Str), `timestamp` (Timestamp) | Logs live passenger boarding sessions to track transit volume. |
| **`news`** | Broadcast UUID | `newsId` (Str), `title` (Str), `content` (Str), `author` (Str), `timestamp` (Timestamp) | Contains system-wide alerts and delay broadcasts compiled by administrators. |
| **`complaints`** | Ticket UUID | `id` (Str), `userId` (Str), `userEmail` (Str), `subject` (Str), `message` (Str), `status` (Str), `adminReply` (Str), `timestamp` (Timestamp) | Stores support tickets submitted by commuters and replies sent from the React Panel. |

---

### 🛡️ 2. Production Firebase Firestore Security Rules
To protect personal commuter data and prevent unauthorized route or vehicle updates, role-based access control (RBAC) has been securely configured. 

Below is the certified production Firestore rule configuration (stored globally as `firestore.rules`):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // --- Helper Functions ---
    function isAuth() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isAuth() && request.auth.uid == userId;
    }
    
    function isAdmin() {
      return isAuth() && 
        (request.auth.token.email.toLowerCase() == 'bwwmas@gmail.com' || 
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'Admin');
    }

    // --- Users Collection Rules ---
    match /users/{userId} {
      allow read: if isOwner(userId) || isAdmin();
      allow create: if isAuth() && request.auth.uid == userId;
      allow update: if isOwner(userId) || isAdmin();
      allow delete: if isAdmin();
    }

    // --- Buses Collection Rules ---
    match /buses/{busId} {
      allow read: if isAuth();
      allow write: if isAdmin();
    }

    // --- Routes Collection Rules ---
    match /routes/{routeId} {
      allow read: if isAuth();
      allow write: if isAdmin();
    }

    // --- Stops Collection Rules ---
    match /stops/{stopId} {
      allow read: if isAuth();
      allow write: if isAdmin();
    }

    // --- Trips Collection Rules ---
    match /trips/{tripId} {
      allow read: if isAuth() && (resource.data.userId == request.auth.uid || isAdmin());
      allow create: if isAuth() && request.resource.data.userId == request.auth.uid;
      allow update: if isAuth() && (resource.data.userId == request.auth.uid || isAdmin());
      allow delete: if isAdmin();
    }

    // --- News Collection Rules ---
    match /news/{newsId} {
      allow read: if isAuth();
      allow write: if isAdmin();
    }

    // --- Complaints Collection Rules ---
    match /complaints/{complaintId} {
      allow read: if isAuth() && (resource.data.userId == request.auth.uid || resource.data.userEmail == request.auth.token.email || isAdmin());
      allow create: if isAuth() && request.resource.data.userId == request.auth.uid;
      allow update: if isAuth() && (resource.data.userId == request.auth.uid || isAdmin());
      allow delete: if isAuth() && (resource.data.userId == request.auth.uid || isAdmin());
    }
  }
}
```

---

### 📦 3. DevOps Production Optimizations (ProGuard & R8)
To prevent code reverse-engineering and shrink the release APK size, a robust ProGuard rule file has been built under `app/proguard-rules.pro`. 

Key components protected by these rules:
- **Firebase Firestore Mapping Reflection**: Prevents model field obfuscation (e.g. mapping of `busNumber` from Firestore to Kotlin) which would otherwise crash on the release build.
- **Secure JavaMail (com.sun.mail) API**: Keeps network sockets and protocol classes open to send OTP emails securely in the background.
- **Kotlin Serialization & Metadata**: Optimizes runtime speed and handles coroutines without overhead.

---

### 🚀 4. DevOps Deployment Instructions

#### Deploying Security Rules via Firebase CLI
1. Initialize firebase on your machine if not already done:
   ```bash
   npm install -g firebase-tools
   firebase login
   firebase init firestore
   ```
2. Write the rules into `firestore.rules` at your project root.
3. Deploy the rules directly to production:
   ```bash
   firebase deploy --only firestore:rules
   ```

#### Building the Android Release APK
To compile a secure, optimized release build using the defined ProGuard settings, run the following Gradle task:
```bash
./gradlew assembleRelease
```
The optimized APK will be built under `app/build/outputs/apk/release/app-release.apk` with obfuscated source code and reduced resource file size.

