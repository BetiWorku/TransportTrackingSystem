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
        val currentUser = auth.currentUser
        tvUserEmail.text = currentUser?.email ?: "Guest"

        val btnBackSettings = findViewById<TextView>(R.id.btnBackSettings)
        btnBackSettings.setOnClickListener { finish() }

        val btnBackArrow = findViewById<android.widget.ImageView>(R.id.btnBack)
        btnBackArrow.setOnClickListener { finish() }

        val etUpdateName = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUpdateName)
        val etUpdatePassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUpdatePassword)
        val btnUpdateProfile = findViewById<Button>(R.id.btnUpdateProfile)

        // Load existing name
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                val currentName = doc.getString("name") ?: ""
                etUpdateName.setText(currentName)
            }
        }
        
        btnUpdateProfile.setOnClickListener {
            val newName = etUpdateName.text.toString().trim()
            val newPassword = etUpdatePassword.text.toString().trim()

            if (newName.isNotEmpty()) {
                currentUser?.uid?.let { uid ->
                    db.collection("users").document(uid).update("name", newName)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
            }

            if (newPassword.isNotEmpty()) {
                if (newPassword.length >= 6) {
                    currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            etUpdatePassword.text?.clear()
                        } else {
                            Toast.makeText(this, "Password update failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}
