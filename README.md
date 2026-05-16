# Addis Transport - Transport Tracking System 🚍

Addis Transport is a comprehensive real-time bus tracking and management suite designed to improve the commuting experience in Addis Ababa. It features a native Android application for passengers and a premium React-based Web Dashboard for fleet administrators.

## 🌟 Today's Major Updates (May 16, 2026)
*   **Premium Admin Gateway**: Redesigned the Web Login into a high-end, two-column enterprise interface with real-time fleet telemetry.
*   **Full Mobile Responsiveness**: Implemented a slide-over navigation system and centered administrative forms for perfect mobile dashboard management.
*   **Live Data Integration**: Connected the login screen to live Firestore metrics to show active vehicle counts instantly.
*   **Optimized Workflows**: Synchronized notification badges with database states for accurate real-time alerting.

## 🚀 Key Modules

### 📱 Android Application (Passengers)
*   **Live Map Tracking**: View real-time locations of buses on Google Maps.
*   **Trip Planner**: Find the fastest routes from your current station to your destination.
*   **Real-time Alerts**: Get notified about traffic congestion or delays.
*   **News & Updates**: Stay informed with the latest transport system announcements.

### 💻 Web Dashboard (Administrators)
*   **Enterprise Fleet Control**: Add, update, or remove buses with a high-density management interface.
*   **Terminal & Route Configuration**: Geolocation-based station setup and route ordering.
*   **Live Command Center**: Monitor fleet status, driver information, and passenger capacity in real-time.
*   **System Notifications**: Broadcast news and alerts to all connected mobile users.

## 🛠 Tech Stack & Architecture

### Mobile App (Android/Kotlin)
*   **Language**: **Kotlin** - Google's official language for Android. Used because it is safe, prevents common crashes (like null pointer exceptions), and makes code highly efficient.
*   **Architecture**: **MVVM** (Model-View-ViewModel) - Separates UI from data logic, preventing app crashes when the device screen is rotated.
*   **Asynchronous Tasks**: **Kotlin Coroutines** - Handles background tasks (like fetching location) smoothly without freezing the app interface.
*   **UI & Maps**: XML Layouts, Material Design, Google Maps SDK.

### Web Dashboard (React.js)
*   **Framework**: **React.js** - Component-based architecture allows us to build complex, reusable UI elements efficiently and update live map data without reloading the page.
*   **Styling**: **Tailwind CSS** - A utility-first CSS framework that allows for rapid, beautiful, and responsive design directly in the code without writing bloated CSS files.
*   **State Management**: Context API / React Hooks.
*   **Backend for both**: Firebase Firestore (NoSQL), Authentication, and Realtime data sync.

## 📂 Project Folder Structures & Purpose

### 1. Passenger Mobile App (`app/src/main/`)
The native Android app uses the following structured directories to maintain clean and scalable code:
```text
app/src/main/
├── java/com/example/transporttrackingsystem/
│   ├── activities/      # Contains the main UI screens (like Map or Login). Controls user interactions.
│   ├── adapters/        # Acts as a bridge between raw data (e.g., a list of buses) and the UI elements (RecyclerViews).
│   ├── models/          # Defines the data blueprints (e.g., how a 'Bus' or 'Complaint' is represented in code).
│   ├── network/         # Manages internet calls, API requests, and Firebase database connections securely.
│   ├── utils/           # Helper functions (formatting time, calculating distances) used across the app to avoid repeating code.
│   └── viewmodels/      # Stores UI data safely so it isn't lost if the app state changes (e.g., getting a phone call).
└── res/
    ├── drawable/        # Contains images, icons, and custom XML shape definitions.
    └── layout/          # The visual design rules (XML layout files) for all screens.
```

### 2. Admin Web Dashboard (`admin-dashboard-web/`)
The React dashboard is organized into logical feature modules:
```text
admin-dashboard-web/src/
├── assets/              # Stores static files like logos, images, and fonts.
├── components/          # Reusable UI parts (like buttons, sidebars, or map cards). Built once and used everywhere.
├── context/             # Manages "Global State" (e.g., holding the currently logged-in Admin's profile data).
├── hooks/               # Custom React functions handling reusable logic (like authentication checks).
├── pages/               # The main full-screen views (e.g., Dashboard page, Fleet Management page).
└── utils/               # Helper JavaScript functions (like sorting algorithms for data tables).
```

## ⚙️ Setup & Installation

### Web Dashboard
```bash
cd admin-dashboard-web
npm install
npm run dev
```

### Android App
1.  Open the root folder in **Android Studio**.
2.  Add your `google-services.json` to the `app/` folder.
3.  Sync Gradle and Run.

## 📸 Screenshots

### User Application Flow
| Splash & Welcome | Registration & Login | Map & Tracking |
| :---: | :---: | :---: |
| ![Splash](Screenshots/User/2_App_Starts.jpg) | ![Register](Screenshots/User/3_Register_User.jpg) | ![Search](Screenshots/User/8_Search.jpg) |
| ![Welcome](Screenshots/User/7_Welcome_User.jpg) | ![Login](Screenshots/User/4_Login_Page.jpg) | ![Live Track](Screenshots/User/13_Track_Live_Anbesa_Bus.jpg) |

### Admin Management Flow
| Dashboard | Fleet Stats | Terminals & Routes |
| :---: | :---: | :---: |
| ![Admin Dash](Screenshots/Admin/3_Fleet_Main_Dashboard.jpg) | ![Fleet Stats](Screenshots/Admin/4_Fleet_Statics.jpg) | ![Terminals](Screenshots/Admin/6_Manage_Terminal.jpg) |
| ![Register Bus](Screenshots/Admin/8_Register_Bus.jpg) | ![Settings](Screenshots/Admin/9_Setting.jpg) | ![News](Screenshots/Admin/10_News.jpg) |

---
*Developed for Addis Ababa Transport Management.*
