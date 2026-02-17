/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.legere.pdfiumandroid.core.jni

import android.view.Surface
import io.legere.pdfiumandroid.api.PdfWriteCallback

/**
 * Contract for native PDFium document operations.
 * This interface defines the JNI methods for interacting with PDF documents as a whole.
 * Implementations of this contract are intended for **internal use only**
 * within the PdfiumAndroid library to abstract native calls.
 */
@Suppress("TooManyFunctions")
interface NativeDocumentContract {
    /**
     * Gets the total number of pages in the PDF document.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @return The number of pages.
     */
    fun getPageCount(docPtr: Long): Int

    /**
     * Loads a specific page from the PDF document.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param pageIndex The 0-based index of the page to load.
     * @return A native pointer (long) to the loaded PDF page.
     */
    fun loadPage(
        docPtr: Long,
        pageIndex: Int,
    ): Long

    /**
     * Deletes a page from the PDF document.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param pageIndex The 0-based index of the page to delete.
     */
    fun deletePage(
        docPtr: Long,
        pageIndex: Int,
    )

    /**
     * Closes the PDF document.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document to close.
     */
    fun closeDocument(docPtr: Long)

    /**
     * Loads a range of pages from the PDF document.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param fromIndex The 0-based start index of the page range.
     * @param toIndex The 0-based end index of the page range.
     * @return A [LongArray] of native pointers to the loaded PDF pages.
     */
    fun loadPages(
        docPtr: Long,
        fromIndex: Int,
        toIndex: Int,
    ): LongArray

    /**
     * Retrieves metadata text from the PDF document.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param tag The name of the metadata tag (e.g., "Title", "Author").
     * @return The metadata text as a [String].
     */
    fun getDocumentMetaText(
        docPtr: Long,
        tag: String,
    ): String

    /**
     * Gets the first child bookmark of a given parent bookmark.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param bookmarkPtr The native pointer (long) to the parent bookmark, or 0L for top-level bookmarks.
     * @return A native pointer (long) to the first child bookmark, or 0L if none exists.
     */
    fun getFirstChildBookmark(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long

    /**
     * Gets the next sibling bookmark of a given bookmark.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param bookmarkPtr The native pointer (long) to the current bookmark.
     * @return A native pointer (long) to the next sibling bookmark, or 0L if none exists.
     */
    fun getSiblingBookmark(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long

    /**
     * Gets the destination page index of a bookmark.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param bookmarkPtr The native pointer (long) to the bookmark.
     * @return The 0-based page index of the bookmark's destination.
     */
    fun getBookmarkDestIndex(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long

    /**
     * Loads the text page object for a given PDF page.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A native pointer (long) to the loaded PDF text page.
     */
    fun loadTextPage(
        docPtr: Long,
        pagePtr: Long,
    ): Long

    /**
     * Gets the title of a bookmark.
     * This is a JNI method.
     *
     * @param bookmarkPtr The native pointer (long) to the bookmark.
     * @return The title of the bookmark as a [String].
     */
    fun getBookmarkTitle(bookmarkPtr: Long): String

    /**
     * Saves a copy of the PDF document.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param callback The [io.legere.pdfiumandroid.api.PdfWriteCallback] to receive the written data.
     * @param flags An integer representing save flags (e.g., incremental, no security).
     * @return `true` if the document was successfully saved, `false` otherwise.
     */
    fun saveAsCopy(
        docPtr: Long,
        callback: PdfWriteCallback,
        flags: Int,
    ): Boolean

    /**
     * Gets the character counts for all pages in the PDF document.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @return An [IntArray] where each element is the character count for the corresponding page.
     */
    fun getPageCharCounts(docPtr: Long): IntArray

    /**
     * Renders multiple PDF pages with transformation matrices onto a pre-locked [Surface] buffer.
     * This is a JNI method.
     *
     * @param pages An array of native pointers (long) to the PDF pages to render.
     * @param bufferPtr The native pointer (long) to the locked [Surface] buffer's pixel data.
     * @param drawSizeHor The horizontal size of the rendering area in device pixels.
     * @param drawSizeVer The vertical size of the rendering area in device pixels.
     * @param matrixFloats A `FloatArray` containing concatenated 2x3 transformation matrices for each page.
     * @param clipFloats A `FloatArray` containing concatenated 4-element clipping
     * rectangles [left, top, right, bottom] for each page.
     * @param renderAnnot `true` to render annotations, `false` otherwise.
     * @param textMask `true` to render text as an image mask, `false` otherwise. (Currently ignored by Pdfium)
     * @param canvasColor The ARGB color to fill the canvas background. Use 0 for no fill.
     * @param pageBackgroundColor The ARGB color to fill the page background. Use 0 for no fill.
     */
    @Suppress("LongParameterList")
    fun renderPagesWithMatrix(
        pages: LongArray,
        bufferPtr: Long,
        drawSizeHor: Int,
        drawSizeVer: Int,
        matrixFloats: FloatArray,
        clipFloats: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    )

    /**
     * Renders multiple PDF pages with transformation matrices directly onto an Android [Surface].
     * This is a JNI method.
     *
     * @param pages An array of native pointers (long) to the PDF pages to render.
     * @param surface The [Surface] to render onto.
     * @param matrixFloats A `FloatArray` containing concatenated 2x3 transformation matrices for each page.
     * @param clipFloats A `FloatArray` containing concatenated 4-element clipping rectangles
     * [left, top, right, bottom] for each page.
     * @param renderAnnot `true` to render annotations, `false` otherwise.
     * @param textMask `true` to render text as an image mask, `false` otherwise. (Currently ignored by Pdfium)
     * @param canvasColor The ARGB color to fill the canvas background. Use 0 for no fill.
     * @param pageBackgroundColor The ARGB color to fill the page background. Use 0 for no fill.
     * @return `true` if rendering was successful, `false` otherwise.
     */
    @Suppress("LongParameterList")
    fun renderPagesSurfaceWithMatrix(
        pages: LongArray,
        surface: Surface,
        matrixFloats: FloatArray,
        clipFloats: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ): Boolean
}

@Suppress("TooManyFunctions")
class NativeDocument : NativeDocumentContract {
    private external fun nativeGetPageCount(docPtr: Long): Int

    private external fun nativeLoadPage(
        docPtr: Long,
        pageIndex: Int,
    ): Long

    private external fun nativeDeletePage(
        docPtr: Long,
        pageIndex: Int,
    )

    private external fun nativeCloseDocument(docPtr: Long)

    private external fun nativeLoadPages(
        docPtr: Long,
        fromIndex: Int,
        toIndex: Int,
    ): LongArray

    private external fun nativeGetDocumentMetaText(
        docPtr: Long,
        tag: String,
    ): String

    private external fun nativeGetFirstChildBookmark(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long

    private external fun nativeGetSiblingBookmark(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long

    private external fun nativeGetBookmarkDestIndex(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long

    private external fun nativeLoadTextPage(
        docPtr: Long,
        pagePtr: Long,
    ): Long

    private external fun nativeGetBookmarkTitle(bookmarkPtr: Long): String

    private external fun nativeSaveAsCopy(
        docPtr: Long,
        callback: PdfWriteCallback,
        flags: Int,
    ): Boolean

    private external fun nativeGetPageCharCounts(docPtr: Long): IntArray

    @Suppress("LongParameterList")
    private external fun nativeRenderPagesWithMatrix(
        pages: LongArray,
        bufferPtr: Long,
        drawSizeHor: Int,
        drawSizeVer: Int,
        matrixFloats: FloatArray,
        clipFloats: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    )

    @Suppress("LongParameterList")
    private external fun nativeRenderPagesSurfaceWithMatrix(
        pages: LongArray,
        surface: Surface,
        matrixFloats: FloatArray,
        clipFloats: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ): Boolean

    override fun getPageCount(docPtr: Long): Int = nativeGetPageCount(docPtr)

    override fun loadPage(
        docPtr: Long,
        pageIndex: Int,
    ): Long = nativeLoadPage(docPtr, pageIndex)

    override fun deletePage(
        docPtr: Long,
        pageIndex: Int,
    ) = nativeDeletePage(docPtr, pageIndex)

    override fun closeDocument(docPtr: Long) = nativeCloseDocument(docPtr)

    override fun loadPages(
        docPtr: Long,
        fromIndex: Int,
        toIndex: Int,
    ): LongArray = nativeLoadPages(docPtr, fromIndex, toIndex)

    override fun getDocumentMetaText(
        docPtr: Long,
        tag: String,
    ): String = nativeGetDocumentMetaText(docPtr, tag)

    override fun getFirstChildBookmark(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long = nativeGetFirstChildBookmark(docPtr, bookmarkPtr)

    override fun getSiblingBookmark(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long = nativeGetSiblingBookmark(docPtr, bookmarkPtr)

    override fun getBookmarkDestIndex(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long = nativeGetBookmarkDestIndex(docPtr, bookmarkPtr)

    override fun loadTextPage(
        docPtr: Long,
        pagePtr: Long,
    ): Long = nativeLoadTextPage(docPtr, pagePtr)

    override fun getBookmarkTitle(bookmarkPtr: Long): String = nativeGetBookmarkTitle(bookmarkPtr)

    override fun saveAsCopy(
        docPtr: Long,
        callback: PdfWriteCallback,
        flags: Int,
    ): Boolean = nativeSaveAsCopy(docPtr, callback, flags)

    override fun getPageCharCounts(docPtr: Long): IntArray = nativeGetPageCharCounts(docPtr)

    @Suppress("LongParameterList")
    override fun renderPagesWithMatrix(
        pages: LongArray,
        bufferPtr: Long,
        drawSizeHor: Int,
        drawSizeVer: Int,
        matrixFloats: FloatArray,
        clipFloats: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ) = nativeRenderPagesWithMatrix(
        pages,
        bufferPtr,
        drawSizeHor,
        drawSizeVer,
        matrixFloats,
        clipFloats,
        renderAnnot,
        textMask,
        canvasColor,
        pageBackgroundColor,
    )

    @Suppress("LongParameterList")
    override fun renderPagesSurfaceWithMatrix(
        pages: LongArray,
        surface: Surface,
        matrixFloats: FloatArray,
        clipFloats: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ): Boolean =
        nativeRenderPagesSurfaceWithMatrix(
            pages,
            surface,
            matrixFloats,
            clipFloats,
            renderAnnot,
            textMask,
            canvasColor,
            pageBackgroundColor,
        )
}
