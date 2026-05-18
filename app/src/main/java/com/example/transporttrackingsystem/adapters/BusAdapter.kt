package com.example.transporttrackingsystem.adapters

import com.example.transporttrackingsystem.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class BusInfo(
    val id: String, 
    val destination: String, 
    val distance: String, 
    val eta: String, 
    var sortSecs: Int = Int.MAX_VALUE,
    val type: String = "City Bus",
    val passengers: Int = 0,
    val capacity: Int = 30,
    val currentStop: String = "Unknown",
    val nextStop: String = "Next Stop...",
    var distKm: Double = 0.0,
    val totalDist: String = "",
    val totalEta: String = ""
)

class BusAdapter(
    private var buses: List<BusInfo>, 
    private val onBusClick: (String) -> Unit,
    private val onTrackClick: (String) -> Unit
) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    class BusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busName: TextView = view.findViewById(R.id.busName)
        val busDestination: TextView = view.findViewById(R.id.busDestination)
        val busDistance: TextView = view.findViewById(R.id.busDistance)
        val busEta: TextView = view.findViewById(R.id.busEta)
        val busType: TextView = view.findViewById(R.id.tvBusType)
        val busPassengers: TextView = view.findViewById(R.id.tvPassengers)
        val tvCurrentStop: TextView = view.findViewById(R.id.tvCurrentStop)
        val tvNextStop: TextView = view.findViewById(R.id.tvNextStop)
        val btnTrackLive: TextView = view.findViewById(R.id.btnItemTrackLive)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val bus = buses[position]
        
        // 🚌 Combined Name: Shows Type and ID (e.g. "Anbessa - Wolo-25-01")
        val fullTitle = if (bus.id.contains(bus.type)) bus.id else "${bus.type} - ${bus.id.replace("⭐ BEST: ", "")}"
        holder.busName.text = if (bus.id.startsWith("⭐ BEST")) "⭐ BEST: $fullTitle" else fullTitle
        
        holder.busDestination.text = "To: ${bus.destination}"
        holder.busDistance.text = if (bus.totalDist.isNotEmpty()) "${bus.distance} (${bus.totalDist} to Dest)" else bus.distance
        holder.busEta.text = if (bus.totalEta.isNotEmpty()) "${bus.eta}\nTotal: ${bus.totalEta}" else bus.eta
        holder.busType.text = bus.type
        holder.busType.visibility = View.GONE // Hide original type pill as it's now in the title
        holder.busPassengers.text = "${bus.passengers}/${bus.capacity}"
        holder.tvCurrentStop.text = "At: ${bus.currentStop}"
        holder.tvNextStop.text = "Next: ${bus.nextStop}"
        
        if (bus.passengers >= bus.capacity) {
            holder.busPassengers.setTextColor(android.graphics.Color.RED)
        } else {
            holder.busPassengers.setTextColor(android.graphics.Color.parseColor("#43A047"))
        }

        holder.itemView.setOnClickListener { onBusClick(bus.id) }
        holder.btnTrackLive.setOnClickListener { onTrackClick(bus.id) }
    }

    override fun getItemCount() = buses.size

    fun updateData(newBuses: List<BusInfo>) {
        buses = newBuses
        notifyDataSetChanged()
    }
}
