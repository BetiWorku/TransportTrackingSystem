package com.example.transporttrackingsystem.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.models.Complaint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ComplaintsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private val allComplaintsList = mutableListOf<Complaint>()
    private val complaintsList = mutableListOf<Complaint>()
    private lateinit var adapter: AdminComplaintAdapter
    private var showingAllComplaints = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_complaints, container, false)
        db = FirebaseFirestore.getInstance()

        val rv = view.findViewById<RecyclerView>(R.id.rvAdminComplaints)
        adapter = AdminComplaintAdapter(complaintsList) { complaint, action ->
            when (action) {
                "reply" -> showReplyDialog(complaint)
                "resolve" -> resolveComplaint(complaint)
                "delete" -> deleteComplaint(complaint)
            }
        }
        
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        val tvViewAll = view.findViewById<TextView>(R.id.tvViewAllAdminComplaints)
        tvViewAll?.setOnClickListener {
            showingAllComplaints = !showingAllComplaints
            updateListUI()
        }

        fetchComplaints()
        return view
    }

    private fun updateListUI() {
        complaintsList.clear()
        if (showingAllComplaints) {
            complaintsList.addAll(allComplaintsList)
        } else {
            complaintsList.addAll(allComplaintsList.take(4))
        }
        adapter.notifyDataSetChanged()
        
        val tvViewAll = view?.findViewById<TextView>(R.id.tvViewAllAdminComplaints)
        if (allComplaintsList.size > 4) {
            tvViewAll?.visibility = View.VISIBLE
            tvViewAll?.text = if (showingAllComplaints) "View Less" else "View All Reports"
        } else {
            tvViewAll?.visibility = View.GONE
        }
    }

    private fun fetchComplaints() {
        db.collection("complaints").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                allComplaintsList.clear()
                snapshots?.forEach { allComplaintsList.add(it.toObject(Complaint::class.java)) }
                updateListUI()
            }
    }

    private fun showReplyDialog(complaint: Complaint) {
        val editText = EditText(context)
        editText.hint = "Type your response..."
        editText.setText(complaint.adminReply)

        AlertDialog.Builder(requireContext())
            .setTitle("Reply to ${complaint.userEmail}")
            .setView(editText)
            .setPositiveButton("Send Reply") { _, _ ->
                val reply = editText.text.toString().trim()
                if (reply.isNotEmpty()) {
                    val updateData = hashMapOf(
                        "adminReply" to reply,
                        "status" to "resolved"
                    )
                    db.collection("complaints").document(complaint.id).update(updateData as Map<String, Any>)
                        .addOnSuccessListener { Toast.makeText(context, "Reply sent and status updated to RESOLVED!", Toast.LENGTH_SHORT).show() }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resolveComplaint(complaint: Complaint) {
        val newStatus = if (complaint.status == "pending") "resolved" else "pending"
        db.collection("complaints").document(complaint.id).update("status", newStatus)
            .addOnSuccessListener { Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show() }
    }

    private fun deleteComplaint(complaint: Complaint) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Complaint")
            .setMessage("Are you sure you want to completely remove this complaint?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("complaints").document(complaint.id).delete()
                    .addOnSuccessListener { Toast.makeText(context, "Complaint deleted", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class AdminComplaintAdapter(
    private val list: List<Complaint>,
    private val onAction: (Complaint, String) -> Unit
) : RecyclerView.Adapter<AdminComplaintAdapter.ViewHolder>() {

    private val expandedMsgIds = mutableSetOf<String>()
    private val expandedReplyIds = mutableSetOf<String>()


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val user: TextView = view.findViewById(R.id.tvAdminCompUser)
        val subject: TextView = view.findViewById(R.id.tvAdminCompSubject)
        val message: TextView = view.findViewById(R.id.tvAdminCompMessage)
        val status: TextView = view.findViewById(R.id.tvAdminCompStatus)
        val btnReply: Button = view.findViewById(R.id.btnAdminReply)
        val btnResolve: Button = view.findViewById(R.id.btnAdminResolve)
        val btnDelete: Button = view.findViewById(R.id.btnAdminDelete)
        val replyPreview: TextView = view.findViewById(R.id.tvAdminReplyPreview)
        val viewMoreMsg: TextView = view.findViewById(R.id.tvViewMoreAdminMsg)
        val viewMoreReply: TextView = view.findViewById(R.id.tvViewMoreAdminReply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_complaint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.user.text = "From: ${item.userEmail}"
        holder.subject.text = item.subject
        holder.message.text = item.message
        holder.status.text = item.status.uppercase()

        // --- View All for Admin Message ---
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


        if (item.status == "resolved") {
            holder.status.setTextColor(android.graphics.Color.parseColor("#1B5E20")) // Green
            holder.status.text = "✓ RESOLVED"
            holder.btnResolve.text = "Re-open"
            holder.btnResolve.alpha = 0.6f
        } else {
            holder.status.setTextColor(android.graphics.Color.parseColor("#D32F2F")) // Red
            holder.status.text = "● PENDING"
            holder.btnResolve.text = "Resolve"
            holder.btnResolve.alpha = 1.0f
        }

        if (item.adminReply.isNotEmpty()) {
            holder.replyPreview.visibility = View.VISIBLE
            holder.replyPreview.text = "Your reply: ${item.adminReply}"
            
            // --- View All for Admin Reply Preview ---
            if (item.adminReply.length > 50) {
                holder.viewMoreReply.visibility = View.VISIBLE
                val isReplyExpanded = expandedReplyIds.contains(item.id)
                holder.replyPreview.maxLines = if (isReplyExpanded) Int.MAX_VALUE else 3
                holder.viewMoreReply.text = if (isReplyExpanded) "View Less" else "View All"
                
                holder.viewMoreReply.setOnClickListener {
                    if (isReplyExpanded) expandedReplyIds.remove(item.id) else expandedReplyIds.add(item.id)
                    notifyItemChanged(position)
                }
            } else {
                holder.viewMoreReply.visibility = View.GONE
                holder.replyPreview.maxLines = Int.MAX_VALUE
            }
        } else {
            holder.replyPreview.visibility = View.GONE
            holder.viewMoreReply.visibility = View.GONE
            holder.replyPreview.maxLines = Int.MAX_VALUE
        }


        holder.btnReply.setOnClickListener { onAction(item, "reply") }
        holder.btnResolve.setOnClickListener { onAction(item, "resolve") }
        holder.btnDelete.setOnClickListener { onAction(item, "delete") }
    }

    override fun getItemCount() = list.size
}
