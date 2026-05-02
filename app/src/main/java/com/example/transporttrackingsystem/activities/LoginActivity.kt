package com.example.transporttrackingsystem.activities

import android.content.Intent
import com.example.transporttrackingsystem.R
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // ✅ Must call setContentView FIRST before any auth redirect
        setContentView(R.layout.activity_login)

        // ✅ SplashActivity already handles auth routing → just show login form

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        
        // 🛠 EMERGENCY WIPE (Separate listener for long-press or a different logic)
        // Let's make it so Clicking tvRegister goes to Register, but LONG CLICK wipes.
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvRegister.setOnLongClickListener {
            val email = etEmail.text.toString()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Trying to wipe $email...", Toast.LENGTH_SHORT).show()
                auth.signInWithEmailAndPassword(email, "12345678").addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        db.collection("users").document(user?.uid ?: "").delete()
                        user?.delete()?.addOnCompleteListener {
                            Toast.makeText(this, "Account Wiped! You can now Register.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Could not find account to wipe.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Enter email & long-press here to wipe", Toast.LENGTH_SHORT).show()
            }
            true
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                
                // 🔐 HARDCODED ADMIN BYPASS
                if (email == "bwwmas@gmail.com" && password == "Yotorb123#") {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Admin Authenticated!", Toast.LENGTH_SHORT).show()
                            // 🚀 GO TO WELCOME SCREEN
                            val intent = Intent(this, WelcomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Admin Auth Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@setOnClickListener
                }

                // Standard Login Flow for other users
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userEmail = user?.email?.lowercase() ?: ""
                            
                            if (user?.isEmailVerified == true || userEmail == "bwwmas@gmail.com") {
                                // GO TO WELCOME SCREEN
                                startActivity(Intent(this, WelcomeActivity::class.java))
                                finish()
                            } else {
                                // 🛑 Block access until verified
                                AlertDialog.Builder(this)
                                    .setTitle("Email Not Verified")
                                    .setMessage("Please verify your email at ${user?.email} before logging in. Check your Spam folder if you don't see it!")
                                    .setPositiveButton("I Verified It") { _, _ -> 
                                        // Refresh user and try again
                                        user?.reload()?.addOnCompleteListener { 
                                            btnLogin.performClick() // Trigger login check again
                                        }
                                    }
                                    .setNegativeButton("Resend Link") { _, _ -> 
                                        user?.sendEmailVerification()?.addOnCompleteListener { resendTask ->
                                            if (resendTask.isSuccessful) {
                                                Toast.makeText(this, "Verification link resent!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this, "Resend failed: ${resendTask.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                    .setNeutralButton("Dismiss") { _, _ -> auth.signOut() }
                                    .show()
                            }
                        } else {
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
