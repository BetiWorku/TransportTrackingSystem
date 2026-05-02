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
import com.google.firebase.firestore.FirebaseFirestore

class RoutesFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private val routeList = mutableListOf<Route>()
    private lateinit var adapter: RouteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_routes, container, false)
        db = FirebaseFirestore.getInstance()

        val etId = view.findViewById<EditText>(R.id.etRouteId)
        val etName = view.findViewById<EditText>(R.id.etRouteName)
        val etBus = view.findViewById<EditText>(R.id.etBusNumber)
        val btnSave = view.findViewById<Button>(R.id.btnSaveRoute)
        val rv = view.findViewById<RecyclerView>(R.id.rvRoutes)

        adapter = RouteAdapter(routeList)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        btnSave.setOnClickListener {
            val route = Route(etId.text.toString(), etName.text.toString(), etBus.text.toString())
            if (route.routeId.isNotEmpty()) {
                db.collection("routes").document(route.routeId).set(route)
                    .addOnSuccessListener { 
                        Toast.makeText(context, "Route Saved", Toast.LENGTH_SHORT).show()
                        etId.text.clear(); etName.text.clear(); etBus.text.clear()
                    }
            }
        }

        fetchRoutes()
        return view
    }

    private fun fetchRoutes() {
        db.collection("routes").addSnapshotListener { snapshots, _ ->
            routeList.clear()
            snapshots?.forEach { routeList.add(it.toObject(Route::class.java)) }
            adapter.notifyDataSetChanged()
        }
    }
}
