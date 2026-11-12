package com.ainewsstudio.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Image models understand English far better than Bengali, so the caption is turned into a short
 * English photo description first. Blocking: call from a background thread. Never throws; falls
 * back to the original caption.
 */
object Translator {
    private const val TEXT_MODEL = "gemini-flash-latest"

    fun toEnglish(geminiKey: String, caption: String, cfAccount: String = "", cfToken: String = ""): String {
        val ask = "Translate this Bengali news headline into one short English sentence that describes " +
            "a photograph illustrating the story. Reply with the sentence only, without quotes.\n\n" + caption
        if (geminiKey.isNotBlank()) {
            try {
                return clean(geminiText(geminiKey, ask))
            } catch (e: Exception) {
                // try the next option
            }
        }
        if (cfAccount.isNotBlank() && cfToken.isNotBlank()) {
            try {
                return clean(cloudflareText(cfAccount, cfToken, ask))
            } catch (e: Exception) {
                // try the next option
            }
        }
        try {
            return clean(pollinationsText(ask))
        } catch (e: Exception) {
            // use the original caption
        }
        return caption
    }

    private fun clean(s: String): String {
        val t = s.trim().trim('"').replace("\n", " ").trim()
        if (t.isBlank()) throw IllegalStateException("empty")
        return t.take(300)
    }

    private fun geminiText(key: String, ask: String): String {
        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", ask)))),
            )
            .toString()
        val conn = URL("https://generativelanguage.googleapis.com/v1beta/models/$TEXT_MODEL:generateContent")
            .openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-goog-api-key", key)
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) throw IOException("code " + conn.responseCode)
            val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            return JSONObject(text).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
        } finally {
            conn.disconnect()
        }
    }

    private fun cloudflareText(account: String, token: String, ask: String): String {
        val body = JSONObject()
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", ask)))
            .put("max_tokens", 120)
            .toString()
        val url = "https://api.cloudflare.com/client/v4/accounts/" + account.trim() +
            "/ai/run/@cf/meta/llama-3.1-8b-instruct"
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer " + token.trim())
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) throw IOException("code " + conn.responseCode)
            val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            return JSONObject(text).getJSONObject("result").getString("response")
        } finally {
            conn.disconnect()
        }
    }

    private fun pollinationsText(ask: String): String {
        val url = "https://text.pollinations.ai/" + URLEncoder.encode(ask, "UTF-8").replace("+", "%20")
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            if (conn.responseCode !in 200..299) throw IOException("code " + conn.responseCode)
            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
