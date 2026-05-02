package com.example.transporttrackingsystem.adapters

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class AdminFleetAdapter(
    private val buses: List<Bus>,
    private val onEdit: (Bus) -> Unit,
    private val onDelete: (Bus) -> Unit
) : RecyclerView.Adapter<AdminFleetAdapter.AdminViewHolder>() {

    class AdminViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.adminBusId)
        val tvDetails: TextView = view.findViewById(R.id.adminBusDetails)
        val tvDriver: TextView = view.findViewById(R.id.adminBusDriver)
        val tvStatus: TextView = view.findViewById(R.id.adminBusStatus)
        val btnEdit: MaterialButton = view.findViewById(R.id.btnEditBus)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDeleteBus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_bus, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val bus = buses[position]
        holder.tvId.text = bus.busId
        holder.tvDetails.text = "${bus.busType} - Route ${bus.routeId}"
        holder.tvDriver.text = "Driver: ${bus.driverName} (${bus.driverPhone})"
        holder.tvStatus.text = bus.status
        
        holder.btnEdit.setOnClickListener { onEdit(bus) }
        holder.btnDelete.setOnClickListener { onDelete(bus) }
    }

    override fun getItemCount() = buses.size
}
