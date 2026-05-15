package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo     = findViewById<ImageView>(R.id.splashLogo)
        val title    = findViewById<TextView>(R.id.splashTitle)
        val subtitle = findViewById<TextView>(R.id.splashSubtitle)
        val loading  = findViewById<TextView>(R.id.splashLoading)

        // 🚌 Animate logo: scale up + fade in
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setStartDelay(100)
            .start()

        // 📝 Fade in title below logo
        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(600)
            .start()

        // 📝 Fade in subtitle
        subtitle.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(900)
            .start()

        // ●●● Fade in loading dots
        loading.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(1100)
            .withEndAction {
                // ⏳ After 2.5s total → navigate to Login or Main
                loading.postDelayed({
                    // Always go to Login (Splash → Login → Dashboard flow)
                    val next = Intent(this, LoginActivity::class.java)
                    startActivity(next)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }, 1200)
            }
            .start()
    }
}
