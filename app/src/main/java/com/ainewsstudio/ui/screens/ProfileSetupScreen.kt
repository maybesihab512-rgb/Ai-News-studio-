package com.ainewsstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ainewsstudio.data.AppViewModel
import com.ainewsstudio.ui.components.PrimaryButton
import com.ainewsstudio.ui.components.ScreenTitle
import com.ainewsstudio.ui.components.SecondaryButton
import com.ainewsstudio.ui.theme.Ink
import com.ainewsstudio.ui.theme.TextMid

/**
 * One-time creator profile, shown on first launch. Every field is optional and is saved on the
 * device as soon as it is typed; the buttons only finish the setup. It can be edited later from
 * Settings or the branding tab.
 */
@Composable
fun ProfileSetupScreen(vm: AppViewModel) {
    Column(
        Modifier.fillMaxSize().background(Ink).systemBarsPadding().imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("আপনার ক্রিয়েটর প্রোফাইল")
        Text(
            "একবার এই তথ্য সংরক্ষণ করলে প্রতিটি ফটোকার্ডে নিজে থেকে যুক্ত হবে, প্রতিবার লিখতে হবে না। সব ঘর ঐচ্ছিক, পরে Settings থেকে বদলানো যাবে।",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMid,
        )
        ProfileFields(vm, showToggles = false)
        PrimaryButton("সংরক্ষণ করে শুরু করুন", onClick = vm::markProfileDone)
        SecondaryButton("এখন বাদ দিন", onClick = vm::markProfileDone)
    }
}
