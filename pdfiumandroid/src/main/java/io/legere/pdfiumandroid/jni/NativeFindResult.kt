package io.legere.pdfiumandroid.jni

class NativeFindResult {
    private external fun nativeFindNext(findHandle: Long): Boolean

    private external fun nativeFindPrev(findHandle: Long): Boolean

    private external fun nativeGetSchResultIndex(findHandle: Long): Int

    private external fun nativeGetSchCount(findHandle: Long): Int

    private external fun nativeCloseFind(findHandle: Long)

    fun findNext(handle: Long): Boolean = nativeFindNext(handle)

    fun findPrev(handle: Long): Boolean = nativeFindPrev(handle)

    fun getSchResultIndex(handle: Long): Int = nativeGetSchResultIndex(handle)

    fun getSchCount(handle: Long): Int = nativeGetSchCount(handle)

    fun closeFind(handle: Long) {
        nativeCloseFind(handle)
    }
}
