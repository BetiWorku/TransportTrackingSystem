package com.example.transporttrackingsystem

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val busMarkers = mutableMapOf<String, Marker>()
    private val notifiedBuses = mutableSetOf<String>()
    private var currentRouteStart: LatLng? = null
    private var currentRouteEnd: LatLng? = null
    private var lastKnownLoc: Location? = null
    private var latestSnapshots: QuerySnapshot? = null
    private var boardedBusId: String? = null
    private val passengerCounts = mutableMapOf<String, Int>()
    private val nearbyBuses = mutableListOf<BusInfo>()
    
    // UI Elements
    private lateinit var tvLocationCoords: TextView
    private lateinit var tvBusCount: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnPlanTrip: Button
    private lateinit var tvWhereTo: TextView
    private lateinit var etFrom: AutoCompleteTextView
    private lateinit var etTo: AutoCompleteTextView
    private lateinit var tvTripInfo: TextView
    private lateinit var rvBuses: RecyclerView
    private lateinit var busAdapter: BusAdapter
    private var currentPolyline: Polyline? = null
    private val lastStopNotified = mutableMapOf<String, String>()
    
    // Onboard UI
    private lateinit var onboardCard: CardView
    private lateinit var tvOnboardBusId: TextView
    private lateinit var tvOnboardNextStop: TextView
    private lateinit var tvOnboardEta: TextView
    private lateinit var tvOnboardDist: TextView
    private lateinit var btnFinishTrip: Button
    private lateinit var tripProgress: com.google.android.material.progressindicator.LinearProgressIndicator

    companion object {
        var isAlertsEnabled: Boolean = true
    }

    // 🔥 Bus Stops Data
    private val stopsList = mutableListOf<Stop>()
    private val routesList = mutableListOf<Route>()
    private val busStops = mutableMapOf<String, LatLng>()

    private var deviceId = "Bus_Unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        deviceId = "Bus_" + (auth.currentUser?.uid?.take(5) ?: "Guest")

        tvLocationCoords = findViewById(R.id.locationCoords)
        tvBusCount = findViewById(R.id.busCount)
        btnLogout = findViewById(R.id.btnLogout)
        btnPlanTrip = findViewById(R.id.btnPlanTrip)
        tvWhereTo = findViewById(R.id.tvWhereTo)
        etFrom = findViewById(R.id.etFrom)
        etTo = findViewById(R.id.etTo)
        tvTripInfo = findViewById(R.id.tvTripInfo)
        rvBuses = findViewById(R.id.rvBuses)

        // Onboard UI
        onboardCard = findViewById(R.id.onboardCard)
        tvOnboardBusId = findViewById(R.id.tvOnboardBusId)
        tvOnboardNextStop = findViewById(R.id.tvOnboardNextStop)
        tvOnboardEta = findViewById(R.id.tvOnboardEta)
        tvOnboardDist = findViewById(R.id.tvOnboardDist)
        btnFinishTrip = findViewById(R.id.btnFinishTrip)
        tripProgress = findViewById(R.id.tripProgress)

        btnFinishTrip.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Exit Bus")
                .setMessage("Are you sure you want to end your trip on $boardedBusId?")
                .setPositiveButton("Exit") { _, _ ->
                    val userId = auth.currentUser?.uid ?: "guest"
                    db.collection("trips").document(userId).update("status", "completed", "exitStop", "Manual Exit")
                    boardedBusId = null
                    onboardCard.visibility = View.GONE
                    Toast.makeText(this, "Trip completed. Thank you for using Addis Transport!", Toast.LENGTH_SHORT).show()
                    refreshBusList()
                }
                .setNegativeButton("No", null)
                .show()
        }



        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    Toast.makeText(this, "Map View", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_news -> {
                    startActivity(Intent(this, UserNewsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        fetchRoutesAndStops()
        startLocationUpdates()

        busAdapter = BusAdapter(emptyList()) { busId ->
            val rawId = busId.replace("⭐ BEST: ", "").replace("🚍 ONBOARD: ", "")
            val marker = busMarkers[rawId]
            if (marker != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 16f))
                marker.showInfoWindow()
                
                if (boardedBusId == null) {
                    val doc = latestSnapshots?.documents?.find { it.id == rawId }
                    val passCount = doc?.getLong("passengers")?.toInt() ?: 0
                    if (passCount >= 30) {
                        Toast.makeText(this, "Bus is FULL (30/30)! Please wait for the next one.", Toast.LENGTH_LONG).show()
                        return@BusAdapter
                    }
                    
                    if (currentRouteStart != null) {
                        val distToStation = Location("").apply { latitude = marker.position.latitude; longitude = marker.position.longitude }.distanceTo(
                            Location("").apply { latitude = currentRouteStart!!.latitude; longitude = currentRouteStart!!.longitude }
                        ) / 1000
                        val timeToStation = (distToStation / 20 * 60).toInt()
                        
                        if (timeToStation > 0) {
                            Toast.makeText(this, "You can only board when the bus arrives at the station ($timeToStation mins left)!", Toast.LENGTH_LONG).show()
                            return@BusAdapter
                        }
                    }

                    AlertDialog.Builder(this)
                        .setTitle("Board Bus")
                        .setMessage("Are you boarding $rawId? (${30 - passCount} seats left)")
                        .setPositiveButton("Yes, Board") { _, _ ->
                            boardedBusId = rawId
                            val userId = auth.currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
                            val currentStop = nearbyBuses.find { it.id.contains(rawId) }?.currentStop ?: "Unknown"
                            
                            val trip = Trip(
                                tripId = "TRIP_${System.currentTimeMillis()}",
                                userId = userId,
                                busNumber = rawId,
                                entryStop = currentStop,
                                status = "onboard"
                            )
                            db.collection("trips").document(userId).set(trip)
                            db.collection("buses").document(rawId).collection("passengers").document(userId).set(hashMapOf("timestamp" to System.currentTimeMillis()))
                            
                            Toast.makeText(this, "Boarded $rawId at $currentStop! Safe trip.", Toast.LENGTH_SHORT).show()
                            
                            // Show Onboard Card
                            onboardCard.visibility = View.VISIBLE
                            tvOnboardBusId.text = "Onboard: $rawId"
                            
                            refreshBusList()
                            val sheet = findViewById<CardView>(R.id.nearbyBusesSheet)
                            BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                        .setNeutralButton("View Details") { _, _ ->
                            val intent = Intent(this, BusDetailsActivity::class.java)
                            intent.putExtra("BUS_ID", rawId)
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    val sheet = findViewById<CardView>(R.id.nearbyBusesSheet)
                    BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
        rvBuses.layoutManager = LinearLayoutManager(this)
        rvBuses.adapter = busAdapter

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        createNotificationChannel()

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }



        btnPlanTrip.setOnClickListener { planTrip() }

        val sheet = findViewById<CardView>(R.id.nearbyBusesSheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(sheet)
        sheet.setOnClickListener {
            bottomSheetBehavior.state = if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        }

        checkLocationPermissions()
        
        // 🔄 Restore Active Trip
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("trips").document(userId).get().addOnSuccessListener { doc ->
                if (doc.exists() && doc.getString("status") == "onboard") {
                    boardedBusId = doc.getString("busNumber")
                    onboardCard.visibility = View.VISIBLE
                    tvOnboardBusId.text = "Onboard: $boardedBusId"
                }
            }
        }
    }

    private fun planTrip() {
        val fromQuery = etFrom.text.toString().trim()
        val toQuery = etTo.text.toString().trim()

        if (fromQuery.isEmpty() || toQuery.isEmpty()) {
            Toast.makeText(this, "Please enter both Start and Destination", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔍 More robust search: find stop that starts with or contains the query
        val fromStop = stopsList.find { it.stopName.equals(fromQuery, true) } 
            ?: stopsList.find { it.stopName.contains(fromQuery, true) }
        val toStop = stopsList.find { it.stopName.equals(toQuery, true) }
            ?: stopsList.find { it.stopName.contains(toQuery, true) }

        val fromPos = fromStop?.let { LatLng(it.latitude, it.longitude) }
        val toPos = toStop?.let { LatLng(it.latitude, it.longitude) }

        if (fromPos != null && toPos != null) {
            if (fromPos == toPos) {
                Toast.makeText(this, "Start and Destination are the same!", Toast.LENGTH_SHORT).show()
                return
            }

            currentRouteStart = fromPos
            currentRouteEnd = toPos

            val alertCard = findViewById<CardView>(R.id.trafficAlert)
            val tvAlertMsg = findViewById<TextView>(R.id.tvAlertMsg)
            findViewById<ImageView>(R.id.btnCloseAlert).setOnClickListener { alertCard.visibility = View.GONE }

            if (toQuery.contains("Megenagna", ignoreCase = true) || toQuery.contains("Piazza", ignoreCase = true)) {
                tvAlertMsg.text = getString(R.string.traffic_delay, toQuery, 15)
                alertCard.visibility = View.VISIBLE
            } else {
                alertCard.visibility = View.GONE
            }
            
            val boundsBuilder = LatLngBounds.Builder()
            boundsBuilder.include(fromPos)
            boundsBuilder.include(toPos)
            
            // 🗺️ DRAW POLYLINE THROUGH ALL INTERMEDIATE STOPS
            val matchingRoute = stopsList.filter { it.stopName.contains(fromQuery, true) || it.stopName.contains(toQuery, true) }
                .groupBy { it.routeId }
                .filter { it.value.size >= 2 }
                .keys.firstOrNull()
            
            currentPolyline?.remove()
            val polyOptions = PolylineOptions().color(Color.BLUE).width(12f).geodesic(true)
            
            if (matchingRoute != null) {
                val routeStops = stopsList.filter { it.routeId == matchingRoute }.sortedBy { it.stopOrder }
                val startIdx = routeStops.indexOfFirst { it.stopName.contains(fromQuery, true) }
                val endIdx = routeStops.indexOfFirst { it.stopName.contains(toQuery, true) }
                
                if (startIdx != -1 && endIdx != -1) {
                    val path = if (startIdx < endIdx) routeStops.subList(startIdx, endIdx + 1) else routeStops.subList(endIdx, startIdx + 1).reversed()
                    path.forEach { 
                        val p = LatLng(it.latitude, it.longitude)
                        polyOptions.add(p)
                        boundsBuilder.include(p) // Include intermediate stops in camera
                    }
                } else {
                    polyOptions.add(fromPos, toPos)
                }
            } else {
                polyOptions.add(fromPos, toPos)
            }
            currentPolyline = mMap.addPolyline(polyOptions)

            try {
                mMap.setPadding(50, 150, 50, 800)
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
            } catch (_: Exception) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fromPos, 14f))
            }

            val startLoc = Location("").apply { latitude = fromPos.latitude; longitude = fromPos.longitude }
            val endLoc = Location("").apply { latitude = toPos.latitude; longitude = toPos.longitude }
            val distanceKm = startLoc.distanceTo(endLoc) / 1000
            val estimatedTime = (distanceKm / 15 * 60).toInt()

            tvTripInfo.text = "Distance: %.1f km | Est. Travel Time: %d mins".format(distanceKm, estimatedTime)
            tvTripInfo.visibility = View.VISIBLE
            
            // 🚀 AUTOMATICALLY OPEN NEARBY BUSES & CALCULATE ETA
            refreshBusList()
            val sheet = findViewById<CardView>(R.id.nearbyBusesSheet)
            BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            
            Toast.makeText(this, "Finding fastest buses for your route...", Toast.LENGTH_SHORT).show()
            
            tvWhereTo.visibility = View.GONE
            etFrom.visibility = View.GONE
            etTo.visibility = View.GONE
            btnPlanTrip.visibility = View.GONE

            tvTripInfo.setOnClickListener {
                tvWhereTo.visibility = View.VISIBLE
                etFrom.visibility = View.VISIBLE
                etTo.visibility = View.VISIBLE
                btnPlanTrip.visibility = View.VISIBLE
                tvTripInfo.visibility = View.GONE
                currentPolyline?.remove()
                currentRouteStart = null
                currentRouteEnd = null
                boardedBusId = null
                mMap.setPadding(0, 0, 0, 0)
                refreshBusList()
            }
            
            refreshBusList()
            Toast.makeText(this, "Route Planned: %.2f km".format(distanceKm), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Stop not found! Try: Bole, Mexico, Stadium, Megenagna...", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel("ARRIVE_CHANNEL", "Bus Arrival", NotificationManager.IMPORTANCE_HIGH)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun sendArrivalNotification(busName: String, etaMins: Int = 0) {
        if (!isAlertsEnabled) return 

        val muteIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "ACTION_MUTE"
        }
        val mutePendingIntent = PendingIntent.getBroadcast(this, 0, muteIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val msg = when {
            etaMins == -1 -> getString(R.string.boarded_msg, busName)
            etaMins > 0 -> getString(R.string.arriving_msg, busName, etaMins)
            else -> getString(R.string.close_msg, busName)
        }
        val title = if (etaMins == -1) getString(R.string.trip_started) else getString(R.string.bus_arriving_soon)

        val builder = NotificationCompat.Builder(this, "ARRIVE_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_lock_silent_mode, "MUTE ALERTS", mutePendingIntent)
            
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(1, builder.build())
            }
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        
        // 🇪🇹 Constrain to Ethiopia (Central Region)
        val ethiopiaBounds = LatLngBounds(LatLng(8.5, 38.0), LatLng(10.0, 40.0))
        mMap.setLatLngBoundsForCameraTarget(ethiopiaBounds)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(9.0122, 38.7578), 13f))

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) mMap.isMyLocationEnabled = true
        
        mMap.setOnMarkerClickListener { marker ->
            val id = marker.title
            if (id != null && busMarkers.containsKey(id.split(" -> ")[0])) calculateETA(marker.position)
            false
        }
        listenToBusUpdates()
    }

    private fun calculateETA(busPos: LatLng) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val dist = Location("").apply { latitude = busPos.latitude; longitude = busPos.longitude }.distanceTo(loc) / 1000
                    Toast.makeText(this, "Bus is %.2f km away (ETA: %d mins)".format(dist, (dist / 20 * 60).toInt()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fetchRoutesAndStops() {
        db.collection("routes").addSnapshotListener { snapshots, _ ->
            routesList.clear()
            snapshots?.forEach { routesList.add(it.toObject(Route::class.java)) }
        }

        db.collection("stops").orderBy("stopOrder").addSnapshotListener { snapshots, _ ->
            stopsList.clear()
            busStops.clear()
            
            if (::mMap.isInitialized) {
                mMap.clear() 
                busMarkers.clear() // CRITICAL: Reset marker cache
            }
            
            snapshots?.forEach { doc ->
                val stop = doc.toObject(Stop::class.java)
                stopsList.add(stop)
                val pos = LatLng(stop.latitude, stop.longitude)
                busStops[stop.stopName] = pos
                
                if (::mMap.isInitialized) {
                    mMap.addMarker(MarkerOptions()
                        .position(pos)
                        .title(stop.stopName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                }
            }
            
            val stopNames = stopsList.map { it.stopName }.distinct()
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stopNames)
            etFrom.setAdapter(adapter)
            etTo.setAdapter(adapter)

            if (::mMap.isInitialized) {
                refreshBusList() 
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                lastKnownLoc = locationResult.lastLocation
                tvLocationCoords.text = "GPS: ${"%.4f".format(lastKnownLoc?.latitude)}, ${"%.4f".format(lastKnownLoc?.longitude)}"
                refreshBusList()
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun listenToBusUpdates() {
        db.collection("buses").addSnapshotListener { snapshots, _ ->
            if (snapshots == null) return@addSnapshotListener
            latestSnapshots = snapshots
            tvBusCount.text = getString(R.string.active_buses, snapshots.size())
            
            snapshots.documents.forEach { doc ->
                val busId = doc.id
                if (!passengerCounts.containsKey(busId)) {
                    db.collection("buses").document(busId).collection("passengers").addSnapshotListener { passSnaps, _ ->
                        passengerCounts[busId] = passSnaps?.size() ?: 0
                        refreshBusList()
                    }
                    passengerCounts[busId] = 0
                }
            }
            refreshBusList()
        }
    }

    private fun refreshBusList() {
        val snapshots = latestSnapshots ?: return
        val activeLoc = lastKnownLoc
        
        nearbyBuses.clear()
        
        if (::mMap.isInitialized) {
            // Keep Stop markers, update or add bus markers
            for (doc in snapshots) {
                val id = doc.id
                val pos = LatLng(doc.getDouble("latitude") ?: 0.0, doc.getDouble("longitude") ?: 0.0)
                val busType = doc.getString("busType") ?: "City Bus"
                val terminal = doc.getString("terminal") ?: "Unknown"

                if (busMarkers.containsKey(id)) {
                    busMarkers[id]?.position = pos
                } else {
                    val marker = mMap.addMarker(MarkerOptions()
                        .position(pos)
                        .title("$id ($busType) -> $terminal")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                    if (marker != null) busMarkers[id] = marker
                }
            }
        }

        snapshots.forEach { doc ->
            val id = doc.id
            val pos = LatLng(doc.getDouble("latitude") ?: 0.0, doc.getDouble("longitude") ?: 0.0)
            val terminal = doc.getString("terminal") ?: "Unknown"
            val busType = doc.getString("busType") ?: "City Bus"
            val passengers = doc.getLong("passengers")?.toInt() ?: 0
            val capacity = doc.getLong("capacity")?.toInt() ?: 30
            
            val routeId = doc.getString("routeId") ?: ""
            val stopsForRoute = stopsList.filter { it.routeId == routeId }.sortedBy { it.stopOrder }
            
            var currentStopName = "In Transit"
            var nextStopName = "Final Destination"
            var distToNextStop = -1f
            var nextStopPos: LatLng? = null
            
            // 🎯 Find the most relevant stops
            val busLoc = Location("").apply { latitude = pos.latitude; longitude = pos.longitude }
            
            var foundCurrent = false
            for (stop in stopsForRoute) {
                val stopLoc = Location("").apply { latitude = stop.latitude; longitude = stop.longitude }
                val d = busLoc.distanceTo(stopLoc)
                
                if (d < 250) { // Within 250m of a stop
                    currentStopName = stop.stopName
                    val next = stopsForRoute.find { it.stopOrder == stop.stopOrder + 1 }
                    nextStopName = next?.stopName ?: "Final Destination"
                    nextStopPos = next?.let { LatLng(it.latitude, it.longitude) }
                    foundCurrent = true
                    
                    // 🔔 Notify Arrival
                    if (lastStopNotified[id] != stop.stopId) {
                        lastStopNotified[id] = stop.stopId
                        if (id == boardedBusId) sendArrivalNotification("Reached: ${stop.stopName}", 0)
                    }
                    break
                }
            }
            
            if (!foundCurrent && stopsForRoute.isNotEmpty()) {
                // If not at a stop, find the next one in sequence
                // We assume the bus moves from stop 1 to N
                // Simplified: find the first stop that is "ahead" or just use the next one after the last known stop
                // For now, let's find the stop with the smallest distance that has a higher order than current or just the closest ahead
                // A better heuristic: the first stop whose order is > than any stop we are "at" or "just passed"
                
                // For simplicity in this demo, we'll find the stop the bus is closest to that hasn't been reached
                // (In a real app, we'd use the bus's direction)
                val next = stopsForRoute.firstOrNull { stop ->
                    val stopLoc = Location("").apply { latitude = stop.latitude; longitude = stop.longitude }
                    busLoc.distanceTo(stopLoc) > 250 && stop.stopOrder > (stopsForRoute.find { it.stopName == currentStopName }?.stopOrder ?: 0)
                } ?: stopsForRoute.firstOrNull()
                
                if (next != null) {
                    nextStopName = next.stopName
                    nextStopPos = LatLng(next.latitude, next.longitude)
                    distToNextStop = busLoc.distanceTo(Location("").apply { latitude = next.latitude; longitude = next.longitude }) / 1000
                }
            }

            val isBest = terminal.contains(etTo.text.toString(), ignoreCase = true) && etTo.text.isNotEmpty()
            
            // 🎯 Calculate distance for Display
            // If Onboard: show distance to next stop
            // If Waiting: show distance to User/Station
            val targetLoc = if (id == boardedBusId && nextStopPos != null) nextStopPos else (currentRouteStart ?: activeLoc?.let { LatLng(it.latitude, it.longitude) })
            
            if (targetLoc != null) {
                val dist = Location("").apply { 
                    latitude = pos.latitude
                    longitude = pos.longitude 
                }.distanceTo(Location("").apply { 
                    latitude = targetLoc.latitude
                    longitude = targetLoc.longitude 
                }) / 1000
                
                // ETA: Distance / Average Speed (20 km/h)
                val timeSecs = (dist / 20 * 3600).toInt()
                val m = timeSecs / 60
                val s = timeSecs % 60
                val timeStr = if (m > 0) "${m}m ${s}s" else "${s}s"
                
                val distStr = if (dist < 1f) "${(dist * 1000).toInt()} m" else "%.2f km".format(dist)
                val displayName = if (isBest) "⭐ BEST: $id" else id
                nearbyBuses.add(BusInfo(displayName, terminal, distStr, timeStr, timeSecs, busType, passengers, capacity, currentStopName, nextStopName))

                // 🚀 Update Onboard Card in real-time
                if (id == boardedBusId) {
                    tvOnboardNextStop.text = nextStopName
                    tvOnboardEta.text = if (m > 0) "$m mins" else "Arriving..."
                    tvOnboardDist.text = if (dist < 1f) "${(dist * 1000).toInt()} m to $nextStopName" else "%.1f km to $nextStopName".format(dist)
                    
                    // Progress logic: distance to next stop (max 5km for scale)
                    val progress = (100 - (dist * 20).coerceIn(0f, 100f)).toInt()
                    tripProgress.setProgress(progress, true)
                }
            } else {
                val displayName = if (isBest) "⭐ BEST: $id" else id
                nearbyBuses.add(BusInfo(displayName, terminal, "---", "Searching GPS...", Int.MAX_VALUE, busType, passengers, capacity, currentStopName, nextStopName))
            }
        }
        
        val sortedList = nearbyBuses.sortedWith(compareBy({ !it.id.contains("BEST", true) }, { it.sortSecs }))
        busAdapter.updateData(sortedList)
    }

    private fun checkLocationPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter { 
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED 
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1001)
        } else {
            startLocationTracking()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startLocationTracking()
    }

    private fun startLocationTracking() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.locations.forEach { 
                    lastKnownLoc = Location("").apply { latitude = it.latitude; longitude = it.longitude }
                    updateFirestoreLocation(it.latitude, it.longitude) 
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }
    }

    private fun updateFirestoreLocation(lat: Double, lng: Double) {
        val data = hashMapOf("latitude" to lat, "longitude" to lng, "lastUpdated" to System.currentTimeMillis(), "userId" to auth.currentUser?.uid)
        db.collection("buses").document(deviceId).set(data).addOnSuccessListener {
            tvLocationCoords.text = getString(R.string.location_coords, lat, lng)
        }
    }

    private fun scheduleOfflineAlarm(busId: String, secondsFromNow: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "ACTION_ALARM"
            putExtra("BUS_ID", busId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, busId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + (secondsFromNow * 1000L)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            Log.d("ALARM", "Offline alarm set for $busId in ${secondsFromNow}s")
        } catch (e: SecurityException) {
            Log.w("ALARM", "Could not set exact alarm: ${e.message}")
        }
    }
}
