package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.unlocked.FindResultU
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

/**
 * Arrow-based suspending version of [io.legere.pdfiumandroid.FindResult] for text search operations
 * within a PDF page.
 *
 * This class wraps the native [FindResultU] object and dispatches its operations
 * to a [CoroutineDispatcher] using the [wrapEither] function. This ensures non-blocking
 * execution and provides robust error handling by returning results as [Either<PdfiumKtFErrors, T>].
 * It allows for navigating through search results, getting the current result index,
 * and the total count of results in a suspendable and functional manner.
 *
 * @property findResult The underlying unlocked native find result object.
 * @property dispatcher The [CoroutineDispatcher] to use for suspending calls.
 */
@Suppress("unused")
class FindResultKtF(
    internal val findResult: FindResultU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.FindResult.findNext].
     * Finds the next occurrence of the search pattern asynchronously.
     *
     * @return An [Either] containing `PdfiumKtFErrors` on the left or `true` if a next occurrence is found, `false` otherwise, on the right.
     */
    suspend fun findNext(): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            findResult.findNext()
        }

    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.FindResult.findPrev].
     * Finds the previous occurrence of the search pattern asynchronously.
     *
     * @return An [Either] containing `PdfiumKtFErrors` on the left or `true` if a previous occurrence is found, `false` otherwise, on the right.
     */
    suspend fun findPrev(): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            findResult.findPrev()
        }

    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.FindResult.getSchResultIndex].
     * Gets the index of the currently found search result asynchronously.
     *
     * @return An [Either] containing `PdfiumKtFErrors` on the left or the 0-based index of the current search result on the right.
     */
    suspend fun getSchResultIndex(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            findResult.getSchResultIndex()
        }

    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.FindResult.getSchCount].
     * Gets the total count of search results found asynchronously.
     *
     * @return An [Either] containing `PdfiumKtFErrors` on the left or the total number of occurrences of the search pattern on the right.
     */
    suspend fun getSchCount(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            findResult.getSchCount()
        }

    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.FindResult.closeFind].
     * Closes the find operation and releases associated native resources asynchronously.
     * This method is called automatically when [close] is invoked.
     *
     * @return An [Either] containing `PdfiumKtFErrors` on the left or [Unit] on the right.
     */
    suspend fun closeFind(): Either<PdfiumKtFErrors, Unit> =
        wrapEither(dispatcher) {
            findResult.closeFind()
        }

    /**
     * Closes the [FindResultKtF] object and releases all associated native resources.
     * This makes the object unusable after this call.
     */
    override fun close() {
        findResult.closeFind()
    }
}
