package io.legere.pdfiumandroid

import android.graphics.RectF
import io.legere.pdfiumandroid.util.Size
import java.io.Closeable
import java.nio.charset.StandardCharsets

@Suppress("TooManyFunctions")
class PdfPageLink(
    private val pageLinkPtr: Long,
) : Closeable {
    private external fun nativeClosePageLink(pageLinkPtr: Long)

    private external fun nativeCountWebLinks(pageLinkPtr: Long): Int

    private external fun nativeGetURL(
        pageLinkPtr: Long,
        index: Int,
        count: Int,
        result: ByteArray,
    ): Int

    private external fun nativeCountRects(
        pageLinkPtr: Long,
        index: Int,
    ): Int

    private external fun nativeGetRect(
        pageLinkPtr: Long,
        linkIndex: Int,
        rectIndex: Int,
    ): FloatArray

    private external fun nativeGetTextRange(pageLinkPtr: Long, index: Int): Size // needs to return a start and an end

    fun countWebLinks(): Int {
        synchronized(PdfiumCore.lock) {
            return nativeCountWebLinks(pageLinkPtr)
        }
    }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getURL(
        index: Int,
        length: Int,
    ): String? {
        synchronized(PdfiumCore.lock) {
            try {
                val bytes = ByteArray(length * 2)
                val r =
                    nativeGetURL(
                        pageLinkPtr,
                        index,
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

    fun countRects(index: Int): Int {
        synchronized(PdfiumCore.lock) {
            return nativeCountRects(pageLinkPtr, index)
        }
    }

    @Suppress("MagicNumber")
    fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF {
        synchronized(PdfiumCore.lock) {
            return nativeGetRect(pageLinkPtr, linkIndex, rectIndex).let {
                RectF(it[0], it[1], it[2], it[3])
            }
        }
    }

    fun getTextRange(index: Int): Pair<Int, Int> {
        synchronized(PdfiumCore.lock) {
            return nativeGetTextRange(pageLinkPtr, index).let {
                Pair(it.width, it.height)
            }
        }
    }

    override fun close() {
        nativeClosePageLink(pageLinkPtr)
    }

    companion object {
        private val TAG = PdfPageLink::class.java.name
    }
}
