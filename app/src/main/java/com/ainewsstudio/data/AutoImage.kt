package com.ainewsstudio.data

import android.graphics.Bitmap

/**
 * Tries the available image services one after another until one returns a picture, so the
 * user never has to pick a service. Blocking: call from a background thread.
 * Order: Cloudflare (if credentials exist) -> Gemini (if a key exists) -> Pollinations -> AI Horde.
 */
object AutoImage {
    fun generate(
        geminiKey: String,
        model: String,
        cfAccount: String,
        cfToken: String,
        caption: String,
        onStep: (String) -> Unit,
    ): Bitmap {
        // Translate once and share the result with every service that needs English.
        val english by lazy { Translator.toEnglish(geminiKey, caption, cfAccount, cfToken) }

        val steps = mutableListOf<Pair<String, () -> Bitmap>>()
        if (cfAccount.isNotBlank() && cfToken.isNotBlank()) {
            steps.add("Cloudflare" to { CloudflareClient.generate(cfAccount, cfToken, geminiKey, caption, english) })
        }
        if (geminiKey.isNotBlank()) {
            steps.add("Gemini" to { GeminiClient.generate(geminiKey, model, caption) })
        }
        steps.add("Pollinations" to { PollinationsClient.generate(geminiKey, caption, english) })
        steps.add("AI Horde" to { HordeClient.generate(geminiKey, cfAccount, cfToken, caption, english) })

        val problems = mutableListOf<String>()
        for ((name, run) in steps) {
            onStep(name)
            try {
                return run()
            } catch (e: PhotoException) {
                problems.add(name + ": " + (e.message ?: ""))
            } catch (e: OutOfMemoryError) {
                throw PhotoException("ফোনের মেমোরি কম। অন্য অ্যাপ বন্ধ করে আবার চেষ্টা করুন।")
            } catch (e: Exception) {
                problems.add(name + ": সংযোগে সমস্যা")
            }
        }
        throw PhotoException(
            "কোনো ফ্রি সার্ভিসই এখন ছবি দিতে পারেনি। কিছুক্ষণ পরে আবার চেষ্টা করুন।\n" +
                problems.joinToString("\n") { "• " + it.take(90) },
        )
    }
}
