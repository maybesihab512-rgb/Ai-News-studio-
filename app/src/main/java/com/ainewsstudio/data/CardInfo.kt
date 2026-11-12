package com.ainewsstudio.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Footer line of a photo card (from the creator profile). Blank means hidden. */
data class CardInfo(
    val channel: String = "",
    val date: String = "",
    val website: String = "",
    val facebook: String = "",
    val contact: String = "",
    val logo: Boolean = false,
)

/** Optional texts the user types for a card. Nothing is pre-filled; blank parts are not drawn. */
data class CardText(
    val category: String = "",
    val speaker: String = "",
    val source: String = "",
)

/** Neutral hint shown inside template previews until the user types a headline. */
const val PLACEHOLDER_HEADLINE = "আপনার শিরোনাম এখানে দেখাবে"

object DateText {
    fun digits(s: String): String = buildString {
        for (c in s) append(if (c in '0'..'9') "০১২৩৪৫৬৭৮৯"[c - '0'] else c)
    }

    /** e.g. ১০ নভেম্বর ২০২৬ */
    fun format(millis: Long): String =
        digits(SimpleDateFormat("d MMMM yyyy", Locale("bn", "BD")).format(Date(millis)))
}
