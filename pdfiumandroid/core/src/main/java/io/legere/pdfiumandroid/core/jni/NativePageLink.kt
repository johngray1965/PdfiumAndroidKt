package io.legere.pdfiumandroid.core.jni

/**
 * Contract for native PDFium page link operations.
 * This interface defines the JNI methods for interacting with web links on PDF pages.
 * Implementations of this contract are intended for **internal use only**
 * within the PdfiumAndroid library to abstract native calls.
 */
interface NativePageLinkContract {
    /**
     * Closes a native PDF page link object.
     * This is a JNI method.
     *
     * @param pageLinkPtr The native pointer (long) to the PDF page link object to close.
     */
    fun closePageLink(pageLinkPtr: Long)

    /**
     * Counts the number of web links on a PDF page.
     * This is a JNI method.
     *
     * @param pageLinkPtr The native pointer (long) to the PDF page link object.
     * @return The total number of web links.
     */
    fun countWebLinks(pageLinkPtr: Long): Int

    /**
     * Retrieves the URL of a specific web link.
     * This is a JNI method.
     *
     * @param pageLinkPtr The native pointer (long) to the PDF page link object.
     * @param index The 0-based index of the web link.
     * @param count The maximum number of characters to retrieve for the URL.
     * @param result A `ByteArray` to store the URL characters.
     * @return The number of characters written into the array.
     */
    fun getURL(
        pageLinkPtr: Long,
        index: Int,
        count: Int,
        result: ByteArray,
    ): Int

    /**
     * Counts the number of bounding rectangles for a specific web link.
     * This is a JNI method.
     *
     * @param pageLinkPtr The native pointer (long) to the PDF page link object.
     * @param index The 0-based index of the web link.
     * @return The number of bounding rectangles for the specified web link.
     */
    fun countRects(
        pageLinkPtr: Long,
        index: Int,
    ): Int

    /**
     * Gets a specific bounding rectangle for a web link.
     * This is a JNI method.
     *
     * @param pageLinkPtr The native pointer (long) to the PDF page link object.
     * @param linkIndex The 0-based index of the web link.
     * @param rectIndex The 0-based index of the rectangle within the web link.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the rectangle.
     */
    fun getRect(
        pageLinkPtr: Long,
        linkIndex: Int,
        rectIndex: Int,
    ): FloatArray

    /**
     * Gets the text range (start index and count) associated with a web link.
     * This is a JNI method.
     *
     * @param pageLinkPtr The native pointer (long) to the PDF page link object.
     * @param index The 0-based index of the web link.
     * @return An [IntArray] of size 2 containing [startIndex, count] of the text range.
     */
    fun getTextRange(
        pageLinkPtr: Long,
        index: Int,
    ): IntArray
}

class NativePageLink : NativePageLinkContract {
    override fun closePageLink(pageLinkPtr: Long) = nativeClosePageLink(pageLinkPtr)

    override fun countWebLinks(pageLinkPtr: Long) = nativeCountWebLinks(pageLinkPtr)

    override fun getURL(
        pageLinkPtr: Long,
        index: Int,
        count: Int,
        result: ByteArray,
    ) = nativeGetURL(pageLinkPtr, index, count, result)

    override fun countRects(
        pageLinkPtr: Long,
        index: Int,
    ) = nativeCountRects(pageLinkPtr, index)

    override fun getRect(
        pageLinkPtr: Long,
        linkIndex: Int,
        rectIndex: Int,
    ) = nativeGetRect(pageLinkPtr, linkIndex, rectIndex)

    override fun getTextRange(
        pageLinkPtr: Long,
        index: Int,
    ) = nativeGetTextRange(pageLinkPtr, index)

    /**
     * @suppress
     */
    companion object {
        @JvmStatic
        private external fun nativeClosePageLink(pageLinkPtr: Long)

        @JvmStatic
        private external fun nativeCountWebLinks(pageLinkPtr: Long): Int

        @JvmStatic
        private external fun nativeGetURL(
            pageLinkPtr: Long,
            index: Int,
            count: Int,
            result: ByteArray,
        ): Int

        @JvmStatic
        private external fun nativeCountRects(
            pageLinkPtr: Long,
            index: Int,
        ): Int

        @JvmStatic
        private external fun nativeGetRect(
            pageLinkPtr: Long,
            linkIndex: Int,
            rectIndex: Int,
        ): FloatArray

        @JvmStatic
        // needs to return a start and an end
        private external fun nativeGetTextRange(pageLinkPtr: Long, index: Int): IntArray
    }
}
