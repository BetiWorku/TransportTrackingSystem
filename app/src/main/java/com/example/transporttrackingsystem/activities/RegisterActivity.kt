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
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // 💾 Save User to Firestore
                                val userRole = if (email.lowercase() == "bwwmas@gmail.com") "Admin" else "Commuter"
                                val user = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "role" to userRole
                                )
                                db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener {
                                        // 📧 1. Send our custom HTML Welcome Email (Async)
                                        Log.d("REGISTER", "Attempting to send welcome email to $email")
                                        EmailHelper.sendWelcomeEmail(email, name)
                                        
                                        // ✅ Check if Admin to bypass verification and auto-login
                                        if (email.lowercase() == "bwwmas@gmail.com") {
                                            Log.d("REGISTER", "Admin detected: Bypassing verification.")
                                            Toast.makeText(this, "Admin Registered! Welcome Betelhem.", Toast.LENGTH_LONG).show()
                                            
                                            val intent = Intent(this, WelcomeActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            // ✅ 2. Send Firebase Verification Link for normal users
                                            Log.d("REGISTER", "Requesting Firebase verification email for $email")
                                            auth.currentUser?.sendEmailVerification()
                                                ?.addOnCompleteListener { verifyTask ->
                                                    if (verifyTask.isSuccessful) {
                                                        Log.d("REGISTER", "Firebase verification email sent successfully.")
                                                        
                                                        // 📢 Show a clear Alert instead of a Toast
                                                        AlertDialog.Builder(this@RegisterActivity)
                                                            .setTitle("Verification Required")
                                                            .setMessage("Verification email sent. Please check your inbox or spam folder.")
                                                            .setCancelable(false)
                                                            .setPositiveButton("Got It") { _, _ ->
                                                                // 🚪 Sign out and Move to Login only after they click OK
                                                                auth.signOut()
                                                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                            .show()
                                                    } else {
                                                        val error = verifyTask.exception?.message ?: "Unknown error"
                                                        Log.e("REGISTER", "Firebase verification failed: $error")
                                                        Toast.makeText(this@RegisterActivity, "Registration successful, but verification email failed: $error", Toast.LENGTH_LONG).show()
                                                        
                                                        // Move anyway but let them know it failed
                                                        auth.signOut()
                                                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                                        finish()
                                                    }
                                                }
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
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvLogin.setOnClickListener {
            finish() // Go back to login
        }
    }
}
