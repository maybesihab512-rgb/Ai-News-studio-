package com.ainewsstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ainewsstudio.BuildConfig
import com.ainewsstudio.data.AppViewModel
import com.ainewsstudio.ui.components.ScreenTitle
import com.ainewsstudio.ui.components.SecondaryButton
import com.ainewsstudio.ui.components.SectionTitle
import com.ainewsstudio.ui.components.SettingRow
import com.ainewsstudio.ui.theme.Accent
import com.ainewsstudio.ui.theme.Outline
import com.ainewsstudio.ui.theme.Surface1
import com.ainewsstudio.ui.theme.TextHi
import com.ainewsstudio.ui.theme.TextMid

@Composable
fun SettingsScreen(vm: AppViewModel, onEditProfile: () -> Unit) {
    val provider = vm.aiProvider
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("সেটিংস")

        SectionTitle("ক্রিয়েটর প্রোফাইল")
        InfoCard(
            if (vm.brandName.isNotBlank()) vm.brandName else "প্রোফাইলে এখনো তথ্য দেওয়া হয়নি",
            "চ্যানেলের নাম, লোগো, ওয়েবসাইট, ফেসবুক পেজ ও যোগাযোগ একবার সংরক্ষণ করলে প্রতিটি ফটোকার্ডে নিজে থেকে যুক্ত হয়।",
        )
        SecondaryButton("প্রোফাইল সম্পাদনা করুন", onClick = onEditProfile)

        SectionTitle("এআই ছবি")
        SettingRow(
            title = "ক্যাপশন থেকে এআই ছবি",
            subtitle = "চালু থাকলে ফটোকার্ড তৈরির সময় নিজে থেকে ছবি তৈরি হবে",
            checked = vm.aiEnabled,
            onChange = vm::updateAiEnabled,
        )

        Text("ছবি তৈরির সার্ভিস", style = MaterialTheme.typography.titleSmall, color = TextHi)
        ProviderRow(
            title = "অটো (প্রস্তাবিত)",
            subtitle = "অ্যাপ নিজেই একের পর এক সার্ভিস চেষ্টা করবে: Cloudflare, Gemini (key থাকলে), Pollinations, AI Horde। একটি আটকে গেলে পরেরটি চলবে।",
            selected = provider == AppViewModel.PROVIDER_AUTO,
            onClick = { vm.updateAiProvider(AppViewModel.PROVIDER_AUTO) },
        )
        ProviderRow(
            title = "Cloudflare (ফ্রি, সবচেয়ে নির্ভরযোগ্য)",
            subtitle = "ফ্রি অ্যাকাউন্ট ও টোকেন লাগে। দিনে প্রায় ২০০টির বেশি ছবি, মান ভালো।",
            selected = provider == AppViewModel.PROVIDER_CLOUDFLARE,
            onClick = { vm.updateAiProvider(AppViewModel.PROVIDER_CLOUDFLARE) },
        )
        ProviderRow(
            title = "Pollinations (ফ্রি)",
            subtitle = "key লাগে না। প্রতি ১৫ সেকেন্ডে একটি অনুরোধ, ভিড় থাকলে আটকাতে পারে।",
            selected = provider == AppViewModel.PROVIDER_POLLINATIONS,
            onClick = { vm.updateAiProvider(AppViewModel.PROVIDER_POLLINATIONS) },
        )
        ProviderRow(
            title = "AI Horde (ফ্রি, ধীর)",
            subtitle = "অ্যাকাউন্ট লাগে না। লাইনে দাঁড়াতে হয়, ছবি আসতে কয়েক মিনিট লাগতে পারে।",
            selected = provider == AppViewModel.PROVIDER_HORDE,
            onClick = { vm.updateAiProvider(AppViewModel.PROVIDER_HORDE) },
        )
        ProviderRow(
            title = "Gemini (গুগল)",
            subtitle = "ছবির জন্য গুগলের পেইড প্ল্যান লাগে। ফ্রি key-তে কোটা শূন্য থাকে।",
            selected = provider == AppViewModel.PROVIDER_GEMINI,
            onClick = { vm.updateAiProvider(AppViewModel.PROVIDER_GEMINI) },
        )

        if (BuildConfig.CF_TOKEN.isNotBlank()) {
            InfoCard("Cloudflare যুক্ত আছে", "এই অ্যাপ তৈরির সময়ই Cloudflare যুক্ত হয়েছে, তাই নিচের ঘর ফাঁকা রাখলেও চলবে।")
        }
        if (provider == AppViewModel.PROVIDER_CLOUDFLARE || provider == AppViewModel.PROVIDER_AUTO) {
            OutlinedTextField(
                value = vm.cfAccount,
                onValueChange = vm::updateCfAccount,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cloudflare Account ID") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
            )
            OutlinedTextField(
                value = vm.cfToken,
                onValueChange = vm::updateCfToken,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cloudflare API টোকেন") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
            )
            InfoCard(
                "Cloudflare টোকেন কীভাবে পাবেন",
                "dash.cloudflare.com-এ লগইন করে AI → Workers AI খুলুন। সেখানে “Use REST API” চাপুন, “Create a Workers AI API token” দিয়ে টোকেন বানিয়ে কপি করুন (একবারই দেখা যায়)। একই পাতা থেকে Account ID কপি করুন।",
            )
        }

        OutlinedTextField(
            value = vm.apiKey,
            onValueChange = vm::updateApiKey,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Gemini API key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = {
                Text(
                    if (provider == AppViewModel.PROVIDER_GEMINI) "aistudio.google.com/apikey থেকে key নেওয়া যায়"
                    else "ঐচ্ছিক: দিলে বাংলা ক্যাপশন আরও ভালোভাবে ইংরেজিতে অনুবাদ হবে (ফ্রি key-তেও চলে)",
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
        )
        if (provider == AppViewModel.PROVIDER_GEMINI || provider == AppViewModel.PROVIDER_AUTO) {
            OutlinedTextField(
                value = vm.aiModel,
                onValueChange = vm::updateAiModel,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("মডেলের নাম") },
                singleLine = true,
                supportingText = { Text("গুগল মডেলের নাম বদলালে এখানে ঠিক করে নিন") },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
            )
        }
        SettingRow(
            title = "কার্ডে “এআই ছবি” ট্যাগ",
            subtitle = "এআই-তৈরি ছবি চেনাতে কার্ডের কোণায় ছোট ট্যাগ থাকবে",
            checked = vm.aiTag,
            onChange = vm::updateAiTag,
        )
        InfoCard(
            "গোপনীয়তা",
            "এআই ছবি তৈরির সময় আপনার ক্যাপশন নির্বাচিত সার্ভিসে (Cloudflare, Pollinations, AI Horde বা গুগল) পাঠানো হয়। key ও টোকেন শুধু এই ফোনে সংরক্ষিত থাকে।",
        )

        SectionTitle("সাধারণ")
        SettingRow(
            title = "অ্যানিমেশন",
            subtitle = "বন্ধ করলে অ্যাপ কম মেমোরি ও ব্যাটারি ব্যবহার করবে",
            checked = vm.animations,
            onChange = { vm.animations = it },
        )
        InfoCard("অ্যাপ সম্পর্কে", "AI News Studio · সংস্করণ ০.৯.০")
    }
}

@Composable
private fun ProviderRow(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(Surface1)
            .border(if (selected) 2.dp else 1.dp, if (selected) Accent else Outline, shape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = Accent),
        )
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextHi)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMid)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Accent,
    unfocusedBorderColor = Outline,
    focusedContainerColor = Surface1,
    unfocusedContainerColor = Surface1,
    cursorColor = Accent,
    focusedLabelColor = Accent,
)

@Composable
private fun InfoCard(title: String, body: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface1).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = TextHi)
        Text(body, style = MaterialTheme.typography.bodySmall, color = TextMid)
    }
}
