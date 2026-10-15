package com.bugsby.companion.brain

import android.content.Context
import com.bugsby.companion.actions.DeviceActions
import com.bugsby.companion.character.CompanionMode
import com.bugsby.companion.data.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class BrainRouter(private val context: Context) {

    private val prefs = AppPreferences(context)
    private val deviceActions = DeviceActions(context)
    private val httpClient = OkHttpClient()

    data class Decision(
        val spokenReply: String,
        val modeChange: CompanionMode? = null
    )

    suspend fun processInput(rawInput: String): Decision = withContext(Dispatchers.IO) {
        val input = rawInput.trim().lowercase()

        // 1. Super Saiyan Evolve & Normal Commands
        if (input.contains("evolve") || input.contains("super saiyan") || input.contains("power up") || input.contains("3d")) {
            prefs.isSuperSaiyan = true
            return@withContext Decision(
                spokenReply = "Super Saiyan transformation activated! Energy surging!",
                modeChange = CompanionMode.SUPER_SAIYAN_3D
            )
        }

        if (input.contains("normal") || input.contains("base form") || input.contains("2d") || input.contains("relax")) {
            prefs.isSuperSaiyan = false
            return@withContext Decision(
                spokenReply = "Stealth mode restored. Back in the shadows.",
                modeChange = CompanionMode.NORMAL_2D
            )
        }

        // 2. Device Intent Actions
        if (input.startsWith("open ")) {
            val appName = input.removePrefix("open ").trim()
            val opened = deviceActions.openAppByName(appName)
            return@withContext if (opened) {
                Decision("Opening $appName.")
            } else {
                Decision("I couldn't find an app called $appName on your device.")
            }
        }

        if (input.startsWith("search ") || input.startsWith("google ")) {
            val query = input.replaceFirst("^(search|google)\s+".toRegex(), "")
            deviceActions.searchWeb(query)
            return@withContext Decision("Searching for $query.")
        }

        if (input.startsWith("play ")) {
            val song = input.removePrefix("play ").trim()
            deviceActions.playMusic(song)
            return@withContext Decision("Playing $song.")
        }

        if (input.contains("flashlight on") || input.contains("turn on torch")) {
            deviceActions.toggleFlashlight(true)
            return@withContext Decision("Flashlight is on.")
        }

        if (input.contains("flashlight off") || input.contains("turn off torch")) {
            deviceActions.toggleFlashlight(false)
            return@withContext Decision("Flashlight is off.")
        }

        if (input.contains("alarm")) {
            deviceActions.openAlarms()
            return@withContext Decision("Opening alarms.")
        }

        // 3. Cloud LLM Fallback (Groq or Gemini)
        if (prefs.geminiApiKey.isNotBlank()) {
            val cloudReply = queryGemini(prefs.geminiApiKey, rawInput)
            if (cloudReply != null) {
                return@withContext Decision(cloudReply)
            }
        }

        // 4. Default Ninja Persona
        val ninjaReplies = listOf(
            "Ninja senses sharp. What is your command?",
            "I am watching over your screen. Say 'evolve' to power up!",
            "Silent as the night, swift as the wind. How can I help you?",
            "Ready for action. Ask me to open any app, search, or play music."
        )
        return@withContext Decision(ninjaReplies.random())
    }

    private fun queryGemini(apiKey: String, prompt: String): String? {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val jsonBody = JSONObject().apply {
                val contents = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "You are Bugsby, a cute fluffy 2D black ninja AI companion with a katana on your back living on the user's Android phone. Be concise (1-2 sentences), warm, and slightly ninja-themed. User says: $prompt")
                            })
                        })
                    })
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val root = JSONObject(body)
                val candidates = root.getJSONArray("candidates")
                val first = candidates.getJSONObject(0)
                val parts = first.getJSONObject("content").getJSONArray("parts")
                parts.getJSONObject(0).getString("text").trim()
            }
        } catch (_: Exception) {
            null
        }
    }
}
