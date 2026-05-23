package com.example.transporttrackingsystem.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val progress = sharedPref.getInt("FontSizeProgress", 50)
        // Progress 0 is 50% (0.5f), 50 is 100% (1.0f), 100 is 150% (1.5f)
        val fontScale = (progress + 50) / 100f
        
        val configuration = newBase.resources.configuration
        configuration.fontScale = fontScale
        val newContext = newBase.createConfigurationContext(configuration)
        
        super.attachBaseContext(newContext)
    }
}
