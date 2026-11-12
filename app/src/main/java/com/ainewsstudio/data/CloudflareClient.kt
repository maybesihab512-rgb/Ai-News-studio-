package com.ainewsstudio.data

import android.graphics.Bitmap
import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Cloudflare Workers AI (FLUX.1 schnell). Free daily allowance; needs a free account. Blocking. */
object CloudflareClient {
    fun generate(account: String, token: String, geminiKey: String, caption: String, englishIn: String? = null): Bitmap {
        val english = englishIn ?: Translator.toEnglish(geminiKey, caption, account, token)
        val prompt = ("Realistic editorial news photograph of: " + english +
            ". Natural lighting, documentary photojournalism style, no text, no letters, no logos.").take(2000)
        val body = JSONObject().put("prompt", prompt).put("steps", 6).toString()

        val url = "https://api.cloudflare.com/client/v4/accounts/" + account.trim() +
            "/ai/run/@cf/black-forest-labs/flux-1-schnell"
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 120000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer " + token.trim())
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            if (code !in 200..299) throw failure(code, text)
            val b64 = JSONObject(text).optJSONObject("result")?.optString("image") ?: ""
            if (b64.isEmpty()) throw PhotoException("ছবি পাওয়া যায়নি। আবার চেষ্টা করুন।")
            return ImageUtil.decodeBytes(Base64.decode(b64, Base64.DEFAULT))
                ?: throw PhotoException("ছবি পড়া যায়নি। আবার চেষ্টা করুন।")
        } finally {
            conn.disconnect()
        }
    }

    private fun failure(code: Int, text: String): PhotoException {
        val detail = try {
            JSONObject(text).optJSONArray("errors")?.optJSONObject(0)?.optString("message") ?: ""
        } catch (e: Exception) {
            ""
        }
        val base = when (code) {
            400 -> "অনুরোধ গ্রহণ হয়নি। ক্যাপশন বদলে আবার চেষ্টা করুন।"
            401, 403 -> "Cloudflare টোকেন বা Account ID ঠিক নয়। টোকেনে Workers AI অনুমতি লাগে।"
            404 -> "Account ID ঠিক নয়। Settings-এ মিলিয়ে দেখুন।"
            429 -> "Cloudflare-এর আজকের ফ্রি সীমা শেষ। কিছুক্ষণ পরে বা কাল আবার চেষ্টা করুন।"
            else -> "সার্ভারে সমস্যা হয়েছে (কোড " + code + ")।"
        }
        return PhotoException(if (detail.isBlank()) base else base + " (" + detail.take(120) + ")")
    }
}
