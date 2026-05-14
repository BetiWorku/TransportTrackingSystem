package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.*
import com.example.transporttrackingsystem.adapters.*

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserNewsActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private val allNewsList = mutableListOf<News>()
    private val newsList = mutableListOf<News>()
    private lateinit var adapter: NewsAdapter
    private var showingAllNews = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_news)

        db = FirebaseFirestore.getInstance()
        val rv = findViewById<RecyclerView>(R.id.rvUserNews)
        
        adapter = NewsAdapter(newsList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<android.widget.ImageView>(R.id.btnBackNews).setOnClickListener { finish() }
        
        val btnClear = findViewById<android.widget.TextView>(R.id.btnClearAllNews)
        btnClear.setOnClickListener {
            if (newsList.isNotEmpty()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete All Notifications")
                    .setMessage("Are you sure you want to delete all notifications?")
                    .setPositiveButton("Delete") { _, _ ->
                        clearAllNotifications()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        val tvViewAll = findViewById<android.widget.TextView>(R.id.tvViewAllNews)
        tvViewAll.setOnClickListener {
            showingAllNews = !showingAllNews
            updateListUI()
        }

        fetchNews()
    }

    private fun updateListUI() {
        newsList.clear()
        if (showingAllNews) {
            newsList.addAll(allNewsList)
            findViewById<android.widget.TextView>(R.id.tvViewAllNews).text = "View Less"
        } else {
            newsList.addAll(allNewsList.take(4))
            findViewById<android.widget.TextView>(R.id.tvViewAllNews).text = "View All"
        }
        
        // Hide View All button if 4 or fewer items
        if (allNewsList.size <= 4) {
            findViewById<android.widget.TextView>(R.id.tvViewAllNews).visibility = android.view.View.GONE
        } else {
            findViewById<android.widget.TextView>(R.id.tvViewAllNews).visibility = android.view.View.VISIBLE
        }
        
        adapter.notifyDataSetChanged()
    }

    private fun clearAllNotifications() {
        val batch = db.batch()
        db.collection("news").get().addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnSuccessListener {
                android.widget.Toast.makeText(this, "Notifications cleared", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchNews() {
        db.collection("news").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                allNewsList.clear()
                snapshots?.forEach { allNewsList.add(it.toObject(News::class.java)) }
                updateListUI()
            }
    }
}
