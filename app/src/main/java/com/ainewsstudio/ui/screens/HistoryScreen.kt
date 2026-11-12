package com.ainewsstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ainewsstudio.data.AppViewModel
import com.ainewsstudio.data.CardText
import com.ainewsstudio.data.HistoryItem
import com.ainewsstudio.data.Templates
import com.ainewsstudio.ui.components.NewsCard
import com.ainewsstudio.ui.components.ScreenTitle
import com.ainewsstudio.ui.theme.Accent
import com.ainewsstudio.ui.theme.AppFonts
import com.ainewsstudio.ui.theme.Surface1
import com.ainewsstudio.ui.theme.TextHi
import com.ainewsstudio.ui.theme.TextMid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(vm: AppViewModel, onOpen: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ScreenTitle("ইতিহাস", Modifier.weight(1f))
            if (vm.history.isNotEmpty()) {
                TextButton(onClick = vm::clearHistory) {
                    Text("ইতিহাস মুছুন", color = Accent, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        if (vm.history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "এখনো কোনো ফটোকার্ড তৈরি করা হয়নি",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMid,
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(vm.history, key = { it.id }) { item ->
                    HistoryRow(item, vm.photos[item.id]) {
                        vm.open(item)
                        onOpen()
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: HistoryItem, photo: ImageBitmap?, onClick: () -> Unit) {
    val t = Templates.byId(item.templateId)
    val time = remember(item.id) {
        SimpleDateFormat("d MMM, hh:mm a", Locale("bn", "BD")).format(Date(item.id))
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface1)
            .clickable(onClick = onClick).padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NewsCard(t, item.caption, null, Modifier.width(64.dp), AppFonts.familyOf(item.fontId), photo = photo, info = item.info, text = CardText(item.category, item.speaker, item.source))
        Column(Modifier.weight(1f)) {
            Text(
                item.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = TextHi,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${t.name} · $time",
                style = MaterialTheme.typography.bodySmall,
                color = TextMid,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
