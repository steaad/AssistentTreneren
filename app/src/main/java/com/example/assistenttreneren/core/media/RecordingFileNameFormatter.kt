package com.example.assistenttreneren.core.media

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class RecordingFileNameFormatter @Inject constructor() {
    private val timeFormatter = SimpleDateFormat("HHmmss", Locale.US)
    private val dateFormatter = SimpleDateFormat("ddMMyy", Locale.US)

    fun format(
        category: String,
        subCategory: String,
        createdAtMillis: Long,
    ): String {
        val createdAt = Date(createdAtMillis)
        return buildString {
            append(category.toFilePart())
            append("_")
            append(subCategory.toFilePart())
            append("_")
            append(timeFormatter.format(createdAt))
            append("_")
            append(dateFormatter.format(createdAt))
            append(".m4a")
        }
    }

    private fun String.toFilePart(): String =
        lowercase(Locale.ROOT)
            .replace("æ", "ae")
            .replace("ø", "o")
            .replace("å", "a")
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "opptak" }
}
