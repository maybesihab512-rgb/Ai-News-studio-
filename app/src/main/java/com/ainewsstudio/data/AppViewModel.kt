package com.ainewsstudio.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import com.ainewsstudio.BuildConfig
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

data class HistoryItem(
    val id: Long,
    val caption: String,
    val templateId: Int,
    val fontId: Int = 0,
    val info: CardInfo = CardInfo(),
    val category: String = "",
    val speaker: String = "",
    val source: String = "",
)

/** State of an AI photo request. A missing entry means idle / finished. */
sealed interface Gen {
    object Loading : Gen
    data class Failed(val message: String) : Gen
}

/** Single source of truth. History is held in memory; branding and AI settings are saved on the device. */
class AppViewModel : ViewModel() {
    companion object {
        const val MAX_CAPTION = 200
        const val MAX_HISTORY = 50
        const val MAX_PHOTOS = 6
        const val PROVIDER_AUTO = "auto"
        const val PROVIDER_POLLINATIONS = "pollinations"
        const val PROVIDER_GEMINI = "gemini"
        const val PROVIDER_CLOUDFLARE = "cloudflare"
        const val PROVIDER_HORDE = "horde"
    }

    var caption by mutableStateOf("")

    // Optional extra texts for the next card. Empty by default: nothing is pre-filled or pre-selected.
    var category by mutableStateOf("")
    var speaker by mutableStateOf("")
    var source by mutableStateOf("")

    fun onCategoryChange(v: String) {
        category = v.take(30)
    }

    fun onSpeakerChange(v: String) {
        speaker = v.take(60)
    }

    fun onSourceChange(v: String) {
        source = v.take(60)
    }
    var captionError by mutableStateOf(false)
    var selectedTemplateId by mutableIntStateOf(1)
    var animations by mutableStateOf(true)

    // Branding (persisted): channel name, website, date
    var brandName by mutableStateOf(Prefs.getString("brand_name", ""))
        private set
    var showBrand by mutableStateOf(Prefs.getBool("show_brand", true))
        private set
    var website by mutableStateOf(Prefs.getString("website", ""))
        private set
    var showWebsite by mutableStateOf(Prefs.getBool("show_website", true))
        private set
    var showDate by mutableStateOf(Prefs.getBool("show_date", true))
        private set
    var customDate by mutableStateOf(Prefs.getString("custom_date", ""))
        private set

    // Creator profile (saved on the device; survives restarts and updates)
    var facebookName by mutableStateOf(Prefs.getString("fb_name", ""))
        private set
    var facebookLink by mutableStateOf(Prefs.getString("fb_link", ""))
        private set
    var contact by mutableStateOf(Prefs.getString("contact", ""))
        private set
    var showFacebook by mutableStateOf(Prefs.getBool("show_fb", true))
        private set
    var showContact by mutableStateOf(Prefs.getBool("show_contact", true))
        private set
    var showLogo by mutableStateOf(Prefs.getBool("show_logo", true))
        private set
    var profileDone by mutableStateOf(Prefs.getBool("profile_done", false))
        private set
    var logo by mutableStateOf<ImageBitmap?>(null)
        private set
    var logoError by mutableStateOf(false)
        private set

    init {
        loadLogo()
    }

    fun markProfileDone() {
        profileDone = true
        Prefs.putBool("profile_done", true)
    }

    fun updateFacebookName(v: String) {
        facebookName = v.take(60)
        Prefs.putString("fb_name", facebookName)
    }

    fun updateFacebookLink(v: String) {
        facebookLink = v.take(100)
        Prefs.putString("fb_link", facebookLink)
    }

    fun updateContact(v: String) {
        contact = v.take(60)
        Prefs.putString("contact", contact)
    }

    fun updateShowFacebook(v: Boolean) {
        showFacebook = v
        Prefs.putBool("show_fb", v)
    }

    fun updateShowContact(v: Boolean) {
        showContact = v
        Prefs.putBool("show_contact", v)
    }

    fun updateShowLogo(v: Boolean) {
        showLogo = v
        Prefs.putBool("show_logo", v)
    }

    private fun logoFile(): File? = Prefs.filesDir()?.let { File(it, "profile_logo.png") }

    private fun loadLogo() {
        val f = logoFile() ?: return
        if (!f.exists()) return
        try {
            BitmapFactory.decodeFile(f.path)?.let { logo = it.asImageBitmap() }
        } catch (e: Exception) {
            // ignore a damaged file
        } catch (e: OutOfMemoryError) {
            // ignore
        }
    }

    /** Logo picked from the gallery: scaled down and stored privately so it persists. */
    fun setLogoFrom(context: Context, uri: Uri) {
        val app = context.applicationContext
        logoError = false
        viewModelScope.launch {
            val bmp: Bitmap? = withContext(Dispatchers.IO) {
                try {
                    val decoded = ImageUtil.decodeUri(app, uri, 640, Bitmap.Config.ARGB_8888)
                        ?: return@withContext null
                    val scaled = ImageUtil.scaleDown(decoded, 512)
                    val f = logoFile()
                    if (f != null) {
                        f.outputStream().use { scaled.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    }
                    scaled
                } catch (e: Exception) {
                    null
                } catch (e: OutOfMemoryError) {
                    null
                }
            }
            if (bmp != null) logo = bmp.asImageBitmap() else logoError = true
        }
    }

    fun removeLogo() {
        logo = null
        logoError = false
        logoFile()?.delete()
    }

    fun updateBrandName(v: String) {
        brandName = v.take(40)
        Prefs.putString("brand_name", brandName)
    }

    fun updateShowBrand(v: Boolean) {
        showBrand = v
        Prefs.putBool("show_brand", v)
    }

    fun updateWebsite(v: String) {
        website = v.take(60)
        Prefs.putString("website", website)
    }

    fun updateShowWebsite(v: Boolean) {
        showWebsite = v
        Prefs.putBool("show_website", v)
    }

    fun updateShowDate(v: Boolean) {
        showDate = v
        Prefs.putBool("show_date", v)
    }

    fun updateCustomDate(v: String) {
        customDate = v.take(30)
        Prefs.putString("custom_date", customDate)
    }

    fun todayText(): String = DateText.format(System.currentTimeMillis())

    /** Footer info for a new card: custom date if typed, otherwise today's date. */
    fun currentInfo(): CardInfo {
        fun clean(url: String) = url.trim().removePrefix("https://").removePrefix("http://")
            .removePrefix("www.").trimEnd('/')
        val fbName = facebookName.trim()
        val fbLink = clean(facebookLink)
        return CardInfo(
            channel = if (showBrand) brandName.trim() else "",
            date = if (showDate) customDate.trim().ifBlank { todayText() } else "",
            website = if (showWebsite) website.trim().removePrefix("https://").removePrefix("http://").trimEnd('/') else "",
            facebook = if (showFacebook) {
                when {
                    fbName.isNotBlank() -> "ফেসবুক: " + fbName
                    fbLink.isNotBlank() -> fbLink
                    else -> ""
                }
            } else {
                ""
            },
            contact = if (showContact && contact.isNotBlank()) "যোগাযোগ: " + contact.trim() else "",
            logo = showLogo && logo != null,
        )
    }

    // AI photo settings (persisted)
    var aiEnabled by mutableStateOf(Prefs.getBool("ai_enabled", true))
        private set
    var aiTag by mutableStateOf(Prefs.getBool("ai_tag", true))
        private set
    var apiKey by mutableStateOf(Prefs.getString("api_key", ""))
        private set
    var aiModel by mutableStateOf(Prefs.getString("ai_model", GeminiClient.DEFAULT_MODEL))
        private set
    var aiProvider by mutableStateOf(Prefs.getString("ai_provider_v2", PROVIDER_AUTO))
        private set

    var cfAccount by mutableStateOf(Prefs.getString("cf_account", ""))
        private set
    var cfToken by mutableStateOf(Prefs.getString("cf_token", ""))
        private set

    fun updateCfAccount(v: String) {
        cfAccount = v.trim()
        Prefs.putString("cf_account", cfAccount)
    }

    fun updateCfToken(v: String) {
        cfToken = v.trim()
        Prefs.putString("cf_token", cfToken)
    }

    fun updateAiProvider(v: String) {
        aiProvider = v
        Prefs.putString("ai_provider_v2", v)
    }

    fun updateAiEnabled(v: Boolean) {
        aiEnabled = v
        Prefs.putBool("ai_enabled", v)
    }

    fun updateAiTag(v: Boolean) {
        aiTag = v
        Prefs.putBool("ai_tag", v)
    }

    fun updateApiKey(v: String) {
        apiKey = v.trim()
        Prefs.putString("api_key", apiKey)
    }

    fun updateAiModel(v: String) {
        aiModel = v.trim()
        Prefs.putString("ai_model", aiModel)
    }

    val history = mutableStateListOf<HistoryItem>()
    var previewItem by mutableStateOf<HistoryItem?>(null)

    /** Generated photos by history item id. Only the latest few are kept to save memory. */
    val photos = mutableStateMapOf<Long, ImageBitmap>()
    val gen = mutableStateMapOf<Long, Gen>()

    /** Name of the service currently being tried for an item (automatic mode). */
    val genStep = mutableStateMapOf<Long, String>()

    /** Ids whose photo was added by the user (no "AI photo" tag for these). */
    val userPhoto = mutableStateMapOf<Long, Boolean>()

    fun onCaptionChange(value: String) {
        caption = value.take(MAX_CAPTION)
        if (captionError && caption.isNotBlank()) captionError = false
    }

    /** Returns true when a preview was prepared and the UI may navigate to it. */
    fun generate(): Boolean {
        if (caption.isBlank()) {
            captionError = true
            return false
        }
        val item = HistoryItem(
            id = System.currentTimeMillis(),
            caption = caption.trim(),
            templateId = selectedTemplateId,
            info = currentInfo(),
            category = category.trim(),
            speaker = speaker.trim(),
            source = source.trim(),
        )
        history.add(0, item)
        while (history.size > MAX_HISTORY) history.removeAt(history.lastIndex)
        previewItem = item
        if (aiEnabled) requestPhoto(item)
        return true
    }

    fun requestPhoto(item: HistoryItem) {
        if (gen[item.id] is Gen.Loading) return
        val provider = aiProvider
        if (provider == PROVIDER_GEMINI && apiKey.ifBlank { BuildConfig.GEMINI_KEY }.isBlank()) {
            gen[item.id] = Gen.Failed("Settings থেকে Gemini API key দিন, অথবা ফ্রি Pollinations বাছাই করুন।")
            return
        }
        if (provider == PROVIDER_CLOUDFLARE &&
            (cfAccount.ifBlank { BuildConfig.CF_ACCOUNT }.isBlank() || cfToken.ifBlank { BuildConfig.CF_TOKEN }.isBlank())
        ) {
            gen[item.id] = Gen.Failed("Settings-এ Cloudflare Account ID ও API টোকেন দিন।")
            return
        }
        gen[item.id] = Gen.Loading
        val cfA = cfAccount.ifBlank { BuildConfig.CF_ACCOUNT }
        val cfT = cfToken.ifBlank { BuildConfig.CF_TOKEN }
        val key = apiKey.ifBlank { BuildConfig.GEMINI_KEY }
        val model = aiModel.ifBlank { GeminiClient.DEFAULT_MODEL }
        viewModelScope.launch {
            try {
                val bmp = withContext(Dispatchers.IO) {
                    when (provider) {
                        PROVIDER_AUTO -> AutoImage.generate(key, model, cfA, cfT, item.caption) { step ->
                            genStep[item.id] = step
                        }
                        PROVIDER_GEMINI -> GeminiClient.generate(key, model, item.caption)
                        PROVIDER_CLOUDFLARE -> CloudflareClient.generate(cfA, cfT, key, item.caption)
                        PROVIDER_HORDE -> HordeClient.generate(key, cfA, cfT, item.caption)
                        else -> PollinationsClient.generate(key, item.caption)
                    }
                }
                putPhoto(item.id, bmp, false)
            } catch (e: PhotoException) {
                gen[item.id] = Gen.Failed(e.message ?: "ছবি তৈরি হয়নি।")
            } catch (e: IOException) {
                gen[item.id] = Gen.Failed("ইন্টারনেট সংযোগে সমস্যা। সংযোগ দেখে আবার চেষ্টা করুন।")
            } catch (e: OutOfMemoryError) {
                gen[item.id] = Gen.Failed("ফোনের মেমোরি কম। অন্য অ্যাপ বন্ধ করে আবার চেষ্টা করুন।")
            } catch (e: Exception) {
                gen[item.id] = Gen.Failed("ছবি তৈরি হয়নি। আবার চেষ্টা করুন।")
            } finally {
                genStep.remove(item.id)
            }
        }
    }

    fun open(item: HistoryItem) {
        previewItem = item
    }

    private fun putPhoto(id: Long, bmp: Bitmap, user: Boolean) {
        photos[id] = bmp.asImageBitmap()
        if (user) userPhoto[id] = true else userPhoto.remove(id)
        gen.remove(id)
        while (photos.size > MAX_PHOTOS) {
            val oldest = photos.keys.filter { it != id }.minOrNull() ?: break
            photos.remove(oldest)
            userPhoto.remove(oldest)
        }
    }

    /** Photo picked from the gallery for the card being edited. */
    fun setUserPhotoFrom(context: Context, uri: Uri) {
        val id = previewItem?.id ?: return
        val app = context.applicationContext
        viewModelScope.launch {
            val bmp: Bitmap? = withContext(Dispatchers.IO) {
                try {
                    ImageUtil.decodeUri(app, uri)
                } catch (e: Exception) {
                    null
                } catch (e: OutOfMemoryError) {
                    null
                }
            }
            if (bmp != null) putPhoto(id, bmp, true) else gen[id] = Gen.Failed("ছবিটি পড়া যায়নি।")
        }
    }

    fun removePhoto() {
        val id = previewItem?.id ?: return
        photos.remove(id)
        userPhoto.remove(id)
        gen.remove(id)
    }

    // Editing the card currently in the preview. Changes are saved to its history entry too.
    private fun updatePreview(transform: (HistoryItem) -> HistoryItem) {
        val item = previewItem ?: return
        val updated = transform(item)
        previewItem = updated
        val i = history.indexOfFirst { it.id == item.id }
        if (i >= 0) history[i] = updated
    }

    fun editCaption(v: String) = updatePreview { it.copy(caption = v.take(MAX_CAPTION)) }
    fun setTemplate(id: Int) = updatePreview { it.copy(templateId = id) }
    fun setFont(fontId: Int) = updatePreview { it.copy(fontId = fontId) }
    fun setInfo(info: CardInfo) = updatePreview { it.copy(info = info) }
    fun editCategory(v: String) = updatePreview { it.copy(category = v.take(30)) }
    fun editSpeaker(v: String) = updatePreview { it.copy(speaker = v.take(60)) }
    fun editSource(v: String) = updatePreview { it.copy(source = v.take(60)) }

    fun clearHistory() {
        history.clear()
        photos.clear()
        userPhoto.clear()
        gen.clear()
        genStep.clear()
    }
}
