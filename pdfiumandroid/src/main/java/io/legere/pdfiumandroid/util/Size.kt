package io.legere.pdfiumandroid.util

import androidx.annotation.Keep

/**
 * Size is a simple value class that represents a width and height.
 * @property width the width
 * @property height the height
 */
@Keep
data class Size(
    val width: Int,
    val height: Int,
)
