package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.FindResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

@Suppress("unused")
class FindResultKt(
    private val findResult: FindResult,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    suspend fun findNext(): Boolean =
        withContext(dispatcher) {
            findResult.findNext()
        }

    suspend fun findPrev(): Boolean =
        withContext(dispatcher) {
            findResult.findPrev()
        }

    suspend fun getSchResultIndex(): Int =
        withContext(dispatcher) {
            findResult.getSchResultIndex()
        }

    suspend fun getSchCount(): Int =
        withContext(dispatcher) {
            findResult.getSchCount()
        }

    suspend fun closeFind() {
        withContext(dispatcher) {
            findResult.closeFind()
        }
    }

    override fun close() {
        findResult.closeFind()
    }
}
