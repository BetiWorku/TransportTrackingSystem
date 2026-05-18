package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.Stop

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

/**
 * BusTrackerActivity — FlightRadar24-style real-time bus tracker
 *
 * Displays:
 * - Live distance countdown (km → m)
 * - Live ETA countdown (MM:SS)
 * - Route progress (current stop → next stop)
 * - Total trip distance + ETA to destination
 * - Speed, passengers, status
 *
 * All constrained to Addis Ababa region (lat 8.7–9.3, lng 38.5–38.95)
 */
@SuppressLint("SetTextI18n")
class BusTrackerActivity : AppCompatActivity() {

    // ── Firebase ──────────────────────────────────────────────────────
    private lateinit var db: FirebaseFirestore
    private var busListener: ListenerRegistration? = null
    private var stopsListener: ListenerRegistration? = null

    // ── State ─────────────────────────────────────────────────────────
    private var busId: String = ""
    private var fromStopName: String = ""
    private var toStopName: String = ""

    // Live countdown state (updated from Firebase, ticked locally)
    private var distanceToStationKm: Double = 0.0     // bus → user's station (FROM stop)
    private var etaSecs: Int = 0                       // seconds until bus reaches station
    private var totalDistKm: Double = 0.0              // bus → final destination (TO stop)
    private var totalEtaSecs: Int = 0

    private val stopsList = mutableListOf<Stop>()

    // ── Ticker ────────────────────────────────────────────────────────
    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            tickCountdown()
            handler.postDelayed(this, 1000)
        }
    }

    // ── Views ─────────────────────────────────────────────────────────
    private lateinit var tvTrackerBusId: TextView
    private lateinit var tvTrackerRouteLabel: TextView
    private lateinit var tvFromStop: TextView
    private lateinit var tvToStop: TextView
    private lateinit var tvCurrentStopTracker: TextView
    private lateinit var tvNextStopTracker: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var progressLeft: View
    private lateinit var progressRight: View
    private lateinit var tvLiveDistance: TextView
    private lateinit var tvDistanceUnit: TextView
    private lateinit var tvLiveEta: TextView
    private lateinit var tvEtaLabel: TextView
    private lateinit var tvLiveSpeed: TextView
    private lateinit var tvLivePassengers: TextView
    private lateinit var tvLiveStatus: TextView
    private lateinit var tvLiveStatusLabel: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalEta: TextView
    private lateinit var tvInfoBusType: TextView
    private lateinit var tvInfoTerminal: TextView
    private lateinit var tvInfoRoute: TextView
    private lateinit var tvLastUpdated: TextView
    private lateinit var livePulseDot: View

    // ── Lifecycle ─────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_tracker)

        db = FirebaseFirestore.getInstance()

        // Grab extras from intent
        busId = intent.getStringExtra(EXTRA_BUS_ID) ?: ""
        fromStopName = intent.getStringExtra(EXTRA_FROM_STOP) ?: ""
        toStopName = intent.getStringExtra(EXTRA_TO_STOP) ?: ""

        bindViews()
        setupHeader()

        // Show what we know immediately
        tvFromStop.text = fromStopName.ifEmpty { "Your Station" }
        tvToStop.text = toStopName.ifEmpty { "Destination" }

        startLivePulse()
        loadStopsAndListen()
    }

    override fun onResume() {
        super.onResume()
        if (MainActivity.isMockTestActive) {
            when {
                busId.contains("Sheger", true) -> {
                    distanceToStationKm = MainActivity.mockShegerDist
                    etaSecs = MainActivity.mockShegerSecs
                }
                busId.contains("Wolo-25-01", true) -> {
                    distanceToStationKm = MainActivity.mockWoloDist
                    etaSecs = MainActivity.mockWoloSecs
                }
                else -> {
                    distanceToStationKm = MainActivity.mockDist
                    etaSecs = MainActivity.mockSecs
                }
            }
            totalDistKm = distanceToStationKm + 2.5
            totalEtaSecs = etaSecs + 450
        }
        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tickRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        busListener?.remove()
        stopsListener?.remove()
        handler.removeCallbacks(tickRunnable)
    }

    // ── Bind Views ────────────────────────────────────────────────────

    private fun bindViews() {
        tvTrackerBusId      = findViewById(R.id.tvTrackerBusId)
        tvTrackerRouteLabel = findViewById(R.id.tvTrackerRouteLabel)
        tvFromStop          = findViewById(R.id.tvFromStop)
        tvToStop            = findViewById(R.id.tvToStop)
        tvCurrentStopTracker = findViewById(R.id.tvCurrentStopTracker)
        tvNextStopTracker   = findViewById(R.id.tvNextStopTracker)
        tvProgressPercent   = findViewById(R.id.tvProgressPercent)
        progressLeft        = findViewById(R.id.progressLeft)
        progressRight       = findViewById(R.id.progressRight)
        tvLiveDistance      = findViewById(R.id.tvLiveDistance)
        tvDistanceUnit      = findViewById(R.id.tvDistanceUnit)
        tvLiveEta           = findViewById(R.id.tvLiveEta)
        tvEtaLabel          = findViewById(R.id.tvEtaLabel)
        tvLiveSpeed         = findViewById(R.id.tvLiveSpeed)
        tvLivePassengers    = findViewById(R.id.tvLivePassengers)
        tvLiveStatus        = findViewById(R.id.tvLiveStatus)
        tvLiveStatusLabel   = findViewById(R.id.tvLiveStatusLabel)
        tvTotalDistance     = findViewById(R.id.tvTotalDistance)
        tvTotalEta          = findViewById(R.id.tvTotalEta)
        tvInfoBusType       = findViewById(R.id.tvInfoBusType)
        tvInfoTerminal      = findViewById(R.id.tvInfoTerminal)
        tvInfoRoute         = findViewById(R.id.tvInfoRoute)
        tvLastUpdated       = findViewById(R.id.tvLastUpdated)
        livePulseDot        = findViewById(R.id.livePulseDot)

        findViewById<ImageButton>(R.id.btnTrackerBack).setOnClickListener { finish() }
    }

    private fun setupHeader() {
        tvTrackerBusId.text = busId
    }

    // ── Firebase Listeners ────────────────────────────────────────────

    private fun loadStopsAndListen() {
        stopsListener = db.collection("stops").orderBy("stopOrder")
            .addSnapshotListener { snapshots, _ ->
                stopsList.clear()
                snapshots?.forEach { stopsList.add(it.toObject(Stop::class.java)) }
                listenToBus()           // (re)attach bus listener after stops are loaded
            }
    }

    private fun listenToBus() {
        busListener?.remove()
        if (busId.isEmpty()) return

        busListener = db.collection("buses").document(busId)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) return@addSnapshotListener

                val busLat  = snap.getDouble("latitude")  ?: 0.0
                val busLng  = snap.getDouble("longitude") ?: 0.0
                val routeId = snap.getString("routeId")   ?: ""
                val terminal = snap.getString("terminal") ?: "Unknown"
                val busType = snap.getString("busType")   ?: "City Bus"
                val driver  = snap.getString("driverName") ?: "Unknown"
                val speed   = snap.getDouble("speed")     ?: 20.0
                val passengers = snap.getLong("passengers")?.toInt() ?: 0
                val capacity   = snap.getLong("capacity")?.toInt()   ?: 30

                // ── Guard: only process buses in Addis Ababa ──────────
                if (!isInAddisAbaba(busLat, busLng)) return@addSnapshotListener

                val busLoc = Location("").apply { latitude = busLat; longitude = busLng }
                val stopsForRoute = stopsList
                    .filter { it.routeId == routeId }
                    .sortedBy { it.stopOrder }

                // ── Determine current & next stop ────────────────────
                var currentStop = "In Transit"
                var nextStop    = terminal
                for (stop in stopsForRoute) {
                    if (stop.latitude == 0.0) continue
                    val stopLoc = Location("").apply { latitude = stop.latitude; longitude = stop.longitude }
                    if (busLoc.distanceTo(stopLoc) < 250) {
                        currentStop = stop.stopName
                        val next = stopsForRoute.find { it.stopOrder == stop.stopOrder + 1 }
                        nextStop = next?.stopName ?: terminal
                        break
                    }
                }

                // ── Calculate distance to user's FROM stop ───────────
                val fromStop = if (fromStopName.isNotEmpty())
                    stopsForRoute.find { it.stopName.contains(fromStopName, true) }
                    ?: stopsList.find { it.stopName.contains(fromStopName, true) }
                else null

                val toStop = if (toStopName.isNotEmpty())
                    stopsForRoute.find { it.stopName.contains(toStopName, true) }
                    ?: stopsList.find { it.stopName.contains(toStopName, true) }
                else null

                val effectiveSpeed = if (speed < 1.0) 20.0 else speed  // fallback 20 km/h

                // Distance & ETA to user's boarding station
                var distToFrom: Double = if (busLat == 0.0 || busLng == 0.0) {
                    0.0
                } else if (fromStop != null && fromStop.latitude != 0.0) {
                    val fLoc = Location("").apply { latitude = fromStop.latitude; longitude = fromStop.longitude }
                    busLoc.distanceTo(fLoc) / 1000.0
                } else 0.0

                var rawSecs = if (distToFrom > 0) ((distToFrom / effectiveSpeed) * 3600).toInt() else 0

                // 🧪 MOCK OVERRIDE for Tracker Activity
                if (MainActivity.isMockTestActive) {
                    when {
                        busId.contains("Sheger", true) -> {
                            distToFrom = MainActivity.mockShegerDist
                            rawSecs = MainActivity.mockShegerSecs
                        }
                        busId.contains("Wolo-25-01", true) -> {
                            distToFrom = MainActivity.mockWoloDist
                            rawSecs = MainActivity.mockWoloSecs
                        }
                        else -> {
                            distToFrom = MainActivity.mockDist
                            rawSecs = MainActivity.mockSecs
                        }
                    }
                }

                // Distance & ETA to final destination
                var distToFinal: Double = if (busLat == 0.0 || busLng == 0.0) {
                    0.0
                } else if (toStop != null && toStop.latitude != 0.0) {
                    val tLoc = Location("").apply { latitude = toStop.latitude; longitude = toStop.longitude }
                    busLoc.distanceTo(tLoc) / 1000.0
                } else 0.0
                var totalRawSecs = if (distToFinal > 0) ((distToFinal / effectiveSpeed) * 3600).toInt() else 0

                // ── Route progress % ─────────────────────────────────
                var progressPct: Int = if (busLat != 0.0 && busLng != 0.0 && fromStop != null && toStop != null && distToFinal > 0) {
                    val fLoc = Location("").apply { latitude = fromStop.latitude; longitude = fromStop.longitude }
                    val tLoc = Location("").apply { latitude = toStop.latitude; longitude = toStop.longitude }
                    val totalRouteKm = fLoc.distanceTo(tLoc) / 1000.0
                    if (totalRouteKm > 0) ((1.0 - distToFinal / totalRouteKm) * 100).toInt().coerceIn(0, 100) else 0
                } else 0

                // 🧪 MOCK OVERRIDE for Tracker Activity Totals & Progress
                if (MainActivity.isMockTestActive) {
                    distToFinal = distToFrom + 2.5
                    totalRawSecs = rawSecs + 450 // + 7.5 mins to final destination
                    progressPct = ((1.0 - (distToFrom / 3.33)) * 100).toInt().coerceIn(0, 100)
                }

                // ── Update state (used by ticker) ────────────────────
                distanceToStationKm = distToFrom
                etaSecs             = rawSecs
                totalDistKm         = distToFinal
                totalEtaSecs        = totalRawSecs

                // ── Update static UI immediately ─────────────────────
                runOnUiThread {
                    tvCurrentStopTracker.text = currentStop
                    tvNextStopTracker.text = nextStop
                    tvProgressPercent.text = "$progressPct%"
                    updateProgressBar(progressPct)

                    tvLiveSpeed.text = effectiveSpeed.toInt().toString()
                    tvLivePassengers.text = "$passengers/$capacity"
                    tvLivePassengers.setTextColor(
                        if (passengers >= capacity) 0xFFFF5252.toInt() else 0xFFFFFFFF.toInt()
                    )

                    val isMoving = distToFrom > 0.02
                    tvLiveStatus.setTextColor(if (isMoving) 0xFF00E676.toInt() else 0xFFFF9100.toInt())
                    tvLiveStatusLabel.text = when {
                        distToFrom < 0.02 -> "At Station"
                        distToFrom < 0.1  -> "Arriving"
                        else              -> "En Route"
                    }

                    tvTotalDistance.text = formatDistKm(distToFinal)
                    tvTotalEta.text = "~${totalRawSecs / 60} min"

                    tvInfoBusType.text = busType
                    tvInfoTerminal.text = terminal
                    tvInfoRoute.text   = routeId.ifEmpty { "N/A" }

                    val now = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                    tvLastUpdated.text = "Last updated: $now"
                }
            }
    }

    // ── Local 1-second Ticker ─────────────────────────────────────────

    /**
     * Called every 1 second to count down distance & ETA locally
     * (prevents waiting for Firebase for smooth animation).
     * Assumes average bus speed = 20 km/h = 5.556 m/s
     */
    private fun tickCountdown() {
        val speedMs = 20.0 / 3.6          // 20 km/h in m/s
        val deltaKm = speedMs / 1000.0    // km per second

        if (distanceToStationKm > 0) {
            distanceToStationKm = (distanceToStationKm - deltaKm).coerceAtLeast(0.0)
            etaSecs = (etaSecs - 1).coerceAtLeast(0)
        }
        if (totalDistKm > 0) {
            totalDistKm = (totalDistKm - deltaKm).coerceAtLeast(0.0)
            totalEtaSecs = (totalEtaSecs - 1).coerceAtLeast(0)
        }
        
        if (MainActivity.isMockTestActive) {
            when {
                busId.contains("Sheger", true) -> {
                    MainActivity.mockShegerSecs = etaSecs
                    MainActivity.mockShegerDist = distanceToStationKm
                }
                busId.contains("Wolo-25-01", true) -> {
                    MainActivity.mockWoloSecs = etaSecs
                    MainActivity.mockWoloDist = distanceToStationKm
                }
                else -> {
                    MainActivity.mockSecs = etaSecs
                    MainActivity.mockDist = distanceToStationKm
                }
            }
        }

        // 🔔 Sync Notifications in Tracker Activity
        if (etaSecs <= 900 && !MainActivity.notifiedBuses.contains("${busId}_15min")) {
            MainActivity.notifiedBuses.add("${busId}_15min")
            Toast.makeText(this, "🔔 Bus $busId is 15 minutes away!", Toast.LENGTH_LONG).show()
        }
        if (etaSecs <= 600 && !MainActivity.notifiedBuses.contains("${busId}_10min")) {
            MainActivity.notifiedBuses.add("${busId}_10min")
            Toast.makeText(this, "🔔 Bus $busId is 10 minutes away!", Toast.LENGTH_LONG).show()
        }
        if (etaSecs <= 300 && !MainActivity.notifiedBuses.contains("${busId}_5min")) {
            MainActivity.notifiedBuses.add("${busId}_5min")
            Toast.makeText(this, "🔔 Bus $busId is 5 minutes away!", Toast.LENGTH_LONG).show()
        }
        if (etaSecs <= 120 && !MainActivity.notifiedBuses.contains("${busId}_2min")) {
            MainActivity.notifiedBuses.add("${busId}_2min")
            Toast.makeText(this, "🔔 Almost there! Bus $busId is 2 minutes away!", Toast.LENGTH_LONG).show()
        }
        if (etaSecs <= 0 && !MainActivity.notifiedBuses.contains("${busId}_here")) {
            MainActivity.notifiedBuses.add("${busId}_here")
            Toast.makeText(this, "🎉 Bus $busId HAS ARRIVED!", Toast.LENGTH_LONG).show()
        }

        // ── Format distance counter ──────────────────────────────────
        val (distText, unitText) = when {
            distanceToStationKm <= 0.0  -> Pair("0", "m — At Station")
            distanceToStationKm < 0.05  -> Pair("${(distanceToStationKm * 1000).toInt()}", "m — Arriving!")
            distanceToStationKm < 1.0   -> Pair("${(distanceToStationKm * 1000).toInt()}", "m to station")
            else                        -> Pair(String.format(Locale.getDefault(), "%.2f", distanceToStationKm), "km to station")
        }

        // ── Format ETA counter (MM:SS) ───────────────────────────────
        val etaText = when {
            etaSecs <= 0 -> "00:00"
            else         -> String.format(Locale.getDefault(), "%02d:%02d", etaSecs / 60, etaSecs % 60)
        }
        val etaLabelText = when {
            etaSecs <= 0 -> "ARRIVED"
            etaSecs < 60 -> "sec remaining"
            else         -> "min:sec remaining"
        }
        val etaColor = when {
            etaSecs <= 0 -> 0xFFFF9100.toInt()    // orange = arrived
            etaSecs < 120 -> 0xFFFF5252.toInt()   // red = imminent
            else          -> 0xFF00E676.toInt()    // green = normal
        }

        tvLiveDistance.text = distText
        tvDistanceUnit.text = unitText
        tvLiveEta.text      = etaText
        tvEtaLabel.text     = etaLabelText
        tvLiveEta.setTextColor(etaColor)
        
        tvTotalDistance.text = formatDistKm(totalDistKm)
        tvTotalEta.text = "~${totalEtaSecs / 60} min"

        if (MainActivity.isMockTestActive) {
            val dynamicProgress = ((1.0 - (distanceToStationKm / 3.33)) * 100).toInt().coerceIn(0, 100)
            tvProgressPercent.text = "$dynamicProgress%"
            updateProgressBar(dynamicProgress)
        }
    }

    // ── Progress Bar (visual bus icon position) ───────────────────────

    private fun updateProgressBar(pct: Int) {
        // We use layout_weight trick: set left weight = pct, right = 100-pct
        val leftPct  = pct.coerceIn(0, 100).toFloat()
        val rightPct = (100 - leftPct).coerceIn(0f, 100f)

        val leftParams  = progressLeft.layoutParams as android.widget.LinearLayout.LayoutParams
        val rightParams = progressRight.layoutParams as android.widget.LinearLayout.LayoutParams
        leftParams.weight  = leftPct
        rightParams.weight = rightPct
        progressLeft.layoutParams  = leftParams
        progressRight.layoutParams = rightParams
    }

    // ── Live Pulse Animation ──────────────────────────────────────────

    private fun startLivePulse() {
        val scaleX = ObjectAnimator.ofFloat(livePulseDot, "scaleX", 1f, 1.6f, 1f)
        val scaleY = ObjectAnimator.ofFloat(livePulseDot, "scaleY", 1f, 1.6f, 1f)
        val alpha  = ObjectAnimator.ofFloat(livePulseDot, "alpha", 1f, 0.4f, 1f)

        scaleX.repeatCount = android.animation.ValueAnimator.INFINITE
        scaleY.repeatCount = android.animation.ValueAnimator.INFINITE
        alpha.repeatCount = android.animation.ValueAnimator.INFINITE

        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY, alpha)
        set.duration = 1200
        set.interpolator = AccelerateDecelerateInterpolator()
        set.start()
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private fun formatDistKm(km: Double): String = when {
        km <= 0.0  -> "0 m"
        km < 1.0   -> "${(km * 1000).toInt()} m"
        else       -> String.format(Locale.getDefault(), "%.1f km", km)
    }

   
    private fun isInAddisAbaba(lat: Double, lng: Double): Boolean {
        if (lat == 0.0 && lng == 0.0) return true  // allow unset (will show 0 distance)
        return lat in 8.7..9.3 && lng in 38.5..38.95
    }

    // ── Companion ─────────────────────────────────────────────────────

    companion object {
        const val EXTRA_BUS_ID    = "TRACKER_BUS_ID"
        const val EXTRA_FROM_STOP = "TRACKER_FROM_STOP"
        const val EXTRA_TO_STOP   = "TRACKER_TO_STOP"
    }
}
