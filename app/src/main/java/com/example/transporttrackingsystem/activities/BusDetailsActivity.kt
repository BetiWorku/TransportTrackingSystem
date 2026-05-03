package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.*

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class BusDetailsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var busId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_details)

        db = FirebaseFirestore.getInstance()
        busId = intent.getStringExtra("BUS_ID")

        val tvBusId = findViewById<TextView>(R.id.tvBusIdHeader)
        val tvBusType = findViewById<TextView>(R.id.tvBusTypeHeader)
        val tvPassengers = findViewById<TextView>(R.id.tvPassengersDetail)
        val tvEta = findViewById<TextView>(R.id.tvEtaDetail)
        val tvRoute = findViewById<TextView>(R.id.tvRouteDetail)
        val tvTerminal = findViewById<TextView>(R.id.tvTerminalDetail)
        val tvDriver = findViewById<TextView>(R.id.tvDriverDetail)
        val tvStatus = findViewById<TextView>(R.id.tvStatusDetail)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnTrack = findViewById<Button>(R.id.btnTrackOnMap)

        val fromStop = intent.getStringExtra("FROM_STOP") ?: ""
        val toStop   = intent.getStringExtra("TO_STOP")   ?: ""

        btnBack.setOnClickListener { finish() }
        btnTrack.setOnClickListener {
            if (busId != null) {
                val intent = Intent(this, BusTrackerActivity::class.java).apply {
                    putExtra(BusTrackerActivity.EXTRA_BUS_ID,    busId)
                    putExtra(BusTrackerActivity.EXTRA_FROM_STOP, fromStop)
                    putExtra(BusTrackerActivity.EXTRA_TO_STOP,   toStop)
                }
                startActivity(intent)
            }
        }

        if (busId != null) {
            tvBusId.text = busId
            listenToBusDetails(busId!!)
        } else {
            Toast.makeText(this, "Bus ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun listenToBusDetails(id: String) {
        db.collection("buses").document(id).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Error fetching details", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val type = snapshot.getString("busType") ?: "City Bus"
                val passengers = snapshot.getLong("passengers") ?: 0
                val capacity = snapshot.getLong("capacity") ?: 30
                val terminal = snapshot.getString("terminal") ?: "Unknown"
                val route = snapshot.getString("routeId") ?: "N/A"
                val driver = snapshot.getString("driverName") ?: "Unknown"
                val status = snapshot.getString("status") ?: "Active"

                findViewById<TextView>(R.id.tvBusTypeHeader).text = type
                findViewById<TextView>(R.id.tvPassengersDetail).text = "$passengers / $capacity"
                findViewById<TextView>(R.id.tvTerminalDetail).text = "Terminal: $terminal"
                findViewById<TextView>(R.id.tvRouteDetail).text = "Route: $route"
                findViewById<TextView>(R.id.tvDriverDetail).text = "Driver: $driver"
                findViewById<TextView>(R.id.tvStatusDetail).text = "Status: $status"
                
                // Color coding for passengers
                if (passengers >= capacity) {
                    findViewById<TextView>(R.id.tvPassengersDetail).setTextColor(android.graphics.Color.RED)
                } else {
                    findViewById<TextView>(R.id.tvPassengersDetail).setTextColor(android.graphics.Color.parseColor("#43A047"))
                }
            }
        }
    }
}
