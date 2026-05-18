package com.example.transporttrackingsystem.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.adapters.BusAdapter
import com.example.transporttrackingsystem.adapters.BusInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var rvFleet: RecyclerView
    private lateinit var busAdapter: BusAdapter
    private val fleetList = mutableListOf<BusInfo>()
    
    private lateinit var tvTotalBuses: TextView
    private lateinit var tvPending: TextView
    private lateinit var tvRoutes: TextView
    private lateinit var tvFleetCount: TextView
    
    private var statsListener: ListenerRegistration? = null
    private var fleetListener: ListenerRegistration? = null
    private var complaintsListener: ListenerRegistration? = null
    private var routesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI
        tvTotalBuses = findViewById(R.id.tvDashTotalBuses)
        tvPending = findViewById(R.id.tvDashPending)
        tvRoutes = findViewById(R.id.tvDashRoutes)
        tvFleetCount = findViewById(R.id.tvFleetCount)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnRefresh).setOnClickListener { 
            Toast.makeText(this, "Refreshing Dashboard...", Toast.LENGTH_SHORT).show()
            startDashboardSync()
        }

        // Setup RecyclerView
        rvFleet = findViewById(R.id.rvDashFleet)
        rvFleet.layoutManager = LinearLayoutManager(this)
        
        busAdapter = BusAdapter(
            fleetList,
            onBusClick = { id -> 
                Toast.makeText(this, "Selected Bus: $id", Toast.LENGTH_SHORT).show() 
            },
            onTrackClick = { id -> 
                Toast.makeText(this, "Tracking Live Bus: $id", Toast.LENGTH_SHORT).show() 
            }
        )
        rvFleet.adapter = busAdapter

        startDashboardSync()
    }

    private fun startDashboardSync() {
        // Remove old listeners if any
        statsListener?.remove()
        fleetListener?.remove()
        complaintsListener?.remove()
        routesListener?.remove()

        // 1. Fetch System Metrics
        statsListener = db.collection("buses").addSnapshotListener { snapshot, _ ->
            var total = 0
            snapshot?.forEach { doc ->
                val busType = doc.getString("busType") ?: ""
                val terminal = doc.getString("terminal") ?: ""
                if (busType.isNotEmpty() && !busType.contains("Unknown", ignoreCase = true) &&
                    terminal.isNotEmpty() && !terminal.contains("Unknown", ignoreCase = true)) {
                    total++
                }
            }
            tvTotalBuses.text = total.toString()
            tvFleetCount.text = "Active Fleet ($total)"
        }

        // Filter pending complaints by the current user's ID
        val currentUser = auth.currentUser
        if (currentUser != null) {
            complaintsListener = db.collection("complaints")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("status", "pending")
                .addSnapshotListener { snapshot, _ ->
                    tvPending.text = (snapshot?.size() ?: 0).toString()
                }
        } else {
            tvPending.text = "0"
        }

        routesListener = db.collection("routes").addSnapshotListener { snapshot, _ ->
            tvRoutes.text = (snapshot?.size() ?: 0).toString()
        }

        // 2. Fetch Fleet List
        fleetListener = db.collection("buses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                fleetList.clear()
                snapshot?.forEach { doc ->
                    val busType = doc.getString("busType") ?: ""
                    val terminal = doc.getString("terminal") ?: ""
                    
                    // Filter out unknown/invalid entries
                    if (busType.isEmpty() || busType.contains("Unknown", ignoreCase = true)) return@forEach
                    if (terminal.isEmpty() || terminal.contains("Unknown", ignoreCase = true)) return@forEach
                    
                    val busIdField = doc.getString("busId") ?: ""
                    val id = if (busIdField.isNotEmpty()) busIdField else doc.id
                    val passengers = doc.getLong("passengers")?.toInt() ?: 0
                    val capacity = doc.getLong("capacity")?.toInt() ?: 30
                    
                    // Manually build BusInfo structure
                    val bus = BusInfo(
                        id = id,
                        destination = terminal,
                        distance = "Live Tracking",
                        eta = "Active Now",
                        type = busType,
                        passengers = passengers,
                        capacity = capacity
                    )
                    fleetList.add(bus)
                }
                busAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        statsListener?.remove()
        fleetListener?.remove()
        complaintsListener?.remove()
        routesListener?.remove()
    }
}
