# Transport Tracking System (Addis Bus Tracker) - Project Documentation

## 1. Introduction
The **Transport Tracking System (Addis Bus Tracker)** is a comprehensive, real-time public transportation management and tracking solution designed for commuters and administrators in Addis Ababa. The system bridges the gap between public transport operators and daily commuters by providing live bus tracking, accurate estimated time of arrivals (ETAs), route planning, and a streamlined channel for user feedback. 

The project is divided into two main components:
1. **Commuter Mobile Application**: An Android application built with Kotlin, allowing users to track buses, plan trips, receive notifications, and submit complaints.
2. **Admin Web Dashboard**: A React.js web application for administrators to monitor the fleet, manage routes, handle complaints, and broadcast news/announcements.
Both platforms are connected via **Firebase**, which provides real-time database capabilities (Firestore) and secure user authentication.

## 2. Problem Statement
Public transportation in fast-growing cities like Addis Ababa often suffers from unpredictability and inefficiency. Commuters frequently face:
* **Uncertain Waiting Times:** Lack of real-time tracking leads to long, frustrating waits at bus stops.
* **Overcrowding:** Commuters have no way of knowing if an approaching bus is already full.
* **Poor Communication:** Sudden route changes, delays, or emergencies are not communicated effectively to passengers.
* **Lack of Administrative Oversight:** Transport authorities struggle to monitor bus performance, track active fleets, and efficiently resolve commuter complaints.

**Solution:** This project solves these issues by providing a centralized system where buses are tracked in real-time. Commuters can view live ETAs, check passenger capacity, and get notified of arrivals, while admins can manage the entire fleet from a dedicated web dashboard.

---

## 3. Folder Structure & Descriptions

The project is structured into two main directories: the mobile app and the admin web dashboard.

### `admin-dashboard-web/` (Admin Web Dashboard)
This directory contains the React.js application used by transport administrators.
*   **`src/`**: The main source code directory.
    *   **`components/`**: Contains UI components.
        *   `AdminLogin.jsx`: Handles administrator authentication.
        *   `DashboardComponents.jsx`: Contains all dashboard views (Fleet Management, Live Map, Complaints, News, Routes, Terminals, Users).
    *   **`App.jsx`**: The main application routing and sidebar navigation logic.
    *   **`firebase.js`**: Firebase configuration and initialization for the web.
    *   **`index.css`**: Global Tailwind CSS and styling configurations.
*   **`vite.config.js`**: Configuration for the Vite build tool.
*   **`package.json`**: NPM dependencies and scripts.

### `app/` (Android Mobile Application)
This directory contains the Kotlin-based Android application for commuters.
*   **`src/main/java/com/example/transporttrackingsystem/`**: The core Kotlin source code.
    *   **`activities/`**: Contains the UI controllers (Screens).
        *   `MainActivity.kt`: The core map interface, trip planning, and live tracking UI.
        *   `BusTrackerActivity.kt`: Detailed real-time tracking, ETA countdowns, and progress bars.
        *   `LoginActivity.kt` / `RegisterActivity.kt` / `OtpVerificationActivity.kt`: User authentication and onboarding.
        *   `UserDashboardActivity.kt`: Overview of system metrics, active fleet, and quick actions.
        *   `ComplaintActivity.kt`: Interface for submitting and viewing feedback/reports.
        *   `SettingsActivity.kt`: User preferences (Language, Dark Mode, Font Size, Profile).
    *   **`adapters/`**: RecyclerView adapters for displaying lists (e.g., `BusAdapter.kt` for nearby buses, `UserComplaintAdapter`).
    *   **`models/`**: Data classes representing Firestore documents (`Models.kt` containing `Bus`, `Route`, `Stop`, `Trip`, `Complaint`, `News`).
    *   **`utils/`**: Helper utilities.
        *   `EmailHelper.kt`: Sends OTPs and welcome emails via SMTP.
        *   `NotificationActionReceiver.kt`: Handles notification actions (e.g., muting).
*   **`src/main/res/`**: Android resources.
    *   **`layout/`**: XML layout files for all activities and list items.
    *   **`values/`**: Global resources (`strings.xml`, `colors.xml`, `themes.xml`). Note: Includes localization folders (`values-am`, `values-om`, `values-ti`, `values-so`) for Amharic, Oromiffa, Tigrinya, and Somali.
    *   **`drawable/` & `mipmap/`**: Icons, images, and visual assets.
*   **`src/main/AndroidManifest.xml`**: Application configuration, defining activities, permissions (Location, Notifications, Internet), and Google Maps API keys.

---

## 4. Key Features

### Commuter Mobile App
*   **Multi-language Support:** English, Amharic, Afaan Oromoo, Tigrinya, and Somali.
*   **Trip Planning:** Search for routes from a starting point to a destination.
*   **Real-Time Tracking & ETAs:** View live bus locations on Google Maps, distance countdowns, and estimated arrival times.
*   **Capacity Monitoring:** View how many passengers are currently on a bus.
*   **OTP & Email Verification:** Secure registration with email OTP verification.
*   **Push Notifications:** Alerts when a bus is 15, 10, 5, or 2 minutes away.
*   **Feedback System:** Submit complaints directly to the admin and receive replies.

### Admin Web Dashboard
*   **Fleet Management:** View, add, edit, or remove buses from the system.
*   **Live Map Monitoring:** Track the entire active fleet simultaneously on a web map.
*   **Complaints Resolution:** Read commuter complaints and reply to them directly.
*   **Route & Terminal Management:** Define bus routes and designate terminal stops.
*   **Announcements:** Publish news and traffic alerts that instantly appear on users' mobile apps.

---

## 5. System Limitations & Future Work

While the Transport Tracking System provides a robust framework, it has certain limitations:
1.  **Dependency on Active Internet Connection:** Both the mobile app and the dashboard require a constant internet connection to fetch real-time Firebase updates.
2.  **GPS Accuracy & Battery Consumption:** Continuous location tracking on the mobile device consumes significant battery power. Poor GPS signals in certain areas can lead to temporary ETA inaccuracies.
3.  **Hardware Dependency for Buses:** For the system to work in the real world, buses must be equipped with GPS tracking hardware or the driver must actively use a driver-side application to broadcast their location. Currently, the system supports a simulated "Mock Test Active" mode for demonstration purposes.
4.  **Capacity Tracking Method:** Passenger counts currently rely on manual updates or automated ticket scans. A hardware integration (e.g., automated people counters at bus doors) would improve accuracy.

---

## 6. Important Code References for Presentation

When presenting the code, you can highlight these core mechanisms:

### A. Real-Time Tracking Logic (Android)
Located in `app/src/main/java/.../activities/BusTrackerActivity.kt`:
```kotlin
// Listens to Firestore for live bus coordinate updates
busListener = db.collection("buses").document(busId)
    .addSnapshotListener { snap, _ ->
        val busLat  = snap.getDouble("latitude")  ?: 0.0
        val busLng  = snap.getDouble("longitude") ?: 0.0
        // ... updates UI and recalculates ETA based on distance to the user's stop
    }
```

### B. Admin Dashboard Statistics (React)
Located in `admin-dashboard-web/src/App.jsx`:
```javascript
// Real-time listener for pending complaints to display in the sidebar badge
useEffect(() => {
  const q = query(collection(db, "complaints"), where("status", "==", "pending"));
  return onSnapshot(q, (snap) => setPendingComplaints(snap.size));
}, []);
```

### C. Email OTP Verification
Located in `app/src/main/java/.../utils/EmailHelper.kt`:
```kotlin
// Sends an OTP via SMTP for secure account verification
fun sendOTPEmail(context: Context, userEmail: String, userName: String, otpCode: String) {
    // ... configures javax.mail Session and Transport
    val message = MimeMessage(session)
    message.subject = "Your Addis Bus Tracker Verification Code"
    message.setContent("... HTML content with $otpCode ...", "text/html; charset=utf-8")
    Transport.send(message)
}
```

### D. Performance Testing (k6)
Located in `test.js` at the root directory:
```javascript
import http from 'k6/http';
import { sleep, check } from 'k6';

// Simulates load testing of the Firebase Firestore endpoints
// Example: Ramping up to 50 concurrent users
export const options = {
  stages: [
    { duration: '30s', target: 20 }, 
    { duration: '1m', target: 50 },  
    { duration: '20s', target: 0 }, 
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
  },
};
```

---

## 7. Screenshots Guide
*For your presentation, you should capture screenshots of the following screens directly from your running application:*

**User Mobile App:**
1.  **Splash & Login Screen** (Demonstrates multi-language selection)
2.  **OTP Verification Screen** (Demonstrates security)
3.  **Home Map View** (Showing the Google Map with Start/Destination markers and polylines)
4.  **Live Bus Tracker (`BusTrackerActivity`)** (Showing the ETA countdown, distance, and progress bar)
5.  **Settings Menu** (Dark mode toggle, font size slider)

**Admin Web Dashboard:**
1.  **Dashboard Overview** (Showing total buses, active fleet, total users)
2.  **Live Map** (Showing multiple buses moving on the web interface)
3.  **Complaints Management** (Showing pending user reports and the reply box)
4.  **Register Bus / Route Management** (Showing administrative capabilities)
