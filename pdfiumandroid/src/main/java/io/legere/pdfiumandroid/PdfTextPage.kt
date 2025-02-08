@file:Suppress("unused", "MemberVisibilityCanBePrivate", "TooGenericExceptionCaught")

package io.legere.pdfiumandroid

import android.graphics.RectF
import dalvik.annotation.optimization.FastNative
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
class PdfTextPage(
    val doc: PdfDocument,
    val pageIndex: Int,
    val pagePtr: Long,
    val pageMap: MutableMap<Int, PdfDocument.PageCount>,
) : Closeable {
    private var isClosed = false

    /**
     * Get character count of the page
     * @return the number of characters on the page
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageCountChars(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        synchronized(PdfiumCore.lock) {
            return nativeTextCountChars(pagePtr)
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
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        synchronized(PdfiumCore.lock) {
            try {
                val buf = ShortArray(length + 1)
                val r =
                    nativeTextGetText(
                        pagePtr,
                        startIndex,
                        length,
                        buf,
                    )

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
                Logger.e(TAG, e, "mContext may be null")
            } catch (e: Exception) {
                Logger.e(TAG, e, "Exception throw from native")
            }
            return null
        }
    }

    @Suppress("ReturnCount")
    fun textPageGetText(
        startIndex: Int,
        length: Int,
    ): String? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        synchronized(PdfiumCore.lock) {
            try {
                val bytes = ByteArray(length * 2)
                val r =
                    nativeTextGetTextByteArray(
                        pagePtr,
                        startIndex,
                        length,
                        bytes,
                    )

                if (r <= 0) {
                    return ""
                }
                return String(bytes, StandardCharsets.UTF_16LE)
            } catch (e: NullPointerException) {
                Logger.e(TAG, e, "mContext may be null")
            } catch (e: Exception) {
                Logger.e(TAG, e, "Exception throw from native")
            }
            return null
        }
    }

    /**
     * Get a unicode character on the page
     * @param index the index of the character to get
     * @return the character
     * @throws IllegalStateException if the page or document is closed
     */
    fun textPageGetUnicode(index: Int): Char {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeTextGetUnicode(
                pagePtr,
                index,
            ).toChar()
        }
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
        synchronized(PdfiumCore.lock) {
            try {
                val o = nativeTextGetCharBox(pagePtr, index)
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
        synchronized(PdfiumCore.lock) {
            try {
                return nativeTextGetCharIndexAtPos(
                    pagePtr,
                    x,
                    y,
                    xTolerance,
                    yTolerance,
                )
            } catch (e: Exception) {
                Logger.e(TAG, e, "Exception throw from native")
            }
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
    fun textPageCountRects(
        startIndex: Int,
        count: Int,
    ): Int {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            try {
                return nativeTextCountRects(
                    pagePtr,
                    startIndex,
                    count,
                )
            } catch (e: NullPointerException) {
                Logger.e(TAG, e, "mContext may be null")
            } catch (e: Exception) {
                Logger.e(TAG, e, "Exception throw from native")
            }
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
        synchronized(PdfiumCore.lock) {
            return try {
                val o = nativeTextGetRect(pagePtr, rectIndex)
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
        synchronized(PdfiumCore.lock) {
            val data = nativeTextGetRects(pagePtr, wordRanges)
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
        synchronized(PdfiumCore.lock) {
            return try {
                val buf = ShortArray(length + 1)
                val r =
                    nativeTextGetBoundedText(
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
    }

    /**
     * Get character font size in PostScript points (1/72th of an inch).<br></br>
     * @param charIndex the index of the character to get
     * @return the font size
     * @throws IllegalStateException if the page or document is closed
     */
    fun getFontSize(charIndex: Int): Double {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return 0.0
        synchronized(PdfiumCore.lock) {
            return nativeGetFontSize(pagePtr, charIndex)
        }
    }

    fun findStart(
        findWhat: String,
        flags: Set<FindFlags>,
        startIndex: Int,
    ): FindResult? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null
        synchronized(PdfiumCore.lock) {
            val apiFlags = flags.fold(0) { acc, flag -> acc or flag.value }
            return FindResult(nativeFindStart(pagePtr, findWhat, apiFlags, startIndex))
        }
    }

    fun loadWebLink(): PdfPageLink {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        val linkPtr = nativeLoadWebLink(pagePtr)
        return PdfPageLink(linkPtr)
    }

    /**
     * Close the page and release all resources
     */
    override fun close() {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return

        synchronized(PdfiumCore.lock) {
            pageMap[pageIndex]?.let {
                if (it.count > 1) {
                    it.count--
                    return
                }
                pageMap.remove(pageIndex)

                isClosed = true
                nativeCloseTextPage(pagePtr)
            }
        }
    }

    companion object {
        private val TAG = PdfTextPage::class.java.name

        @JvmStatic
        private external fun nativeCloseTextPage(pagePtr: Long)

        @JvmStatic
        @FastNative
        private external fun nativeTextCountChars(textPagePtr: Long): Int

        @JvmStatic
        @FastNative
        private external fun nativeTextGetCharBox(
            textPagePtr: Long,
            index: Int,
        ): DoubleArray

        @JvmStatic
        @FastNative
        private external fun nativeTextGetRect(
            textPagePtr: Long,
            rectIndex: Int,
        ): DoubleArray

        @JvmStatic
        @FastNative
        private external fun nativeTextGetRects(
            textPagePtr: Long,
            wordRanges: IntArray,
        ): DoubleArray?

        @Suppress("LongParameterList")
        @JvmStatic
        @FastNative
        private external fun nativeTextGetBoundedText(
            textPagePtr: Long,
            left: Double,
            top: Double,
            right: Double,
            bottom: Double,
            arr: ShortArray,
        ): Int

        @JvmStatic
        private external fun nativeFindStart(
            textPagePtr: Long,
            findWhat: String,
            flags: Int,
            startIndex: Int,
        ): Long

        @JvmStatic
        private external fun nativeLoadWebLink(textPagePtr: Long): Long

        @JvmStatic
        private external fun nativeTextGetCharIndexAtPos(
            textPagePtr: Long,
            x: Double,
            y: Double,
            xTolerance: Double,
            yTolerance: Double,
        ): Int

        @JvmStatic
        private external fun nativeTextGetText(
            textPagePtr: Long,
            startIndex: Int,
            count: Int,
            result: ShortArray,
        ): Int

        //
        @JvmStatic
        private external fun nativeTextGetTextByteArray(
            textPagePtr: Long,
            startIndex: Int,
            count: Int,
            result: ByteArray,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeTextGetUnicode(
            textPagePtr: Long,
            index: Int,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeTextCountRects(
            textPagePtr: Long,
            startIndex: Int,
            count: Int,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeGetFontSize(
            pagePtr: Long,
            charIndex: Int,
        ): Double
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
