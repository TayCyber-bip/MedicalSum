package com.example.medicalsum

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class SummaryDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_details)

        val summaryTextView = findViewById<TextView>(R.id.summarized_text)
        val titleTextView = findViewById<TextView>(R.id.summary_title)
        val summary = intent.getStringExtra("summary_text") ?: "No summary received"
        val title = intent.getStringExtra("summary_title") ?: ""

        summaryTextView.text = summary
        titleTextView.text = title

        val homeIcon = findViewById<ImageView>(R.id.home_icon)
        val turnBackButton = findViewById<Button>(R.id.btn_turn_back)
        homeIcon.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        turnBackButton.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
            finish()
        }
    }
}