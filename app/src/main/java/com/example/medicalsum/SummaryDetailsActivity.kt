package com.example.medicalsum

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SummaryDetailsActivity : ComponentActivity() {
    private lateinit var tvSummary: TextView
    private lateinit var tvTitle: TextView
    private lateinit var etTitle: EditText
    private lateinit var btnEditTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: ImageView
    private lateinit var radioGroup: RadioGroup

    private var currentTitle: String = "Title"
    private var currentSummary: String = "No summary"
    private var summaryId: Int = -1
    private var originalText: String = ""

    private lateinit var repository: SummaryRepository
    private val client = OkHttpClient()

    private val saveFileLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                saveSummaryToFile(uri)
            }
        }
    }

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
        btnSave = findViewById(R.id.btn_save_summary)
        summaryId = intent.getIntExtra("summary_id", -1)
    }

    private fun loadDataFromIntent() {
        val summaryText = intent.getStringExtra("summary_text") ?: "No summary"
        currentTitle = intent.getStringExtra("summary_title") ?: "Title"
        currentSummary = summaryText

        tvSummary.text = summaryText
        tvTitle.text = currentTitle
        etTitle.setText(currentTitle)

        if (summaryId != -1) {
            lifecycleScope.launch {
                val entity = repository.getById(summaryId)
                entity?.let {
                    originalText = it.originalText
                }
            }
        }
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

        btnSave.setOnClickListener {
            saveSummaryAsTextFile()
        }


        findViewById<Button>(R.id.btn_quiz).setOnClickListener {
            Toast.makeText(this, "Quiz feature in development!", Toast.LENGTH_SHORT).show()
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

        // Update UI
        currentTitle = newTitle
        tvTitle.text = newTitle
        tvTitle.visibility = View.VISIBLE
        etTitle.visibility = View.GONE
        btnEditTitle.text = "Edit"
        if (summaryId != -1) {
            lifecycleScope.launch {
                repository.updateTitle(summaryId, newTitle)
                Toast.makeText(this@SummaryDetailsActivity, "Title updated!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun saveSummaryAsTextFile() {
        val fileName = if (currentTitle.isNotEmpty() && currentTitle != "Title") {
            "$currentTitle.txt"
        } else {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            "Summary_$timeStamp.txt"
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        saveFileLauncher.launch(intent)
    }

    private fun saveSummaryToFile(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(currentSummary.toByteArray())
                outputStream.flush()
            }
            Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Save failed!", Toast.LENGTH_SHORT).show()
        }
    }



    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_library
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.chat_with_ai -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }

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
}