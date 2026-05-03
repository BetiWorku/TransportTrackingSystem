package com.example.transporttrackingsystem.adapters

import com.example.transporttrackingsystem.models.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RouteAdapter(private val routes: List<Route>) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(android.R.id.text1)
        val id: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val route = routes[position]
        holder.name.text = route.routeName
        holder.name.setTextColor(android.graphics.Color.parseColor("#1A237E"))
        holder.name.textSize = 16f
        holder.name.setTypeface(null, android.graphics.Typeface.BOLD)

        holder.id.text = "ID: ${route.routeId} | Bus: ${route.busNumber}"
        holder.id.setTextColor(android.graphics.Color.parseColor("#333333"))
    }

    override fun getItemCount() = routes.size
}

class StopAdapter(private val stops: List<Stop>) : RecyclerView.Adapter<StopAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(android.R.id.text1)
        val details: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stop = stops[position]
        holder.name.text = "${stop.stopOrder}. ${stop.stopName}"
        holder.name.setTextColor(android.graphics.Color.parseColor("#1A237E"))
        holder.name.textSize = 16f
        holder.name.setTypeface(null, android.graphics.Typeface.BOLD)

        holder.details.text = "Route: ${stop.routeId} | Lat: ${stop.latitude}, Lng: ${stop.longitude}"
        holder.details.setTextColor(android.graphics.Color.parseColor("#333333"))
    }

    override fun getItemCount() = stops.size
}

class NewsAdapter(private val newsItems: List<News>) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val content: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = newsItems[position]
        holder.title.text = item.title
        holder.title.setTextColor(android.graphics.Color.parseColor("#1A237E"))
        holder.title.textSize = 16f
        holder.title.setTypeface(null, android.graphics.Typeface.BOLD)

        holder.content.text = item.content
        holder.content.setTextColor(android.graphics.Color.parseColor("#333333"))
    }

    override fun getItemCount() = newsItems.size
}
