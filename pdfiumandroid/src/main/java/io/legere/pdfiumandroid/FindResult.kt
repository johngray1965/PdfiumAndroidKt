package io.legere.pdfiumandroid

import java.io.Closeable

@Suppress("TooManyFunctions")
class FindResult(
    val handle: FindHandle,
) : Closeable {
    private external fun nativeFindNext(findHandle: Long): Boolean

    private external fun nativeFindPrev(findHandle: Long): Boolean

    private external fun nativeGetSchResultIndex(findHandle: Long): Int

    private external fun nativeGetSchCount(findHandle: Long): Int

    private external fun nativeCloseFind(findHandle: Long)

    fun findNext(): Boolean {
        synchronized(PdfiumCore.lock) {
            return nativeFindNext(handle)
        }
    }

    fun findPrev(): Boolean {
        synchronized(PdfiumCore.lock) {
            return nativeFindPrev(handle)
        }
    }

    fun getSchResultIndex(): Int {
        synchronized(PdfiumCore.lock) {
            return nativeGetSchResultIndex(handle)
        }
    }

    fun getSchCount(): Int {
        synchronized(PdfiumCore.lock) {
            return nativeGetSchCount(handle)
        }
    }

    fun closeFind() {
        synchronized(PdfiumCore.lock) {
            nativeCloseFind(handle)
        }
    }

    override fun close() {
        nativeCloseFind(handle)
    }
}
