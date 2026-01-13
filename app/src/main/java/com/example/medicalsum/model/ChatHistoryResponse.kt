package com.example.medicalsum.model

data class ChatHistoryResponse(
    val session_id: String,
    val messages: List<Map<String, String>>
)
