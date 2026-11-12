package com.ainewsstudio.data

import android.graphics.Bitmap
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Free image service that needs no API key (anonymous use is rate-limited). Blocking. */
object PollinationsClient {
    private const val MAX_RETRIES = 2
    private const val RETRY_WAIT_MS = 16000L

    fun generate(geminiKey: String, caption: String, englishIn: String? = null): Bitmap {
        val english = englishIn ?: Translator.toEnglish(geminiKey, caption)
        val prompt = "Realistic editorial news photograph of: " + english +
            ". Natural lighting, documentary photojournalism style, no text, no letters, no logos."
        val seed = (System.currentTimeMillis() % 100000).toInt()
        val url = "https://image.pollinations.ai/prompt/" +
            URLEncoder.encode(prompt, "UTF-8").replace("+", "%20") +
            "?width=1024&height=1280&nologo=true&model=flux&seed=" + seed

        var retries = 0
        while (true) {
            val conn = URL(url).openConnection() as HttpURLConnection
            try {
                conn.connectTimeout = 20000
                conn.readTimeout = 120000
                conn.setRequestProperty("User-Agent", "AINewsStudio/0.6")
                val code = conn.responseCode
                if (code == 429 && retries < MAX_RETRIES) {
                    // The free tier allows about one request per 15 seconds: wait and try again.
                    retries++
                    Thread.sleep(RETRY_WAIT_MS)
                    continue
                }
                when {
                    code == 429 ->
                        throw PhotoException("ফ্রি সার্ভিসে এখন অনেক অনুরোধ। এক মিনিট পরে আবার চেষ্টা করুন।")
                    code == 401 || code == 403 ->
                        throw PhotoException("ফ্রি ছবির সার্ভিস এখন অনুমতি দিচ্ছে না। Settings থেকে Gemini বাছাই করুন।")
                    code !in 200..299 ->
                        throw PhotoException("ছবির সার্ভিস সাড়া দিচ্ছে না (কোড " + code + ")। কিছুক্ষণ পরে আবার চেষ্টা করুন।")
                }
                val bytes = conn.inputStream.use { it.readBytes() }
                return ImageUtil.decodeBytes(bytes) ?: throw PhotoException("ছবি পড়া যায়নি। আবার চেষ্টা করুন।")
            } finally {
                conn.disconnect()
            }
        }
    }
}
