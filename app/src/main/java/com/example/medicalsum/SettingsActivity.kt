package com.example.medicalsum

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_settings

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(android.content.Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_library -> {
                    startActivity(android.content.Intent(this, SummaryActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_settings -> true
                else -> false
            }
        }

        // Placeholder
        findViewById<TextView>(R.id.tv_about)?.setOnClickListener {
            Toast.makeText(this, "About app - Version 1.0", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.tv_privacy)?.setOnClickListener {
            Toast.makeText(this, "Privacy Policy coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.tv_feedback)?.setOnClickListener {
            Toast.makeText(this, "Send feedback via email", Toast.LENGTH_SHORT).show()
        }
    }
}