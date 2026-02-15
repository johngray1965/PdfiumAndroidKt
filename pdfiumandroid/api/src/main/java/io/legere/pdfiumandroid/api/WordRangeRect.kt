package io.legere.pdfiumandroid.api

import android.graphics.RectF

data class WordRangeRect(
    val rangeStart: Int,
    val rangeLength: Int,
    val rect: RectF,
)
