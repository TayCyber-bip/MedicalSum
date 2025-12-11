package com.example.medicalsum.repository

import android.content.Context
import com.example.medicalsum.data.AppDatabase
import com.example.medicalsum.data.SummaryDao
import com.example.medicalsum.data.SummaryEntity

class SummaryRepository(private val dao: SummaryDao) {

    suspend fun insert(entity: SummaryEntity) {
        dao.insert(entity)
    }

    suspend fun getAll() = dao.getAll()

    suspend fun updateTitle(id: Int, newTitle: String) = dao.updateTitle(id, newTitle)
    suspend fun deleteAll() {
        dao.deleteAll()
    }
}