@file:Suppress("unused")

package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfWriteCallback
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

/**
 * PdfDocumentKtF represents a PDF file and allows you to load pages from it.
 * @property document the [PdfDocument] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 * @constructor create a [PdfDocumentKtF] from a [PdfDocument]
 */
class PdfDocumentKtF(
    val document: PdfDocument,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    /**
     *  suspend version of [PdfDocument.getPageCount]
     */
    suspend fun getPageCount(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            document.getPageCount()
        }

    /**
     *  suspend version of [PdfDocument.getPageCharCounts]
     */
    suspend fun getPageCharCounts(): Either<PdfiumKtFErrors, IntArray> =
        wrapEither(dispatcher) {
            document.getPageCharCounts()
        }

    /**
     * suspend version of [PdfDocument.openPage]
     */
    suspend fun openPage(pageIndex: Int): Either<PdfiumKtFErrors, PdfPageKtF> =
        wrapEither(dispatcher) {
            PdfPageKtF(document.openPage(pageIndex), dispatcher)
        }

    /**
     * suspend version of [PdfDocument.openPages]
     */
    suspend fun openPages(
        fromIndex: Int,
        toIndex: Int,
    ): Either<PdfiumKtFErrors, List<PdfPageKtF>> =
        wrapEither(dispatcher) {
            document.openPages(fromIndex, toIndex).map { PdfPageKtF(it, dispatcher) }
        }

    /**
     * suspend version of [PdfDocument.getDocumentMeta]
     */
    suspend fun getDocumentMeta(): Either<PdfiumKtFErrors, PdfDocument.Meta> =
        wrapEither(dispatcher) {
            document.getDocumentMeta()
        }

    /**
     * suspend version of [PdfDocument.getTableOfContents]
     */
    suspend fun getTableOfContents(): Either<PdfiumKtFErrors, List<PdfDocument.Bookmark>> =
        wrapEither(dispatcher) {
            document.getTableOfContents()
        }

    /**
     * suspend version of [PdfDocument.openTextPages]
     */
    suspend fun openTextPages(
        fromIndex: Int,
        toIndex: Int,
    ): Either<PdfiumKtFErrors, List<PdfTextPageKtF>> =
        wrapEither(dispatcher) {
            document.openTextPages(fromIndex, toIndex).map { PdfTextPageKtF(it, dispatcher) }
        }

    /**
     * suspend version of [PdfDocument.saveAsCopy]
     */
    suspend fun saveAsCopy(callback: PdfWriteCallback): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            document.saveAsCopy(callback)
        }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        document.close()
    }

    fun safeClose(): Either<PdfiumKtFErrors, Boolean> =
        Either
            .catch {
                document.close()
                true
            }.mapLeft { exceptionToPdfiumKtFError(it) }
}
