@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import androidx.annotation.Keep
import io.legere.pdfiumandroid.FindFlags
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfTextPage
import io.legere.pdfiumandroid.WordRangeRect
import io.legere.pdfiumandroid.suspend.PdfiumCoreKt.Companion.mutex
import io.legere.pdfiumandroid.unlocked.PdfTextPageU
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfTextPageKt represents a single text page of a PDF file.
 * @property page the [PdfTextPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
@Keep
class PdfTextPageKt(
    val page: PdfTextPageU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    /**
     * suspend version of [PdfTextPage.textPageCountChars]
     */
    suspend fun textPageCountChars(): Int =
        mutex.withLock {
            withContext(dispatcher) {
                page.textPageCountChars()
            }
        }

    /**
     * suspend version of [PdfTextPage.textPageGetText]
     */
    suspend fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): String? =
        mutex.withLock {
            withContext(dispatcher) {
                page.textPageGetText(startIndex, length)
            }
        }

    /**
     * suspend version of [PdfTextPage.textPageGetUnicode]
     */
    suspend fun textPageGetUnicode(index: Int): Char =
        mutex.withLock {
            withContext(dispatcher) {
                page.textPageGetUnicode(index)
            }
        }

    /**
     * suspend version of [PdfTextPage.textPageGetCharBox]
     */
    suspend fun textPageGetCharBox(index: Int): RectF? =
        mutex.withLock {
            withContext(dispatcher) {
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
    ): Int =
        mutex.withLock {
            withContext(dispatcher) {
                page.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
            }
        }

    /**
     * suspend version of [PdfTextPage.textPageCountRects]
     */
    suspend fun textPageCountRects(
        startIndex: Int,
        count: Int,
    ): Int =
        mutex.withLock {
            withContext(dispatcher) {
                page.textPageCountRects(startIndex, count)
            }
        }

    /**
     * suspend version of [PdfTextPage.textPageGetRect]
     */
    suspend fun textPageGetRect(rectIndex: Int): RectF? =
        mutex.withLock {
            withContext(dispatcher) {
                page.textPageGetRect(rectIndex)
            }
        }

    /**
     * suspend version of [PdfTextPage.textPageGetRectsForRanges]
     */
    suspend fun textPageGetRectsForRanges(wordRanges: IntArray): List<WordRangeRect>? =
        mutex.withLock {
            withContext(dispatcher) {
                page.textPageGetRectsForRanges(wordRanges)
            }
        }

    /**
     * suspend version of [PdfTextPage.textPageGetBoundedText]
     */
    suspend fun textPageGetBoundedText(
        rect: RectF,
        length: Int,
    ): String? =
        mutex.withLock {
            withContext(dispatcher) {
                page.textPageGetBoundedText(rect, length)
            }
        }

    /**
     * suspend version of [PdfTextPage.getFontSize]
     */
    suspend fun getFontSize(charIndex: Int): Double =
        mutex.withLock {
            withContext(dispatcher) {
                page.getFontSize(charIndex)
            }
        }

    suspend fun findStart(
        findWhat: String,
        flags: Set<FindFlags>,
        startIndex: Int,
    ): FindResultKt? =
        mutex.withLock {
            withContext(dispatcher) {
                val findResult = page.findStart(findWhat, flags, startIndex)
                if (findResult == null) {
                    null
                } else {
                    FindResultKt(findResult, dispatcher)
                }
            }
        }

    suspend fun loadWebLink(): PdfPageLinkKt =
        mutex.withLock {
            withContext(dispatcher) {
                PdfPageLinkKt(page.loadWebLink(), dispatcher)
            }
        }

    /**
     * Close the page and free all resources.
     */
    override fun close() {
        page.close()
    }

    fun safeClose(): Boolean =
        try {
            page.close()
            true
        } catch (e: IllegalStateException) {
            Logger.e("PdfTextPageKt", e, "PdfTextPageKt.safeClose")
            false
        }
}
