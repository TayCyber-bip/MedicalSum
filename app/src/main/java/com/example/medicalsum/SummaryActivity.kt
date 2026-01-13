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
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class SummaryActivity : ComponentActivity() {
    private lateinit var repository: SummaryRepository
    private lateinit var btnDelete: ImageView
    private lateinit var recyclerView: RecyclerView
    private val adapter = SummaryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary)
        repository = SummaryRepository(AppDatabase.getDatabase(this).summaryDao())

        initViews()
        setupRecyclerView()
        loadSummaries()
        setupListeners()
        setupBottomNavigation()
    }

    private fun initViews() {
        btnDelete = findViewById<ImageView>(R.id.delete_all_button)
        recyclerView = findViewById<RecyclerView>(R.id.rvSummaries)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                repository.deleteAll()
                loadSummaries()
            }
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

                R.id.navigation_library -> true
                R.id.chat_with_ai -> {
                    startActivity(Intent(this, ChatActivity::class.java))
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

    private fun loadSummaries() {
        lifecycleScope.launch {
            val list = repository.getAll()
            adapter.submitList(list)
        }
    }
}