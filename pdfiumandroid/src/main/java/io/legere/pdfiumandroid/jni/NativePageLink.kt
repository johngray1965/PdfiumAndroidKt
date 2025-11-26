package io.legere.pdfiumandroid.jni

class NativePageLink {
    internal fun closePageLink(pageLinkPtr: Long) = nativeClosePageLink(pageLinkPtr)

    internal fun countWebLinks(pageLinkPtr: Long) = nativeCountWebLinks(pageLinkPtr)

    internal fun getURL(
        pageLinkPtr: Long,
        index: Int,
        count: Int,
        result: ByteArray,
    ) = nativeGetURL(pageLinkPtr, index, count, result)

    internal fun countRects(
        pageLinkPtr: Long,
        index: Int,
    ) = nativeCountRects(pageLinkPtr, index)

    internal fun getRect(
        pageLinkPtr: Long,
        linkIndex: Int,
        rectIndex: Int,
    ) = nativeGetRect(pageLinkPtr, linkIndex, rectIndex)

    internal fun getTextRange(
        pageLinkPtr: Long,
        index: Int,
    ) = nativeGetTextRange(pageLinkPtr, index)

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
