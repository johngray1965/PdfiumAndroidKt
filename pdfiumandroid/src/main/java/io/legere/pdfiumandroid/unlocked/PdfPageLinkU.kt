package io.legere.pdfiumandroid.unlocked

import android.graphics.RectF
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.NativePageLinkContract
import io.legere.pdfiumandroid.jni.defaultNativeFactory
import java.io.Closeable
import java.nio.charset.StandardCharsets

@Suppress("TooManyFunctions")
class PdfPageLinkU(
    private val pageLinkPtr: Long,
    nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    private val nativePageLink: NativePageLinkContract = nativeFactory.getNativePageLink()

    fun countWebLinks(): Int = nativePageLink.countWebLinks(pageLinkPtr)

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getURL(
        index: Int,
        length: Int,
    ): String? {
        try {
            val bytes = ByteArray(length * 2)
            val r =
                nativePageLink.getURL(
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

    fun countRects(index: Int): Int = nativePageLink.countRects(pageLinkPtr, index)

    @Suppress("MagicNumber")
    fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        nativePageLink.getRect(pageLinkPtr, linkIndex, rectIndex).let {
            RectF(it[0], it[1], it[2], it[3])
        }

    fun getTextRange(index: Int): Pair<Int, Int> =
        nativePageLink.getTextRange(pageLinkPtr, index).let {
            Pair(it[0], it[1])
        }

    override fun close() {
        nativePageLink.closePageLink(pageLinkPtr)
    }

    /**
     * @suppress
     */
    companion object {
        private val TAG = PdfPageLinkU::class.java.name
    }
}
