package com.example.transporttrackingsystem

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
    val sortSecs: Int = Int.MAX_VALUE,
    val type: String = "City Bus",
    val passengers: Int = 0,
    val capacity: Int = 30,
    val currentStop: String = "Unknown",
    val nextStop: String = "Next Stop..."
)

class BusAdapter(private var buses: List<BusInfo>, private val onBusClick: (String) -> Unit) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    class BusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busName: TextView = view.findViewById(R.id.busName)
        val busDestination: TextView = view.findViewById(R.id.busDestination)
        val busDistance: TextView = view.findViewById(R.id.busDistance)
        val busEta: TextView = view.findViewById(R.id.busEta)
        val busType: TextView = view.findViewById(R.id.tvBusType)
        val busPassengers: TextView = view.findViewById(R.id.tvPassengers)
        val tvCurrentStop: TextView = view.findViewById(R.id.tvCurrentStop)
        val tvNextStop: TextView = view.findViewById(R.id.tvNextStop)
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
        holder.busType.text = bus.type
        holder.busPassengers.text = "${bus.passengers}/${bus.capacity}"
        holder.tvCurrentStop.text = "At: ${bus.currentStop}"
        holder.tvNextStop.text = "Next: ${bus.nextStop}"
        
        if (bus.passengers >= bus.capacity) {
            holder.busPassengers.setTextColor(android.graphics.Color.RED)
        } else {
            holder.busPassengers.setTextColor(android.graphics.Color.parseColor("#43A047"))
        }

        holder.itemView.setOnClickListener { onBusClick(bus.id) }
    }

    override fun getItemCount() = buses.size

    fun updateData(newBuses: List<BusInfo>) {
        buses = newBuses
        notifyDataSetChanged()
    }
}
