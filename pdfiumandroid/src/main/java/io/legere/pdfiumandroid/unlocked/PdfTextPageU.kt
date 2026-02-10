@file:Suppress("unused", "MemberVisibilityCanBePrivate", "TooGenericExceptionCaught")

package io.legere.pdfiumandroid.unlocked

import android.graphics.RectF
import io.legere.pdfiumandroid.FindFlags
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.WordRangeRect
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.NativeTextPage
import io.legere.pdfiumandroid.jni.defaultNativeFactory
import io.legere.pdfiumandroid.util.handleAlreadyClosed
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
 * PdfTextPage is a wrapper around the native PdfiumCore text page
 * It is used to get text and other information about the text on a page
 * @property doc the PdfDocument this page belongs to
 * @property pageIndex the index of this page in the document
 * @property pagePtr the pointer to the native page
 */
@Suppress("TooManyFunctions")
class PdfTextPageU(
    val doc: PdfDocumentU,
    val pageIndex: Int,
    val pagePtr: Long,
    val pageMap: MutableMap<Int, PdfDocument.PageCount>,
    nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    @Volatile
    private var isClosed = false

    val nativeTextPage: NativeTextPage = nativeFactory.getNativeTextPage()

    /**
     * Get character count of the page
     * @return the number of characters on the page
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageCountChars(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativeTextPage.textCountChars(pagePtr)
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
     * Get a unicode character on the page
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
     * Get the bounding box of a character on the page
     * @param index the index of the character to get
     * @return the bounding box
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("ReturnCount", "MagicNumber")
    fun textPageGetCharBox(index: Int): RectF? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        try {
            val o = nativeTextPage.textGetCharBox(pagePtr, index)
            // Note these are in an odd order left, right, bottom, top
            // what what Pdfium native code returns
            val r = RectF()
            r.left = o[0].toFloat()
            r.right = o[1].toFloat()
            r.bottom = o[2].toFloat()
            r.top = o[3].toFloat()
            return r
        } catch (e: NullPointerException) {
            Logger.e(TAG, e, "mContext may be null")
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
        }
        return null
    }

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
     * Get the count of rectangles that bound the text on the page in a given range
     * @param startIndex the index of the first character to get
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
     * Get the bounding box of a text on the page
     * @param rectIndex the index of the rectangle to get
     * @return the bounding box
     * @throws IllegalStateException if the page or document is closed
     */
    @Suppress("MagicNumber")
    fun textPageGetRect(rectIndex: Int): RectF? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        return try {
            val o = nativeTextPage.textGetRect(pagePtr, rectIndex)
            val r = RectF()
            r.left = o[LEFT_OFFSET].toFloat()
            r.top = o[TOP_OFFSET].toFloat()
            r.right = o[RIGHT_OFFSET].toFloat()
            r.bottom = o[BOTTOM_OFFSET].toFloat()
            r
        } catch (e: NullPointerException) {
            Logger.e(TAG, e, "mContext may be null")
            null
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
            null
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
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        val data = nativeTextPage.textGetRects(pagePtr, wordRanges)
        if (data != null) {
            val wordRangeRects = mutableListOf<WordRangeRect>()
            for (i in data.indices step RANGE_RECT_DATA_SIZE) {
                val r = RectF()
                r.left = data[i + LEFT_OFFSET].toFloat()
                r.top = data[i + TOP_OFFSET].toFloat()
                r.right = data[i + RIGHT_OFFSET].toFloat()
                r.bottom = data[i + BOTTOM_OFFSET].toFloat()
                val rangeStart = data[i + RANGE_START_OFFSET].toInt()
                val rangeLength = data[i + RANGE_LENGTH_OFFSET].toInt()
                WordRangeRect(rangeStart, rangeLength, r).let {
                    wordRangeRects.add(it)
                }
            }
            return wordRangeRects
        }
        return null
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
     * Get character font size in PostScript points (1/72th of an inch).<br></br>
     * @param charIndex the index of the character to get
     * @return the font size
     * @throws IllegalStateException if the page or document is closed
     */
    fun getFontSize(charIndex: Int): Double {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return 0.0
        return nativeTextPage.getFontSize(pagePtr, charIndex)
    }

    fun findStart(
        findWhat: String,
        flags: Set<FindFlags>,
        startIndex: Int,
    ): FindResultU? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        val apiFlags = flags.fold(0) { acc, flag -> acc or flag.value }
        return FindResultU(nativeTextPage.findStart(pagePtr, findWhat, apiFlags, startIndex))
    }

    fun loadWebLink(): PdfPageLinkU? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        val linkPtr = nativeTextPage.loadWebLink(pagePtr)
        return PdfPageLinkU(linkPtr)
    }

    /**
     * Close the page and release all resources
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

    companion object {
        private val TAG = PdfTextPageU::class.java.name
    }
}
