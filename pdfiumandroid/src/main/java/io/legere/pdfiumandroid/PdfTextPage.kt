@file:Suppress("unused", "MemberVisibilityCanBePrivate", "TooGenericExceptionCaught")

package io.legere.pdfiumandroid

import android.graphics.RectF
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.core.util.wrapLock
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
 * @property page the interface to the native text page object.
 */
@Suppress("TooManyFunctions")
class PdfTextPage internal constructor(
    internal val page: PdfTextPageU,
) : Closeable {
    @Deprecated(
        "Moved to io.legere.pdfiumandroid.api.FindFlags",
        ReplaceWith("io.legere.pdfiumandroid.api.FindFlags", "io.legere.pdfiumandroid.api.FindFlags"),
    )
    typealias FindFlags = io.legere.pdfiumandroid.api.FindFlags

    @Deprecated(
        "Moved to io.legere.pdfiumandroid.api.WordRangeRect",
        ReplaceWith(
            "io.legere.pdfiumandroid.api.WordRangeRect",
            "io.legere.pdfiumandroid.api.WordRangeRect",
        ),
    )
    typealias WordRangeRect = io.legere.pdfiumandroid.api.WordRangeRect

    @Volatile
    private var isClosed = false

    val pageIndex: Int
        get() = page.pageIndex

    /**
     * Get character count of the page
     * @return the number of characters on the page
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageCountChars(): Int =
        wrapLock {
            page.textPageCountChars()
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
    ): String? =
        wrapLock {
            page.textPageGetTextLegacy(startIndex, length)
        }

    @Suppress("ReturnCount")
    fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): String? =
        wrapLock {
            page.textPageGetText(startIndex, length)
        }

    /**
     * Get a unicode character on the page
     * @param index the index of the character to get
     * @return the character
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetUnicode(index: Int): Char =
        wrapLock {
            page.textPageGetUnicode(index)
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
    ): Int =
        wrapLock {
            page.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
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
    ): Int =
        wrapLock {
            page.textPageCountRects(startIndex, count)
        }

    /**
     * Get the bounding box of a text on the page
     * @param rectIndex the index of the rectangle to get
     * @return the bounding box
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("MagicNumber")
    fun textPageGetRect(rectIndex: Int): RectF? =
        wrapLock {
            page.textPageGetRect(rectIndex)
        }

    /**
     * Get the bounding box of a range of texts on the page
     * @param wordRanges an array of word ranges to get the bounding boxes for.
     * Even indices are the start index, odd indices are the length
     * @return list of bounding boxes with their start and length
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount")
    fun textPageGetRectsForRanges(wordRanges: IntArray): List<io.legere.pdfiumandroid.api.WordRangeRect>? =
        wrapLock {
            page.textPageGetRectsForRanges(wordRanges)
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
    ): String? =
        wrapLock {
            page.textPageGetBoundedText(rect, length)
        }

    /**
     * Get character font size in PostScript points (1/72th of an inch).<br></br>
     * @param charIndex the index of the character to get
     * @return the font size
     * @throws IllegalStateException if the page or document is closed
     */
    fun getFontSize(charIndex: Int): Double =
        wrapLock {
            page.getFontSize(charIndex)
        }

    fun findStart(
        findWhat: String,
        flags: Set<io.legere.pdfiumandroid.api.FindFlags>,
        startIndex: Int,
    ): FindResult? =
        wrapLock {
            page.findStart(findWhat, flags, startIndex)?.let { FindResult(it) }
        }

    fun loadWebLink(): PdfPageLink? =
        wrapLock {
            page.loadWebLink()?.let { PdfPageLink(it) }
        }

    /**
     * Close the page and release all resources
     */
    override fun close() {
        wrapLock {
            page.close()
        }
    }
}
