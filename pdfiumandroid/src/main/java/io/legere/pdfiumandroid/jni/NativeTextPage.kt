package io.legere.pdfiumandroid.jni

import dalvik.annotation.optimization.FastNative

@Suppress("TooManyFunctions")
class NativeTextPage {
    internal fun closeTextPage(textPagePtr: Long) = nativeCloseTextPage(textPagePtr)

    internal fun textCountChars(textPagePtr: Long) = nativeTextCountChars(textPagePtr)

    internal fun textGetCharBox(
        textPagePtr: Long,
        index: Int,
    ) = nativeTextGetCharBox(textPagePtr, index)

    internal fun textGetRect(
        textPagePtr: Long,
        rectIndex: Int,
    ) = nativeTextGetRect(textPagePtr, rectIndex)

    internal fun textGetRects(
        textPagePtr: Long,
        wordRanges: IntArray,
    ) = nativeTextGetRects(textPagePtr, wordRanges)

    @Suppress("LongParameterList")
    internal fun textGetBoundedText(
        textPagePtr: Long,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        arr: ShortArray,
    ) = nativeTextGetBoundedText(textPagePtr, left, top, right, bottom, arr)

    internal fun findStart(
        textPagePtr: Long,
        findWhat: String,
        flags: Int,
        startIndex: Int,
    ) = nativeFindStart(textPagePtr, findWhat, flags, startIndex)

    internal fun loadWebLink(textPagePtr: Long) = nativeLoadWebLink(textPagePtr)

    internal fun textGetCharIndexAtPos(
        textPagePtr: Long,
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ) = nativeTextGetCharIndexAtPos(textPagePtr, x, y, xTolerance, yTolerance)

    internal fun textGetText(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
        result: ShortArray,
    ) = nativeTextGetText(textPagePtr, startIndex, count, result)

    internal fun textGetTextByteArray(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
        result: ByteArray,
    ) = nativeTextGetTextByteArray(textPagePtr, startIndex, count, result)

    internal fun textGetUnicode(
        textPagePtr: Long,
        index: Int,
    ) = nativeTextGetUnicode(textPagePtr, index)

    internal fun textCountRects(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
    ) = nativeTextCountRects(textPagePtr, startIndex, count)

    internal fun textGetTextString(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
    ) = nativeTextGetTextString(textPagePtr, startIndex, count)

    internal fun getFontSize(
        textPagePtr: Long,
        charIndex: Int,
    ) = nativeGetFontSize(textPagePtr, charIndex)

    companion object {
        @JvmStatic
        private external fun nativeCloseTextPage(textPagePtr: Long)

        @JvmStatic
        @FastNative
        private external fun nativeTextCountChars(textPagePtr: Long): Int

        @JvmStatic
        @FastNative
        private external fun nativeTextGetCharBox(
            textPagePtr: Long,
            index: Int,
        ): DoubleArray

        @JvmStatic
        @FastNative
        private external fun nativeTextGetTextString(
            textPagePtr: Long,
            startIndex: Int,
            count: Int,
        ): String

        @JvmStatic
        @FastNative
        private external fun nativeTextGetRect(
            textPagePtr: Long,
            rectIndex: Int,
        ): DoubleArray

        @JvmStatic
        @FastNative
        private external fun nativeTextGetRects(
            textPagePtr: Long,
            wordRanges: IntArray,
        ): DoubleArray?

        @Suppress("LongParameterList")
        @JvmStatic
        @FastNative
        private external fun nativeTextGetBoundedText(
            textPagePtr: Long,
            left: Double,
            top: Double,
            right: Double,
            bottom: Double,
            arr: ShortArray,
        ): Int

        @JvmStatic
        private external fun nativeFindStart(
            textPagePtr: Long,
            findWhat: String,
            flags: Int,
            startIndex: Int,
        ): Long

        @JvmStatic
        private external fun nativeLoadWebLink(textPagePtr: Long): Long

        @JvmStatic
        @FastNative
        private external fun nativeTextGetCharIndexAtPos(
            textPagePtr: Long,
            x: Double,
            y: Double,
            xTolerance: Double,
            yTolerance: Double,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeTextGetText(
            textPagePtr: Long,
            startIndex: Int,
            count: Int,
            result: ShortArray,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeTextGetTextByteArray(
            textPagePtr: Long,
            startIndex: Int,
            count: Int,
            result: ByteArray,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeTextGetUnicode(
            textPagePtr: Long,
            index: Int,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeTextCountRects(
            textPagePtr: Long,
            startIndex: Int,
            count: Int,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeGetFontSize(
            textPagePtr: Long,
            charIndex: Int,
        ): Double
    }
}
