@file:Suppress("unused", "MemberVisibilityCanBePrivate", "TooGenericExceptionCaught")

package io.legere.pdfiumandroid

import android.graphics.RectF
import android.util.Log
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

@Suppress("TooManyFunctions")
class PdfTextPage(val doc: PdfDocument, val pageIndex: Int, val pagePtr: Long) : Closeable {

    var isClosed = false
        private set

    private external fun nativeCloseTextPage(pagePtr: Long)
    private external fun nativeTextCountChars(textPagePtr: Long): Int
    private external fun nativeTextGetText(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
        result: ShortArray
    ): Int

    private external fun nativeTextGetUnicode(textPagePtr: Long, index: Int): Int
    private external fun nativeTextGetCharBox(textPagePtr: Long, index: Int): DoubleArray
    private external fun nativeTextGetCharIndexAtPos(
        textPagePtr: Long,
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double
    ): Int

    private external fun nativeTextCountRects(textPagePtr: Long, startIndex: Int, count: Int): Int
    private external fun nativeTextGetRect(textPagePtr: Long, rectIndex: Int): DoubleArray

    private external fun nativeGetFontSize(pagePtr: Long, charIndex: Int): Double

    @Suppress("LongParameterList")
    private external fun nativeTextGetBoundedText(
        textPagePtr: Long,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        arr: ShortArray
    ): Int

    fun textPageCountChars(): Int {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeTextCountChars(pagePtr)
        }
    }

    fun textPageGetText(
        startIndex: Int,
        length: Int
    ): String? {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            try {
                val buf = ShortArray(length + 1)
                val r = nativeTextGetText(
                    pagePtr,
                    startIndex,
                    length,
                    buf
                )
                val bytes = ByteArray((r - 1) * 2)
                val bb = ByteBuffer.wrap(bytes)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until r - 1) {
                    val s = buf[i]
                    bb.putShort(s)
                }
                return String(bytes, StandardCharsets.UTF_16LE)
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
            return null
        }
    }

    fun textPageGetUnicode(index: Int): Char {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeTextGetUnicode(
                pagePtr,
                index
            ).toChar()
        }
    }

    fun textPageGetCharBox(index: Int): RectF? {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            try {
                val o =
                    nativeTextGetCharBox(pagePtr, index)
                // Note these are in an odd order left, right, bottom, top
                // what what Pdfium native code returns
                val r = RectF()
                r.left = o[0].toFloat()
                r.right = o[1].toFloat()
                r.bottom = o[2].toFloat()
                r.top = o[3].toFloat()
                return r
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
        }
        return null
    }

    fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double
    ): Int {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            try {
                return nativeTextGetCharIndexAtPos(
                    pagePtr,
                    x,
                    y,
                    xTolerance,
                    yTolerance
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native", e)
            }
        }
        return -1
    }

    fun textPageCountRects(
        startIndex: Int,
        count: Int
    ): Int {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            try {
                return nativeTextCountRects(
                    pagePtr,
                    startIndex,
                    count
                )
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
        }
        return -1
    }

    fun textPageGetRect(rectIndex: Int): RectF? {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            try {
                val o =
                    nativeTextGetRect(pagePtr, rectIndex)
                val r = RectF()
                r.left = o[0].toFloat()
                r.top = o[1].toFloat()
                r.right = o[2].toFloat()
                r.bottom = o[3].toFloat()
                return r
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
        }
        return null
    }

    fun textPageGetBoundedText(
        rect: RectF,
        length: Int
    ): String? {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            try {
                val buf = ShortArray(length + 1)
                val r = nativeTextGetBoundedText(
                    pagePtr,
                    rect.left.toDouble(),
                    rect.top.toDouble(),
                    rect.right.toDouble(),
                    rect.bottom.toDouble(),
                    buf
                )
                val bytes = ByteArray((r - 1) * 2)
                val bb = ByteBuffer.wrap(bytes)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until r - 1) {
                    val s = buf[i]
                    bb.putShort(s)
                }
                return String(bytes, StandardCharsets.UTF_16LE)
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
            return null
        }
    }

    /**
     * Get character font size in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getFontSize(charIndex: Int): Double {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeGetFontSize(pagePtr, charIndex)
        }
    }

    override fun close() {
        if (isClosed) return
        synchronized(PdfiumCore.lock) {
            isClosed = true
            nativeCloseTextPage(pagePtr)
        }
    }

    companion object {
        private val TAG = PdfTextPage::class.java.name
    }
}
