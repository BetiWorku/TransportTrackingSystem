package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.activities.MainActivity

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : BaseActivity() {

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
        val etOldPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOldPassword)
        val etUpdatePassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUpdatePassword)
        val btnUpdateProfile = findViewById<Button>(R.id.btnUpdateProfile)
        val switchNotifications = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchNotifications)

        // Sync notification toggle with global setting
        switchNotifications.isChecked = MainActivity.isAlertsEnabled
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            MainActivity.isAlertsEnabled = isChecked
            val state = if (isChecked) "Enabled" else "Disabled"
            Toast.makeText(this, "Notifications $state", Toast.LENGTH_SHORT).show()
        }

        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // --- HEADER: Dark Mode Icon Button ---
        val btnDarkModeToggle = findViewById<ImageView>(R.id.btnDarkModeToggle)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)

        // Set correct icon on load
        fun updateDarkModeIcon(isDark: Boolean) {
            if (isDark) {
                btnDarkModeToggle.setImageResource(android.R.drawable.ic_menu_month) // moon-like icon
            } else {
                btnDarkModeToggle.setImageResource(android.R.drawable.ic_menu_day) // sun-like icon
            }
        }
        updateDarkModeIcon(isDarkMode)

        btnDarkModeToggle.setOnClickListener {
            val currentMode = sharedPref.getBoolean("DarkMode", false)
            val newMode = !currentMode
            sharedPref.edit().putBoolean("DarkMode", newMode).apply()
            updateDarkModeIcon(newMode)
            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "Dark Mode On", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "Light Mode On", Toast.LENGTH_SHORT).show()
            }
        }

        // --- HEADER: Language Icon opens AlertDialog picker ---
        val btnLanguageToggle = findViewById<ImageView>(R.id.btnLanguageToggle)
        val headerLanguages = arrayOf("English", "አማርኛ (Amharic)", "Afaan Oromoo", "ትግርኛ (Tigrinya)", "Somali")
        val headerLanguageCodes = arrayOf("en", "am", "om", "ti", "so")

        val savedHeaderLangCode = sharedPref.getString("Language", "en")
        var selectedLangIndex = headerLanguageCodes.indexOf(savedHeaderLangCode).takeIf { it >= 0 } ?: 0

        btnLanguageToggle.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setSingleChoiceItems(headerLanguages, selectedLangIndex) { dialog, which ->
                    val selectedCode = headerLanguageCodes[which]
                    val currentCode = sharedPref.getString("Language", "en")
                    if (selectedCode != currentCode) {
                        selectedLangIndex = which
                        sharedPref.edit().putString("Language", selectedCode).apply()
                        Toast.makeText(this@SettingsActivity, "Language: ${headerLanguages[which]}", Toast.LENGTH_SHORT).show()
                        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(selectedCode)
                        AppCompatDelegate.setApplicationLocales(localeList)
                    }
                    dialog.dismiss()
                }
                .show()
        }


        // Font Size Slider
        val seekBarFontSize = findViewById<android.widget.SeekBar>(R.id.seekBarFontSize)
        val tvFontSizeValue = findViewById<TextView>(R.id.tvFontSizeValue)
        
        // Progress 0-100 maps to 50%-150% scale
        val savedProgress = sharedPref.getInt("FontSizeProgress", 50)
        seekBarFontSize.progress = savedProgress
        tvFontSizeValue.text = "${savedProgress + 50}%"
        
        seekBarFontSize.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val percentage = progress + 50
                tvFontSizeValue.text = "$percentage%"
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                val progress = seekBar?.progress ?: 50
                val percentage = progress + 50
                val oldProgress = sharedPref.getInt("FontSizeProgress", 50)
                
                if (progress != oldProgress) {
                    sharedPref.edit().putInt("FontSizeProgress", progress).apply()
                    Toast.makeText(this@SettingsActivity, "Font size saved: $percentage%", Toast.LENGTH_SHORT).show()
                    recreate() // Reload the UI so the new font scale applies immediately
                }
            }
        })

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
                val oldPassword = etOldPassword.text.toString().trim()
                if (oldPassword.isEmpty()) {
                    Toast.makeText(this, "Please enter your current password to change it.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                if (newPassword.length >= 6) {
                    val credential = EmailAuthProvider.getCredential(currentUser?.email!!, oldPassword)
                    currentUser.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                        if (reAuthTask.isSuccessful) {
                            currentUser.updatePassword(newPassword).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                    etOldPassword.text?.clear()
                                    etUpdatePassword.text?.clear()
                                } else {
                                    Toast.makeText(this, "Password update failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Authentication failed. Incorrect current password.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "New password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}
