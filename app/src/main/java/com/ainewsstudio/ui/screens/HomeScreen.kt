package com.ainewsstudio.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.material3.TextButton
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ainewsstudio.data.AppViewModel
import com.ainewsstudio.data.CardInfo
import com.ainewsstudio.data.CardText
import com.ainewsstudio.data.PLACEHOLDER_HEADLINE
import com.ainewsstudio.data.NewsTemplate
import com.ainewsstudio.data.Templates
import com.ainewsstudio.ui.components.LocalAnimations
import com.ainewsstudio.ui.components.NewsCard
import com.ainewsstudio.ui.components.PrimaryButton
import com.ainewsstudio.ui.components.SectionTitle
import com.ainewsstudio.ui.components.bn
import com.ainewsstudio.ui.components.motion
import com.ainewsstudio.ui.theme.Accent
import com.ainewsstudio.ui.theme.Outline
import com.ainewsstudio.ui.theme.Surface1
import com.ainewsstudio.ui.theme.TextHi
import com.ainewsstudio.ui.theme.TextMid

@Composable
fun HomeScreen(vm: AppViewModel, onGenerate: () -> Unit) {
    val focus = LocalFocusManager.current
    val info = vm.currentInfo()
    // Template previews follow what the user types, with a short delay to keep typing smooth.
    var draft by remember { mutableStateOf(Pair(vm.caption, CardText(vm.category, vm.speaker, vm.source))) }
    LaunchedEffect(vm.caption, vm.category, vm.speaker, vm.source) {
        delay(450)
        draft = Pair(vm.caption, CardText(vm.category, vm.speaker, vm.source))
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { BrandHeader() }
        item(span = { GridItemSpan(maxLineSpan) }) { CaptionBlock(vm, focus, onGenerate) }
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle("টেমপ্লেট নির্বাচন করুন", "${bn(Templates.all.size)}টি")
        }
        items(Templates.all, key = { it.id }) { t ->
            TemplateTile(
                t = t,
                selected = t.id == vm.selectedTemplateId,
                info = info,
                headline = draft.first.ifBlank { PLACEHOLDER_HEADLINE },
                text = draft.second,
                logo = vm.logo,
                onClick = { vm.selectedTemplateId = t.id },
            )
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(40.dp).background(Accent))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "AI News Studio",
                color = TextHi,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
            )
            Text(
                "বাংলা নিউজ ফটোকার্ড মেকার",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMid,
            )
        }
    }
}

@Composable
private fun CaptionBlock(vm: AppViewModel, focus: FocusManager, onGenerate: () -> Unit) {
    val selected = Templates.byId(vm.selectedTemplateId)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("সংবাদের ক্যাপশন", style = MaterialTheme.typography.titleMedium, color = TextHi)
        OutlinedTextField(
            value = vm.caption,
            onValueChange = vm::onCaptionChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("এখানে সংবাদের শিরোনাম বা ক্যাপশন লিখুন…", color = TextMid) },
            minLines = 4,
            maxLines = 8,
            isError = vm.captionError,
            supportingText = {
                Text(
                    if (vm.captionError) "অনুগ্রহ করে ক্যাপশন লিখুন"
                    else "${bn(vm.caption.length)} / ${bn(AppViewModel.MAX_CAPTION)}",
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = Outline,
                focusedContainerColor = Surface1,
                unfocusedContainerColor = Surface1,
                cursorColor = Accent,
            ),
        )
        ExtraFields(vm)
        Text(
            "নির্বাচিত টেমপ্লেট: ${bn(selected.id)} · ${selected.name}",
            style = MaterialTheme.typography.bodySmall,
            color = TextMid,
        )
        PrimaryButton(
            text = "ফটোকার্ড তৈরি করুন",
            onClick = {
                focus.clearFocus()
                if (vm.generate()) onGenerate()
            },
        )
    }
}

@Composable
private fun TemplateTile(
    t: NewsTemplate,
    selected: Boolean,
    info: CardInfo,
    headline: String,
    text: CardText,
    logo: ImageBitmap?,
    onClick: () -> Unit,
) {
    val anim = LocalAnimations.current
    val scale by animateFloatAsState(if (selected) 1f else 0.965f, motion(anim), label = "tileScale")
    val borderColor by animateColorAsState(if (selected) Accent else Outline, motion(anim), label = "tileBorder")
    val shape = RoundedCornerShape(14.dp)

    Column(
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(Surface1)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(8.dp),
    ) {
        Box {
            NewsCard(t, headline, null, Modifier.fillMaxWidth(), info = info, text = text, logo = logo)
            Box(
                Modifier.align(Alignment.TopStart).padding(6.dp).size(24.dp)
                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(bn(t.id), color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
            }
            if (selected) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp)
                        .clip(CircleShape).background(Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "নির্বাচিত", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(t.name, style = MaterialTheme.typography.titleSmall, color = TextHi, maxLines = 1)
        Text(t.style, style = MaterialTheme.typography.bodySmall, color = TextMid, maxLines = 1)
    }
}

/** Optional category, speaker and source. All empty until the user types something. */
@Composable
private fun ExtraFields(vm: AppViewModel) {
    var expanded by remember {
        mutableStateOf(vm.category.isNotBlank() || vm.speaker.isNotBlank() || vm.source.isNotBlank())
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = { expanded = !expanded }) {
            Text(
                if (expanded) "ক্যাটাগরি, বক্তা ও সূত্র লুকান" else "＋ ক্যাটাগরি, বক্তা ও সূত্র যোগ করুন (ঐচ্ছিক)",
                color = Accent,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (expanded) {
            ExtraField("ক্যাটাগরি (ঐচ্ছিক)", vm.category, vm::onCategoryChange)
            ExtraField("বক্তা বা বিবৃতিদাতা (ঐচ্ছিক)", vm.speaker, vm::onSpeakerChange)
            ExtraField("সূত্র (ঐচ্ছিক)", vm.source, vm::onSourceChange)
            Text(
                "যেটি ফাঁকা রাখবেন সেটি কার্ডে দেখাবে না।",
                style = MaterialTheme.typography.bodySmall,
                color = TextMid,
            )
        }
    }
}

@Composable
private fun ExtraField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent,
            unfocusedBorderColor = Outline,
            focusedContainerColor = Surface1,
            unfocusedContainerColor = Surface1,
            cursorColor = Accent,
            focusedLabelColor = Accent,
        ),
    )
}
