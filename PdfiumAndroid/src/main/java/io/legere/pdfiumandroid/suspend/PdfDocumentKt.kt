@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfWriteCallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfDocumentKt represents a PDF file and allows you to load pages from it.
 * @property document the [PdfDocument] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 * @constructor create a [PdfDocumentKt] from a [PdfDocument]
 */
@Suppress("TooManyFunctions")
class PdfDocumentKt(val document: PdfDocument, private val dispatcher: CoroutineDispatcher) :
    Closeable {

    /**
     *  suspend version of [PdfDocument.getPageCount]
     */
    suspend fun getPageCount(): Int {
        return withContext(dispatcher) {
            document.getPageCount()
        }
    }

    /**
     *  suspend version of [PdfDocument.getPageCharCounts]
     */
    suspend fun getPageCharCounts(): IntArray {
        return withContext(dispatcher) {
            document.getPageCharCounts()
        }
    }

    /**
     * suspend version of [PdfDocument.openPage]
     */
    suspend fun openPage(pageIndex: Int): PdfPageKt {
        return withContext(dispatcher) {
            PdfPageKt(document.openPage(pageIndex), dispatcher)
        }
    }

    /**
     * suspend version of [PdfDocument.openPages]
     */
    suspend fun openPages(fromIndex: Int, toIndex: Int): List<PdfPageKt> {
        return withContext(dispatcher) {
            document.openPages(fromIndex, toIndex).map { PdfPageKt(it, dispatcher) }
        }
    }

    /**
     * suspend version of [PdfDocument.getDocumentMeta]
     */
    suspend fun getDocumentMeta(): PdfDocument.Meta {
        return withContext(dispatcher) {
            document.getDocumentMeta()
        }
    }

    /**
     * suspend version of [PdfDocument.getTableOfContents]
     */
    suspend fun getTableOfContents(): List<PdfDocument.Bookmark> {
        return withContext(dispatcher) {
            document.getTableOfContents()
        }
    }

    /**
     * suspend version of [PdfDocument.openTextPage]
     */
    @Deprecated("use PdfPageKt.openTextPage", ReplaceWith("page.openTextPage()"))
    @Suppress("DEPRECATION")
    suspend fun openTextPage(page: PdfPageKt): PdfTextPageKt {
        return withContext(dispatcher) {
            PdfTextPageKt(document.openTextPage(page.page), dispatcher)
        }
    }

    /**
     * suspend version of [PdfDocument.openTextPages]
     */
    suspend fun openTextPages(fromIndex: Int, toIndex: Int): List<PdfTextPageKt> {
        return withContext(dispatcher) {
            document.openTextPages(fromIndex, toIndex).map { PdfTextPageKt(it, dispatcher) }
        }
    }

    /**
     * suspend version of [PdfDocument.saveAsCopy]
     */
    suspend fun saveAsCopy(callback: PdfWriteCallback): Boolean {
        return withContext(dispatcher) {
            document.saveAsCopy(callback)
        }
    }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        document.close()
    }

    fun safeClose(): Boolean {
        return try {
            document.close()
            true
        } catch (e: IllegalStateException) {
            Logger.e("PdfDocumentKt", e, "PdfDocumentKt.safeClose")
            false
        }
    }
}
