package com.example.medicalsum

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.medicalsum.data.AppDatabase
import com.example.medicalsum.data.SummaryEntity
import com.example.medicalsum.repository.SummaryRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
    private lateinit var repository: SummaryRepository
    private lateinit var btnApply: MaterialButton
    private lateinit var progressApply: ProgressBar
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnApply = findViewById(R.id.btn_apply)
        progressApply = findViewById(R.id.progress_apply)
        val inputText = findViewById<EditText>(R.id.input_text)
        val db = AppDatabase.getDatabase(this)
        repository = SummaryRepository(db.summaryDao())

        btnApply.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter some text!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLoading) return@setOnClickListener

            startLoading()
            summarizeText(text)
        }

        setupBottomNavigation()
    }

    private fun startLoading() {
        isLoading = true
        btnApply.text = ""
        progressApply.visibility = View.VISIBLE
        btnApply.isEnabled = false
    }

    private fun stopLoading() {
        isLoading = false
        btnApply.text = "Apply"
        progressApply.visibility = View.GONE
        btnApply.isEnabled = true
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_library -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun summarizeText(text: String) {
        val url = "http://10.0.2.2:8000/summarize"
        val json = JSONObject()
        json.put("text", text)
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    stopLoading()
                    Toast.makeText(this@MainActivity, "Request failed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread { stopLoading() }

                if (!response.isSuccessful || body == null) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Server error", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val jsonObj = JSONObject(body)
                val summary = jsonObj.getString("summary")
                val defaultTitle = "Title"

                lifecycleScope.launch {
                    repository.insert(
                        SummaryEntity(
                            title = defaultTitle,
                            originalText = text,
                            summaryText = summary
                        )
                    )
                    val intent = Intent(this@MainActivity, SummaryDetailsActivity::class.java)
                    intent.putExtra("summary_text", summary)
                    intent.putExtra("summary_title", defaultTitle)
                    startActivity(intent)
                }
            }
        })
    }
}