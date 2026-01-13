package com.example.medicalsum

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.medicalsum.model.ChatMessage
import com.example.medicalsum.model.ChatRequest
import com.example.medicalsum.model.SessionManager
import com.example.medicalsum.network.RetrofitClient
import kotlinx.coroutines.launch

class ChatViewModel(
    private val app: Application
) : AndroidViewModel(app) {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    var sessionId: String? = null
        private set

    init {
        sessionId = SessionManager.get(app)
        if (sessionId != null) {
            loadHistory()
        }
    }

    fun sendMessage(text: String) = viewModelScope.launch {
        val current = _messages.value.orEmpty().toMutableList()
        current.add(ChatMessage(text, true))
        _messages.value = current

        val res = RetrofitClient.api.chat(
            ChatRequest(text, sessionId)
        )

        if (sessionId == null) {
            sessionId = res.session_id
            SessionManager.save(app, sessionId!!)
        }

        current.add(ChatMessage(res.response, false))
        _messages.value = current
    }

    private fun loadHistory() = viewModelScope.launch {
        val res = RetrofitClient.api.getChatHistory(sessionId!!)
        val history = mutableListOf<ChatMessage>()

        res.messages.forEach {
            history.add(ChatMessage(it["user"]!!, true))
            history.add(ChatMessage(it["assistant"]!!, false))
        }

        _messages.value = history
    }
}


