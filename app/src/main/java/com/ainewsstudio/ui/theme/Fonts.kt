package com.ainewsstudio.ui.theme

import android.content.res.AssetManager
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

/**
 * Bengali fonts are bundled as TTF files in assets/fonts (open-source, SIL OFL).
 * Any missing family is skipped and a system font is used instead, so the app never crashes.
 * Static and variable font files are both supported.
 */
object AppFonts {
    class Choice(val id: Int, val label: String, val family: FontFamily?)

    var sans: FontFamily = FontFamily.Default
    var serif: FontFamily = FontFamily.Serif
    var display: FontFamily = FontFamily.Default
    var elegant: FontFamily = FontFamily.Serif
    var calli: FontFamily = FontFamily.Serif
    var notoSans: FontFamily = FontFamily.Default
    var notoSerif: FontFamily = FontFamily.Serif
    var condensed: FontFamily = FontFamily.Default
    var anek: FontFamily = FontFamily.Default
    var baloo: FontFamily = FontFamily.Default

    /** True when at least one bundled font was found. */
    var loaded: Boolean = false
    var choices: List<Choice> = listOf(Choice(0, "ডিফল্ট", null))

    fun familyOf(id: Int): FontFamily? = choices.firstOrNull { it.id == id }?.family

    private fun weightOf(name: String): FontWeight = when {
        name.contains("ExtraBold") -> FontWeight.ExtraBold
        name.contains("SemiBold") -> FontWeight.SemiBold
        name.contains("Bold") -> FontWeight.Bold
        name.contains("ExtraLight") -> FontWeight.ExtraLight
        name.contains("Light") -> FontWeight.Light
        name.contains("Thin") -> FontWeight.Thin
        name.contains("Medium") -> FontWeight.Medium
        name.contains("Black") -> FontWeight.Black
        else -> FontWeight.Normal
    }

    @OptIn(ExperimentalTextApi::class)
    fun init(assets: AssetManager) {
        val present: Set<String> = try {
            assets.list("fonts")?.toSet() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }

        fun family(prefix: String): FontFamily? {
            val files = present.filter { it.startsWith(prefix) && it.endsWith(".ttf") }.sorted()
            val fonts = mutableListOf<Font>()
            for (name in files) {
                val path = "fonts/" + name
                if (name.contains("wght")) {
                    // Variable font: weight axis is applied automatically per requested weight.
                    if (name.contains("Italic")) continue
                    for (w in listOf(400, 500, 600, 700, 800, 900)) {
                        fonts.add(Font(assetManager = assets, path = path, weight = FontWeight(w), style = FontStyle.Normal))
                    }
                } else {
                    val style = if (name.contains("Italic")) FontStyle.Italic else FontStyle.Normal
                    fonts.add(Font(assetManager = assets, path = path, weight = weightOf(name), style = style))
                }
            }
            return if (fonts.isEmpty()) null else FontFamily(fonts)
        }

        val hind = family("HindSiliguri")
        val notoSansF = family("NotoSansBengali")
        val notoSerifF = family("NotoSerifBengali")
        val tiro = family("TiroBangla")
        val anekF = family("AnekBangla")
        val balooF = family("BalooDa2")
        val atma = family("Atma")
        val mina = family("Mina")
        val galada = family("Galada")

        // Narrow (condensed) cut of Noto Sans Bengali: typical TV-headline look.
        val condensedF: FontFamily? = present
            .firstOrNull { it.startsWith("NotoSansBengali") && it.contains("wght") }
            ?.let { name ->
                FontFamily(
                    listOf(500, 700, 900).map { w ->
                        Font(
                            assetManager = assets,
                            path = "fonts/" + name,
                            weight = FontWeight(w),
                            style = FontStyle.Normal,
                            variationSettings = FontVariation.Settings(
                                FontVariation.weight(w),
                                FontVariation.width(75f),
                            ),
                        )
                    },
                )
            }

        sans = hind ?: notoSansF ?: FontFamily.Default
        serif = tiro ?: notoSerifF ?: FontFamily.Serif
        display = atma ?: hind ?: FontFamily.Default
        elegant = mina ?: tiro ?: FontFamily.Serif
        calli = galada ?: tiro ?: FontFamily.Serif
        notoSans = notoSansF ?: sans
        notoSerif = notoSerifF ?: serif
        condensed = condensedF ?: notoSansF ?: sans
        anek = anekF ?: sans
        baloo = balooF ?: sans

        val list = mutableListOf(Choice(0, "ডিফল্ট", null))
        if (hind != null) list.add(Choice(1, "হিন্দ শিলিগুড়ি", hind))
        if (notoSansF != null) list.add(Choice(2, "নোটো স্যান্স", notoSansF))
        if (notoSerifF != null) list.add(Choice(3, "নোটো সেরিফ", notoSerifF))
        if (condensedF != null) list.add(Choice(4, "নোটো কন্ডেন্সড", condensedF))
        if (tiro != null) list.add(Choice(5, "তিরো বাংলা", tiro))
        if (anekF != null) list.add(Choice(6, "অনেক বাংলা", anekF))
        if (balooF != null) list.add(Choice(7, "বালু দা", balooF))
        if (atma != null) list.add(Choice(8, "আত্মা", atma))
        if (mina != null) list.add(Choice(9, "মিনা", mina))
        if (galada != null) list.add(Choice(10, "গালাদা", galada))
        choices = list
        loaded = list.size > 1
    }
}
