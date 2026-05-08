package com.example.transporttrackingsystem.adapters

import com.example.transporttrackingsystem.models.*
import com.example.transporttrackingsystem.R
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

data class RouteGroup(
    val routeId: String,
    val pathSummary: String,
    val stops: List<Stop>,
    var isExpanded: Boolean = false
)

class StopAdapter(
    private var groups: List<RouteGroup> = emptyList()
) : RecyclerView.Adapter<StopAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val routeId: TextView = view.findViewById(R.id.tvRouteId)
        val routePath: TextView = view.findViewById(R.id.tvRoutePath)
        val stopsContainer: android.widget.LinearLayout = view.findViewById(R.id.stopsContainer)
        val btnExpand: android.widget.Button = view.findViewById(R.id.btnExpandStops)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_route_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.routeId.text = group.routeId
        holder.routePath.text = group.pathSummary
        
        holder.stopsContainer.visibility = if (group.isExpanded) View.VISIBLE else View.GONE
        holder.btnExpand.text = if (group.isExpanded) "Hide Stops" else "View All (${group.stops.size})"
        
        holder.btnExpand.setOnClickListener {
            group.isExpanded = !group.isExpanded
            notifyItemChanged(position)
        }
        
        // Add stops dynamically
        holder.stopsContainer.removeAllViews()
        if (group.isExpanded) {
            group.stops.forEach { stop ->
                val stopView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.item_admin_stop_mini, holder.stopsContainer, false)
                stopView.findViewById<TextView>(R.id.tvMiniStopName).text = "${stop.stopOrder}. ${stop.stopName}"
                stopView.findViewById<TextView>(R.id.tvMiniStopCoords).text = "Lat: ${stop.latitude}, Lng: ${stop.longitude}"
                holder.stopsContainer.addView(stopView)
            }
        }
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<RouteGroup>) {
        this.groups = newGroups
        notifyDataSetChanged()
    }
}

class NewsAdapter(private val newsItems: List<News>) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvNewsTitle)
        val content: TextView = view.findViewById(R.id.tvNewsContent)
        val time: TextView = view.findViewById(R.id.tvNewsTime)
        val author: TextView = view.findViewById(R.id.tvNewsAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = newsItems[position]
        holder.title.text = item.title
        holder.content.text = item.content
        holder.author.text = item.author
        
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        holder.time.text = sdf.format(item.timestamp.toDate())

        if (item.author.contains("Alert", true)) {
            holder.author.setTextColor(android.graphics.Color.parseColor("#E65100"))
        } else {
            holder.author.setTextColor(android.graphics.Color.parseColor("#3F51B5"))
        }
    }

    override fun getItemCount() = newsItems.size
}
