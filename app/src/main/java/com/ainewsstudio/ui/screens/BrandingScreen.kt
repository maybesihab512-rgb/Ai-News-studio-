package com.ainewsstudio.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ainewsstudio.data.AppViewModel
import com.ainewsstudio.data.PLACEHOLDER_HEADLINE
import com.ainewsstudio.data.Templates
import com.ainewsstudio.ui.components.NewsCard
import com.ainewsstudio.ui.components.ScreenTitle
import com.ainewsstudio.ui.components.SecondaryButton
import com.ainewsstudio.ui.components.SectionTitle
import com.ainewsstudio.ui.components.SettingRow
import com.ainewsstudio.ui.theme.Accent
import com.ainewsstudio.ui.theme.Outline
import com.ainewsstudio.ui.theme.Surface1
import com.ainewsstudio.ui.theme.TextMid

/** Creator profile + date options. The same profile is used on every card. */
@Composable
fun BrandingScreen(vm: AppViewModel) {
    val t = Templates.byId(vm.selectedTemplateId)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("ব্র্যান্ডিং ও প্রোফাইল")
        Text(
            "এখানে একবার তথ্য দিলে প্রতিটি ফটোকার্ডে নিজে থেকে যুক্ত হবে। সব ঘর ঐচ্ছিক, যা ফাঁকা থাকবে তা কার্ডে দেখাবে না।",
            style = MaterialTheme.typography.bodySmall,
            color = TextMid,
        )

        ProfileFields(vm, showToggles = true)

        SectionTitle("তারিখ")
        SettingRow(
            title = "কার্ডে তারিখ দেখান",
            subtitle = "কার্ডের নিচের পট্টিতে ডান পাশে থাকবে",
            checked = vm.showDate,
            onChange = vm::updateShowDate,
        )
        ProfileField(
            label = "নিজের তারিখ লিখুন (ঐচ্ছিক)",
            value = vm.customDate,
            onChange = vm::updateCustomDate,
            supporting = "ফাঁকা রাখলে আজকের তারিখ: ${vm.todayText()}",
        )

        SectionTitle("প্রিভিউ")
        NewsCard(
            template = t,
            headline = PLACEHOLDER_HEADLINE,
            brand = null,
            modifier = Modifier.fillMaxWidth(0.7f).align(Alignment.CenterHorizontally),
            info = vm.currentInfo(),
            logo = vm.logo,
        )
        Text(
            "বর্তমান টেমপ্লেট: ${t.name}",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodySmall,
            color = TextMid,
        )
    }
}

/** Logo, channel name, website, Facebook page and contact. Used by the setup screen and this tab. */
@Composable
internal fun ProfileFields(vm: AppViewModel, showToggles: Boolean) {
    val context = LocalContext.current
    val pickLogo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vm.setLogoFrom(context, uri)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle("লোগো")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val frame = RoundedCornerShape(14.dp)
            Box(
                Modifier.size(80.dp).clip(frame).background(Surface1).border(1.dp, Outline, frame),
                contentAlignment = Alignment.Center,
            ) {
                val logo = vm.logo
                if (logo != null) {
                    Image(
                        bitmap = logo,
                        contentDescription = "লোগো",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                    )
                } else {
                    Text("লোগো নেই", style = MaterialTheme.typography.bodySmall, color = TextMid)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton(
                    if (vm.logo == null) "গ্যালারি থেকে লোগো বাছুন" else "লোগো বদলান",
                    onClick = { pickLogo.launch("image/*") },
                )
                if (vm.logo != null) SecondaryButton("লোগো সরান", onClick = vm::removeLogo)
            }
        }
        if (vm.logoError) {
            Text(
                "লোগোটি পড়া যায়নি। অন্য একটি ছবি বেছে দেখুন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (showToggles && vm.logo != null) {
            SettingRow("কার্ডে লোগো দেখান", "কার্ডের নিচের পট্টিতে বাম পাশে থাকবে", vm.showLogo, vm::updateShowLogo)
        }

        SectionTitle("চ্যানেলের নাম")
        ProfileField("চ্যানেল বা পোর্টালের নাম", vm.brandName, vm::updateBrandName)
        if (showToggles) {
            SettingRow("কার্ডে নাম দেখান", "কার্ডের নিচের পট্টিতে থাকবে", vm.showBrand, vm::updateShowBrand)
        }

        SectionTitle("ওয়েবসাইট")
        ProfileField(
            "ওয়েবসাইটের ঠিকানা (ঐচ্ছিক)", vm.website, vm::updateWebsite,
            placeholder = "www.example.com", keyboardType = KeyboardType.Uri,
        )
        if (showToggles) {
            SettingRow("কার্ডে ওয়েবসাইট দেখান", "ঠিকানা ফাঁকা থাকলে কিছু দেখাবে না", vm.showWebsite, vm::updateShowWebsite)
        }

        SectionTitle("ফেসবুক পেজ")
        ProfileField("পেজের নাম (ঐচ্ছিক)", vm.facebookName, vm::updateFacebookName)
        ProfileField(
            "পেজের লিংক (ঐচ্ছিক)", vm.facebookLink, vm::updateFacebookLink,
            placeholder = "facebook.com/আপনার-পেজ", keyboardType = KeyboardType.Uri,
        )
        if (showToggles) {
            SettingRow(
                "কার্ডে ফেসবুক দেখান",
                "নাম থাকলে নাম, না থাকলে লিংক দেখাবে",
                vm.showFacebook, vm::updateShowFacebook,
            )
        }

        SectionTitle("যোগাযোগ")
        ProfileField("ফোন বা ইমেইল (ঐচ্ছিক)", vm.contact, vm::updateContact)
        if (showToggles) {
            SettingRow("কার্ডে যোগাযোগ দেখান", "কার্ডের নিচের পট্টিতে থাকবে", vm.showContact, vm::updateShowContact)
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    supporting: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { if (placeholder != null) Text(placeholder, color = TextMid) },
        supportingText = { if (supporting != null) Text(supporting) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
