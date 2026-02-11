package io.legere.pdfiumandroid

import android.graphics.RectF
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import java.io.Closeable

@Suppress("TooManyFunctions")
class PdfPageLink(
    val pageLink: PdfPageLinkU,
) : Closeable {
    fun countWebLinks(): Int =
        PdfiumCore.lock.withLockBlocking {
            pageLink.countWebLinks()
        }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getURL(
        index: Int,
        length: Int,
    ): String? =
        PdfiumCore.lock.withLockBlocking {
            pageLink.getURL(index, length)
        }

    fun countRects(index: Int): Int =
        PdfiumCore.lock.withLockBlocking {
            pageLink.countRects(index)
        }

    @Suppress("MagicNumber")
    fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        PdfiumCore.lock.withLockBlocking {
            pageLink.getRect(linkIndex, rectIndex)
        }

    fun getTextRange(index: Int): Pair<Int, Int> =
        PdfiumCore.lock.withLockBlocking {
            pageLink.getTextRange(index)
        }

    override fun close() {
        PdfiumCore.lock.withLockBlocking {
            pageLink.close()
        }
    }
}
