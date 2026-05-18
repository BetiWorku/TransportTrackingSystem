package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.utils.*

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Registration Failed")
                    .setMessage("Please fill in all fields.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                AlertDialog.Builder(this)
                    .setTitle("Invalid Email")
                    .setMessage("Please enter a valid email address.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                AlertDialog.Builder(this)
                    .setTitle("Weak Password")
                    .setMessage("Password must be at least 6 characters long.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // 💾 Save User to Firestore
                                val userRole = if (email.lowercase() == "bwwmas@gmail.com") "Admin" else "Commuter"
                                val otpCode = if (userRole == "Admin") "" else (100000 + java.util.Random().nextInt(900000)).toString()
                                val isVerified = userRole == "Admin" // Admin bypasses verification
                                
                                val user = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "role" to userRole,
                                    "isVerified" to isVerified,
                                    "otp" to otpCode
                                )
                                db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener {
                                        // ✅ Check if Admin to bypass verification and auto-login
                                        if (userRole == "Admin") {
                                            Log.d("REGISTER", "Admin detected: Bypassing verification.")
                                            Toast.makeText(this, "Admin Registered! Welcome Betelhem.", Toast.LENGTH_LONG).show()
                                            
                                            // Send Welcome Email
                                            EmailHelper.sendWelcomeEmail(this@RegisterActivity, email, name)
                                            
                                            val intent = Intent(this, WelcomeActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Log.d("REGISTER", "Commuter registered. Navigating to OTP page for $email")

                                            // ✅ Navigate to OTP page IMMEDIATELY
                                            val intent = Intent(this@RegisterActivity, OtpVerificationActivity::class.java).apply {
                                                putExtra("EXTRA_EMAIL", email)
                                                putExtra("EXTRA_USER_ID", userId)
                                                putExtra("EXTRA_NAME", name)
                                            }
                                            startActivity(intent)
                                            finish()

                                            // 📧 Send OTP Email in background AFTER navigation
                                            EmailHelper.sendOTPEmail(this@RegisterActivity, email, name, otpCode)
                                            EmailHelper.sendWelcomeEmail(this@RegisterActivity, email, name)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            val exception = task.exception
                            val errorMsg = when (exception) {
                                is com.google.firebase.auth.FirebaseAuthUserCollisionException -> 
                                    "This email is already registered. Please login instead."
                                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> 
                                    "The email address is badly formatted."
                                else -> "Registration Failed: ${exception?.message}"
                            }
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
        }

        tvLogin.setOnClickListener {
            finish() // Go back to login
        }
    }
}
