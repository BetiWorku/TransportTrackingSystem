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
                
                // 🛑 BLOCK ADMIN LOGIN ON MOBILE
                if (email.lowercase() == "bwwmas@gmail.com") {
                    AlertDialog.Builder(this)
                        .setTitle("Access Denied")
                        .setMessage("Admin accounts can only log in via the Web Dashboard. Please use a passenger account for the mobile app.")
                        .setPositiveButton("OK", null)
                        .show()
                    return@setOnClickListener
                }

                // Standard Login Flow for other users
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid ?: ""
                            val userEmail = user?.email?.lowercase() ?: ""
                            
                            // Check verification status in Firestore
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val isVerified = document.getBoolean("isVerified") ?: false
                                        val name = document.getString("name") ?: ""
                                        
                                        if (isVerified || userEmail == "bwwmas@gmail.com") {
                                            // GO TO WELCOME SCREEN
                                            startActivity(Intent(this, WelcomeActivity::class.java))
                                            finish()
                                        } else {
                                            // 🛑 Block access until verified by OTP
                                            AlertDialog.Builder(this@LoginActivity)
                                                .setTitle("Verification Required")
                                                .setMessage("Your account has not been verified yet. Please enter the 6-digit code we sent to $userEmail to verify your email and activate your account.")
                                                .setCancelable(false)
                                                .setPositiveButton("Verify Now") { _, _ ->
                                                    val intent = Intent(this@LoginActivity, OtpVerificationActivity::class.java).apply {
                                                        putExtra("EXTRA_EMAIL", userEmail)
                                                        putExtra("EXTRA_USER_ID", userId)
                                                        putExtra("EXTRA_NAME", name)
                                                    }
                                                    startActivity(intent)
                                                    finish()
                                                }
                                                .setNegativeButton("Dismiss") { _, _ ->
                                                    auth.signOut()
                                                }
                                                .show()
                                        }
                                    } else {
                                        // No user doc found, sign out
                                        Toast.makeText(this, "User profile not found. Please register again.", Toast.LENGTH_LONG).show()
                                        auth.signOut()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to check verification: ${e.message}", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                }
                        } else {
                            val exception = task.exception
                            val message = when (exception) {
                                is com.google.firebase.FirebaseNetworkException -> "Connection lost. Please check your internet."
                                is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Email is incorrect. Please try again."
                                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "The email and password is incorrect. Please find the correct details and try again."
                                else -> "Connection lost or email and password incorrect. Please find the correct details and try again."
                            }

                            AlertDialog.Builder(this)
                                .setTitle("Login Failed")
                                .setMessage(message)
                                .setPositiveButton("Try Again", null)
                                .show()
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
