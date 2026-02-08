package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.unlocked.FindResultU
import java.io.Closeable

@Suppress("TooManyFunctions")
class FindResult(
    private val findResult: FindResultU,
) : Closeable {
    fun findNext(): Boolean {
        synchronized(PdfiumCore.lock) {
            return findResult.findNext()
        }
    }

    fun findPrev(): Boolean {
        synchronized(PdfiumCore.lock) {
            return findResult.findPrev()
        }
    }

    fun getSchResultIndex(): Int {
        synchronized(PdfiumCore.lock) {
            return findResult.getSchResultIndex()
        }
    }

    fun getSchCount(): Int {
        synchronized(PdfiumCore.lock) {
            return findResult.getSchCount()
        }
    }

    fun closeFind() {
        synchronized(PdfiumCore.lock) {
            findResult.closeFind()
        }
    }

    override fun close() {
        findResult.close()
    }
}
