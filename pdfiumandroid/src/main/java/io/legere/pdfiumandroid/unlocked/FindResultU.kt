package io.legere.pdfiumandroid.unlocked

import io.legere.pdfiumandroid.FindHandle
import java.io.Closeable

@Suppress("TooManyFunctions")
class FindResultU(
    val handle: FindHandle,
) : Closeable {
    private external fun nativeFindNext(findHandle: Long): Boolean

    private external fun nativeFindPrev(findHandle: Long): Boolean

    private external fun nativeGetSchResultIndex(findHandle: Long): Int

    private external fun nativeGetSchCount(findHandle: Long): Int

    private external fun nativeCloseFind(findHandle: Long)

    fun findNext(): Boolean = nativeFindNext(handle)

    fun findPrev(): Boolean = nativeFindPrev(handle)

    fun getSchResultIndex(): Int = nativeGetSchResultIndex(handle)

    fun getSchCount(): Int = nativeGetSchCount(handle)

    fun closeFind() {
        nativeCloseFind(handle)
    }

    override fun close() {
        nativeCloseFind(handle)
    }
}
