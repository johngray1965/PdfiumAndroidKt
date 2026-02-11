package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

class PdfPageLinkKt(
    internal val pageLink: PdfPageLinkU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    suspend fun countWebLinks(): Int =
        wrapSuspend(dispatcher) {
            pageLink.countWebLinks()
        }

    suspend fun getURL(
        index: Int,
        length: Int,
    ): String? =
        wrapSuspend(dispatcher) {
            pageLink.getURL(index, length)
        }

    suspend fun countRects(index: Int): Int =
        wrapSuspend(dispatcher) {
            pageLink.countRects(index)
        }

    suspend fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        wrapSuspend(dispatcher) {
            pageLink.getRect(linkIndex, rectIndex)
        }

    suspend fun getTextRange(index: Int): Pair<Int, Int> =
        wrapSuspend(dispatcher) {
            pageLink.getTextRange(index)
        }

    override fun close() {
        pageLink.close()
    }
}
