package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.unlocked.FindResultU
import io.legere.pdfiumandroid.wrapLock
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

@Suppress("unused")
class FindResultKt(
    internal val findResult: FindResultU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    suspend fun findNext(): Boolean =
        wrapSuspend(dispatcher) {
            findResult.findNext()
        }

    suspend fun findPrev(): Boolean =
        wrapSuspend(dispatcher) {
            findResult.findPrev()
        }

    suspend fun getSchResultIndex(): Int =
        wrapSuspend(dispatcher) {
            findResult.getSchResultIndex()
        }

    suspend fun getSchCount(): Int =
        wrapSuspend(dispatcher) {
            findResult.getSchCount()
        }

    suspend fun closeFind() {
        wrapSuspend(dispatcher) {
            findResult.closeFind()
        }
    }

    override fun close() {
        wrapLock {
            findResult.closeFind()
        }
    }
}
