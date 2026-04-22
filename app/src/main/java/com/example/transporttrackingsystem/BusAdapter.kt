package com.example.transporttrackingsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class BusInfo(val id: String, val destination: String, val distance: String, val eta: String)

class BusAdapter(private var buses: List<BusInfo>) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    class BusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busName: TextView = view.findViewById(R.id.busName)
        val busDestination: TextView = view.findViewById(R.id.busDestination)
        val busDistance: TextView = view.findViewById(R.id.busDistance)
        val busEta: TextView = view.findViewById(R.id.busEta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val bus = buses[position]
        holder.busName.text = bus.id
        holder.busDestination.text = "To: ${bus.destination}"
        holder.busDistance.text = bus.distance
        holder.busEta.text = bus.eta
    }

    override fun getItemCount() = buses.size

    fun updateData(newBuses: List<BusInfo>) {
        buses = newBuses
        notifyDataSetChanged()
    }
}
