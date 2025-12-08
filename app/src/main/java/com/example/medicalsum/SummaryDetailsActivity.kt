package com.example.medicalsum

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class SummaryDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_details)

        val summaryTextView = findViewById<TextView>(R.id.input_text)
        val summary = intent.getStringExtra("summary_text") ?: "No summary received"

        summaryTextView.text = summary
    }
}