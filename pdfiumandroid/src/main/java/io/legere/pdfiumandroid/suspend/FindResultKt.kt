package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.unlocked.FindResultU
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable

@Suppress("unused")
class FindResultKt(
    val findResult: FindResultU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    suspend fun findNext(): Boolean =
        PdfiumCoreKt.mutex.withLock {
            withContext(dispatcher) {
                findResult.findNext()
            }
        }

    suspend fun findPrev(): Boolean =
        PdfiumCoreKt.mutex.withLock {
            withContext(dispatcher) {
                findResult.findPrev()
            }
        }

    suspend fun getSchResultIndex(): Int =
        PdfiumCoreKt.mutex.withLock {
            withContext(dispatcher) {
                findResult.getSchResultIndex()
            }
        }

    suspend fun getSchCount(): Int =
        PdfiumCoreKt.mutex.withLock {
            withContext(dispatcher) {
                findResult.getSchCount()
            }
        }

    suspend fun closeFind() {
        PdfiumCoreKt.mutex.withLock {
            withContext(dispatcher) {
                findResult.closeFind()
            }
        }
    }

    override fun close() {
        findResult.closeFind()
    }
}
