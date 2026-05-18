package com.example.transporttrackingsystem.adapters

import com.example.transporttrackingsystem.models.*
import com.example.transporttrackingsystem.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt


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

        val context = holder.itemView.context
        if (item.title.contains("Arriving", true) || item.author.contains("Alert", true)) {
            // ✅ SHOW TEXT: Dark Blue text on White background
            holder.title.setTextColor("#1A237E".toColorInt())
            holder.title.background = null
            holder.title.setPadding(0, 0, 0, 0)
            
            holder.author.setTextColor("#E65100".toColorInt())
        } else {
            // ℹ️ STYLE AS NORMAL NEWS: Dark text on transparent background
            holder.title.setTextColor("#1A237E".toColorInt())
            holder.title.background = null
            holder.title.setPadding(0, 0, 0, 0)
            
            holder.author.setTextColor("#3F51B5".toColorInt())
        }
    }

    override fun getItemCount() = newsItems.size
}
