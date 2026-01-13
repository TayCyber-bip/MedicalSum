package com.example.medicalsum.model

import android.content.Context

object SessionManager {

    private const val PREF_NAME = "chat_session"
    private const val KEY_SESSION_ID = "session_id"

    fun save(context: Context, sessionId: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SESSION_ID, sessionId)
            .apply()
    }

    fun get(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SESSION_ID, null)

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}