package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import io.legere.pdfiumandroid.LockManager
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import io.legere.pdfiumandroid.util.pdfiumConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

class PdfPageLinkKt(
    internal val pageLink: PdfPageLinkU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    private val lock: LockManager = pdfiumConfig.lock

    suspend fun countWebLinks(): Int =
        lock.withLock {
            withContext(dispatcher) {
                pageLink.countWebLinks()
            }
        }

    suspend fun getURL(
        index: Int,
        length: Int,
    ): String? =
        lock.withLock {
            withContext(dispatcher) {
                pageLink.getURL(index, length)
            }
        }

    suspend fun countRects(index: Int): Int =
        lock.withLock {
            withContext(dispatcher) {
                pageLink.countRects(index)
            }
        }

    suspend fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        lock.withLock {
            withContext(dispatcher) {
                pageLink.getRect(linkIndex, rectIndex)
            }
        }

    suspend fun getTextRange(index: Int): Pair<Int, Int> =
        lock.withLock {
            withContext(dispatcher) {
                pageLink.getTextRange(index)
            }
        }

    override fun close() {
        pageLink.close()
    }
}
