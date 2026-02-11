package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.unlocked.FindResultU
import java.io.Closeable

@Suppress("TooManyFunctions")
class FindResult(
    internal val findResult: FindResultU,
) : Closeable {
    fun findNext(): Boolean =
        PdfiumCore.lock.withLockBlocking {
            findResult.findNext()
        }

    fun findPrev(): Boolean =
        PdfiumCore.lock.withLockBlocking {
            findResult.findPrev()
        }

    fun getSchResultIndex(): Int =
        PdfiumCore.lock.withLockBlocking {
            findResult.getSchResultIndex()
        }

    fun getSchCount(): Int =
        PdfiumCore.lock.withLockBlocking {
            findResult.getSchCount()
        }

    fun closeFind() {
        PdfiumCore.lock.withLockBlocking {
            findResult.closeFind()
        }
    }

    override fun close() {
        findResult.close()
    }
}
