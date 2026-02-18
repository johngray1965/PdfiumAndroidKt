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

@file:Suppress("unused", "MemberVisibilityCanBePrivate", "TooGenericExceptionCaught")

package io.legere.pdfiumandroid.core.unlocked

import io.legere.pdfiumandroid.api.FindFlags
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.WordRangeRect
import io.legere.pdfiumandroid.api.handleAlreadyClosed
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.NativeTextPageContract
import io.legere.pdfiumandroid.core.jni.defaultNativeFactory
import io.legere.pdfiumandroid.core.util.PageCount
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

typealias FindHandle = Long

private const val LEFT_OFFSET = 0
private const val TOP_OFFSET = 1
private const val RIGHT_OFFSET = 2
private const val BOTTOM_OFFSET = 3
private const val RANGE_START_OFFSET = 4
private const val RANGE_LENGTH_OFFSET = 5

private const val RANGE_RECT_DATA_SIZE = 6

/**
 * Represents an **unlocked** text layer of a single page in a [PdfDocumentU].
 * This class is for **internal use only** within the PdfiumAndroid library.
 * Direct use from outside the library is not recommended as it bypasses thread-safety mechanisms.
 *
 * @property doc The parent [PdfDocumentU] this text page belongs to.
 * @property pageIndex The 0-based index of the page this text layer corresponds to.
 * @property pagePtr The native pointer to the FPDF_TEXTPAGE object.
 * @property pageMap A mutable map used internally to track open page counts for the parent document.
 * @property nativeTextPage The native interface for text page operations.
 */
@Suppress("TooManyFunctions")
class PdfTextPageU(
    val doc: PdfDocumentU,
    val pageIndex: Int,
    val pagePtr: Long,
    val pageMap: MutableMap<Int, PageCount>,
    nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    @Volatile
    private var isClosed = false

    val nativeTextPage: NativeTextPageContract = nativeFactory.getNativeTextPage()

    /**
     * Get character count of the page.
     * For internal use only.
     *
     * @return the number of characters on the page
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageCountChars(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativeTextPage.textCountChars(pagePtr)
    }

    /**
     * Get the text on the page using a legacy method.
     * For internal use only. Prefer `textPageGetText`.
     *
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
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        try {
            println("textPageGetTextLegacy $startIndex $length")
            val buf = ShortArray(length + 1)
            val r =
                nativeTextPage.textGetText(
                    pagePtr,
                    startIndex,
                    length,
                    buf,
                )

            println("textPageGetTextLegacy $r")
            if (r <= 0) {
                return ""
            }

            val bytes = ByteArray((r - 1) * 2)
            val bb = ByteBuffer.wrap(bytes)
            bb.order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until r - 1) {
                val s = buf[i]
                bb.putShort(s)
            }
            return String(bytes, StandardCharsets.UTF_16LE)
        } catch (e: NullPointerException) {
            println("textPageGetTextLegacy $e")
            Logger.e(TAG, e, "mContext may be null")
        } catch (e: Exception) {
            println("textPageGetTextLegacy $e")
            Logger.e(TAG, e, "Exception throw from native")
        }
        println("textPageGetTextLegacy null")
        return null
    }

    /**
     * Get the text on the page.
     * For internal use only.
     *
     * @param startIndex the index of the first character to get
     * @param length the number of characters to get
     * @return the text
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount")
    fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): String? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        try {
            return nativeTextPage.textGetTextString(
                pagePtr,
                startIndex,
                length,
            )
        } catch (e: NullPointerException) {
            println("textPageGetText $e")
            Logger.e(TAG, e, "mContext may be null")
        } catch (e: Exception) {
            println("textPageGetText $e")
            Logger.e(TAG, e, "Exception throw from native")
        }
        return null
    }

    /**
     * Get a unicode character on the page.
     * For internal use only.
     *
     * @param index the index of the character to get
     * @return the character
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetUnicode(index: Int): Char {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return Char.MIN_VALUE
        return nativeTextPage
            .textGetUnicode(
                pagePtr,
                index,
            ).toChar()
    }

    /**
     * Get the bounding box of a character on the page.
     * For internal use only.
     *
     * @param index the index of the character to get
     * @return the bounding box as a [PdfRectF], or `null` if an error occurs
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount", "MagicNumber")
    fun textPageGetCharBox(index: Int): PdfRectF? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        try {
            val o = nativeTextPage.textGetCharBox(pagePtr, index)
            // Note these are in an odd order left, right, bottom, top
            // what what Pdfium native code returns
            return PdfRectF(
                o[0].toFloat(),
                o[3].toFloat(),
                o[1].toFloat(),
                o[2].toFloat(),
            )
        } catch (e: NullPointerException) {
            Logger.e(TAG, e, "mContext may be null")
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
        }
        return null
    }

    /**
     * Get the index of the character at a given position on the page.
     * For internal use only.
     *
     * @param x the x position in page coordinates
     * @param y the y position in page coordinates
     * @param xTolerance the x tolerance
     * @param yTolerance the y tolerance
     * @return the 0-based index of the character at the position, or -1 if no character is found
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount")
    fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        try {
            return nativeTextPage.textGetCharIndexAtPos(
                pagePtr,
                x,
                y,
                xTolerance,
                yTolerance,
            )
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
        }
        return -1
    }

    /**
     * Get the count of rectangles that bound the text on the page in a given range.
     * For internal use only.
     *
     * @param startIndex the 0-based index of the first character to get
     * @param count the number of characters to get
     * @return the number of rectangles
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount")
    fun textPageCountRects(
        startIndex: Int,
        count: Int,
    ): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        try {
            return nativeTextPage.textCountRects(
                pagePtr,
                startIndex,
                count,
            )
        } catch (e: NullPointerException) {
            Logger.e(TAG, e, "mContext may be null")
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
        }
        return -1
    }

    /**
     * Get the bounding box of a text rectangle on the page.
     * For internal use only.
     *
     * @param rectIndex the 0-based index of the rectangle to get
     * @return the bounding box as a [PdfRectF], or `null` if an error occurs
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("MagicNumber")
    fun textPageGetRect(rectIndex: Int): PdfRectF? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        return try {
            val o = nativeTextPage.textGetRect(pagePtr, rectIndex)
            PdfRectF(
                o[LEFT_OFFSET],
                o[TOP_OFFSET],
                o[RIGHT_OFFSET],
                o[BOTTOM_OFFSET],
            )
        } catch (e: NullPointerException) {
            Logger.e(TAG, e, "mContext may be null")
            null
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
            null
        }
    }

    /**
     * Get the bounding boxes of a range of texts on the page.
     * For internal use only.
     *
     * @param wordRanges an array of word ranges to get the bounding boxes for.
     *                    Even indices are the start index, odd indices are the length.
     * @return a list of [io.legere.pdfiumandroid.api.WordRangeRect] containing bounding boxes
     * with their start and length or `null` if an error occurs.
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount")
    fun textPageGetRectsForRanges(wordRanges: IntArray): List<WordRangeRect>? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        val data = nativeTextPage.textGetRects(pagePtr, wordRanges)
        if (data != null) {
            val count = data.size / RANGE_RECT_DATA_SIZE
            // Pre-allocating the exact size avoids "resizing" overhead
            val wordRangeRects =
                Array(count) { i ->
                    val offset = i * RANGE_RECT_DATA_SIZE
                    WordRangeRect(
                        rangeStart = data[offset + RANGE_START_OFFSET].toInt(),
                        rangeLength = data[offset + RANGE_LENGTH_OFFSET].toInt(),
                        rect =
                            PdfRectF(
                                data[offset + LEFT_OFFSET],
                                data[offset + TOP_OFFSET],
                                data[offset + RIGHT_OFFSET],
                                data[offset + BOTTOM_OFFSET],
                            ),
                    )
                }
            return wordRangeRects.toList()
        }
        return null
    }

    /**
     * Get the text bounded by the given rectangle.
     * For internal use only.
     *
     * @param rect the rectangle to bound the text
     * @param length the maximum number of characters to get
     * @return the text bounded by the rectangle as a [String], or `null` if an error occurs.
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetBoundedText(
        rect: PdfRectF,
        length: Int,
    ): String? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        return try {
            val buf = ShortArray(length + 1)
            val r =
                nativeTextPage.textGetBoundedText(
                    pagePtr,
                    rect.left.toDouble(),
                    rect.top.toDouble(),
                    rect.right.toDouble(),
                    rect.bottom.toDouble(),
                    buf,
                )
            val bytes = ByteArray((r - 1) * 2)
            val bb = ByteBuffer.wrap(bytes)
            bb.order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until r - 1) {
                val s = buf[i]
                bb.putShort(s)
            }
            String(bytes, StandardCharsets.UTF_16LE)
        } catch (e: NullPointerException) {
            Logger.e(TAG, e, "mContext may be null")
            null
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
            null
        }
    }

    /**
     * Get character font size in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @param charIndex the index of the character to get
     * @return the font size
     * @throws IllegalStateException if the page or document is closed
     */
    fun getFontSize(charIndex: Int): Double {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return 0.0
        return nativeTextPage.getFontSize(pagePtr, charIndex)
    }

    /**
     * Initiates a text search operation on the page.
     * For internal use only.
     *
     * @param findWhat The string to search for.
     * @param flags A set of [io.legere.pdfiumandroid.api.FindFlags] to control the search behavior.
     * @param startIndex The 0-based index to start the search from.
     * @return A [FindResultU] object representing the search session, or `null` if the page or document is closed.
     * @throws IllegalStateException if the page or document is closed.
     */
    fun findStart(
        findWhat: String,
        flags: Set<FindFlags>,
        startIndex: Int,
    ): FindResultU? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        val apiFlags = flags.fold(0) { acc, flag -> acc or flag.value }
        return FindResultU(nativeTextPage.findStart(pagePtr, findWhat, apiFlags, startIndex))
    }

    /**
     * Loads web links from the text page.
     * For internal use only.
     *
     * @return A [PdfPageLinkU] object containing the web links, or `null` if the page or document is closed.
     * @throws IllegalStateException if the page or document is closed.
     */
    fun loadWebLink(): PdfPageLinkU? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        val linkPtr = nativeTextPage.loadWebLink(pagePtr)
        return PdfPageLinkU(linkPtr)
    }

    /**
     * Close the text page and release all resources.
     * For internal use only.
     */
    override fun close() {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return

        pageMap[pageIndex]?.let {
            if (it.count > 1) {
                it.count--
                return
            }
            pageMap.remove(pageIndex)

            isClosed = true
            nativeTextPage.closeTextPage(pagePtr)
        }
    }

    /**
     * @suppress
     */
    companion object {
        private val TAG = PdfTextPageU::class.java.name
    }
}
