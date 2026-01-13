package com.example.medicalsum.model

data class ChatRequest(
    val message: String,
    val session_id: String?
)
