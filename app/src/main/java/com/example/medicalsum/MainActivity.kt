package com.example.medicalsum

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.medicalsum.data.AppDatabase
import com.example.medicalsum.data.SummaryEntity
import com.example.medicalsum.repository.SummaryRepository
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MainActivity : ComponentActivity() {

    private val client = OkHttpClient()
    private lateinit var repository: SummaryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val applyBtn = findViewById<Button>(R.id.btn_apply)
        val inputText = findViewById<EditText>(R.id.input_text)
        val libraryIcon = findViewById<ImageView>(R.id.library_icon)

        val db = AppDatabase.getDatabase(this)
        repository = SummaryRepository(db.summaryDao())

        applyBtn.setOnClickListener {

            val text = inputText.text.toString().trim()

            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter some text!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            summarizeText(text)
        }
        libraryIcon.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }
    }

    private fun summarizeText(text: String) {

        val url = "http://10.0.2.2:8000/summarize"
        val requestBody = text.toRequestBody(
            "text/plain".toMediaType()
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "text/plain")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Request failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string()

                if (!response.isSuccessful || body == null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Server error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return
                }

                val jsonObj = JSONObject(body)
                val summary = jsonObj.getString("summary")
                val defaultTitle = "Title"

                // SAVE DB
                lifecycleScope.launch {

                    repository.insert(
                        SummaryEntity(
                            title = defaultTitle,
                            originalText = text,
                            summaryText = summary
                        )
                    )

                    // OPEN SUMMARY DETAIL PAGE
                    val intent = Intent(
                        this@MainActivity,
                        SummaryDetailsActivity::class.java
                    )
                    intent.putExtra("summary_text", summary)
                    intent.putExtra("summary_title", defaultTitle)
                    startActivity(intent)
                }
            }
        })
    }
}

