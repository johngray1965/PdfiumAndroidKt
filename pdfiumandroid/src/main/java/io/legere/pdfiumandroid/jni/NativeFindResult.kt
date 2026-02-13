package io.legere.pdfiumandroid.jni

class NativeFindResult {
    private external fun nativeFindNext(findHandle: Long): Boolean

    private external fun nativeFindPrev(findHandle: Long): Boolean

    private external fun nativeGetSchResultIndex(findHandle: Long): Int

    private external fun nativeGetSchCount(findHandle: Long): Int

    private external fun nativeCloseFind(findHandle: Long)

    internal fun findNext(handle: Long): Boolean = nativeFindNext(handle)

    internal fun findPrev(handle: Long): Boolean = nativeFindPrev(handle)

    internal fun getSchResultIndex(handle: Long): Int = nativeGetSchResultIndex(handle)

    internal fun getSchCount(handle: Long): Int = nativeGetSchCount(handle)

    internal fun closeFind(handle: Long) {
        nativeCloseFind(handle)
    }
}
