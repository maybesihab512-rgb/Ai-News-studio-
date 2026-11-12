package com.ainewsstudio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.ainewsstudio.data.NewsTemplate
import com.ainewsstudio.ui.theme.AppFonts

// 11 — International TV: full-bleed image, left panel with accent bar, condensed headline
@Composable
internal fun GlobalBanner(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Box(Modifier.fillMaxSize().background(t.bg)) {
        DemoPhoto(Modifier.fillMaxSize(), Color(0xFF45566F), Color(0xFF0A0F1A))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color(0xF0000000)),
            ),
        )
        if (!brand.isNullOrBlank()) {
            Box(
                Modifier.padding(s.dp(5f)).background(t.accent)
                    .padding(horizontal = s.dp(3f), vertical = s.dp(1.2f)),
            ) {
                BrandText(brand, s, Color.White, 3.4f)
            }
        }
        Row(
            Modifier.align(Alignment.BottomStart).fillMaxWidth()
                .padding(start = s.dp(5f), end = s.dp(12f), bottom = s.dp(6f))
                .height(IntrinsicSize.Min),
        ) {
            Box(Modifier.width(s.dp(1.4f)).fillMaxHeight().background(t.accent))
            Spacer(Modifier.width(s.dp(3f)))
            Column {
                Kicker(s, 3.6f, t.accent)
                Spacer(Modifier.height(s.dp(1f)))
                Text(
                    h,
                    style = headStyle(s, 8.6f, Color.White, h, family = AppFonts.condensed, weight = FontWeight.Bold),
                    maxLines = 4, overflow = TextOverflow.Ellipsis,
                )
                Attribution(s, t.accent, Color.White, Color(0xCCFFFFFF), family = AppFonts.condensed)
            }
        }
    }
}

// 12 — International newspaper, digital edition: photo on top, serif text below
@Composable
internal fun WorldDesk(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Column(Modifier.fillMaxSize().background(t.bg)) {
        Box(Modifier.fillMaxWidth().height(s.dp(1.2f)).background(t.accent))
        DemoPhoto(Modifier.fillMaxWidth().weight(0.55f), Color(0xFF5E6B7C), Color(0xFF1C222B))
        Column(
            Modifier.fillMaxWidth().weight(0.45f)
                .padding(horizontal = s.dp(6f), vertical = s.dp(4f)),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Kicker(s, 3.4f, t.accent)
                Spacer(Modifier.height(s.dp(1.5f)))
                Text(
                    h, style = headStyle(s, 7.4f, t.fg, h, family = AppFonts.notoSerif),
                    maxLines = 4, overflow = TextOverflow.Ellipsis,
                )
                Attribution(s, t.accent, t.fg, t.fg.copy(alpha = 0.6f), family = AppFonts.notoSerif)
            }
            if (!brand.isNullOrBlank()) {
                Column {
                    Box(Modifier.fillMaxWidth().height(s.dp(0.25f)).background(t.fg.copy(alpha = 0.3f)))
                    Spacer(Modifier.height(s.dp(1.5f)))
                    BrandText(brand, s, t.fg.copy(alpha = 0.7f), 3.2f, family = AppFonts.notoSerif)
                }
            }
        }
    }
}

// 13 — TV flash update: black and yellow, slanted tag and slanted image edge
@Composable
internal fun FlashUpdate(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    val tag = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width - size.height * 0.6f, size.height)
        lineTo(0f, size.height)
        close()
    }
    val cut = GenericShape { size, _ ->
        moveTo(0f, size.height * 0.12f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    Column(Modifier.fillMaxSize().background(t.bg).padding(top = s.dp(5f))) {
        if (LocalCardText.current.category.isNotBlank()) {
            Box(
                Modifier.clip(tag).background(t.accent)
                    .padding(start = s.dp(5f), end = s.dp(9f), top = s.dp(1.6f), bottom = s.dp(1.6f)),
            ) {
                Text(LocalCardText.current.category, style = labelStyle(s, 4.4f, Color.Black, FontWeight.Black))
            }
            Spacer(Modifier.height(s.dp(3.5f)))
        }
        Text(
            h,
            modifier = Modifier.padding(horizontal = s.dp(5f)),
            style = headStyle(s, 8.8f, Color.White, h, family = AppFonts.condensed, weight = FontWeight.Black),
            maxLines = 4, overflow = TextOverflow.Ellipsis,
        )
        Attribution(
            s, t.accent, Color.White, Color(0xCCFFFFFF),
            modifier = Modifier.padding(horizontal = s.dp(5f)), family = AppFonts.condensed,
        )
        Spacer(Modifier.height(s.dp(3.5f)))
        DemoPhoto(Modifier.fillMaxWidth().weight(1f), Color(0xFF7A6A1E), Color(0xFF0B0B0B), cut)
        if (!brand.isNullOrBlank()) {
            Box(
                Modifier.fillMaxWidth().height(s.dp(7f)).background(t.accent).padding(horizontal = s.dp(5f)),
                contentAlignment = Alignment.CenterStart,
            ) {
                BrandText(brand, s, Color.Black, 3.3f)
            }
        }
    }
}

// 14 — Business news: navy gradient, faint grid, rounded image, ticker strip
@Composable
internal fun BusinessTicker(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0A2750), t.bg)))) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.height / 10f
            for (i in 1..9) {
                drawLine(
                    Color.White.copy(alpha = 0.05f),
                    Offset(0f, step * i), Offset(size.width, step * i), strokeWidth = 1f,
                )
            }
        }
        Column(Modifier.fillMaxSize()) {
            if (LocalCardText.current.category.isNotBlank()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = s.dp(5f), vertical = s.dp(4f)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(s.dp(2.4f)).background(t.accent))
                    Spacer(Modifier.width(s.dp(2f)))
                    Text(LocalCardText.current.category, style = labelStyle(s, 3.8f, t.accent))
                }
            } else {
                Spacer(Modifier.height(s.dp(5f)))
            }
            Text(
                h,
                modifier = Modifier.padding(horizontal = s.dp(5f)),
                style = headStyle(s, 7.8f, Color.White, h, family = AppFonts.notoSans),
                maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(
                s, t.accent, Color.White, Color(0xB3FFFFFF),
                modifier = Modifier.padding(horizontal = s.dp(5f)), family = AppFonts.notoSans,
            )
            Spacer(Modifier.height(s.dp(4f)))
            DemoPhoto(
                Modifier.padding(horizontal = s.dp(5f)).fillMaxWidth().weight(1f),
                Color(0xFF1D5C8A), Color(0xFF061B3A), RoundedCornerShape(s.dp(1.5f)),
            )
            if (!brand.isNullOrBlank()) {
                Spacer(Modifier.height(s.dp(3f)))
                Row(
                    Modifier.fillMaxWidth().height(s.dp(8f)).background(Color(0xFF020B1C)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(s.dp(1.4f)).fillMaxHeight().background(t.accent))
                    Spacer(Modifier.width(s.dp(4f)))
                    BrandText(brand, s, Color.White, 3.4f)
                }
            }
        }
    }
}

// 15 — Premium tech: full-bleed image with a frosted glass panel
@Composable
internal fun TechGlass(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    val panel = RoundedCornerShape(s.dp(3f))
    Box(Modifier.fillMaxSize().background(t.bg)) {
        DemoPhoto(Modifier.fillMaxSize(), Color(0xFF3A2E8C), Color(0xFF070A14))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color(0x66070A14), 1f to Color(0xCC070A14)),
            ),
        )
        BrandText(
            brand, s, Color.White, 3.4f,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = s.dp(5f)),
        )
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(s.dp(5f))
                .clip(panel).background(Color.White.copy(alpha = 0.14f))
                .border(s.dp(0.3f), Color.White.copy(alpha = 0.28f), panel)
                .padding(s.dp(5f)),
        ) {
            if (LocalCardText.current.category.isNotBlank()) {
                Box(
                    Modifier.clip(RoundedCornerShape(50)).background(t.accent)
                        .padding(horizontal = s.dp(3f), vertical = s.dp(0.8f)),
                ) {
                    Text(LocalCardText.current.category, style = labelStyle(s, 3.2f, Color.White))
                }
                Spacer(Modifier.height(s.dp(2.5f)))
            }
            Text(
                h, style = headStyle(s, 7.2f, Color.White, h, family = AppFonts.anek),
                maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, Color.White, Color(0xCCFFFFFF), family = AppFonts.anek)
        }
    }
}

// 16 — Heritage newspaper: cream paper, maroon double frame, calligraphic masthead
@Composable
internal fun HeritagePress(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Box(Modifier.fillMaxSize().background(t.bg).padding(s.dp(3.5f))) {
        Box(Modifier.fillMaxSize().border(s.dp(0.5f), t.accent))
        Box(Modifier.fillMaxSize().padding(s.dp(1f)).border(s.dp(0.2f), t.accent))
        Column(
            Modifier.fillMaxSize().padding(horizontal = s.dp(7f), vertical = s.dp(6f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!brand.isNullOrBlank()) {
                Text(
                    brand,
                    style = TextStyle(
                        color = t.accent, fontSize = s.sp(7f), lineHeight = s.sp(10f),
                        fontFamily = AppFonts.calli,
                    ),
                    textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(s.dp(2f)))
            }
            Box(Modifier.fillMaxWidth().height(s.dp(0.5f)).background(t.fg))
            Spacer(Modifier.height(s.dp(0.6f)))
            Box(Modifier.fillMaxWidth().height(s.dp(0.2f)).background(t.fg))
            Spacer(Modifier.height(s.dp(2.5f)))
            Kicker(s, 3.2f, t.accent, textAlign = TextAlign.Center)
            Spacer(Modifier.height(s.dp(1f)))
            Text(
                h, style = headStyle(s, 6.8f, t.fg, h, family = AppFonts.serif),
                textAlign = TextAlign.Center, maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, t.fg, t.fg.copy(alpha = 0.6f), centered = true, family = AppFonts.serif)
            Spacer(Modifier.height(s.dp(3f)))
            DemoPhoto(
                Modifier.fillMaxWidth().weight(1f).border(s.dp(0.3f), t.fg).padding(s.dp(0.8f)),
                Color(0xFF8A7355), Color(0xFF2A1A12),
            )
        }
    }
}

// 17 — Premium modern: image with diagonal lower edge over a teal panel
@Composable
internal fun DiagonalSplit(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    val cut = GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height * 0.82f)
        lineTo(0f, size.height)
        close()
    }
    Box(Modifier.fillMaxSize().background(t.bg)) {
        DemoPhoto(Modifier.fillMaxWidth().fillMaxHeight(0.64f), Color(0xFF3D8F96), Color(0xFF062A2E), cut)
        if (LocalCardText.current.category.isNotBlank()) {
            Box(
                Modifier.padding(s.dp(5f)).background(Color.White)
                    .padding(horizontal = s.dp(3f), vertical = s.dp(1f)),
            ) {
                Text(LocalCardText.current.category, style = labelStyle(s, 3.6f, t.bg))
            }
        }
        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth()
                .padding(horizontal = s.dp(5.5f), vertical = s.dp(5f)),
        ) {
            Text(
                h, style = headStyle(s, 7.6f, Color.White, h, family = AppFonts.baloo),
                maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, Color.White, Color(0xCCD6F0EE), family = AppFonts.baloo)
            Spacer(Modifier.height(s.dp(2.5f)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(s.dp(10f)).height(s.dp(1f)).background(t.accent))
                Spacer(Modifier.width(s.dp(2.5f)))
                BrandText(brand, s, Color(0xFFD6F0EE), 3.2f)
            }
        }
    }
}

// 18 — TV bold banner: big red category banner, headline, image, footer
@Composable
internal fun BoldBanner(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Column(Modifier.fillMaxSize().background(t.bg)) {
        if (LocalCardText.current.category.isNotBlank()) {
            Box(
                Modifier.fillMaxWidth().background(t.accent)
                    .padding(horizontal = s.dp(5f), vertical = s.dp(3f)),
            ) {
                Text(
                    LocalCardText.current.category,
                    style = labelStyle(s, 8f, Color.White, FontWeight.Black, AppFonts.condensed),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Box(Modifier.fillMaxWidth().height(s.dp(2f)).background(t.accent))
        }
        Column(Modifier.padding(horizontal = s.dp(5f), vertical = s.dp(4f))) {
            Text(
                h,
                style = headStyle(s, 7.8f, t.fg, h, family = AppFonts.anek, weight = FontWeight.ExtraBold),
                maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, t.fg, t.fg.copy(alpha = 0.55f), family = AppFonts.anek)
        }
        DemoPhoto(
            Modifier.padding(horizontal = s.dp(5f)).fillMaxWidth().weight(1f),
            Color(0xFF8A8F98), Color(0xFF2B2F36),
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = s.dp(5f), vertical = s.dp(3f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(s.dp(2.2f)).background(t.accent))
            Spacer(Modifier.width(s.dp(2f)))
            BrandText(brand, s, t.fg, 3.4f)
        }
    }
}

// 19 — Documentary: dark full-bleed image, thin inset frame, serif headline on top
@Composable
internal fun Documentary(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Box(Modifier.fillMaxSize().background(t.bg)) {
        DemoPhoto(Modifier.fillMaxSize(), Color(0xFF5B5B5B), Color(0xFF0A0A0A))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0xCC000000), 0.5f to Color.Transparent, 1f to Color(0xCC000000),
                ),
            ),
        )
        Box(Modifier.fillMaxSize().padding(s.dp(3.5f)).border(s.dp(0.3f), Color.White.copy(alpha = 0.6f)))
        Column(
            Modifier.align(Alignment.TopStart).padding(horizontal = s.dp(7f), vertical = s.dp(7f)),
        ) {
            Kicker(s, 3.6f, t.accent)
            Spacer(Modifier.height(s.dp(1.5f)))
            Text(
                h, style = headStyle(s, 8.4f, Color.White, h, family = AppFonts.notoSerif),
                maxLines = 4, overflow = TextOverflow.Ellipsis,
            )
            Attribution(s, t.accent, Color.White, Color(0xCCFFFFFF), family = AppFonts.notoSerif)
        }
        BrandText(
            brand, s, Color.White, 3.2f,
            modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = s.dp(7f), vertical = s.dp(7f)),
        )
    }
}

// 20 — Analysis: circular portrait with accent ring, centered headline
@Composable
internal fun AnalysisPortrait(t: NewsTemplate, h: String, brand: String?, s: Sc) {
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3A1426), t.bg)))
            .padding(horizontal = s.dp(6f), vertical = s.dp(6f)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Kicker(s, 3.6f, t.accent)
        Spacer(Modifier.height(s.dp(3f)))
        val extras = LocalCardText.current.speaker.isNotBlank() || LocalCardText.current.source.isNotBlank()
        Box(
            Modifier.fillMaxWidth(if (extras) 0.4f else 0.5f).aspectRatio(1f)
                .border(s.dp(0.8f), t.accent, CircleShape).padding(s.dp(1.2f)),
        ) {
            DemoPhoto(Modifier.fillMaxSize(), Color(0xFF8C4A5E), Color(0xFF2A0F1A), CircleShape)
        }
        Spacer(Modifier.height(s.dp(4f)))
        Text(
            h, style = headStyle(s, 6.8f, t.fg, h, family = AppFonts.elegant),
            textAlign = TextAlign.Center, maxLines = 4, overflow = TextOverflow.Ellipsis,
        )
        Attribution(s, t.accent, t.fg, t.fg.copy(alpha = 0.6f), centered = true, family = AppFonts.elegant)
        Spacer(Modifier.weight(1f))
        BrandText(brand, s, t.fg.copy(alpha = 0.7f), 3.2f)
    }
}
