package com.example.transporttrackingsystem.fragments

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.*
import com.example.transporttrackingsystem.adapters.*

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class TerminalsFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private val stopList = mutableListOf<Stop>()
    private lateinit var adapter: StopAdapter
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_stops, container, false)
        db = FirebaseFirestore.getInstance()

        val etName = view.findViewById<EditText>(R.id.etStopName)
        val etLat = view.findViewById<EditText>(R.id.etLat)
        val etLng = view.findViewById<EditText>(R.id.etLng)
        val etRoute = view.findViewById<EditText>(R.id.etRouteId)
        val etOrder = view.findViewById<EditText>(R.id.etStopOrder)
        val btnAdd = view.findViewById<Button>(R.id.btnAddStop)
        val rv = view.findViewById<RecyclerView>(R.id.rvStops)

        adapter = StopAdapter(stopList)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        btnAdd.setOnClickListener {
            val stop = Stop(
                stopId = "STOP_${System.currentTimeMillis()}",
                stopName = etName.text.toString(),
                latitude = etLat.text.toString().toDoubleOrNull() ?: 0.0,
                longitude = etLng.text.toString().toDoubleOrNull() ?: 0.0,
                routeId = etRoute.text.toString(),
                stopOrder = etOrder.text.toString().toIntOrNull() ?: 0
            )
            if (stop.stopName.isNotEmpty() && stop.routeId.isNotEmpty()) {
                if (stop.latitude == 0.0 || stop.longitude == 0.0) {
                    Toast.makeText(context, "Please provide valid coordinates (Latitude/Longitude)!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                db.collection("stops").document(stop.stopId).set(stop)
                    .addOnSuccessListener { 
                        Toast.makeText(context, "Stop Added", Toast.LENGTH_SHORT).show()
                        etName.text.clear(); etLat.text.clear(); etLng.text.clear(); etOrder.text.clear()
                    }
            } else {
                Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.btnGetCurrentLocation).setOnClickListener {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        etLat.setText(location.latitude.toString())
                        etLng.setText(location.longitude.toString())
                        Toast.makeText(context, "Location updated from GPS", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Could not get location. Is GPS on?", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            }
        }

        fetchStops()
        return view
    }

    private fun fetchStops() {
        db.collection("stops").orderBy("stopOrder").addSnapshotListener { snapshots, _ ->
            stopList.clear()
            snapshots?.forEach { stopList.add(it.toObject(Stop::class.java)) }
            adapter.notifyDataSetChanged()
        }
    }
}
