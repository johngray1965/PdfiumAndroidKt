package io.legere.pdfiumandroid

import android.graphics.RectF
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import io.legere.pdfiumandroid.util.pdfiumConfig
import java.io.Closeable

@Suppress("TooManyFunctions")
class PdfPageLink(
    val pageLink: PdfPageLinkU,
) : Closeable {
    private val lock: LockManager = pdfiumConfig.lock

    fun countWebLinks(): Int =
        lock.withLockBlocking {
            pageLink.countWebLinks()
        }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getURL(
        index: Int,
        length: Int,
    ): String? =
        lock.withLockBlocking {
            pageLink.getURL(index, length)
        }

    fun countRects(index: Int): Int =
        lock.withLockBlocking {
            pageLink.countRects(index)
        }

    @Suppress("MagicNumber")
    fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        lock.withLockBlocking {
            pageLink.getRect(linkIndex, rectIndex)
        }

    fun getTextRange(index: Int): Pair<Int, Int> =
        lock.withLockBlocking {
            pageLink.getTextRange(index)
        }

    override fun close() {
        lock.withLockBlocking {
            pageLink.close()
        }
    }
}
