package com.example.medicalsum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: SummaryEntity)

    @Query("SELECT * FROM summaries ORDER BY createdAt DESC")
    suspend fun getAll(): List<SummaryEntity>

    @Query("SELECT * FROM summaries WHERE id = :id")
    suspend fun getById(id: Int): SummaryEntity

    @Query("UPDATE summaries SET title = :newTitle WHERE id = :summaryId")
    suspend fun updateTitle(summaryId: Int, newTitle: String)

    @Query("DELETE FROM summaries")
    suspend fun deleteAll()

    @Query("UPDATE summaries SET summaryText = :newSummary WHERE id = :id")
    suspend fun updateSummaryText(id: Int, newSummary: String)
}