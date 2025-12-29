package com.example.medicalsum.repository

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

    suspend fun getById(id: Int): SummaryEntity? = dao.getById(id)

    suspend fun updateSummary(id: Int, newSummary: String) = dao.updateSummaryText(id, newSummary)
}