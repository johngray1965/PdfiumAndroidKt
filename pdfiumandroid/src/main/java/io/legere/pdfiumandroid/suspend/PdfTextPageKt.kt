@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfTextPage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfTextPageKt represents a single text page of a PDF file.
 * @property page the [PdfTextPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
class PdfTextPageKt(val page: PdfTextPage, private val dispatcher: CoroutineDispatcher) : Closeable {
    /**
     * suspend version of [PdfTextPage.textPageCountChars]
     */
    suspend fun textPageCountChars(): Int {
        return withContext(dispatcher) {
            page.textPageCountChars()
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetText]
     */
    suspend fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): String? {
        return withContext(dispatcher) {
            page.textPageGetText(startIndex, length)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetUnicode]
     */
    suspend fun textPageGetUnicode(index: Int): Char {
        return withContext(dispatcher) {
            page.textPageGetUnicode(index)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetCharBox]
     */
    suspend fun textPageGetCharBox(index: Int): RectF? {
        return withContext(dispatcher) {
            page.textPageGetCharBox(index)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetCharIndexAtPos]
     */
    suspend fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ): Int {
        return withContext(dispatcher) {
            page.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageCountRects]
     */
    suspend fun textPageCountRects(
        startIndex: Int,
        count: Int,
    ): Int {
        return withContext(dispatcher) {
            page.textPageCountRects(startIndex, count)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetRect]
     */
    suspend fun textPageGetRect(rectIndex: Int): RectF? {
        return withContext(dispatcher) {
            page.textPageGetRect(rectIndex)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetBoundedText]
     */
    suspend fun textPageGetBoundedText(
        rect: RectF,
        length: Int,
    ): String? {
        return withContext(dispatcher) {
            page.textPageGetBoundedText(rect, length)
        }
    }

    /**
     * suspend version of [PdfTextPage.getFontSize]
     */
    suspend fun getFontSize(charIndex: Int): Double {
        return withContext(dispatcher) {
            page.getFontSize(charIndex)
        }
    }

    /**
     * Close the page and free all resources.
     */
    override fun close() {
        page.close()
    }

    fun safeClose(): Boolean {
        return try {
            page.close()
            true
        } catch (e: IllegalStateException) {
            Logger.e("PdfTextPageKt", e, "PdfTextPageKt.safeClose")
            false
        }
    }
}
