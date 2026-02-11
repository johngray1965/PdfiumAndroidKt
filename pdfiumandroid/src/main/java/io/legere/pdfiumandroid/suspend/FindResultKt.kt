package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.LockManager
import io.legere.pdfiumandroid.unlocked.FindResultU
import io.legere.pdfiumandroid.util.pdfiumConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

@Suppress("unused")
class FindResultKt(
    internal val findResult: FindResultU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    private val lock: LockManager = pdfiumConfig.lock

    suspend fun findNext(): Boolean =
        lock.withLock {
            withContext(dispatcher) {
                findResult.findNext()
            }
        }

    suspend fun findPrev(): Boolean =
        lock.withLock {
            withContext(dispatcher) {
                findResult.findPrev()
            }
        }

    suspend fun getSchResultIndex(): Int =
        lock.withLock {
            withContext(dispatcher) {
                findResult.getSchResultIndex()
            }
        }

    suspend fun getSchCount(): Int =
        lock.withLock {
            withContext(dispatcher) {
                findResult.getSchCount()
            }
        }

    suspend fun closeFind() {
        lock.withLock {
            withContext(dispatcher) {
                findResult.closeFind()
            }
        }
    }

    override fun close() {
        findResult.closeFind()
    }
}
