package com.ainewsstudio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.ainewsstudio.data.NewsTemplate
import com.ainewsstudio.ui.theme.AppFonts

/**
 * Broadcast-style info bar at the bottom of every card, in the template's accent colour.
 * Row 1: logo and channel name on the left, date on the right.
 * Row 2 (only if present): website, Facebook page and contact line.
 */
@Composable
internal fun InfoFooter(
    t: NewsTemplate,
    channel: String,
    date: String,
    links: String,
    logo: ImageBitmap?,
    s: Sc,
) {
    val bg = t.accent
    val fg = if (bg.luminance() > 0.5f) Color(0xFF111111) else Color.White
    val hasLeft = channel.isNotBlank() || logo != null
    Column(
        Modifier.fillMaxWidth().background(bg)
            .padding(horizontal = s.dp(4f), vertical = s.dp(1.9f)),
        verticalArrangement = Arrangement.spacedBy(s.dp(0.7f)),
    ) {
        if (hasLeft || date.isNotBlank()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = if (hasLeft) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasLeft) {
                    Row(
                        Modifier.weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(s.dp(1.8f)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (logo != null) {
                            Image(
                                bitmap = logo,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.height(s.dp(5f)).widthIn(max = s.dp(14f)),
                            )
                        }
                        if (channel.isNotBlank()) {
                            Text(
                                channel,
                                modifier = Modifier.weight(1f, fill = false),
                                style = labelStyle(s, 3.3f, fg, FontWeight.Bold, AppFonts.sans),
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (date.isNotBlank()) {
                    Text(
                        date,
                        style = labelStyle(s, 3f, fg, FontWeight.Medium, AppFonts.sans),
                        maxLines = 1,
                    )
                }
            }
        }
        if (links.isNotBlank()) {
            Text(
                links,
                style = labelStyle(s, 2.8f, fg.copy(alpha = 0.92f), FontWeight.Medium, AppFonts.sans),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
