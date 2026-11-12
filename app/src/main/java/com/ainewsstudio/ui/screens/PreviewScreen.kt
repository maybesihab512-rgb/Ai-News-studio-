package com.ainewsstudio.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ainewsstudio.data.AppViewModel
import com.ainewsstudio.data.CardText
import com.ainewsstudio.data.Gen
import com.ainewsstudio.data.HistoryItem
import com.ainewsstudio.data.Templates
import com.ainewsstudio.ui.CardExporter
import com.ainewsstudio.ui.components.LocalAnimations
import com.ainewsstudio.ui.components.NewsCard
import com.ainewsstudio.ui.components.PrimaryButton
import com.ainewsstudio.ui.components.ScreenTitle
import com.ainewsstudio.ui.components.SecondaryButton
import com.ainewsstudio.ui.components.SectionTitle
import com.ainewsstudio.ui.components.bn
import com.ainewsstudio.ui.components.motion
import com.ainewsstudio.ui.findActivity
import com.ainewsstudio.ui.theme.Accent
import com.ainewsstudio.ui.theme.AppFonts
import com.ainewsstudio.ui.theme.AppTheme
import com.ainewsstudio.ui.theme.Outline
import com.ainewsstudio.ui.theme.Surface1
import com.ainewsstudio.ui.theme.TextHi
import com.ainewsstudio.ui.theme.TextMid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PreviewScreen(vm: AppViewModel, onBack: () -> Unit) {
    val item = vm.previewItem
    val anim = LocalAnimations.current
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()

    var shown by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingShare by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) { shown = true }
    val a by animateFloatAsState(if (shown) 1f else 0f, motion(anim), label = "previewIn")

    fun runExport(share: Boolean) {
        val act = activity ?: return
        val current = vm.previewItem ?: return
        if (busy) return
        busy = true
        message = null
        scope.launch {
            try {
                val photo = vm.photos[current.id]
                val showTag = vm.aiTag && vm.userPhoto[current.id] != true
                val bmp = CardExporter.render(act) {
                    AppTheme {
                        NewsCard(
                            template = Templates.byId(current.templateId),
                            headline = current.caption,
                            brand = null,
                            modifier = Modifier.fillMaxSize(),
                            headlineFont = AppFonts.familyOf(current.fontId),
                            photo = photo,
                            aiTag = showTag,
                            info = current.info,
                            text = CardText(current.category, current.speaker, current.source),
                            logo = vm.logo,
                            rounded = false,
                        )
                    }
                }
                val uri = withContext(Dispatchers.IO) {
                    CardExporter.saveToGallery(act, bmp, "AINews_" + current.id)
                }
                bmp.recycle()
                if (uri == null) {
                    message = "ছবি সংরক্ষণ করা যায়নি।"
                } else if (share) {
                    CardExporter.share(act, uri)
                } else {
                    message = "গ্যালারিতে সংরক্ষিত হয়েছে (Pictures/AI News Studio)।"
                }
            } catch (e: Throwable) {
                message = "ফটোকার্ড তৈরি করা যায়নি। আবার চেষ্টা করুন।"
            } finally {
                busy = false
            }
        }
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val share = pendingShare
        pendingShare = null
        if (granted) {
            if (share != null) runExport(share)
        } else {
            message = "সংরক্ষণের অনুমতি দেওয়া হয়নি।"
        }
    }

    fun requestExport(share: Boolean) {
        val needsPermission = Build.VERSION.SDK_INT < 29 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingShare = share
            permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            runExport(share)
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vm.setUserPhotoFrom(context, uri)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = TextHi)
            }
            ScreenTitle("ফটোকার্ড প্রিভিউ")
        }

        if (item == null) {
            Text("প্রিভিউ করার মতো কোনো ফটোকার্ড নেই", color = TextMid)
        } else {
            val t = Templates.byId(item.templateId)
            NewsCard(
                template = t,
                headline = item.caption,
                brand = null,
                modifier = Modifier.fillMaxWidth().graphicsLayer {
                    alpha = a
                    translationY = (1f - a) * 24f
                },
                headlineFont = AppFonts.familyOf(item.fontId),
                photo = vm.photos[item.id],
                aiTag = vm.aiTag && vm.userPhoto[item.id] != true,
                info = item.info,
                text = CardText(item.category, item.speaker, item.source),
                logo = vm.logo,
            )
            Text(
                "টেমপ্লেট ${bn(t.id)} · ${t.name}",
                style = MaterialTheme.typography.titleSmall,
                color = TextHi,
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(
                    if (busy) "তৈরি হচ্ছে…" else "ডাউনলোড করুন",
                    onClick = { requestExport(false) },
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                )
                SecondaryButton(
                    "শেয়ার করুন",
                    onClick = { requestExport(true) },
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                )
            }
            val note = message
            if (note != null) {
                Text(note, style = MaterialTheme.typography.bodySmall, color = TextMid)
            } else {
                Text(
                    "ডাউনলোড করলে ছবিটি গ্যালারির Pictures/AI News Studio ফোল্ডারে ১০৮০×১৩৫০ পিক্সেলে সংরক্ষিত হবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMid,
                )
            }

            EditPanel(vm, item) { pickPhoto.launch("image/*") }
        }
    }
}

@Composable
private fun EditPanel(vm: AppViewModel, item: HistoryItem, onPickPhoto: () -> Unit) {
    val photo = vm.photos[item.id]
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle("সম্পাদনা")

        OutlinedTextField(
            value = item.caption,
            onValueChange = vm::editCaption,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("শিরোনাম বা ক্যাপশন") },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(14.dp),
            colors = editFieldColors(),
        )

        EditField("ক্যাটাগরি (ঐচ্ছিক)", item.category, vm::editCategory)
        EditField("বক্তা বা বিবৃতিদাতা (ঐচ্ছিক)", item.speaker, vm::editSpeaker)
        EditField("সূত্র (ঐচ্ছিক)", item.source, vm::editSource)

        Text("টেমপ্লেট বদলান", style = MaterialTheme.typography.titleSmall, color = TextHi)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(Templates.all, key = { it.id }) { tpl ->
                val selected = tpl.id == item.templateId
                val shape = RoundedCornerShape(10.dp)
                Column(
                    Modifier.width(80.dp).clip(shape).background(Surface1)
                        .border(if (selected) 2.dp else 1.dp, if (selected) Accent else Outline, shape)
                        .selectable(selected = selected, onClick = { vm.setTemplate(tpl.id) }, role = Role.RadioButton)
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NewsCard(
                        template = tpl,
                        headline = item.caption,
                        brand = null,
                        modifier = Modifier.fillMaxWidth(),
                        headlineFont = AppFonts.familyOf(item.fontId),
                        photo = photo,
                        info = item.info,
                text = CardText(item.category, item.speaker, item.source),
                logo = vm.logo,
                    )
                    Text(bn(tpl.id), style = MaterialTheme.typography.bodySmall, color = if (selected) TextHi else TextMid)
                }
            }
        }

        if (AppFonts.loaded) {
            Text("শিরোনামের ফন্ট", style = MaterialTheme.typography.titleSmall, color = TextHi)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppFonts.choices.forEach { c ->
                    FontChip(c.label, c.family, item.fontId == c.id) { vm.setFont(c.id) }
                }
            }
        }

        Text("ছবি", style = MaterialTheme.typography.titleSmall, color = TextHi)
        PhotoStatus(vm, item)
        SecondaryButton("গ্যালারি থেকে নিজের ছবি যোগ করুন", onClick = onPickPhoto)
        if (photo != null) {
            SecondaryButton("ছবি সরিয়ে ফেলুন", onClick = { vm.removePhoto() })
        }

        Text(
            "নিচের পট্টির তথ্য (শুধু এই কার্ডের জন্য)",
            style = MaterialTheme.typography.titleSmall,
            color = TextHi,
        )
        OutlinedTextField(
            value = item.info.channel,
            onValueChange = { vm.setInfo(item.info.copy(channel = it.take(40))) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("চ্যানেলের নাম") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = editFieldColors(),
        )
        OutlinedTextField(
            value = item.info.date,
            onValueChange = { vm.setInfo(item.info.copy(date = it.take(30))) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("তারিখ") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = editFieldColors(),
        )
        OutlinedTextField(
            value = item.info.website,
            onValueChange = { vm.setInfo(item.info.copy(website = it.take(60))) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ওয়েবসাইট") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = editFieldColors(),
        )
        Text(
            "যেটি ফাঁকা রাখবেন সেটি কার্ডে দেখাবে না।",
            style = MaterialTheme.typography.bodySmall,
            color = TextMid,
        )
    }
}

@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Accent,
    unfocusedBorderColor = Outline,
    focusedContainerColor = Surface1,
    unfocusedContainerColor = Surface1,
    cursorColor = Accent,
    focusedLabelColor = Accent,
)

@Composable
private fun FontChip(label: String, family: FontFamily?, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Text(
        label,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Accent.copy(alpha = 0.2f) else Surface1)
            .border(if (selected) 2.dp else 1.dp, if (selected) Accent else Outline, shape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = TextStyle(
            fontFamily = family ?: AppFonts.sans,
            fontSize = 16.sp,
            lineHeight = 26.sp,
            color = TextHi,
        ),
        maxLines = 1,
    )
}

@Composable
private fun PhotoStatus(vm: AppViewModel, item: HistoryItem) {
    val g = vm.gen[item.id]
    val has = vm.photos[item.id] != null
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (g) {
            Gen.Loading -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Accent, strokeWidth = 2.dp)
                Text(
                    vm.genStep[item.id]?.let { "এআই ছবি তৈরি হচ্ছে… ($it দিয়ে চেষ্টা চলছে, ধৈর্য ধরুন)" }
                        ?: "এআই ছবি তৈরি হচ্ছে… এক মিনিট পর্যন্ত লাগতে পারে",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMid,
                )
            }
            is Gen.Failed -> {
                Text(g.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                PrimaryButton("আবার চেষ্টা করুন", onClick = { vm.requestPhoto(item) })
            }
            null -> PrimaryButton(
                if (has) "এআই দিয়ে নতুন ছবি তৈরি করুন" else "এআই ছবি তৈরি করুন",
                onClick = { vm.requestPhoto(item) },
            )
        }
    }
}

@Composable
private fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = editFieldColors(),
    )
}
