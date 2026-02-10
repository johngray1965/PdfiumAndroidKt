package io.legere.pdfiumandroid

import android.graphics.Matrix
import android.graphics.RectF
import androidx.annotation.Keep

/**
 * PageAttributes contains various attributes of a PDF page.
 */
@Keep
data class PageAttributes(
    val page: Int,
    val pageWidth: Int,
    val pageHeight: Int,
    val pageRotation: Int,
    val rect: RectF,
    val mediaBox: RectF,
    val cropBox: RectF,
    val bleedBox: RectF,
    val trimBox: RectF,
    val artBox: RectF,
    val boundingBox: RectF,
    val links: List<PdfDocument.Link>,
    val pageMatrix: Matrix,
) {
    companion object {
        /**
         * An empty PageInfo object.
         */
        val EMPTY =
            PageAttributes(
                page = -1,
                pageWidth = 0,
                pageHeight = 0,
                pageRotation = 0,
                rect = RectF(),
                mediaBox = RectF(),
                cropBox = RectF(),
                bleedBox = RectF(),
                trimBox = RectF(),
                artBox = RectF(),
                boundingBox = RectF(),
                links = emptyList(),
                pageMatrix = Matrix(),
            )
    }
}
