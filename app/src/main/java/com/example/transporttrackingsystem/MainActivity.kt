package com.example.transporttrackingsystem

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val busMarkers = mutableMapOf<String, Marker>()
    
    // UI Elements
    private lateinit var tvLocationCoords: TextView
    private lateinit var tvBusCount: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnSimulate: Button
    private lateinit var btnPlanTrip: Button
    
    companion object {
        var isAlertsEnabled: Boolean = true
    }
    
    private lateinit var etFrom: android.widget.EditText
    private lateinit var etTo: android.widget.EditText
    private lateinit var tvTripInfo: TextView
    private lateinit var rvBuses: androidx.recyclerview.widget.RecyclerView
    private lateinit var busAdapter: BusAdapter
    private var currentPolyline: Polyline? = null

    // 🔥 Bus Stops Data
    private val busStops = mapOf(
        "Bole" to LatLng(8.9892, 38.7885),
        "Mexico" to LatLng(9.0101, 38.7460),
        "Piazza" to LatLng(9.0357, 38.7523),
        "Merkato" to LatLng(9.0305, 38.7400),
        "Arat Kilo" to LatLng(9.0333, 38.7636),
        "Bole Bulbula" to LatLng(8.9647, 38.7997),
        "Stadium" to LatLng(9.0182, 38.7520),
        "Megenagna" to LatLng(9.0185, 38.8012),
        "Kality" to LatLng(8.9163, 38.7667),
        "Sarbet" to LatLng(9.0012, 38.7300),
        "Jemo" to LatLng(8.9723, 38.6923)
    )

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
        btnSimulate = findViewById(R.id.btnSimulate)
        btnPlanTrip = findViewById(R.id.btnPlanTrip)
        etFrom = findViewById(R.id.etFrom)
        etTo = findViewById(R.id.etTo)
        tvTripInfo = findViewById(R.id.tvTripInfo)
        rvBuses = findViewById(R.id.rvBuses)

        busAdapter = BusAdapter(emptyList())
        rvBuses.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvBuses.adapter = busAdapter

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        createNotificationChannel()

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnSimulate.setOnClickListener { simulateMultipleBuses() }

        btnPlanTrip.setOnClickListener { planTrip() }

        val sheet = findViewById<androidx.cardview.widget.CardView>(R.id.nearbyBusesSheet)
        val bottomSheetBehavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)
        sheet.setOnClickListener {
            bottomSheetBehavior.state = if (bottomSheetBehavior.state != com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED)
                com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED else com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
        }

        checkLocationPermissions()
    }

    private fun planTrip() {
        val fromQuery = etFrom.text.toString()
        val toQuery = etTo.text.toString()

        val fromPos = busStops.entries.find { it.key.contains(fromQuery, ignoreCase = true) }?.value
        val toPos = busStops.entries.find { it.key.contains(toQuery, ignoreCase = true) }?.value

        if (fromPos != null && toPos != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fromPos, 14f))
            
            // 🛣️ Draw Line
            currentPolyline?.remove()
            currentPolyline = mMap.addPolyline(PolylineOptions()
                .add(fromPos, toPos)
                .width(12f).color(Color.BLUE).geodesic(true))

            // 📏 Calculate Distance
            val startLoc = android.location.Location("").apply { latitude = fromPos.latitude; longitude = fromPos.longitude }
            val endLoc = android.location.Location("").apply { latitude = toPos.latitude; longitude = toPos.longitude }
            val distanceKm = startLoc.distanceTo(endLoc) / 1000

            tvTripInfo.visibility = android.view.View.VISIBLE
            tvTripInfo.text = "Trip: %.2f km | Look for buses going to $toQuery".format(distanceKm)
            
            Toast.makeText(this, "Route Planned: %.2f km".format(distanceKm), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Stop not found! Try: Bole, Mexico, Stadium, Megenagna, Bulbula...", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ARRIVE_CHANNEL", "Bus Arrival", NotificationManager.IMPORTANCE_HIGH)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun sendArrivalNotification(busName: String) {
        if (!MainActivity.isAlertsEnabled) return 

        // 🔇 Intent to mute alerts
        val muteIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "ACTION_MUTE"
        }
        val mutePendingIntent = PendingIntent.getBroadcast(this, 0, muteIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, "ARRIVE_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Bus Arriving!")
            .setContentText("$busName is very close. Get ready!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_lock_silent_mode, "MUTE ALERTS", mutePendingIntent) // 👈 ADDED THIS
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33) {
                notify(1, builder.build())
            }
        }
    }

    private fun simulateMultipleBuses() {
        val list = listOf(
            "Bus 102" to "Bole", 
            "Bus 305" to "Mexico", 
            "Bus 404" to "Stadium", 
            "Anbessa 01" to "Megenagna", 
            "Sheger 99" to "Kality",
            "Anbessa 07" to "Sarbet",
            "Sheger 12" to "Jemo"
        )
        for ((name, dest) in list) {
            val data = hashMapOf(
                "latitude" to 8.9 + (Math.random() * 0.15), 
                "longitude" to 38.65 + (Math.random() * 0.15), 
                "destination" to dest, 
                "lastUpdated" to System.currentTimeMillis()
            )
            db.collection("buses").document(name).set(data)
        }
        Toast.makeText(this, "Simulated 7 Buses across Addis!", Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) mMap.isMyLocationEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(9.0122, 38.7578), 12f))
        for ((name, pos) in busStops) {
            mMap.addMarker(MarkerOptions().position(pos).title(name))
        }
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
                    val dist = android.location.Location("").apply { latitude = busPos.latitude; longitude = busPos.longitude }.distanceTo(loc) / 1000
                    Toast.makeText(this, "Bus is %.2f km away (ETA: %d mins)".format(dist, (dist / 20 * 60).toInt()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun listenToBusUpdates() {
        db.collection("buses").addSnapshotListener { snapshots, _ ->
            val nearbyBuses = mutableListOf<BusInfo>()
            tvBusCount.text = "Active Buses: ${snapshots?.size() ?: 0}"
            snapshots?.forEach { doc ->
                val id = doc.id
                val pos = LatLng(doc.getDouble("latitude") ?: 0.0, doc.getDouble("longitude") ?: 0.0)
                val dest = doc.getString("destination") ?: "Unknown"
                if (busMarkers.containsKey(id)) busMarkers[id]?.position = pos else {
                    val marker = mMap.addMarker(MarkerOptions().position(pos).title("$id -> $dest").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                    if (marker != null) busMarkers[id] = marker
                }
                val isBest = dest.contains(etTo.text.toString(), ignoreCase = true) && etTo.text.isNotEmpty()
                
                calculateNearbyInfo(id, dest, pos, isBest) { info ->
                    nearbyBuses.add(info)
                    // Sort so best bus is at the top
                    val sortedList = nearbyBuses.sortedByDescending { it.id.contains("BEST", true) }
                    busAdapter.updateData(sortedList)
                }
            }
        }
    }

    private fun calculateNearbyInfo(id: String, dest: String, busPos: LatLng, isBest: Boolean, callback: (BusInfo) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val dist = android.location.Location("").apply { latitude = busPos.latitude; longitude = busPos.longitude }.distanceTo(loc) / 1000
                    if (dist < 0.5) sendArrivalNotification(id)
                    
                    val displayName = if (isBest) "⭐ BEST: $id" else id
                    callback(BusInfo(displayName, dest, "%.2f km away".format(dist), "${(dist / 20 * 60).toInt()} mins"))
                }
            }
        }
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
                res.locations.forEach { updateFirestoreLocation(it.latitude, it.longitude) }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            fusedLocationClient.requestLocationUpdates(request, callback, null)
    }

    private fun updateFirestoreLocation(lat: Double, lng: Double) {
        val data = hashMapOf("latitude" to lat, "longitude" to lng, "lastUpdated" to System.currentTimeMillis(), "userId" to auth.currentUser?.uid)
        db.collection("buses").document(deviceId).set(data).addOnSuccessListener {
            tvLocationCoords.text = "%.4f, %.4f".format(lat, lng)
        }
    }
}
