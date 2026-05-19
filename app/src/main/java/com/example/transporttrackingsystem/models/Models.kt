package com.example.transporttrackingsystem.models

import com.google.firebase.Timestamp

/**
 * User — Represents the profile and authorization state of a registered passenger or administrator.
 * Synced with Firestore collection: "users"
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Commuter", // Admin, Commuter
    val isVerified: Boolean = false,
    val otp: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * Route — Represents a defined bus transit line in Addis Ababa.
 * Synced with Firestore collection: "routes"
 */
data class Route(
    val routeId: String = "",
    val routeName: String = "",
    val busNumber: String = ""
)

/**
 * Stop — Represents an individual geolocated boarding/drop-off station along a route.
 * Synced with Firestore collection: "stops"
 */
data class Stop(
    val stopId: String = "",
    val stopName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val routeId: String = "",
    val stopOrder: Int = 0
)

/**
 * Trip — Records an individual passenger's commute session from an entry terminal to their destination.
 * Synced with Firestore collection: "trips"
 */
data class Trip(
    val tripId: String = "",
    val userId: String = "",
    val busNumber: String = "",
    val entryStop: String = "",
    val exitStop: String? = null,
    val status: String = "onboard", // onboard, completed
    val timestamp: Timestamp = Timestamp.now()
)

/**
 * Bus — Renders all core active properties and real-time geolocations of transit vehicles.
 * Synced with Firestore collection: "buses"
 */
data class Bus(
    val busId: String = "",
    val busNumber: String = "",
    val busName: String = "",
    val busType: String = "",
    val routeId: String = "",
    val terminal: String = "",
    val capacity: Int = 30,
    val passengers: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val currentStop: String = "In Transit",
    val nextStop: String = "Calculating",
    val speed: Double = 0.0,
    val driverName: String = "Unknown",
    val driverPhone: String = "N/A",
    val status: String = "Active",
    val createdAt: Timestamp? = null
)

/**
 * News — Defines general broadcast notifications and route delay alerts.
 * Synced with Firestore collection: "news"
 */
data class News(
    val newsId: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "Admin",
    val timestamp: Timestamp = Timestamp.now()
)

/**
 * Complaint — Allows passengers to submit complaints/feedback directly to administrators.
 * Synced with Firestore collection: "complaints"
 */
data class Complaint(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "pending", // pending, resolved
    val adminReply: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

