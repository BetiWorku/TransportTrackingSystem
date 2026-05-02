package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogoutSettings)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)
        val btnBack = findViewById<Button>(R.id.btnBackSettings)

        val btnRegisterBusNav = findViewById<Button>(R.id.btnRegisterBusNav)
        val tvAdminHeader = findViewById<TextView>(R.id.tvAdminHeader)

        val currentUser = auth.currentUser
        tvUserEmail.text = "Account: ${currentUser?.email ?: "Guest"}"

        // Check for Admin Role
        currentUser?.uid?.let { uid ->
            val userEmail = currentUser.email?.lowercase() ?: ""
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "Commuter"
                
                // Allow if role is Admin OR if it's Betelhem's specific email
                if (role == "Admin" || userEmail == "bwwmas@gmail.com") {
                    tvAdminHeader.visibility = android.view.View.VISIBLE
                    btnRegisterBusNav.visibility = android.view.View.VISIBLE
                } else {
                    tvAdminHeader.visibility = android.view.View.GONE
                    btnRegisterBusNav.visibility = android.view.View.GONE
                }
            }
        }

        btnRegisterBusNav.setOnClickListener {
            startActivity(Intent(this, BusRegistrationActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnDeleteAccount.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Delete Account?")
                .setMessage("This will permanently remove your profile and all data. You will be able to register again as a new user.")
                .setPositiveButton("Delete Everything") { _, _ ->
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // 1. Delete from Firestore
                        db.collection("users").document(userId).delete()
                        
                        // 2. Delete from Auth
                        auth.currentUser?.delete()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Account Wiped Successfully", Toast.LENGTH_LONG).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Error: Re-login required to delete account.", Toast.LENGTH_LONG).show()
                                auth.signOut()
                                finish()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
