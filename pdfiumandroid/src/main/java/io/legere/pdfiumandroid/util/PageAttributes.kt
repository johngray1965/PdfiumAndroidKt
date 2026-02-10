package io.legere.pdfiumandroid.util

import android.graphics.Matrix
import android.graphics.RectF
import androidx.annotation.Keep
import io.legere.pdfiumandroid.PdfDocument

/**
 * PageAttributes contains various attributes of a PDF page.
 */
@Keep
data class PageAttributes(
    val pageWidth: Int,
    val pageHeight: Int,
    val pageRotation: Int,
    val mediaBox: RectF,
    val cropBox: RectF,
    val bleedBox: RectF,
    val trimBox: RectF,
    val artBox: RectF,
    val boundingBox: RectF,
    val pageMatrix: Matrix,
    val links: List<PdfDocument.Link>
)
