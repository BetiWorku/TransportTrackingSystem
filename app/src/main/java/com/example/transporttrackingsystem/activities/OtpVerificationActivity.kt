package com.example.transporttrackingsystem.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.utils.EmailHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.util.Random

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etOtpCode: TextInputEditText
    private lateinit var btnVerifyOtp: Button
    private lateinit var tvResendOtp: TextView
    private lateinit var tvEmailInfo: TextView
    private lateinit var tvErrorMsg: TextView
    private lateinit var btnBack: ImageButton

    private var email: String = ""
    private var userId: String = ""
    private var userName: String = ""
    
    private var resendTimer: CountDownTimer? = null
    private val resendCooldownMs: Long = 60000 // 60 seconds cooldown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get extras from Intent
        email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        userId = intent.getStringExtra("EXTRA_USER_ID") ?: ""
        userName = intent.getStringExtra("EXTRA_NAME") ?: ""

        // Initialize UI Elements
        etOtpCode = findViewById(R.id.etOtpCode)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        tvEmailInfo = findViewById(R.id.tvEmailInfo)
        tvErrorMsg = findViewById(R.id.tvErrorMsg)
        btnBack = findViewById(R.id.btnBack)

        tvEmailInfo.text = "Code sent to: $email"

        // Setup Listeners
        btnBack.setOnClickListener {
            // Sign out and go back to Login
            auth.signOut()
            navigateToLogin()
        }

        btnVerifyOtp.setOnClickListener {
            verifyEnteredOtp()
        }

        tvResendOtp.setOnClickListener {
            resendOtpCode()
        }

        // Start Resend Cooldown on screen launch
        startResendCooldown()
    }

    private fun verifyEnteredOtp() {
        val enteredCode = etOtpCode.text.toString().trim()

        if (enteredCode.length != 6) {
            tvErrorMsg.text = "Please enter the full 6-digit code."
            tvErrorMsg.visibility = View.VISIBLE
            return
        }

        tvErrorMsg.visibility = View.GONE
        tvErrorMsg.setTextColor(android.graphics.Color.parseColor("#D32F2F")) // reset to red for errors
        btnVerifyOtp.isEnabled = false
        btnVerifyOtp.text = "Verifying..."

        // Query Firestore to get stored OTP code
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val storedOtp = document.getString("otp") ?: ""
                    
                    if (enteredCode == storedOtp) {
                        // OTP Matches! Mark user as verified in Firestore
                        val updates = hashMapOf<String, Any>(
                            "isVerified" to true,
                            "otp" to "" // Clear the OTP code from DB
                        )
                        
                        db.collection("users").document(userId).update(updates)
                            .addOnSuccessListener {
                                // Show Premium Success Alert and navigate to Login
                                btnVerifyOtp.text = "Verified!"
                                AlertDialog.Builder(this)
                                    .setTitle("Verification Successful")
                                    .setMessage("Your email address has been successfully verified! You can now log in to your account.")
                                    .setCancelable(false)
                                    .setPositiveButton("Proceed to Login") { _, _ ->
                                        auth.signOut()
                                        navigateToLogin()
                                    }
                                    .show()
                            }
                            .addOnFailureListener { e ->
                                btnVerifyOtp.isEnabled = true
                                btnVerifyOtp.text = "Verify & Register"
                                tvErrorMsg.text = "Database update failed: ${e.message}"
                                tvErrorMsg.visibility = View.VISIBLE
                            }
                    } else {
                        btnVerifyOtp.isEnabled = true
                        btnVerifyOtp.text = "Verify & Register"
                        tvErrorMsg.text = "Incorrect code. Please check and try again."
                        tvErrorMsg.visibility = View.VISIBLE
                    }
                } else {
                    btnVerifyOtp.isEnabled = true
                    btnVerifyOtp.text = "Verify & Register"
                    tvErrorMsg.text = "User record not found. Please register again."
                    tvErrorMsg.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                btnVerifyOtp.isEnabled = true
                btnVerifyOtp.text = "Verify & Register"
                tvErrorMsg.text = "Verification error: ${e.message}"
                tvErrorMsg.visibility = View.VISIBLE
            }
    }

    private fun resendOtpCode() {
        // Generate a new 6-digit OTP code
        val newOtpCode = String.format(Locale.US, "%06d", Random().nextInt(1000000))

        tvResendOtp.isEnabled = false

        // Update Firestore with the new code
        db.collection("users").document(userId).update("otp", newOtpCode)
            .addOnSuccessListener {
                // Send the new code via Email
                EmailHelper.sendOTPEmail(this, email, userName, newOtpCode)
                Toast.makeText(this, "New verification code sent to your email.", Toast.LENGTH_LONG).show()
                startResendCooldown()
            }
            .addOnFailureListener { e ->
                tvResendOtp.isEnabled = true
                Toast.makeText(this, "Failed to regenerate code: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startResendCooldown() {
        tvResendOtp.isEnabled = false
        tvResendOtp.setTextColor(android.graphics.Color.parseColor("#888888"))
        
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(resendCooldownMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                tvResendOtp.text = "Resend Code (${secondsRemaining}s)"
            }

            override fun onFinish() {
                tvResendOtp.isEnabled = true
                tvResendOtp.text = "Resend Code"
                tvResendOtp.setTextColor(android.graphics.Color.parseColor("#2196F3"))
            }
        }.start()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        super.onDestroy()
    }
}
