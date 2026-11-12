package com.ainewsstudio.data

import androidx.compose.ui.graphics.Color

enum class TemplateLayout {
    Broadsheet, BreakingTv, PremiumGold, ModernSplit, MagazineCover,
    Sports, TvStudio, Political, EditorialLight, NightReport,
    GlobalBanner, WorldDesk, FlashUpdate, BusinessTicker, TechGlass,
    HeritagePress, DiagonalSplit, BoldBanner, Documentary, AnalysisPortrait
}

data class NewsTemplate(
    val id: Int,
    val name: String,
    val style: String,
    val layout: TemplateLayout,
    val bg: Color,
    val fg: Color,
    val accent: Color,
)

object Templates {
    val all: List<NewsTemplate> = listOf(
        NewsTemplate(1, "আন্তর্জাতিক ব্রডশিট", "আন্তর্জাতিক পত্রিকা", TemplateLayout.Broadsheet,
            Color(0xFFF4EFE6), Color(0xFF121212), Color(0xFFB3201F)),
        NewsTemplate(2, "ব্রেকিং নিউজ", "টিভি নিউজ", TemplateLayout.BreakingTv,
            Color(0xFF0A0A0F), Color.White, Color(0xFFE10600)),
        NewsTemplate(3, "প্রিমিয়াম গোল্ড", "প্রিমিয়াম আধুনিক", TemplateLayout.PremiumGold,
            Color(0xFF0E0C09), Color(0xFFF3E9D2), Color(0xFFC9A24B)),
        NewsTemplate(4, "মডার্ন স্প্লিট", "প্রিমিয়াম আধুনিক", TemplateLayout.ModernSplit,
            Color(0xFF101826), Color.White, Color(0xFF4CC9F0)),
        NewsTemplate(5, "ম্যাগাজিন কভার", "আন্তর্জাতিক পত্রিকা", TemplateLayout.MagazineCover,
            Color(0xFF1B1B1B), Color.White, Color(0xFFFFC400)),
        NewsTemplate(6, "স্পোর্টস নিউজ", "ক্রীড়া", TemplateLayout.Sports,
            Color(0xFF06130C), Color.White, Color(0xFFB6FF3B)),
        NewsTemplate(7, "টিভি স্টুডিও", "টিভি নিউজ", TemplateLayout.TvStudio,
            Color(0xFF04122B), Color.White, Color(0xFF1E88FF)),
        NewsTemplate(8, "রাজনৈতিক সংবাদ", "রাজনীতি", TemplateLayout.Political,
            Color(0xFF0F1B3D), Color.White, Color(0xFFC8102E)),
        NewsTemplate(9, "এডিটোরিয়াল লাইট", "প্রিমিয়াম আধুনিক", TemplateLayout.EditorialLight,
            Color(0xFFFAFAF7), Color(0xFF16161A), Color(0xFF0B6E4F)),
        NewsTemplate(10, "নাইট রিপোর্ট", "টিভি নিউজ", TemplateLayout.NightReport,
            Color(0xFF071A1F), Color(0xFFE8F6F4), Color(0xFF2EE6C3)),
        NewsTemplate(11, "গ্লোবাল ব্যানার", "আন্তর্জাতিক টিভি", TemplateLayout.GlobalBanner,
            Color(0xFF0A0F1A), Color.White, Color(0xFFD71920)),
        NewsTemplate(12, "ওয়ার্ল্ড ডেস্ক", "আন্তর্জাতিক পত্রিকা", TemplateLayout.WorldDesk,
            Color(0xFFFFFFFF), Color(0xFF14181F), Color(0xFFC8102E)),
        NewsTemplate(13, "ফ্ল্যাশ আপডেট", "টিভি নিউজ", TemplateLayout.FlashUpdate,
            Color(0xFF0B0B0B), Color.White, Color(0xFFFFD400)),
        NewsTemplate(14, "বিজনেস টিকার", "অর্থনীতি", TemplateLayout.BusinessTicker,
            Color(0xFF061B3A), Color.White, Color(0xFF00C2A8)),
        NewsTemplate(15, "টেক গ্লাস", "প্রিমিয়াম আধুনিক", TemplateLayout.TechGlass,
            Color(0xFF070A14), Color.White, Color(0xFF7C5CFF)),
        NewsTemplate(16, "হেরিটেজ প্রেস", "আন্তর্জাতিক পত্রিকা", TemplateLayout.HeritagePress,
            Color(0xFFF3ECDD), Color(0xFF2A1A12), Color(0xFF7B1E2B)),
        NewsTemplate(17, "ডায়াগনাল স্প্লিট", "প্রিমিয়াম আধুনিক", TemplateLayout.DiagonalSplit,
            Color(0xFF0B6E6A), Color.White, Color(0xFFFFD166)),
        NewsTemplate(18, "বোল্ড ব্যানার", "টিভি নিউজ", TemplateLayout.BoldBanner,
            Color(0xFFF5F5F5), Color(0xFF111111), Color(0xFFE30613)),
        NewsTemplate(19, "ডকুমেন্টারি", "অনুসন্ধানী", TemplateLayout.Documentary,
            Color(0xFF0A0A0A), Color.White, Color(0xFFFF6A00)),
        NewsTemplate(20, "বিশ্লেষণ পোর্ট্রেট", "বিশ্লেষণ", TemplateLayout.AnalysisPortrait,
            Color(0xFF2A0F1A), Color(0xFFFFF4E5), Color(0xFFFFB300)),
    )

    fun byId(id: Int): NewsTemplate = all.firstOrNull { it.id == id } ?: all.first()
}
