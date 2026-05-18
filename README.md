# Addis Transport - Transport Tracking System 🚍

Addis Transport is a comprehensive Android-based real-time bus tracking and management system designed to improve the commuting experience in Addis Ababa. It provides users with live bus locations, estimated arrival times (ETA), and route planning, while giving administrators tools to manage the fleet, routes, and news updates.

## 🚀 Features

### For Users
*   **Live Map Tracking**: View real-time locations of buses on Google Maps.
*   **Trip Planner**: Find the fastest routes from your current station to your destination.
*   **Real-time Alerts**: Get notified about traffic congestion or delays.
*   **Detailed Bus Info**: See passenger occupancy, speed, and terminal information.
*   **News & Updates**: Stay informed with the latest transport system announcements.

### For Administrators
*   **Fleet Management**: Add, update, or remove buses from the network.
*   **Route & Stop Management**: Configure bus routes and specific station stops.
*   **News Posting**: Broadcast system-wide alerts and news to all users.
*   **Real-time Statistics**: Monitor active fleet status and capacity levels.

## 🛠 Tech Stack

*   **Language**: Kotlin
*   **UI Layout**: XML (Material Design)
*   **Backend**: Firebase (Authentication, Cloud Firestore)
*   **Maps**: Google Maps SDK for Android
*   **Networking**: JavaMail API (for email services)
*   **Architecture**: Fragment-based navigation with Activity controllers

## 📂 Project Structure

```text
TransportTrackingSystem/
├── app/
│   ├── src/main/java/com/example/transporttrackingsystem/
│   │   ├── activities/      # Screen controllers (Splash, Login, Main, etc.)
│   │   ├── fragments/       # Admin and User sub-screens
│   │   ├── adapters/        # RecyclerView adapters for lists
│   │   ├── models/          # Data classes (Bus, Route, News, etc.)
│   │   └── utils/           # Helper classes (Email, Notifications)
│   ├── src/main/res/        # Resources (Layouts, Drawables, Strings)
│   └── AndroidManifest.xml  # App configuration and permissions
├── Screenshots/             # Application visual documentation
│   ├── User/                # User interface screenshots
│   └── Admin/               # Admin dashboard screenshots
└── build.gradle.kts         # Build configuration
```

## ⚙️ Setup & Installation

### Prerequisites
*   **Android Studio**: Ladybug (or newer)
*   **JDK**: Version 11 or 17
*   **Firebase Account**: To connect the database and authentication.
*   **Google Maps API Key**: To enable map features.

### Build Instructions
1.  **Clone the repository**:
    ```bash
    git clone [repository-url]
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

<div align="center">
  <video src="Screenshots/Transport App Demo.mp4" width="100%" style="max-width: 800px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.15);" controls autoplay loop muted>
    Your browser does not support the video tag.
  </video>
  <br/>
  <p><i>Addis Transport Passenger App & Command Center in Action.</i></p>
</div>

> [!TIP]
> Place your screen recording inside the `Screenshots/` directory and name it `Transport App Demo.mp4` to display your demo video live on GitHub.

---
*Developed for Addis Ababa Transport Management.*
