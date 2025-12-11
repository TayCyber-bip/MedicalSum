package com.example.medicalsum

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.medicalsum.data.AppDatabase
import com.example.medicalsum.repository.SummaryRepository
import kotlinx.coroutines.launch

class SummaryDetailsActivity : ComponentActivity() {
    private lateinit var tvSummary: TextView
    private lateinit var tvTitle: TextView
    private lateinit var etTitle: EditText
    private lateinit var btnEditTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnHome: ImageView

    private var currentTitle: String = ""
    private var summaryId: Int = -1

    private lateinit var repository: SummaryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_details)
        repository = SummaryRepository(
            AppDatabase.getDatabase(this).summaryDao()
        )
        initViews()
        loadDataFromIntent()
        setupListeners()
    }

    private fun initViews() {
        tvSummary = findViewById(R.id.summarized_text)
        tvTitle = findViewById(R.id.summary_title)
        etTitle = findViewById(R.id.summary_title_edit)
        btnEditTitle = findViewById(R.id.edit_title_button)
        btnBack = findViewById(R.id.btn_turn_back)
        btnHome = findViewById(R.id.home_icon)
        summaryId = intent.getIntExtra("summary_id", -1)
    }

    private fun loadDataFromIntent() {
        tvSummary.text = intent.getStringExtra("summary_text") ?: "No summary"
        currentTitle = intent.getStringExtra("summary_title") ?: ""
        summaryId = intent.getIntExtra("summary_id", -1)
        tvTitle.text = currentTitle
        etTitle.setText(currentTitle)
    }


    private fun setupListeners() {

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

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
    }

    private fun enableEditing() {
        tvTitle.visibility = View.GONE
        etTitle.visibility = View.VISIBLE

        etTitle.requestFocus()

        btnEditTitle.text = "Save"
    }

    private fun saveNewTitle() {
        val newTitle = etTitle.text.toString()

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
        tvTitle.text = newTitle
        tvTitle.visibility = View.VISIBLE
        etTitle.visibility = View.GONE

        btnEditTitle.text = "Edit"
    }
}