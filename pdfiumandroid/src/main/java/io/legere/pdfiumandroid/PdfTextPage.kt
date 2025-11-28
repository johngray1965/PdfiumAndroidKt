@file:Suppress("unused", "MemberVisibilityCanBePrivate", "TooGenericExceptionCaught")

package io.legere.pdfiumandroid

import android.graphics.RectF
import io.legere.pdfiumandroid.unlocked.PdfTextPageU
import java.io.Closeable

typealias FindHandle = Long

private const val LEFT_OFFSET = 0
private const val TOP_OFFSET = 1
private const val RIGHT_OFFSET = 2
private const val BOTTOM_OFFSET = 3
private const val RANGE_START_OFFSET = 4
private const val RANGE_LENGTH_OFFSET = 5

private const val RANGE_RECT_DATA_SIZE = 6

/**
 * PdfTextPage is a wrapper around the native PdfiumCore text page
 * It is used to get text and other information about the text on a page
 * @property doc the PdfDocument this page belongs to
 * @property pageIndex the index of this page in the document
 * @property pagePtr the pointer to the native page
 */
@Suppress("TooManyFunctions")
class PdfTextPage(
    val page: PdfTextPageU,
) : Closeable {
    @Volatile
    private var isClosed = false

    /**
     * Get character count of the page
     * @return the number of characters on the page
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageCountChars(): Int {
        synchronized(PdfiumCore.lock) {
            return page.textPageCountChars()
        }
    }

    /**
     * Get the text on the page
     * @param startIndex the index of the first character to get
     * @param length the number of characters to get
     * @return the text
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount")
    fun textPageGetTextLegacy(
        startIndex: Int,
        length: Int,
    ): String? {
        synchronized(PdfiumCore.lock) {
            return page.textPageGetTextLegacy(startIndex, length)
        }
    }

    @Suppress("ReturnCount")
    fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): String? {
        synchronized(PdfiumCore.lock) {
            return page.textPageGetText(startIndex, length)
        }
    }

    /**
     * Get a unicode character on the page
     * @param index the index of the character to get
     * @return the character
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetUnicode(index: Int): Char {
        synchronized(PdfiumCore.lock) {
            return page.textPageGetUnicode(index)
        }
    }

    /**
     * Get the bounding box of a character on the page
     * @param index the index of the character to get
     * @return the bounding box
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount", "MagicNumber")
    fun textPageGetCharBox(index: Int): RectF? = page.textPageGetCharBox(index)

    /**
     * Get the index of the character at a given position on the page
     * @param x the x position
     * @param y the y position
     * @param xTolerance the x tolerance
     * @param yTolerance the y tolerance
     * @return the index of the character at the position
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount")
    fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ): Int {
        synchronized(PdfiumCore.lock) {
            return page.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
        }
    }

    /**
     * Get the count of rectangles that bound the text on the page in a given range
     * @param startIndex the index of the first character to get
     * @param count the number of characters to get
     * @return the number of rectangles
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageCountRects(
        startIndex: Int,
        count: Int,
    ): Int {
        synchronized(PdfiumCore.lock) {
            return page.textPageCountRects(startIndex, count)
        }
    }

    /**
     * Get the bounding box of a text on the page
     * @param rectIndex the index of the rectangle to get
     * @return the bounding box
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("MagicNumber")
    fun textPageGetRect(rectIndex: Int): RectF? {
        synchronized(PdfiumCore.lock) {
            return page.textPageGetRect(rectIndex)
        }
    }

    /**
     * Get the bounding box of a range of texts on the page
     * @param wordRanges an array of word ranges to get the bounding boxes for.
     * Even indices are the start index, odd indices are the length
     * @return list of bounding boxes with their start and length
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount")
    fun textPageGetRectsForRanges(wordRanges: IntArray): List<WordRangeRect>? {
        synchronized(PdfiumCore.lock) {
            return page.textPageGetRectsForRanges(wordRanges)
        }
    }

    /**
     * Get the text bounded by the given rectangle
     * @param rect the rectangle to bound the text
     * @param length the maximum number of characters to get
     * @return the text bounded by the rectangle
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetBoundedText(
        rect: RectF,
        length: Int,
    ): String? {
        synchronized(PdfiumCore.lock) {
            return page.textPageGetBoundedText(rect, length)
        }
    }

    /**
     * Get character font size in PostScript points (1/72th of an inch).<br></br>
     * @param charIndex the index of the character to get
     * @return the font size
     * @throws IllegalStateException if the page or document is closed
     */
    fun getFontSize(charIndex: Int): Double {
        synchronized(PdfiumCore.lock) {
            return page.getFontSize(charIndex)
        }
    }

    fun findStart(
        findWhat: String,
        flags: Set<FindFlags>,
        startIndex: Int,
    ): FindResult? {
        synchronized(PdfiumCore.lock) {
            return page.findStart(findWhat, flags, startIndex)?.let { FindResult(it) }
        }
    }

    fun loadWebLink(): PdfPageLink? {
        synchronized(PdfiumCore.lock) {
            return page.loadWebLink()?.let { PdfPageLink(it) }
        }
    }

    /**
     * Close the page and release all resources
     */
    override fun close() {
        synchronized(PdfiumCore.lock) {
            page.close()
        }
    }
}

@Suppress("MagicNumber")
enum class FindFlags(
    val value: Int,
) {
    MatchCase(0x00000001),
    MatchWholeWord(0x00000002),
    Consecutive(0x00000004),
}

data class WordRangeRect(
    val rangeStart: Int,
    val rangeLength: Int,
    val rect: RectF,
)
