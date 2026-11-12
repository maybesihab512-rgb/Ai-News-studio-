package com.ainewsstudio.ui

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.drawToBitmap

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Renders a composable to a 1080x1350 bitmap, saves it to the gallery and shares it. */
object CardExporter {
    const val W = 1080
    const val H = 1350

    /** Must be called from the main thread inside a Compose coroutine (needs a frame clock). */
    suspend fun render(activity: Activity, content: @Composable () -> Unit): Bitmap {
        val decor = activity.window.decorView as ViewGroup
        val view = ComposeView(activity)
        view.setContent(content)
        view.translationX = -20000f // keep it off-screen while it is composed
        decor.addView(view, FrameLayout.LayoutParams(W, H))
        try {
            var waited = 0
            while ((view.width != W || view.height != H) && waited < 30) {
                withFrameNanos { }
                waited++
            }
            repeat(3) { withFrameNanos { } }
            return view.drawToBitmap(Bitmap.Config.ARGB_8888)
        } finally {
            decor.removeView(view)
        }
    }

    /** Blocking; call on a background thread. Returns the gallery Uri or null. */
    fun saveToGallery(context: Context, bitmap: Bitmap, name: String): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AI News Studio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        val ok = resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } ?: false
        if (!ok) {
            resolver.delete(uri, null, null)
            return null
        }
        if (Build.VERSION.SDK_INT >= 29) {
            val done = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
            resolver.update(uri, done, null, null)
        }
        return uri
    }

    fun share(context: Context, uri: Uri) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "শেয়ার করুন"))
    }
}
