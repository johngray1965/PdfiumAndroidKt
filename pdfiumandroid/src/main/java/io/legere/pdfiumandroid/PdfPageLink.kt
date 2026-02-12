package io.legere.pdfiumandroid

import android.graphics.RectF
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import java.io.Closeable

@Suppress("TooManyFunctions")
class PdfPageLink(
    val pageLink: PdfPageLinkU,
) : Closeable {
    fun countWebLinks(): Int =
        wrapLock {
            pageLink.countWebLinks()
        }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getURL(
        index: Int,
        length: Int,
    ): String? =
        wrapLock {
            pageLink.getURL(index, length)
        }

    fun countRects(index: Int): Int =
        wrapLock {
            pageLink.countRects(index)
        }

    @Suppress("MagicNumber")
    fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        wrapLock {
            pageLink.getRect(linkIndex, rectIndex)
        }

    fun getTextRange(index: Int): Pair<Int, Int> =
        wrapLock {
            pageLink.getTextRange(index)
        }

    override fun close() {
        wrapLock {
            pageLink.close()
        }
    }
}
