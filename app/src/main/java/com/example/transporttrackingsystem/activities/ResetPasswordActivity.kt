package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var oobCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()

        // 🔗 Handle the Deep Link
        handleIntent(intent)

        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val code = oobCode
            if (code != null) {
                auth.confirmPasswordReset(code, newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset successful! You can now login.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Reset failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Invalid or expired reset link", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkData: Uri? = intent?.data
        if (appLinkData != null) {
            oobCode = appLinkData.getQueryParameter("oobCode")
            val mode = appLinkData.getQueryParameter("mode")

            when (mode) {
                "resetPassword" -> {
                    if (oobCode == null) {
                        Toast.makeText(this, "Invalid reset link", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                "verifyEmail" -> {
                    if (oobCode != null) {
                        verifyEmail(oobCode!!)
                    } else {
                        Toast.makeText(this, "Invalid verification link", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                else -> {
                    // If it's some other mode, just let it be or handle accordingly
                    // But for now, if it's not reset, it might be verification
                    Toast.makeText(this, "Processing link...", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun verifyEmail(code: String) {
        auth.applyActionCode(code)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email verified successfully! You can now login.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Verification failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
    }
}
