package com.example.transporttrackingsystem.fragments

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.*
import com.example.transporttrackingsystem.adapters.*

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NewsFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private val newsList = mutableListOf<News>()
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_news, container, false)
        db = FirebaseFirestore.getInstance()

        val etTitle = view.findViewById<EditText>(R.id.etNewsTitle)
        val etContent = view.findViewById<EditText>(R.id.etNewsContent)
        val btnPost = view.findViewById<Button>(R.id.btnPostNews)
        val rv = view.findViewById<RecyclerView>(R.id.rvNews)

        adapter = NewsAdapter(newsList)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        btnPost.setOnClickListener {
            val news = News(
                newsId = "NEWS_${System.currentTimeMillis()}",
                title = etTitle.text.toString(),
                content = etContent.text.toString()
            )
            if (news.title.isNotEmpty() && news.content.isNotEmpty()) {
                db.collection("news").document(news.newsId).set(news)
                    .addOnSuccessListener { 
                        Toast.makeText(context, "News Posted", Toast.LENGTH_SHORT).show()
                        etTitle.text.clear()
                        etContent.text.clear()
                    }
            }
        }

        fetchNews()
        return view
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
