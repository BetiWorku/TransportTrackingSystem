package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var oobCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()

        // 🔗 Check the deep link mode BEFORE setting the content view
        val appLinkData: Uri? = intent?.data
        val mode = appLinkData?.getQueryParameter("mode")
        oobCode = appLinkData?.getQueryParameter("oobCode")

        if (mode == "verifyEmail") {
            setContentView(R.layout.activity_email_verification)

            val pbLoader = findViewById<ProgressBar>(R.id.pbLoader)
            val ivSuccess = findViewById<ImageView>(R.id.ivSuccess)
            val ivError = findViewById<ImageView>(R.id.ivError)
            val tvStatus = findViewById<TextView>(R.id.tvVerificationStatus)
            val tvDetail = findViewById<TextView>(R.id.tvVerificationDetail)
            val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

            btnBackToLogin.setOnClickListener {
                navigateToLogin()
            }

            if (oobCode != null) {
                // Execute email verification
                auth.applyActionCode(oobCode!!)
                    .addOnCompleteListener { task ->
                        pbLoader.visibility = View.GONE
                        if (task.isSuccessful) {
                            ivSuccess.visibility = View.VISIBLE
                            tvStatus.text = "Email Verified!"
                            tvDetail.text = "Your email address has been successfully verified. Redirecting you to login..."
                            
                            // Auto-redirect to Login screen after 2.5 seconds
                            Handler(Looper.getMainLooper()).postDelayed({
                                navigateToLogin()
                            }, 2500)
                        } else {
                            ivError.visibility = View.VISIBLE
                            tvStatus.text = "Verification Failed"
                            tvDetail.text = task.exception?.message ?: "The verification link is invalid or has expired."
                            btnBackToLogin.visibility = View.VISIBLE
                        }
                    }
            } else {
                pbLoader.visibility = View.GONE
                ivError.visibility = View.VISIBLE
                tvStatus.text = "Invalid Link"
                tvDetail.text = "This verification link is malformed or invalid."
                btnBackToLogin.visibility = View.VISIBLE
            }
        } else {
            // Default reset password flow
            setContentView(R.layout.activity_reset_password)

            if (oobCode == null) {
                Toast.makeText(this, "Invalid or missing reset token.", Toast.LENGTH_SHORT).show()
                navigateToLogin()
                return
            }

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

                auth.confirmPasswordReset(oobCode!!, newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset successful! You can now login.", Toast.LENGTH_LONG).show()
                            navigateToLogin()
                        } else {
                            Toast.makeText(this, "Reset failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
