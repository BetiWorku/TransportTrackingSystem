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

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val tvResendEmail = findViewById<TextView>(R.id.tvResendEmail)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 🔍 Fetch user's actual name from Firestore
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val nameFromFirestore = document.getString("name")
                    val nameFromAuth = currentUser.displayName
                    
                    val finalName = when {
                        !nameFromFirestore.isNullOrEmpty() -> nameFromFirestore
                        !nameFromAuth.isNullOrEmpty() -> nameFromAuth
                        else -> "Passenger" // Fallback if name is missing
                    }
                    
                    tvUserName.text = "Welcome, $finalName!"
                }
                .addOnFailureListener {
                    tvUserName.text = "Welcome!"
                }
        } else {
            tvUserName.text = "Welcome!"
        }

        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvResendEmail.setOnClickListener {
            currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


