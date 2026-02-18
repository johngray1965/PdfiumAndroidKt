/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.core.util.wrapLock
import java.io.Closeable

private const val LEFT = 0
private const val TOP = 1
private const val RIGHT = 2
private const val BOTTOM = 3

private const val RECT_SIZE = 4

/**
 * Represents a text layer of a single page in a [PdfDocument].
 */
@Suppress("TooManyFunctions")
class PdfTextPage internal constructor(
    internal val textPage: PdfTextPageU,
) : Closeable {
    @Deprecated(
        "Moved to io.legere.pdfiumandroid.api.FindFlags",
        ReplaceWith("FindFlags", "io.legere.pdfiumandroid.api.FindFlags"),
    )
    typealias FindFlags = io.legere.pdfiumandroid.api.FindFlags

    @Deprecated(
        "Moved to io.legere.pdfiumandroid.api.WordRangeRect",
        ReplaceWith(
            "WordRangeRect",
            "io.legere.pdfiumandroid.api.WordRangeRect",
        ),
    )
    typealias WordRangeRect = io.legere.pdfiumandroid.api.WordRangeRect

    val pageIndex: Int
        get() = textPage.pageIndex

    /**
     * Get character count of the page
     * @return the number of characters on the page
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageCountChars(): Int =
        wrapLock {
            textPage.textPageCountChars()
        }

    /**
     * Get the text on the page using a legacy method.
     * Prefer `textPageGetText`.
     * @param startIndex the index of the first character to get
     * @param length the number of characters to get
     * @return the text
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetTextLegacy(
        startIndex: Int,
        length: Int,
    ): String? =
        wrapLock {
            textPage.textPageGetTextLegacy(startIndex, length)
        }

    /**
     * Get the text on the page.
     * @param startIndex the index of the first character to get
     * @param length the number of characters to get
     * @return the text
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): String? =
        wrapLock {
            textPage.textPageGetText(startIndex, length)
        }

    /**
     * Get a unicode character on the page.
     * @param index the index of the character to get
     * @return the character
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetUnicode(index: Int): Char =
        wrapLock {
            textPage.textPageGetUnicode(index)
        }

    /**
     * Get the bounding box of a character on the page.
     * @param index the index of the character to get
     * @return the bounding box as a [PdfRectF], or `null` if an error occurs
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetCharBox(index: Int): PdfRectF? =
        wrapLock {
            textPage.textPageGetCharBox(index)
        }

    /**
     * Get the index of the character at a given position on the page.
     * @param x the x position in page coordinates
     * @param y the y position in page coordinates
     * @param xTolerance the x tolerance
     * @param yTolerance the y tolerance
     * @return the 0-based index of the character at the position, or -1 if no character is found
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ): Int =
        wrapLock {
            textPage.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
        }

    /**
     * Get the count of rectangles that bound the text on the page in a given range.
     * @param startIndex the 0-based index of the first character to get
     * @param count the number of characters to get
     * @return the number of rectangles
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageCountRects(
        startIndex: Int,
        count: Int,
    ): Int =
        wrapLock {
            textPage.textPageCountRects(startIndex, count)
        }

    /**
     * Get the bounding box of a text rectangle on the page.
     * @param rectIndex the 0-based index of the rectangle to get
     * @return the bounding box as a [PdfRectF], or `null` if an error occurs
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetRect(rectIndex: Int): PdfRectF? =
        wrapLock {
            textPage.textPageGetRect(rectIndex)
        }

    /**
     * Get the bounding boxes of a range of texts on the page.
     * @param wordRanges an array of word ranges to get the bounding boxes for.
     *                    Even indices are the start index, odd indices are the length.
     * @return a list of [io.legere.pdfiumandroid.api.WordRangeRect] containing bounding boxes
     * with their start and length or `null` if an error occurs.
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetRectsForRanges(wordRanges: IntArray): List<WordRangeRect>? =
        wrapLock {
            textPage.textPageGetRectsForRanges(wordRanges)
        }

    /**
     * Get the text bounded by the given rectangle.
     * @param rect the rectangle to bound the text
     * @param length the maximum number of characters to get
     * @return the text bounded by the rectangle as a [String], or `null` if an error occurs.
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetBoundedText(
        rect: PdfRectF,
        length: Int,
    ): String? =
        wrapLock {
            textPage.textPageGetBoundedText(rect, length)
        }

    /**
     * Get character font size in PostScript points (1/72th of an inch).
     * @param charIndex the index of the character to get
     * @return the font size
     * @throws IllegalStateException if the page or document is closed
     */
    fun getFontSize(charIndex: Int): Double =
        wrapLock {
            textPage.getFontSize(charIndex)
        }

    /**
     * Initiates a text search operation on the page.
     * @param findWhat The string to search for.
     * @param flags A set of [io.legere.pdfiumandroid.api.FindFlags] to control the search behavior.
     * @param startIndex The 0-based index to start the search from.
     * @return A [FindResult] object representing the search session, or `null` if the page or document is closed.
     * @throws IllegalStateException if the page or document is closed.
     */
    fun findStart(
        findWhat: String,
        flags: Set<FindFlags>,
        startIndex: Int,
    ): FindResult? =
        wrapLock {
            textPage.findStart(findWhat, flags, startIndex)?.let { FindResult(it) }
        }

    /**
     * Loads web links from the text page.
     * @return A [PdfPageLink] object containing the web links, or `null` if the page or document is closed.
     * @throws IllegalStateException if the page or document is closed.
     */
    fun loadWebLink(): PdfPageLink? =
        wrapLock {
            textPage.loadWebLink()?.let { PdfPageLink(it) }
        }

    /**
     * Close the text page and release all resources
     */
    override fun close() {
        wrapLock {
            textPage.close()
        }
    }
}
