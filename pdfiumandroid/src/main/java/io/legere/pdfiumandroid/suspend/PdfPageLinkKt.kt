package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import io.legere.pdfiumandroid.suspend.PdfiumCoreKt.Companion.mutex
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable

class PdfPageLinkKt(
    internal val pageLink: PdfPageLinkU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    suspend fun countWebLinks(): Int =
        mutex.withLock {
            withContext(dispatcher) {
                pageLink.countWebLinks()
            }
        }

    suspend fun getURL(
        index: Int,
        length: Int,
    ): String? =
        mutex.withLock {
            withContext(dispatcher) {
                pageLink.getURL(index, length)
            }
        }

    suspend fun countRects(index: Int): Int =
        mutex.withLock {
            withContext(dispatcher) {
                pageLink.countRects(index)
            }
        }

    suspend fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        mutex.withLock {
            withContext(dispatcher) {
                pageLink.getRect(linkIndex, rectIndex)
            }
        }

    suspend fun getTextRange(index: Int): Pair<Int, Int> =
        mutex.withLock {
            withContext(dispatcher) {
                pageLink.getTextRange(index)
            }
        }

    override fun close() {
        pageLink.close()
    }
}
