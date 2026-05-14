package com.example.transporttrackingsystem.activities

import android.os.Bundle
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.Complaint
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Filter
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class ComplaintActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val allComplaintsList = mutableListOf<Complaint>()
    private val complaintsList = mutableListOf<Complaint>()
    private val uidList = mutableListOf<Complaint>()
    private val emailList = mutableListOf<Complaint>()
    private lateinit var adapter: UserComplaintAdapter
    private var showingAllComplaints = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<android.widget.ImageView>(R.id.btnBackComplaint)
        btnBack.setOnClickListener { finish() }

        val etSubject = findViewById<TextInputEditText>(R.id.etComplaintSubject)
        val etMessage = findViewById<TextInputEditText>(R.id.etComplaintMessage)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitComplaint)
        val rv = findViewById<RecyclerView>(R.id.rvUserComplaints)

        adapter = UserComplaintAdapter(complaintsList) { complaint ->
            deleteComplaint(complaint)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        rv.isNestedScrollingEnabled = false // Fix for ScrollView issue
        
        val tvViewAll = findViewById<TextView>(R.id.tvViewAllComplaints)
        tvViewAll.setOnClickListener {
            showingAllComplaints = !showingAllComplaints
            updateListUI()
        }

        btnSubmit.setOnClickListener {
            val subject = etSubject.text.toString().trim()
            val message = etMessage.text.toString().trim()
            val user = auth.currentUser

            if (subject.isNotEmpty() && message.isNotEmpty() && user != null) {
                val complaint = Complaint(
                    id = UUID.randomUUID().toString(),
                    userId = user.uid,
                    userEmail = user.email ?: "Anonymous",
                    subject = subject,
                    message = message,
                    status = "pending",
                    timestamp = Timestamp.now()
                )

                db.collection("complaints").document(complaint.id).set(complaint)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Report submitted successfully!", Toast.LENGTH_SHORT).show()
                        etSubject.text?.clear()
                        etMessage.text?.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Submission failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                fetchUserComplaints()
            } else {
                Toast.makeText(this, "Please log in to view your reports.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteComplaint(complaint: Complaint) {
        AlertDialog.Builder(this)
            .setTitle("Delete Report")
            .setMessage("Are you sure you want to delete this report?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("complaints").document(complaint.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateListUI() {
        complaintsList.clear()
        if (showingAllComplaints) {
            complaintsList.addAll(allComplaintsList)
        } else {
            complaintsList.addAll(allComplaintsList.take(2))
        }
        adapter.notifyDataSetChanged()
        
        val tvViewAll = findViewById<TextView>(R.id.tvViewAllComplaints)
        if (allComplaintsList.size > 2) {
            tvViewAll.visibility = View.VISIBLE
            tvViewAll.text = if (showingAllComplaints) "View Less" else "View All Reports"
        } else {
            tvViewAll.visibility = View.GONE
        }
    }

    private fun combineAndRefresh() {
        val combined = (uidList + emailList).distinctBy { it.id }.sortedByDescending { it.timestamp }
        allComplaintsList.clear()
        allComplaintsList.addAll(combined)
        
        // Auto-expand list if launched from notification
        val highlightId = intent.getStringExtra("HIGHLIGHT_COMPLAINT_ID")
        if (highlightId != null) {
            showingAllComplaints = true
        }
        
        updateListUI()
        
        if (allComplaintsList.isEmpty()) {
            Log.d("ComplaintActivity", "No complaints found.")
        } else {
            Log.d("ComplaintActivity", "Displaying ${allComplaintsList.size} complaints.")
            
            if (highlightId != null) {
                val highlightItem = allComplaintsList.find { it.id == highlightId }
                val index = complaintsList.indexOfFirst { it.id == highlightId }
                
                if (index != -1 && highlightItem != null) {
                    val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvUserComplaints)
                    rv.post {
                        rv.smoothScrollToPosition(index)
                    }
                    
                    if (highlightItem.adminReply.isNotEmpty()) {
                        androidx.appcompat.app.AlertDialog.Builder(this@ComplaintActivity)
                            .setTitle("Admin Reply: ${highlightItem.subject}")
                            .setMessage(highlightItem.adminReply)
                            .setPositiveButton("OK") { dialog, _ -> 
                                dialog.dismiss()
                                intent.removeExtra("HIGHLIGHT_COMPLAINT_ID")
                            }
                            .show()
                    }
                }
            }
        }
    }

    private fun fetchUserComplaints() {
        val user = auth.currentUser ?: return
        val userEmail = user.email ?: ""

        uidList.clear()
        emailList.clear()

        // updateListUI moved to class level

        // combineAndRefresh moved to class level

        // Query 1: By User ID
        db.collection("complaints")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("ComplaintActivity", "UID Fetch error: ${error.message}")
                    return@addSnapshotListener
                }
                uidList.clear()
                snapshots?.forEach { doc ->
                    try {
                        uidList.add(doc.toObject(Complaint::class.java))
                    } catch (e: Exception) {
                        Log.e("ComplaintActivity", "Parsing error: ${e.message}")
                    }
                }
                combineAndRefresh()
            }

        // Query 2: By User Email (Safety Fallback)
        if (userEmail.isNotEmpty() && userEmail != "Anonymous") {
            db.collection("complaints")
                .whereEqualTo("userEmail", userEmail)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("ComplaintActivity", "Email Fetch error: ${error.message}")
                        return@addSnapshotListener
                    }
                    emailList.clear()
                    snapshots?.forEach { doc ->
                        try {
                            emailList.add(doc.toObject(Complaint::class.java))
                        } catch (e: Exception) {
                            Log.e("ComplaintActivity", "Parsing error: ${e.message}")
                        }
                    }
                    combineAndRefresh()
                }
        }
    }

    private fun markAsRead() {
        val prefs = getSharedPreferences("ComplaintsPrefs", Context.MODE_PRIVATE)
        val readIds = prefs.getStringSet("read_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        
        var changed = false
        complaintsList.forEach { complaint ->
            if (complaint.adminReply.isNotEmpty() && !readIds.contains(complaint.id)) {
                readIds.add(complaint.id)
                changed = true
            }
        }
        
        if (changed) {
            prefs.edit().putStringSet("read_ids", readIds).apply()
            Log.d("ComplaintActivity", "All new replies marked as read locally")
        }
    }

    override fun onPause() {
        super.onPause()
        markAsRead()
    }
}

class UserComplaintAdapter(
    private val list: List<Complaint>,
    private val onDelete: (Complaint) -> Unit
) : RecyclerView.Adapter<UserComplaintAdapter.ViewHolder>() {
    
    private val expandedMsgIds = mutableSetOf<String>()
    private val expandedReplyIds = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subject: TextView = view.findViewById(R.id.tvCompSubject)
        val message: TextView = view.findViewById(R.id.tvCompMessage)
        val status: TextView = view.findViewById(R.id.tvCompStatus)
        val time: TextView = view.findViewById(R.id.tvCompTime)
        val adminReplyLayout: View = view.findViewById(R.id.layoutAdminReply)
        val adminReplyText: TextView = view.findViewById(R.id.tvAdminReply)
        val btnDelete: View = view.findViewById(R.id.btnDeleteComplaint)
        val newBadge: TextView = view.findViewById(R.id.tvNewReplyBadge)
        val viewMoreMsg: TextView = view.findViewById(R.id.tvViewMoreMsg)
        val viewMoreReply: TextView = view.findViewById(R.id.tvViewMoreReply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_complaint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.subject.text = item.subject
        holder.message.text = item.message
        holder.status.text = item.status.uppercase()
        
        // --- View All for Message ---
        if (item.message.length > 50) {
            holder.viewMoreMsg.visibility = View.VISIBLE
            val isMsgExpanded = expandedMsgIds.contains(item.id)
            holder.message.maxLines = if (isMsgExpanded) Int.MAX_VALUE else 3
            holder.viewMoreMsg.text = if (isMsgExpanded) "View Less" else "View All"
            
            holder.viewMoreMsg.setOnClickListener {
                if (isMsgExpanded) expandedMsgIds.remove(item.id) else expandedMsgIds.add(item.id)
                notifyItemChanged(position)
            }
        } else {
            holder.viewMoreMsg.visibility = View.GONE
            holder.message.maxLines = Int.MAX_VALUE
        }

        
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        holder.time.text = sdf.format(item.timestamp.toDate())

        when (item.status.lowercase()) {
            "resolved" -> {
                holder.status.setTextColor("#1B5E20".toColorInt()) // Deep Green
                holder.status.setBackgroundResource(R.drawable.bg_status_resolved)
                holder.status.text = "✓ RESOLVED"
            }
            "pending" -> {
                holder.status.setTextColor("#E65100".toColorInt()) // Deep Orange
                holder.status.setBackgroundResource(R.drawable.bg_status_pending)
                holder.status.text = "● PENDING"
            }
            else -> {
                holder.status.setTextColor(android.graphics.Color.GRAY)
            }
        }

        if (item.adminReply.isNotEmpty()) {
            holder.adminReplyLayout.visibility = View.VISIBLE
            holder.adminReplyText.text = item.adminReply
            
            // --- View All for Admin Reply ---
            if (item.adminReply.length > 50) {
                holder.viewMoreReply.visibility = View.VISIBLE
                val isReplyExpanded = expandedReplyIds.contains(item.id)
                holder.adminReplyText.maxLines = if (isReplyExpanded) Int.MAX_VALUE else 3
                holder.viewMoreReply.text = if (isReplyExpanded) "View Less" else "View All"
                
                holder.viewMoreReply.setOnClickListener {
                    if (isReplyExpanded) expandedReplyIds.remove(item.id) else expandedReplyIds.add(item.id)
                    notifyItemChanged(position)
                }
            } else {
                holder.viewMoreReply.visibility = View.GONE
                holder.adminReplyText.maxLines = Int.MAX_VALUE
            }
            
            // Highlight item if it's a new reply (not in local read_ids)
            val prefs = holder.itemView.context.getSharedPreferences("ComplaintsPrefs", Context.MODE_PRIVATE)
            val readIds = prefs.getStringSet("read_ids", emptySet()) ?: emptySet()
            
            if (!readIds.contains(item.id)) {
                holder.newBadge.visibility = View.VISIBLE
                holder.itemView.setBackgroundColor("#FFF9C4".toColorInt()) // Light yellow highlight
            } else {
                holder.newBadge.visibility = View.GONE
                holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
            holder.itemView.alpha = 1.0f
        } else {
            holder.adminReplyLayout.visibility = View.GONE
            holder.newBadge.visibility = View.GONE
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            holder.itemView.alpha = 0.9f
        }

        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = list.size
}
