package com.ainewsstudio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainewsstudio.data.CardInfo
import com.ainewsstudio.data.CardText
import com.ainewsstudio.data.NewsTemplate
import com.ainewsstudio.data.TemplateLayout
import com.ainewsstudio.ui.theme.AppFonts

/** Scale helper: 1 unit = 1% of the card width, so every layout renders identically at any size. */
@Immutable
class Sc(val u: Float) {
    fun sp(k: Float): TextUnit = (u * k).sp
    fun dp(k: Float): Dp = (u * k).dp
}

/** Lets the preview screen override the headline font of every template. */
val LocalHeadlineFont = compositionLocalOf<FontFamily?> { null }

/** AI-generated photo shown in place of the placeholder artwork. */
val LocalPhoto = compositionLocalOf<ImageBitmap?> { null }

/** Category / speaker / source typed by the user for the card being drawn. */
val LocalCardText = compositionLocalOf { CardText() }

/** Category label. Draws nothing when the user left it empty. */
@Composable
internal fun Kicker(
    s: Sc, size: Float, color: Color,
    textAlign: TextAlign = TextAlign.Start, modifier: Modifier = Modifier,
    family: FontFamily = AppFonts.sans,
) {
    val c = LocalCardText.current.category
    if (c.isBlank()) return
    Text(
        c,
        modifier = modifier,
        style = labelStyle(s, size, color, FontWeight.Bold, family),
        textAlign = textAlign,
        maxLines = 1, overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Speaker ("statement by") and source lines under the headline. The speaker is clearly smaller
 * than the headline and set like a TV lower-third name; the source is a quiet small line.
 * Each part is hidden when empty.
 */
@Composable
internal fun Attribution(
    s: Sc, accent: Color, nameColor: Color, mutedColor: Color,
    modifier: Modifier = Modifier, centered: Boolean = false,
    family: FontFamily = AppFonts.sans, nameSize: Float = 4.6f,
) {
    val e = LocalCardText.current
    if (e.speaker.isBlank() && e.source.isBlank()) return
    Column(
        modifier,
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Spacer(Modifier.height(s.dp(2f)))
        if (e.speaker.isNotBlank()) {
            val nameStyle = labelStyle(s, nameSize, nameColor, FontWeight.Medium, family)
            if (centered) {
                Box(Modifier.width(s.dp(6f)).height(s.dp(0.35f)).background(accent))
                Spacer(Modifier.height(s.dp(1f)))
                Text(
                    e.speaker, style = nameStyle, textAlign = TextAlign.Center,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
            } else {
                Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(s.dp(0.9f)).fillMaxHeight().background(accent))
                    Spacer(Modifier.width(s.dp(2f)))
                    Text(e.speaker, style = nameStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (e.source.isNotBlank()) {
            if (e.speaker.isNotBlank()) Spacer(Modifier.height(s.dp(1.2f)))
            Text(
                "সূত্র: " + e.source,
                style = labelStyle(s, 2.9f, mutedColor, FontWeight.Normal, family),
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

internal fun fit(h: String): Float = when {
    h.length > 110 -> 0.68f
    h.length > 80 -> 0.78f
    h.length > 55 -> 0.9f
    else -> 1f
}

@Composable
internal fun headStyle(
    s: Sc, size: Float, color: Color, h: String,
    family: FontFamily = AppFonts.sans, weight: FontWeight = FontWeight.Bold, italic: Boolean = false,
): TextStyle {
    val k = size * fit(h)
    return TextStyle(
        color = color,
        fontSize = s.sp(k),
        lineHeight = s.sp(k * 1.45f),
        fontWeight = weight,
        fontFamily = LocalHeadlineFont.current ?: family,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    )
}

internal fun labelStyle(
    s: Sc, size: Float, color: Color, weight: FontWeight = FontWeight.Bold, family: FontFamily = AppFonts.sans,
) = TextStyle(
    color = color,
    fontSize = s.sp(size),
    lineHeight = s.sp(size * 1.5f),
    fontWeight = weight,
    fontFamily = family,
)

@Composable
internal fun BrandText(
    brand: String?, s: Sc, color: Color,
    size: Float = 3.4f, family: FontFamily = AppFonts.sans, modifier: Modifier = Modifier,
) {
    if (brand.isNullOrBlank()) return
    Text(
        brand,
        modifier = modifier,
        style = labelStyle(s, size, color, FontWeight.SemiBold, family),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Abstract placeholder artwork. Real AI/photo imagery is not implemented yet. */
@Composable
internal fun DemoPhoto(modifier: Modifier, c1: Color, c2: Color, shape: Shape = RectangleShape) {
    val photo = LocalPhoto.current
    if (photo != null) {
        Image(
            bitmap = photo,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape),
        )
        return
    }
    Canvas(modifier.clip(shape)) {
        val w = size.width
        val h = size.height
        drawRect(Brush.linearGradient(listOf(c1, c2)))
        drawCircle(Color.White.copy(alpha = 0.10f), radius = h * 0.35f, center = Offset(w * 0.72f, h * 0.30f))
        val ridge = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.68f)
            lineTo(w * 0.28f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.66f)
            lineTo(w * 0.78f, h * 0.42f)
            lineTo(w, h * 0.62f)
            lineTo(w, h)
            close()
        }
        drawPath(ridge, Color.Black.copy(alpha = 0.28f))
    }
}

/**
 * Renders a complete news photo card for [template]. Aspect ratio is 4:5.
 * [info] (channel, date, website) is drawn as a broadcast-style footer bar; templates with a
 * big masthead (magazine cover, heritage press, tech glass) show the channel name there instead.
 */
@Composable
fun NewsCard(
    template: NewsTemplate,
    headline: String,
    brand: String?,
    modifier: Modifier = Modifier,
    headlineFont: FontFamily? = null,
    photo: ImageBitmap? = null,
    aiTag: Boolean = false,
    info: CardInfo = CardInfo(),
    rounded: Boolean = true,
    text: CardText = CardText(),
    logo: ImageBitmap? = null,
) {
    CompositionLocalProvider(
        LocalHeadlineFont provides headlineFont,
        LocalPhoto provides photo,
        LocalCardText provides text,
        LocalDensity provides Density(LocalDensity.current.density, 1f),
    ) {
        BoxWithConstraints(modifier.aspectRatio(0.8f).clip(RoundedCornerShape(if (rounded) 6.dp else 0.dp))) {
            val s = Sc(maxWidth.value / 100f)
            val channel = info.channel.ifBlank { brand ?: "" }
            val masthead = template.layout == TemplateLayout.MagazineCover ||
                template.layout == TemplateLayout.HeritagePress ||
                template.layout == TemplateLayout.TechGlass
            val layoutBrand: String? = if (masthead && channel.isNotBlank()) channel else null
            val footerChannel = if (masthead) "" else channel
            val links = listOf(info.website, info.facebook, info.contact)
                .filter { it.isNotBlank() }.joinToString("   •   ")
            val footerLogo = if (info.logo) logo else null
            val footerOn = footerChannel.isNotBlank() || info.date.isNotBlank() || links.isNotBlank() || footerLogo != null
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    when (template.layout) {
                    TemplateLayout.Broadsheet -> Broadsheet(template, headline, layoutBrand, s)
                    TemplateLayout.BreakingTv -> BreakingTv(template, headline, layoutBrand, s)
                    TemplateLayout.PremiumGold -> PremiumGold(template, headline, layoutBrand, s)
                    TemplateLayout.ModernSplit -> ModernSplit(template, headline, layoutBrand, s)
                    TemplateLayout.MagazineCover -> MagazineCover(template, headline, layoutBrand, s)
                    TemplateLayout.Sports -> Sports(template, headline, layoutBrand, s)
                    TemplateLayout.TvStudio -> TvStudio(template, headline, layoutBrand, s)
                    TemplateLayout.Political -> Political(template, headline, layoutBrand, s)
                    TemplateLayout.EditorialLight -> EditorialLight(template, headline, layoutBrand, s)
                    TemplateLayout.NightReport -> NightReport(template, headline, layoutBrand, s)
                    TemplateLayout.GlobalBanner -> GlobalBanner(template, headline, layoutBrand, s)
                    TemplateLayout.WorldDesk -> WorldDesk(template, headline, layoutBrand, s)
                    TemplateLayout.FlashUpdate -> FlashUpdate(template, headline, layoutBrand, s)
                    TemplateLayout.BusinessTicker -> BusinessTicker(template, headline, layoutBrand, s)
                    TemplateLayout.TechGlass -> TechGlass(template, headline, layoutBrand, s)
                    TemplateLayout.HeritagePress -> HeritagePress(template, headline, layoutBrand, s)
                    TemplateLayout.DiagonalSplit -> DiagonalSplit(template, headline, layoutBrand, s)
                    TemplateLayout.BoldBanner -> BoldBanner(template, headline, layoutBrand, s)
                    TemplateLayout.Documentary -> Documentary(template, headline, layoutBrand, s)
                    TemplateLayout.AnalysisPortrait -> AnalysisPortrait(template, headline, layoutBrand, s)
                    }
                    if (photo != null && aiTag) {
                        Box(
                            Modifier.align(Alignment.BottomEnd).padding(s.dp(2f))
                                .clip(RoundedCornerShape(s.dp(1f))).background(Color(0x99000000))
                                .padding(horizontal = s.dp(1.6f), vertical = s.dp(0.6f)),
                        ) {
                            Text("এআই ছবি", style = labelStyle(s, 2.6f, Color.White, FontWeight.Medium))
                        }
                    }
                }
                if (footerOn) InfoFooter(template, footerChannel, info.date, links, footerLogo, s)
            }
        }
    }
}

// 1 — International newspaper: cream paper, serif, double rule
@Composable
private fun Broadsheet(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Column(
        Modifier.fillMaxSize().background(t.bg)
            .padding(horizontal = s.dp(6f), vertical = s.dp(5f)),
    ) {
        Box(Modifier.fillMaxWidth().height(s.dp(0.9f)).background(t.fg))
        Spacer(Modifier.height(s.dp(0.7f)))
        Box(Modifier.fillMaxWidth().height(s.dp(0.3f)).background(t.fg))
        Spacer(Modifier.height(s.dp(2.5f)))
        Kicker(s, 3.4f, t.accent)
        Spacer(Modifier.height(s.dp(1.5f)))
        Text(
            h, style = headStyle(s, 8.2f, t.fg, h, family = AppFonts.serif),
            maxLines = 5, overflow = TextOverflow.Ellipsis,
        )
        Attribution(s, t.accent, t.fg, t.fg.copy(alpha = 0.6f), family = AppFonts.serif)
        Spacer(Modifier.height(s.dp(3f)))
        DemoPhoto(Modifier.fillMaxWidth().weight(1f), Color(0xFF6B6B6B), Color(0xFF2A2A2A))
        if (!brand.isNullOrBlank()) {
            Spacer(Modifier.height(s.dp(2.5f)))
            Box(Modifier.fillMaxWidth().height(s.dp(0.3f)).background(t.fg))
            Spacer(Modifier.height(s.dp(1.5f)))
            BrandText(brand, s, t.fg, 3.4f, family = AppFonts.serif)
        }
    }
}

// 2 — TV breaking news: full-bleed image with lower-third
@Composable
private fun BreakingTv(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Box(Modifier.fillMaxSize().background(t.bg)) {
        DemoPhoto(Modifier.fillMaxSize(), Color(0xFF3A4660), Color(0xFF0A0A0F))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color(0xE6000000)),
            ),
        )
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
            if (LocalCardText.current.category.isNotBlank()) {
                Box(
                    Modifier.padding(start = s.dp(5f)).background(t.accent)
                        .padding(horizontal = s.dp(3f), vertical = s.dp(1f)),
                ) {
                    Text(LocalCardText.current.category, style = labelStyle(s, 4.2f, Color.White))
                }
            }
            Box(
                Modifier.fillMaxWidth().background(Color.White)
                    .padding(horizontal = s.dp(5f), vertical = s.dp(3f)),
            ) {
                Column {
                    Text(
                        h, style = headStyle(s, 7f, Color(0xFF0A0A0F), h, weight = FontWeight.ExtraBold),
                        maxLines = 4, overflow = TextOverflow.Ellipsis,
                    )
                    Attribution(s, t.accent, Color(0xFF0A0A0F), Color(0xFF5A5A66))
                }
            }
            if (!brand.isNullOrBlank()) {
                Box(
                    Modifier.fillMaxWidth().height(s.dp(7f)).background(Color(0xFF14141A))
                        .padding(horizontal = s.dp(5f)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BrandText(brand, s, Color.White, 3.4f)
                }
            }
        }
    }
}

// 3 — Premium modern: black and gold, framed, centered serif
@Composable
private fun PremiumGold(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Box(Modifier.fillMaxSize().background(t.bg).padding(s.dp(3.5f))) {
        Box(Modifier.fillMaxSize().border(s.dp(0.35f), t.accent))
        Column(
            Modifier.fillMaxSize().padding(s.dp(6f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DemoPhoto(
                Modifier.fillMaxWidth().weight(1f)
                    .border(s.dp(0.35f), t.accent).padding(s.dp(0.9f)),
                Color(0xFF5A4A2A), Color(0xFF14100A),
            )
            Spacer(Modifier.height(s.dp(3.5f)))
            Kicker(s, 3.2f, t.accent, textAlign = TextAlign.Center)
            Spacer(Modifier.height(s.dp(1.5f)))
            Text(
                h, style = headStyle(s, 6.8f, t.fg, h, family = AppFonts.elegant),
                textAlign = TextAlign.Center, maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, t.fg, t.fg.copy(alpha = 0.6f), centered = true, family = AppFonts.elegant)
            Spacer(Modifier.height(s.dp(2.5f)))
            Box(Modifier.width(s.dp(14f)).height(s.dp(0.4f)).background(t.accent))
            if (!brand.isNullOrBlank()) {
                Spacer(Modifier.height(s.dp(2.5f)))
                BrandText(brand, s, t.accent, 3.2f, family = AppFonts.elegant)
            }
        }
    }
}

// 4 — Premium modern: vertical split, image column + text column
@Composable
private fun ModernSplit(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Row(Modifier.fillMaxSize().background(t.bg)) {
        DemoPhoto(Modifier.weight(0.42f).fillMaxHeight(), Color(0xFF2B4B6F), Color(0xFF0A1220))
        Column(
            Modifier.weight(0.58f).fillMaxHeight().padding(s.dp(5f)),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            if (LocalCardText.current.category.isNotBlank()) {
                Box(
                    Modifier.clip(RoundedCornerShape(50)).background(t.accent.copy(alpha = 0.16f))
                        .padding(horizontal = s.dp(2.5f), vertical = s.dp(0.8f)),
                ) {
                    Text(LocalCardText.current.category, style = labelStyle(s, 3.2f, t.accent))
                }
            }
            Column {
                Text(
                    h, style = headStyle(s, 6.4f, t.fg, h, weight = FontWeight.ExtraBold),
                    maxLines = 8, overflow = TextOverflow.Ellipsis,
                )
                Attribution(s, t.accent, t.fg, Color(0xFFB8C4D6), nameSize = 4.2f)
            }
            Column {
                Box(Modifier.width(s.dp(8f)).height(s.dp(0.8f)).background(t.accent))
                if (!brand.isNullOrBlank()) {
                    Spacer(Modifier.height(s.dp(2f)))
                    BrandText(brand, s, Color(0xFFB8C4D6), 3.1f)
                }
            }
        }
    }
}

// 5 — International magazine cover: masthead + full-bleed image
@Composable
private fun MagazineCover(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Box(Modifier.fillMaxSize().background(t.bg)) {
        DemoPhoto(Modifier.fillMaxSize(), Color(0xFF6B5B3E), Color(0xFF111111))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0x99000000), 0.25f to Color.Transparent,
                    0.55f to Color.Transparent, 1f to Color(0xEE000000),
                ),
            ),
        )
        if (!brand.isNullOrBlank()) {
            Text(
                brand,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = s.dp(5f), start = s.dp(5f), end = s.dp(5f)),
                style = TextStyle(
                    color = Color.White, fontSize = s.sp(10f), lineHeight = s.sp(14f),
                    fontWeight = FontWeight.Black, fontFamily = AppFonts.calli,
                ),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            Modifier.align(Alignment.BottomStart).padding(horizontal = s.dp(6f), vertical = s.dp(6f))
                .height(IntrinsicSize.Min),
        ) {
            Box(Modifier.width(s.dp(1.2f)).fillMaxHeight().background(t.accent))
            Spacer(Modifier.width(s.dp(3f)))
            Column {
                Kicker(s, 3.4f, t.accent)
                Spacer(Modifier.height(s.dp(1f)))
                Text(
                    h, style = headStyle(s, 7.6f, Color.White, h, family = AppFonts.serif),
                    maxLines = 5, overflow = TextOverflow.Ellipsis,
                )
                Attribution(s, t.accent, Color.White, Color(0xCCFFFFFF), family = AppFonts.serif)
            }
        }
    }
}

// 6 — Sports: neon on dark green, diagonal image cut, italic heavy headline
@Composable
private fun Sports(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    val cut = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height * 0.86f)
        lineTo(0f, size.height)
        close()
    }
    Box(Modifier.fillMaxSize().background(t.bg)) {
        DemoPhoto(Modifier.fillMaxWidth().fillMaxHeight(0.58f), Color(0xFF1F6B3A), Color(0xFF06130C), cut)
        Canvas(Modifier.fillMaxWidth().fillMaxHeight(0.58f)) {
            drawLine(
                t.accent, Offset(0f, size.height), Offset(size.width, size.height * 0.86f),
                strokeWidth = size.width * 0.012f,
            )
        }
        if (LocalCardText.current.category.isNotBlank()) {
            Box(Modifier.padding(s.dp(5f)).background(t.accent).padding(horizontal = s.dp(3f), vertical = s.dp(1f))) {
                Text(LocalCardText.current.category, style = labelStyle(s, 4f, Color.Black, FontWeight.Black))
            }
        }
        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth()
                .padding(horizontal = s.dp(5f), vertical = s.dp(5f)),
        ) {
            Text(
                h, style = headStyle(s, 8.4f, t.fg, h, family = AppFonts.display, weight = FontWeight.Bold),
                maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, t.fg, Color(0xFFB7CDBF), family = AppFonts.display)
            Spacer(Modifier.height(s.dp(2.5f)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(s.dp(12f)).height(s.dp(1f)).background(t.accent))
                Spacer(Modifier.width(s.dp(2.5f)))
                BrandText(brand, s, Color(0xFFD7E8DC), 3.2f)
            }
        }
    }
}

// 7 — TV studio: blue panels, framed image, white headline plate, ticker
@Composable
private fun TvStudio(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Column(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A2A66), t.bg)),
        ),
    ) {
        if (LocalCardText.current.category.isNotBlank()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = s.dp(5f), vertical = s.dp(3.5f)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(s.dp(2.2f)).background(t.accent))
                Spacer(Modifier.width(s.dp(2f)))
                Text(LocalCardText.current.category, style = labelStyle(s, 3.8f, Color.White))
            }
        } else {
            Spacer(Modifier.height(s.dp(4f)))
        }
        DemoPhoto(
            Modifier.padding(horizontal = s.dp(5f)).fillMaxWidth().weight(1f)
                .border(s.dp(0.5f), Color.White.copy(alpha = 0.5f)),
            Color(0xFF2E63B8), Color(0xFF04122B),
        )
        Spacer(Modifier.height(s.dp(2f)))
        Box(
            Modifier.fillMaxWidth().padding(horizontal = s.dp(5f)).background(Color.White)
                .padding(horizontal = s.dp(4f), vertical = s.dp(3f)),
        ) {
            Column {
                Text(
                    h, style = headStyle(s, 6.6f, Color(0xFF04122B), h, weight = FontWeight.ExtraBold),
                    maxLines = 4, overflow = TextOverflow.Ellipsis,
                )
                Attribution(s, t.accent, Color(0xFF04122B), Color(0xFF4A5A7A), nameSize = 4.2f)
            }
        }
        Spacer(Modifier.height(s.dp(3f)))
        if (!brand.isNullOrBlank()) {
            Box(
                Modifier.fillMaxWidth().height(s.dp(7f)).background(t.accent).padding(horizontal = s.dp(5f)),
                contentAlignment = Alignment.CenterStart,
            ) {
                BrandText(brand, s, Color.White, 3.4f)
            }
        }
    }
}

// 8 — Political news: navy and crimson, headline on top, image below, tricolour band
@Composable
private fun Political(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Column(Modifier.fillMaxSize().background(t.bg)) {
        if (LocalCardText.current.category.isNotBlank()) {
            Row(
                Modifier.fillMaxWidth().background(t.accent)
                    .padding(horizontal = s.dp(5f), vertical = s.dp(2.4f)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    LocalCardText.current.category, modifier = Modifier.weight(1f),
                    style = labelStyle(s, 4.2f, Color.White), maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                BrandText(brand, s, Color.White, 3.2f, modifier = Modifier.weight(1f, fill = false))
            }
        } else {
            Box(Modifier.fillMaxWidth().height(s.dp(1.6f)).background(t.accent))
        }
        Column(Modifier.padding(horizontal = s.dp(5f), vertical = s.dp(4f))) {
            Text(
                h, style = headStyle(s, 8f, Color.White, h, weight = FontWeight.ExtraBold),
                maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, Color.White, Color(0xFFB8C0D8))
            Spacer(Modifier.height(s.dp(2.5f)))
            Box(Modifier.width(s.dp(14f)).height(s.dp(1.1f)).background(t.accent))
        }
        Box(Modifier.fillMaxWidth().height(s.dp(0.8f)).background(Color.White))
        DemoPhoto(Modifier.fillMaxWidth().weight(1f), Color(0xFF3B4C7A), Color(0xFF0F1B3D))
        Row(Modifier.fillMaxWidth().height(s.dp(1.8f))) {
            Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF24366B)))
            Box(Modifier.weight(1f).fillMaxHeight().background(Color.White))
            Box(Modifier.weight(1f).fillMaxHeight().background(t.accent))
        }
    }
}

// 9 — Premium modern, light editorial: big sans headline, rounded image
@Composable
private fun EditorialLight(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Column(Modifier.fillMaxSize().background(t.bg).padding(s.dp(6f))) {
        if (LocalCardText.current.category.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(s.dp(2.2f)).background(t.accent))
                Spacer(Modifier.width(s.dp(2f)))
                Text(LocalCardText.current.category, style = labelStyle(s, 3.6f, t.accent))
            }
            Spacer(Modifier.height(s.dp(3f)))
        }
        Text(
            h, style = headStyle(s, 9.2f, t.fg, h, weight = FontWeight.Black),
            maxLines = 5, overflow = TextOverflow.Ellipsis,
        )
        Attribution(s, t.accent, t.fg, t.fg.copy(alpha = 0.55f))
        Spacer(Modifier.height(s.dp(4f)))
        DemoPhoto(
            Modifier.fillMaxWidth().weight(1f),
            Color(0xFFB9C7BF), Color(0xFF5E7A6C), RoundedCornerShape(s.dp(4f)),
        )
        if (!brand.isNullOrBlank()) {
            Spacer(Modifier.height(s.dp(3f)))
            Box(Modifier.fillMaxWidth().height(s.dp(0.25f)).background(t.fg.copy(alpha = 0.25f)))
            Spacer(Modifier.height(s.dp(2f)))
            BrandText(brand, s, t.fg.copy(alpha = 0.7f), 3.2f)
        }
    }
}

// 10 — TV / night report: timeline rail, outlined tag, rounded image
@Composable
private fun NightReport(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Row(Modifier.fillMaxSize().background(t.bg)) {
        Spacer(Modifier.width(s.dp(5f)))
        Box(
            Modifier.width(s.dp(1.1f)).fillMaxHeight()
                .background(Brush.verticalGradient(listOf(t.accent, Color.Transparent))),
        )
        Column(
            Modifier.weight(1f).fillMaxHeight().padding(horizontal = s.dp(5f), vertical = s.dp(6f)),
        ) {
            if (LocalCardText.current.category.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(s.dp(2.4f)).clip(CircleShape).background(t.accent))
                    Spacer(Modifier.width(s.dp(2f)))
                    Text(LocalCardText.current.category, style = labelStyle(s, 3.6f, t.accent))
                }
                Spacer(Modifier.height(s.dp(3f)))
            }
            Text(
                h, style = headStyle(s, 7.2f, t.fg, h, weight = FontWeight.Medium),
                maxLines = 5, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, t.fg, t.fg.copy(alpha = 0.6f))
            Spacer(Modifier.height(s.dp(3.5f)))
            DemoPhoto(
                Modifier.fillMaxWidth().weight(1f),
                Color(0xFF1B5560), Color(0xFF071A1F), RoundedCornerShape(s.dp(2.5f)),
            )
            if (!brand.isNullOrBlank()) {
                Spacer(Modifier.height(s.dp(2.5f)))
                BrandText(brand, s, t.fg.copy(alpha = 0.7f), 3.2f)
            }
        }
    }
}
