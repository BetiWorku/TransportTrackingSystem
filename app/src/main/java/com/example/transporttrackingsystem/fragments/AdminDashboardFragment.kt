package com.example.transporttrackingsystem.fragments

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.*
import com.example.transporttrackingsystem.adapters.*
import com.example.transporttrackingsystem.activities.*

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminFleetAdapter
    private val busList = mutableListOf<Bus>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
        db = FirebaseFirestore.getInstance()

        val rvFleet = view.findViewById<RecyclerView>(R.id.rvAdminFleet)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalBuses)
        val tvActive = view.findViewById<TextView>(R.id.tvActiveBuses)
        val tvCapacity = view.findViewById<TextView>(R.id.tvTotalCapacity)

        adapter = AdminFleetAdapter(busList, 
            onEdit = { bus -> showEditBusDialog(bus) },
            onDelete = { bus -> deleteBus(bus) }
        )

        rvFleet.layoutManager = LinearLayoutManager(context)
        rvFleet.adapter = adapter

        fetchFleetData(tvTotal, tvActive, tvCapacity)

        view.findViewById<View>(R.id.fabAddBus).setOnClickListener {
            (activity as? BusRegistrationActivity)?.let { adminActivity ->
                val navView = adminActivity.findViewById<com.google.android.material.navigation.NavigationView>(R.id.adminNavigationView)
                navView.setCheckedItem(R.id.nav_register_bus)
                adminActivity.supportFragmentManager.beginTransaction()
                    .replace(R.id.adminFragmentContainer, RegisterBusFragment())
                    .commit()
                adminActivity.findViewById<TextView>(R.id.adminPageTitle).text = "Register Vehicle"
            }
        }

        view.findViewById<View>(R.id.btnWipeFleet).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Wipe Fleet Data")
                .setMessage("Are you sure you want to remove ALL buses from the system? This cannot be undone.")
                .setPositiveButton("Wipe All") { _, _ -> wipeAllBuses() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return view
    }

    private fun fetchFleetData(tvTotal: TextView, tvActive: TextView, tvCapacity: TextView) {
        db.collection("buses").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener

            busList.clear()
            var totalCap = 0
            var activeCount = 0

            for (doc in snapshot.documents) {
                val bus = doc.toObject(Bus::class.java)
                if (bus != null) {
                    busList.add(bus)
                    totalCap += bus.capacity
                    if (bus.status == "Active") activeCount++
                }
            }

            tvTotal.text = busList.size.toString()
            tvActive.text = activeCount.toString()
            tvCapacity.text = totalCap.toString()
            adapter.notifyDataSetChanged()
        }
    }

    private fun deleteBus(bus: Bus) {
        db.collection("buses").document(bus.busId).delete()
            .addOnSuccessListener { Toast.makeText(context, "Bus Deleted", Toast.LENGTH_SHORT).show() }
    }

    private fun showEditBusDialog(bus: Bus) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_bus, null)
        val etType = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editBusType)
        val etRoute = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editRouteId)
        val etTerminal = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTerminal)
        val etDriver = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editDriverName)
        val etStatus = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editStatus)

        // Pre-fill
        etType.setText(bus.busType)
        etRoute.setText(bus.routeId)
        etTerminal.setText(bus.terminal)
        etDriver.setText(bus.driverName)
        etStatus.setText(bus.status)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updates = hashMapOf<String, Any>(
                    "busType" to etType.text.toString(),
                    "routeId" to etRoute.text.toString(),
                    "terminal" to etTerminal.text.toString(),
                    "driverName" to etDriver.text.toString(),
                    "status" to etStatus.text.toString()
                )
                db.collection("buses").document(bus.busId).update(updates)
                    .addOnSuccessListener { Toast.makeText(context, "Bus Updated", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { e -> Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun wipeAllBuses() {
        db.collection("buses").get().addOnSuccessListener { snapshots ->
            val batch = db.batch()
            for (doc in snapshots) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnSuccessListener {
                Toast.makeText(context, "All buses removed from fleet.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
