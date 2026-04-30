package com.example.transporttrackingsystem

import com.google.firebase.Timestamp

data class Route(
    val routeId: String = "",
    val routeName: String = "",
    val busNumber: String = ""
)

data class Stop(
    val stopId: String = "",
    val stopName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val routeId: String = "",
    val stopOrder: Int = 0
)

data class Trip(
    val tripId: String = "",
    val userId: String = "",
    val busNumber: String = "",
    val entryStop: String = "",
    val exitStop: String? = null,
    val status: String = "onboard", // onboard, completed
    val timestamp: Timestamp = Timestamp.now()
)

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

data class News(
    val newsId: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "Admin",
    val timestamp: Timestamp = Timestamp.now()
)
