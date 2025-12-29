package com.example.medicalsum

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.medicalsum.data.AppDatabase
import com.example.medicalsum.repository.SummaryRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class SummaryDetailsActivity : ComponentActivity() {
    private lateinit var tvSummary: TextView
    private lateinit var tvTitle: TextView
    private lateinit var etTitle: EditText
    private lateinit var btnEditTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var radioGroup: RadioGroup

    private var currentTitle: String = "Title"
    private var summaryId: Int = -1
    private var originalText: String = ""

    private lateinit var repository: SummaryRepository
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_details)
        repository = SummaryRepository(AppDatabase.getDatabase(this).summaryDao())

        initViews()
        loadDataFromIntent()
        setupListeners()
        setupBottomNavigation()
    }

    private fun initViews() {
        tvSummary = findViewById(R.id.summarized_text)
        tvTitle = findViewById(R.id.summary_title)
        etTitle = findViewById(R.id.summary_title_edit)
        btnEditTitle = findViewById(R.id.edit_title_button)
        btnBack = findViewById(R.id.btn_turn_back)
        radioGroup = findViewById(R.id.radioGroupOptions)
        summaryId = intent.getIntExtra("summary_id", -1)
    }

    private fun loadDataFromIntent() {
        val summaryText = intent.getStringExtra("summary_text") ?: "No summary"
        currentTitle = intent.getStringExtra("summary_title") ?: "Title"

        tvSummary.text = summaryText
        tvTitle.text = currentTitle
        etTitle.setText(currentTitle)
        // Lấy originalText từ DB nếu có ID (từ danh sách)
        if (summaryId != -1) {
            lifecycleScope.launch {
                val entity = repository.getById(summaryId)
                entity?.let {
                    originalText = it.originalText
                }
            }
        }
        // Nếu từ MainActivity mới tạo (không có ID), originalText sẽ được lưu tạm khi resummarize (xem bên dưới)
    }


    private fun setupListeners() {

        btnBack.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
            finish()
        }

        btnEditTitle.setOnClickListener {
            if (btnEditTitle.text == "Edit") {
                enableEditing()
            } else {
                saveNewTitle()
            }
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rb_extract -> "extract"
                R.id.rb_short -> "short"
                R.id.rb_tldr -> "tldr"
                else -> "extract"
            }
            // Chỉ gọi API nếu đã có văn bản gốc
            if (originalText.isNotEmpty()) {
                resummarizeText(originalText, mode)
            } else {
                Toast.makeText(this, "Đang tải dữ liệu gốc...", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btn_quiz).setOnClickListener {
            Toast.makeText(this, "Tính năng Quiz đang phát triển!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_chat_ai).setOnClickListener {
            Toast.makeText(this, "Tính năng Chat với AI đang phát triển!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun enableEditing() {
        tvTitle.visibility = View.GONE
        etTitle.visibility = View.VISIBLE
        etTitle.requestFocus()
        btnEditTitle.text = "Save"
    }

    private fun saveNewTitle() {
        val newTitle = etTitle.text.toString().trim()

        if (newTitle.isBlank()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            repository.updateTitle(summaryId, newTitle)
            currentTitle = newTitle
            Toast.makeText(this@SummaryDetailsActivity, "Updated!", Toast.LENGTH_SHORT).show()
        }

        // Update UI
        currentTitle = newTitle
        tvTitle.text = newTitle
        tvTitle.visibility = View.VISIBLE
        etTitle.visibility = View.GONE
        btnEditTitle.text = "Edit"
        if (summaryId != -1) {
            lifecycleScope.launch {
                repository.updateTitle(summaryId, newTitle)
                Toast.makeText(
                    this@SummaryDetailsActivity,
                    "Đã cập nhật tiêu đề!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resummarizeText(text: String, mode: String) {
        val url = "http://192.168.0.182:8000/summarize"
        val json = JSONObject().apply {
            put("text", text)
            put("mode", mode)  // Backend cần hỗ trợ param này nhé!
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@SummaryDetailsActivity,
                        "Lỗi kết nối server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val jsonObj = JSONObject(body)
                    val newSummary = jsonObj.getString("summary")
                    runOnUiThread {
                        tvSummary.text = newSummary
                        // Cập nhật DB nếu có ID
                        if (summaryId != -1) {
                            lifecycleScope.launch {
                                repository.updateSummary(summaryId, newSummary)
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@SummaryDetailsActivity,
                            "Lỗi từ server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_library
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }

                R.id.navigation_library -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                    finish()
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
}