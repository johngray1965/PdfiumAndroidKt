package io.legere.pdfiumandroid

import android.graphics.RectF
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import java.io.Closeable

@Suppress("TooManyFunctions")
class PdfPageLink(
    val pageLink: PdfPageLinkU,
) : Closeable {
    fun countWebLinks(): Int {
        synchronized(PdfiumCore.lock) {
            return pageLink.countWebLinks()
        }
    }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getURL(
        index: Int,
        length: Int,
    ): String? {
        synchronized(PdfiumCore.lock) {
            return pageLink.getURL(index, length)
        }
    }

    fun countRects(index: Int): Int {
        synchronized(PdfiumCore.lock) {
            return pageLink.countRects(index)
        }
    }

    @Suppress("MagicNumber")
    fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF {
        synchronized(PdfiumCore.lock) {
            return pageLink.getRect(linkIndex, rectIndex)
        }
    }

    fun getTextRange(index: Int): Pair<Int, Int> {
        synchronized(PdfiumCore.lock) {
            return pageLink.getTextRange(index)
        }
    }

    override fun close() {
        pageLink.close()
    }
}
