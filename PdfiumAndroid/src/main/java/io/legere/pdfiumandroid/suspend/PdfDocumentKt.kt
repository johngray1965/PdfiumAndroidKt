@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfWriteCallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

class PdfDocumentKt(val document: PdfDocument, private val dispatcher: CoroutineDispatcher) :
    Closeable {

    suspend fun getPageCount(): Int {
        return withContext(dispatcher) {
            document.getPageCount()
        }
    }

    suspend fun openPage(pageIndex: Int): PdfPageKt {
        return withContext(dispatcher) {
            PdfPageKt(document.openPage(pageIndex), dispatcher)
        }
    }

    suspend fun openPages(fromIndex: Int, toIndex: Int): List<PdfPageKt> {
        return withContext(dispatcher) {
            document.openPages(fromIndex, toIndex).map { PdfPageKt(it, dispatcher) }
        }
    }

    suspend fun getDocumentMeta(): PdfDocument.Meta {
        return withContext(dispatcher) {
            document.getDocumentMeta()
        }
    }

    suspend fun getTableOfContents(): List<PdfDocument.Bookmark> {
        return withContext(dispatcher) {
            document.getTableOfContents()
        }
    }

    suspend fun openTextPage(pageIndex: Int): PdfTextPageKt {
        return withContext(dispatcher) {
            PdfTextPageKt(document.openTextPage(pageIndex), dispatcher)
        }
    }

    suspend fun openTextPages(fromIndex: Int, toIndex: Int): List<PdfTextPageKt> {
        return withContext(dispatcher) {
            document.openTextPages(fromIndex, toIndex).map { PdfTextPageKt(it, dispatcher) }
        }
    }

    suspend fun saveAsCopy(callback: PdfWriteCallback): Boolean {
        return withContext(dispatcher) {
            document.saveAsCopy(callback)
        }
    }

    override fun close() {
        document.close()
    }
}
