package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.unlocked.FindResultU
import io.legere.pdfiumandroid.util.pdfiumConfig
import java.io.Closeable

@Suppress("TooManyFunctions")
class FindResult(
    internal val findResult: FindResultU,
) : Closeable {
    private val lock: LockManager = pdfiumConfig.lock

    fun findNext(): Boolean =
        lock.withLockBlocking {
            findResult.findNext()
        }

    fun findPrev(): Boolean =
        lock.withLockBlocking {
            findResult.findPrev()
        }

    fun getSchResultIndex(): Int =
        lock.withLockBlocking {
            findResult.getSchResultIndex()
        }

    fun getSchCount(): Int =
        lock.withLockBlocking {
            findResult.getSchCount()
        }

    fun closeFind() {
        lock.withLockBlocking {
            findResult.closeFind()
        }
    }

    override fun close() {
        lock.withLockBlocking {
            findResult.close()
        }
    }
}
