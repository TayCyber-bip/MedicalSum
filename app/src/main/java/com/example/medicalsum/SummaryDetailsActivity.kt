package com.example.medicalsum

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class SummaryDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_details)

        val summaryTextView = findViewById<TextView>(R.id.input_text)
        val summary = intent.getStringExtra("summary_text") ?: "No summary received"
        val homeIcon = findViewById<ImageView>(R.id.home_icon)
        homeIcon.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        summaryTextView.text = summary
    }
}