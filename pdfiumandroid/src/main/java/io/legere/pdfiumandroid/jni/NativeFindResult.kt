package io.legere.pdfiumandroid.jni

/**
 * Contract for native PDFium text search result operations.
 * This interface defines the JNI methods for navigating and querying text search results.
 * Implementations of this contract are intended for **internal use only**
 * within the PdfiumAndroid library to abstract native calls.
 */
interface NativeFindResultContract {
    /**
     * Finds the next occurrence of the search pattern.
     * This is a JNI method.
     *
     * @param handle The native pointer (long) to the search handle.
     * @return `true` if a next occurrence is found, `false` otherwise.
     */
    fun findNext(handle: Long): Boolean

    /**
     * Finds the previous occurrence of the search pattern.
     * This is a JNI method.
     *
     * @param handle The native pointer (long) to the search handle.
     * @return `true` if a previous occurrence is found, `false` otherwise.
     */
    fun findPrev(handle: Long): Boolean

    /**
     * Gets the index of the currently found search result.
     * This is a JNI method.
     *
     * @param handle The native pointer (long) to the search handle.
     * @return The 0-based index of the current search result.
     */
    fun getSchResultIndex(handle: Long): Int

    /**
     * Gets the total count of search results.
     * This is a JNI method.
     *
     * @param handle The native pointer (long) to the search handle.
     * @return The total number of occurrences of the search pattern.
     */
    fun getSchCount(handle: Long): Int

    /**
     * Closes the search operation and releases associated native resources.
     * This is a JNI method.
     *
     * @param handle The native pointer (long) to the search handle.
     */
    fun closeFind(handle: Long)
}

internal class NativeFindResult : NativeFindResultContract {
    private external fun nativeFindNext(findHandle: Long): Boolean

    private external fun nativeFindPrev(findHandle: Long): Boolean

    private external fun nativeGetSchResultIndex(findHandle: Long): Int

    private external fun nativeGetSchCount(findHandle: Long): Int

    private external fun nativeCloseFind(findHandle: Long)

    override fun findNext(handle: Long): Boolean = nativeFindNext(handle)

    override fun findPrev(handle: Long): Boolean = nativeFindPrev(handle)

    override fun getSchResultIndex(handle: Long): Int = nativeGetSchResultIndex(handle)

    override fun getSchCount(handle: Long): Int = nativeGetSchCount(handle)

    override fun closeFind(handle: Long) {
        nativeCloseFind(handle)
    }
}
