package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.FindResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

@Suppress("unused")
class FindResultKt(
    private val findResult: FindResult,
    private val dispatcher: CoroutineDispatcher
): Closeable {
    suspend fun findNext(): Boolean {
        return withContext(dispatcher) {
            findResult.findNext()
        }
    }

    suspend fun findPrev(): Boolean {
        return withContext(dispatcher) {
            findResult.findPrev()
        }
    }

    suspend fun getSchResultIndex(): Int {
        return withContext(dispatcher) {
            findResult.getSchResultIndex()
        }
    }

    suspend fun getSchCount(): Int {
        return withContext(dispatcher) {
            findResult.getSchCount()
        }
    }

    suspend fun  closeFind() {
        withContext(dispatcher) {
            findResult.closeFind()
        }
    }

    override fun close() {
        findResult.closeFind()
    }
}
