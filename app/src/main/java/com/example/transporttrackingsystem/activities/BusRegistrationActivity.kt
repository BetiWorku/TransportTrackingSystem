package com.example.transporttrackingsystem.activities

import com.example.transporttrackingsystem.R
import com.example.transporttrackingsystem.fragments.*

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class BusRegistrationActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_registration)

        drawerLayout = findViewById(R.id.drawerLayout)
        tvTitle = findViewById(R.id.adminPageTitle)
        val navView = findViewById<NavigationView>(R.id.adminNavigationView)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set Default Fragment
        replaceFragment(AdminDashboardFragment(), "Fleet Dashboard")

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dashboard -> replaceFragment(AdminDashboardFragment(), "Fleet Dashboard")
                R.id.nav_register_bus -> replaceFragment(RegisterBusFragment(), "Register Vehicle")
                R.id.nav_live_map -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.nav_admin_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_terminals -> replaceFragment(TerminalsFragment(), "Manage Terminals")
                R.id.nav_routes -> replaceFragment(RoutesFragment(), "Manage Routes")
                R.id.nav_news -> replaceFragment(NewsFragment(), "Manage News")
                R.id.nav_admin_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        tvTitle.text = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }
}
