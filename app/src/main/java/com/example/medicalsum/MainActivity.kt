package com.example.medicalsum

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.medicalsum.ui.theme.MedicalSumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeIcon = findViewById<ImageView>(R.id.home_icon)
        val libraryIcon = findViewById<ImageView>(R.id.library_icon)
        val applyBtn = findViewById<Button>(R.id.btn_apply)

        homeIcon.setBackgroundResource(R.drawable.bg_circle_selected)

        libraryIcon.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            startActivity(intent)
        }
        applyBtn.setOnClickListener {
            val intent = Intent(this, SummaryDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}

