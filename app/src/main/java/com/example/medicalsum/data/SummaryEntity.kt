package com.example.medicalsum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val originalText: String,
    val summaryText: String,
    val createdAt: Long = System.currentTimeMillis()
)