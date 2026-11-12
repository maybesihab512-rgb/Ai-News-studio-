package com.ainewsstudio.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Error whose message is already user-readable Bengali. */
class PhotoException(message: String) : Exception(message)

/** Minimal Gemini REST client (no extra dependencies). Blocking: call from a background thread. */
object GeminiClient {
    const val DEFAULT_MODEL = "gemini-3.1-flash-image"
    private const val BASE = "https://generativelanguage.googleapis.com/v1beta/models/"

    private class Resp(val code: Int, val body: String)

    fun prompt(caption: String): String =
        "Create a realistic editorial news photograph that illustrates this Bengali news headline: \"" + caption + "\". " +
            "Portrait orientation, natural lighting, documentary photojournalism style. " +
            "Do not include any text, letters, captions, logos or watermarks in the image. " +
            "Do not depict real or identifiable public figures; show generic people only if needed."

    fun generate(apiKey: String, model: String, caption: String): Bitmap {
        val p = prompt(caption)
        // Some models reject imageConfig or image-only output, so fall back on HTTP 400.
        val attempts = listOf(
            Pair(listOf("IMAGE"), true),
            Pair(listOf("TEXT", "IMAGE"), false),
        )
        var last: Resp? = null
        for ((modalities, aspect) in attempts) {
            val resp = post(apiKey, model, body(p, modalities, aspect))
            last = resp
            if (resp.code == 400) continue
            if (resp.code !in 200..299) throw failure(resp)
            return parse(resp.body)
        }
        throw failure(last!!)
    }

    private fun body(prompt: String, modalities: List<String>, aspect: Boolean): String {
        val cfg = JSONObject().put("responseModalities", JSONArray(modalities))
        if (aspect) cfg.put("imageConfig", JSONObject().put("aspectRatio", "4:5"))
        val content = JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
        return JSONObject()
            .put("contents", JSONArray().put(content))
            .put("generationConfig", cfg)
            .toString()
    }

    private fun post(apiKey: String, model: String, json: String): Resp {
        val conn = URL(BASE + model + ":generateContent").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 90000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-goog-api-key", apiKey)
            conn.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            return Resp(code, text)
        } finally {
            conn.disconnect()
        }
    }

    private fun failure(r: Resp): PhotoException {
        val detail = try {
            JSONObject(r.body).optJSONObject("error")?.optString("message") ?: ""
        } catch (e: Exception) {
            ""
        }
        val base = when (r.code) {
            400 -> "অনুরোধ গ্রহণ হয়নি। API key ও মডেলের নাম ঠিক আছে কিনা দেখুন।"
            401, 403 -> "API key ঠিক নয় বা এই কাজের অনুমতি নেই।"
            404 -> "মডেলের নাম ঠিক নয়। Settings থেকে সঠিক নাম দিন।"
            429 -> "ব্যবহারের সীমা শেষ। কিছুক্ষণ পরে আবার চেষ্টা করুন।"
            else -> "সার্ভারে সমস্যা হয়েছে (কোড " + r.code + ")।"
        }
        return PhotoException(if (detail.isBlank()) base else base + " (" + detail.take(120) + ")")
    }

    private fun parse(text: String): Bitmap {
        val root = JSONObject(text)
        val parts = root.optJSONArray("candidates")
            ?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val inline = part.optJSONObject("inlineData") ?: part.optJSONObject("inline_data") ?: continue
                val data = inline.optString("data")
                if (data.isNotEmpty()) return decode(Base64.decode(data, Base64.DEFAULT))
            }
        }
        throw PhotoException("ক্যাপশন থেকে ছবি তৈরি হয়নি। ক্যাপশন বদলে আবার চেষ্টা করুন।")
    }

    /** Down-samples and uses RGB_565 to stay light on low-memory phones. */
    private fun decode(bytes: ByteArray): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 1400) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            ?: throw PhotoException("ছবি পড়া যায়নি।")
    }
}
