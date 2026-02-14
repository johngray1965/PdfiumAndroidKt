package io.legere.pdfiumandroid.jni

import dalvik.annotation.optimization.FastNative

/**
 * Contract for native PDFium text page operations.
 * This interface defines the JNI methods for interacting with text content on PDF pages.
 * Implementations of this contract are intended for **internal use only**
 * within the PdfiumAndroid library to abstract native calls.
 */
@Suppress("TooManyFunctions")
interface NativeTextPageContract {
    /**
     * Closes a native PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page to close.
     */
    fun closeTextPage(textPagePtr: Long)

    /**
     * Gets the number of characters on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @return The total number of characters on the page.
     */
    fun textCountChars(textPagePtr: Long): Int

    /**
     * Gets the bounding box of a character on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param index The 0-based index of the character.
     * @return A `DoubleArray` of 4 elements [left, right, bottom, top] representing the character's bounding box.
     * Note: The order of elements might be different from typical Android `RectF`.
     */
    fun textGetCharBox(
        textPagePtr: Long,
        index: Int,
    ): DoubleArray

    /**
     * Gets the bounding rectangle of a specific text segment on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param rectIndex The 0-based index of the rectangle.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the rectangle.
     */
    fun textGetRect(
        textPagePtr: Long,
        rectIndex: Int,
    ): FloatArray

    /**
     * Gets multiple bounding rectangles for specified word ranges on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param wordRanges An [IntArray] where pairs of `(startIndex, length)` define the text ranges.
     * @return A `FloatArray` containing concatenated `[left, top, right, bottom, rangeStart, rangeLength]`
     * for each rectangle, or `null` if no data.
     */
    fun textGetRects(
        textPagePtr: Long,
        wordRanges: IntArray,
    ): FloatArray?

    /**
     * Gets text bounded by a given rectangle on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param left The left coordinate of the bounding rectangle.
     * @param top The top coordinate of the bounding rectangle.
     * @param right The right coordinate of the bounding rectangle.
     * @param bottom The bottom coordinate of the bounding rectangle.
     * @param arr A `ShortArray` to store the bounded text characters.
     * @return The number of characters written into the array, including the null terminator.
     */
    @Suppress("LongParameterList")
    fun textGetBoundedText(
        textPagePtr: Long,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        arr: ShortArray,
    ): Int

    /**
     * Initiates a text search operation on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param findWhat The string to search for.
     * @param flags An integer representing a bitmask of search flags (e.g., case sensitivity).
     * @param startIndex The 0-based character index to start the search from.
     * @return A native pointer (long) to the search handle.
     */
    fun findStart(
        textPagePtr: Long,
        findWhat: String,
        flags: Int,
        startIndex: Int,
    ): Long

    /**
     * Loads web links present on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @return A native pointer (long) to the PDF page link object.
     */
    fun loadWebLink(textPagePtr: Long): Long

    /**
     * Gets the character index at a specific position on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param x The X coordinate in page space.
     * @param y The Y coordinate in page space.
     * @param xTolerance The horizontal tolerance for finding a character.
     * @param yTolerance The vertical tolerance for finding a character.
     * @return The 0-based character index at the given position, or -1 if none found.
     */
    @Suppress("LongParameterList")
    fun textGetCharIndexAtPos(
        textPagePtr: Long,
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ): Int

    /**
     * Gets a portion of the text from a PDF text page.
     * This is a JNI method (legacy, prefer [textGetTextString]).
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param startIndex The 0-based index of the first character to retrieve.
     * @param count The number of characters to retrieve.
     * @param result A `ShortArray` to store the characters.
     * @return The number of characters written into the array, including the null terminator.
     */
    fun textGetText(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
        result: ShortArray,
    ): Int

    /**
     * Gets a portion of the text from a PDF text page as a byte array.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param startIndex The 0-based index of the first character to retrieve.
     * @param count The number of characters to retrieve.
     * @param result A `ByteArray` to store the characters.
     * @return The number of characters written into the array.
     */
    fun textGetTextByteArray(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
        result: ByteArray,
    ): Int

    /**
     * Gets the Unicode value of a character at a specific index on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param index The 0-based index of the character.
     * @return The Unicode value (int) of the character.
     */
    fun textGetUnicode(
        textPagePtr: Long,
        index: Int,
    ): Int

    /**
     * Counts the number of bounding rectangles for a given text range on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param startIndex The 0-based index of the first character in the range.
     * @param count The number of characters in the range.
     * @return The number of bounding rectangles for the specified text range.
     */
    fun textCountRects(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
    ): Int

    /**
     * Gets a portion of the text from a PDF text page as a String.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param startIndex The 0-based index of the first character to retrieve.
     * @param count The number of characters to retrieve.
     * @return The retrieved text as a [String].
     */
    fun textGetTextString(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
    ): String

    /**
     * Gets the font size of a character on a PDF text page.
     * This is a JNI method.
     *
     * @param textPagePtr The native pointer (long) to the PDF text page.
     * @param charIndex The 0-based index of the character.
     * @return The font size in PostScript points (1/72th of an inch).
     */
    fun getFontSize(
        textPagePtr: Long,
        charIndex: Int,
    ): Double
}

@Suppress("TooManyFunctions")
internal class NativeTextPage : NativeTextPageContract {
    override fun closeTextPage(textPagePtr: Long) = nativeCloseTextPage(textPagePtr)

    override fun textCountChars(textPagePtr: Long) = nativeTextCountChars(textPagePtr)

    override fun textGetCharBox(
        textPagePtr: Long,
        index: Int,
    ) = nativeTextGetCharBox(textPagePtr, index)

    override fun textGetRect(
        textPagePtr: Long,
        rectIndex: Int,
    ) = nativeTextGetRect(textPagePtr, rectIndex)

    override fun textGetRects(
        textPagePtr: Long,
        wordRanges: IntArray,
    ) = nativeTextGetRects(textPagePtr, wordRanges)

    @Suppress("LongParameterList")
    override fun textGetBoundedText(
        textPagePtr: Long,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        arr: ShortArray,
    ) = nativeTextGetBoundedText(textPagePtr, left, top, right, bottom, arr)

    override fun findStart(
        textPagePtr: Long,
        findWhat: String,
        flags: Int,
        startIndex: Int,
    ) = nativeFindStart(textPagePtr, findWhat, flags, startIndex)

    override fun loadWebLink(textPagePtr: Long) = nativeLoadWebLink(textPagePtr)

    override fun textGetCharIndexAtPos(
        textPagePtr: Long,
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double,
    ) = nativeTextGetCharIndexAtPos(textPagePtr, x, y, xTolerance, yTolerance)

    override fun textGetText(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
        result: ShortArray,
    ) = nativeTextGetText(textPagePtr, startIndex, count, result)

    override fun textGetTextByteArray(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
        result: ByteArray,
    ) = nativeTextGetTextByteArray(textPagePtr, startIndex, count, result)

    override fun textGetUnicode(
        textPagePtr: Long,
        index: Int,
    ) = nativeTextGetUnicode(textPagePtr, index)

    override fun textCountRects(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
    ) = nativeTextCountRects(textPagePtr, startIndex, count)

    override fun textGetTextString(
        textPagePtr: Long,
        startIndex: Int,
        count: Int,
    ) = nativeTextGetTextString(textPagePtr, startIndex, count)

    override fun getFontSize(
        textPagePtr: Long,
        charIndex: Int,
    ) = nativeGetFontSize(textPagePtr, charIndex)

    /**
     * @suppress
     */
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
        ): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeTextGetRects(
            textPagePtr: Long,
            wordRanges: IntArray,
        ): FloatArray?

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
