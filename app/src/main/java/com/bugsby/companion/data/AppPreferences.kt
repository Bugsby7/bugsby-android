package com.bugsby.companion.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bugsby_prefs", Context.MODE_PRIVATE)

    var geminiApiKey: String
        get() = prefs.getString("gemini_api_key", "") ?: ""
        set(value) = prefs.edit().putString("gemini_api_key", value).apply()

    var groqApiKey: String
        get() = prefs.getString("groq_api_key", "") ?: ""
        set(value) = prefs.edit().putString("groq_api_key", value).apply()

    var isSuperSaiyan: Boolean
        get() = prefs.getBoolean("is_super_saiyan", false)
        set(value) = prefs.edit().putBoolean("is_super_saiyan", value).apply()

    var characterSize: Int
        get() = prefs.getInt("character_size", 100) // dp
        set(value) = prefs.edit().putInt("character_size", value).apply()

    var tintColorHex: String
        get() = prefs.getString("tint_color", "#1A1A1D") ?: "#1A1A1D"
        set(value) = prefs.edit().putString("tint_color", value).apply()

    var idleTimeoutSeconds: Int
        get() = prefs.getInt("idle_timeout", 10)
        set(value) = prefs.edit().putInt("idle_timeout", value).apply()
}
