package com.ainewsstudio.data

import android.graphics.Bitmap
import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * AI Horde: a free, volunteer-run network. No account needed (anonymous key), but anonymous
 * requests wait in a queue, so images can take a few minutes. Blocking.
 */
object HordeClient {
    private const val BASE = "https://aihorde.net/api/v2"
    private const val ANON_KEY = "0000000000"
    private const val POLL_MS = 4000L
    private const val MAX_WAIT_MS = 5 * 60 * 1000L

    private class Resp(val code: Int, val body: String)

    fun generate(geminiKey: String, cfAccount: String, cfToken: String, caption: String, englishIn: String? = null): Bitmap {
        val english = englishIn ?: Translator.toEnglish(geminiKey, caption, cfAccount, cfToken)
        val prompt = "photograph, realistic editorial news photo of " + english +
            ", natural lighting, photojournalism ### text, letters, watermark, logo, cartoon, deformed"
        val body = JSONObject()
            .put("prompt", prompt)
            .put("params", JSONObject().put("width", 512).put("height", 512).put("steps", 25).put("n", 1))
            .put("nsfw", false)
            .put("censor_nsfw", true)
            .toString()

        val start = request("POST", "/generate/async", body)
        if (start.code !in 200..299) throw failure(start)
        val id = JSONObject(start.body).optString("id")
        if (id.isEmpty()) throw PhotoException("ফ্রি সার্ভিস অনুরোধ নেয়নি। আবার চেষ্টা করুন।")

        val begin = System.currentTimeMillis()
        while (true) {
            Thread.sleep(POLL_MS)
            val check = request("GET", "/generate/check/" + id, null)
            if (check.code !in 200..299) throw failure(check)
            val j = JSONObject(check.body)
            if (j.optBoolean("faulted")) throw PhotoException("ছবি তৈরিতে ত্রুটি হয়েছে। আবার চেষ্টা করুন।")
            if (j.optBoolean("done")) break
            if (System.currentTimeMillis() - begin > MAX_WAIT_MS) {
                try {
                    request("DELETE", "/generate/status/" + id, null)
                } catch (e: Exception) {
                    // ignore
                }
                throw PhotoException("লাইনে অনেক ভিড়, পাঁচ মিনিটেও ছবি আসেনি। পরে আবার চেষ্টা করুন।")
            }
        }

        val status = request("GET", "/generate/status/" + id, null)
        if (status.code !in 200..299) throw failure(status)
        val gen = JSONObject(status.body).optJSONArray("generations")?.optJSONObject(0)
            ?: throw PhotoException("ছবি পাওয়া যায়নি। আবার চেষ্টা করুন।")
        if (gen.optBoolean("censored")) {
            throw PhotoException("ছবিটি ফিল্টারে আটকে গেছে। ক্যাপশন বদলে আবার চেষ্টা করুন।")
        }
        val img = gen.optString("img")
        if (img.isEmpty()) throw PhotoException("ছবি পাওয়া যায়নি। আবার চেষ্টা করুন।")
        val bytes = if (img.startsWith("http")) download(img) else Base64.decode(img, Base64.DEFAULT)
        return ImageUtil.decodeBytes(bytes) ?: throw PhotoException("ছবি পড়া যায়নি। আবার চেষ্টা করুন।")
    }

    private fun request(method: String, path: String, body: String?): Resp {
        val conn = URL(BASE + path).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 20000
            conn.readTimeout = 60000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Client-Agent", "AINewsStudio:0.7:unknown")
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            return Resp(code, text)
        } finally {
            conn.disconnect()
        }
    }

    private fun download(url: String): ByteArray {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 20000
            conn.readTimeout = 60000
            if (conn.responseCode !in 200..299) throw PhotoException("ছবি নামানো যায়নি। আবার চেষ্টা করুন।")
            return conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
    }

    private fun failure(r: Resp): PhotoException {
        val detail = try {
            JSONObject(r.body).optString("message")
        } catch (e: Exception) {
            ""
        }
        val base = when (r.code) {
            401, 403 -> "ফ্রি সার্ভিস এখন অনুমতি দিচ্ছে না।"
            429 -> "ফ্রি সার্ভিসে অনেক ভিড়। কিছুক্ষণ পরে আবার চেষ্টা করুন।"
            503 -> "ফ্রি সার্ভিস এখন ব্যস্ত বা বন্ধ। পরে আবার চেষ্টা করুন।"
            else -> "ফ্রি সার্ভিসে সমস্যা হয়েছে (কোড " + r.code + ")।"
        }
        return PhotoException(if (detail.isBlank()) base else base + " (" + detail.take(120) + ")")
    }
}
