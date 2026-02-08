package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.unlocked.FindResultU
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

@Suppress("unused")
class FindResultKtF(
    private val findResult: FindResultU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    suspend fun findNext(): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            findResult.findNext()
        }

    suspend fun findPrev(): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            findResult.findPrev()
        }

    suspend fun getSchResultIndex(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            findResult.getSchResultIndex()
        }

    suspend fun getSchCount(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            findResult.getSchCount()
        }

    suspend fun closeFind() {
        wrapEither(dispatcher) {
            findResult.closeFind()
        }
    }

    override fun close() {
        findResult.closeFind()
    }
}
