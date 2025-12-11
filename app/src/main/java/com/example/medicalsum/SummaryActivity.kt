package com.example.medicalsum


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medicalsum.data.AppDatabase
import com.example.medicalsum.repository.SummaryRepository
import kotlinx.coroutines.launch

class SummaryActivity : ComponentActivity() {
    private lateinit var repository: SummaryRepository
    private val adapter = SummaryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary)

        val homeIcon = findViewById<ImageView>(R.id.home_icon)
        homeIcon.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        val db = AppDatabase.getDatabase(this)
        repository = SummaryRepository(db.summaryDao())

        val recyclerView = findViewById<RecyclerView>(R.id.rvSummaries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val deleteBtn = findViewById<ImageView>(R.id.delete_all_button)
        loadSummaries()

        deleteBtn.setOnClickListener {
            lifecycleScope.launch {
                repository.deleteAll()
                loadSummaries()
            }
        }
    }

    private fun loadSummaries() {
        lifecycleScope.launch {
            val list = repository.getAll()
            adapter.submitList(list)
        }
    }
}