package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import io.legere.pdfiumandroid.PdfPageLink
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

class PdfPageLinkKt(
    val pageLink: PdfPageLink,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    suspend fun countWebLinks(): Int =
        withContext(dispatcher) {
            pageLink.countWebLinks()
        }

    suspend fun getURL(
        index: Int,
        length: Int,
    ): String? =
        withContext(dispatcher) {
            pageLink.getURL(index, length)
        }

    suspend fun countRects(index: Int): Int =
        withContext(dispatcher) {
            pageLink.countRects(index)
        }

    suspend fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        withContext(dispatcher) {
            pageLink.getRect(linkIndex, rectIndex)
        }

    suspend fun getTextRange(index: Int): Pair<Int, Int> =
        withContext(dispatcher) {
            pageLink.getTextRange(index)
        }

    override fun close() {
        pageLink.close()
    }
}
