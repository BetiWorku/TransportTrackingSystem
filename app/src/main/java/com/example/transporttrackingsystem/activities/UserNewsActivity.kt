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
    private val newsList = mutableListOf<News>()
    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_news)

        db = FirebaseFirestore.getInstance()
        val rv = findViewById<RecyclerView>(R.id.rvUserNews)
        
        adapter = NewsAdapter(newsList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        fetchNews()
    }

    private fun fetchNews() {
        db.collection("news").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                newsList.clear()
                snapshots?.forEach { newsList.add(it.toObject(News::class.java)) }
                adapter.notifyDataSetChanged()
            }
    }
}
