package com.example.medicalsum.network

import com.example.medicalsum.model.ChatHistoryResponse
import com.example.medicalsum.model.ChatRequest
import com.example.medicalsum.model.ChatResponse
import com.example.medicalsum.model.SessionListResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("chat")
    suspend fun chat(
        @Body request: ChatRequest
    ): ChatResponse

    @GET("chat/history/{sessionId}")
    suspend fun getChatHistory(
        @Path("sessionId") sessionId: String
    ): ChatHistoryResponse

    @GET("chat/sessions")
    suspend fun getSessions(): SessionListResponse

    @DELETE("chat/history/{sessionId}")
    suspend fun deleteHistory(
        @Path("sessionId") sessionId: String
    )
}