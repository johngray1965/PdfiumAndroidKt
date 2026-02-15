@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import androidx.annotation.Keep
import io.legere.pdfiumandroid.PdfTextPage
import io.legere.pdfiumandroid.api.FindFlags
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.WordRangeRect
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.core.util.wrapLock
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

/**
 * PdfTextPageKt represents a single text page of a PDF file.
 * @property page the [PdfTextPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
@Keep
class PdfTextPageKt internal constructor(
    internal val page: PdfTextPageU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    constructor(page: PdfTextPage, dispatcher: CoroutineDispatcher) : this(
        page.page,
        dispatcher,
    )

    val pageIndex: Int
        get() = page.pageIndex

    /**
     * suspend version of [PdfTextPage.textPageCountChars]
     */
    suspend fun textPageCountChars(): Int =
        wrapSuspend(dispatcher) {
            page.textPageCountChars()
        }

    /**
     * suspend version of [PdfTextPage.textPageGetText]
     */
    suspend fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): String? =
        wrapSuspend(dispatcher) {
            page.textPageGetText(startIndex, length)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetUnicode]
     */
    suspend fun textPageGetUnicode(index: Int): Char =
        wrapSuspend(dispatcher) {
            page.textPageGetUnicode(index)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetCharBox]
     */
    suspend fun textPageGetCharBox(index: Int): RectF? =
        wrapSuspend(dispatcher) {
            page.textPageGetCharBox(index)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetCharIndexAtPos]
     */
    suspend fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ): Int =
        wrapSuspend(dispatcher) {
            page.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
        }

    /**
     * suspend version of [PdfTextPage.textPageCountRects]
     */
    suspend fun textPageCountRects(
        startIndex: Int,
        count: Int,
    ): Int =
        wrapSuspend(dispatcher) {
            page.textPageCountRects(startIndex, count)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetRect]
     */
    suspend fun textPageGetRect(rectIndex: Int): RectF? =
        wrapSuspend(dispatcher) {
            page.textPageGetRect(rectIndex)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetRectsForRanges]
     */
    suspend fun textPageGetRectsForRanges(wordRanges: IntArray): List<WordRangeRect>? =
        wrapSuspend(dispatcher) {
            page.textPageGetRectsForRanges(wordRanges)
        }

    /**
     * suspend version of [PdfTextPage.textPageGetBoundedText]
     */
    suspend fun textPageGetBoundedText(
        rect: RectF,
        length: Int,
    ): String? =
        wrapSuspend(dispatcher) {
            page.textPageGetBoundedText(rect, length)
        }

    /**
     * suspend version of [PdfTextPage.getFontSize]
     */
    suspend fun getFontSize(charIndex: Int): Double =
        wrapSuspend(dispatcher) {
            page.getFontSize(charIndex)
        }

    suspend fun findStart(
        findWhat: String,
        flags: Set<FindFlags>,
        startIndex: Int,
    ): FindResultKt? =
        wrapSuspend(dispatcher) {
            val findResult = page.findStart(findWhat, flags, startIndex)
            if (findResult == null) {
                null
            } else {
                FindResultKt(findResult, dispatcher)
            }
        }

    suspend fun loadWebLink(): PdfPageLinkKt? =
        wrapSuspend(dispatcher) {
            page.loadWebLink()?.let {
                PdfPageLinkKt(it, dispatcher)
            }
        }

    /**
     * Close the page and free all resources.
     */
    override fun close() {
        wrapLock {
            page.close()
        }
    }

    fun safeClose(): Boolean =
        try {
            wrapLock {
                page.close()
            }
            true
        } catch (e: IllegalStateException) {
            Logger.e("PdfTextPageKt", e, "PdfTextPageKt.safeClose")
            false
        }
}
