package com.example.transporttrackingsystem

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.ActionCodeSettings

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val etResetEmail = findViewById<TextInputEditText>(R.id.etResetEmail)
        val btnSendReset = findViewById<Button>(R.id.btnSendReset)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        btnSendReset.setOnClickListener {
            val email = etResetEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                // ⚙️ Configure settings to redirect back to the app
                val actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setUrl("https://transportation-tracker-app.firebaseapp.com/__/auth/action")
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName(
                        "com.example.transporttrackingsystem",
                        true, // Install if not available
                        "1"   // Min version
                    )
                    .build()

                auth.sendPasswordResetEmail(email, actionCodeSettings)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Reset link sent! Check your email to set a new password in the app.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
