package io.legere.pdfiumandroid.jni

import android.view.Surface
import io.legere.pdfiumandroid.PdfWriteCallback

@Suppress("TooManyFunctions")
class NativeDocument {
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

    internal fun getPageCount(docPtr: Long): Int = nativeGetPageCount(docPtr)

    internal fun loadPage(
        docPtr: Long,
        pageIndex: Int,
    ): Long = nativeLoadPage(docPtr, pageIndex)

    internal fun deletePage(
        docPtr: Long,
        pageIndex: Int,
    ) = nativeDeletePage(docPtr, pageIndex)

    internal fun closeDocument(docPtr: Long) = nativeCloseDocument(docPtr)

    internal fun loadPages(
        docPtr: Long,
        fromIndex: Int,
        toIndex: Int,
    ): LongArray = nativeLoadPages(docPtr, fromIndex, toIndex)

    internal fun getDocumentMetaText(
        docPtr: Long,
        tag: String,
    ): String = nativeGetDocumentMetaText(docPtr, tag)

    internal fun getFirstChildBookmark(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long = nativeGetFirstChildBookmark(docPtr, bookmarkPtr)

    internal fun getSiblingBookmark(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long = nativeGetSiblingBookmark(docPtr, bookmarkPtr)

    internal fun getBookmarkDestIndex(
        docPtr: Long,
        bookmarkPtr: Long,
    ): Long = nativeGetBookmarkDestIndex(docPtr, bookmarkPtr)

    internal fun loadTextPage(
        docPtr: Long,
        pagePtr: Long,
    ): Long = nativeLoadTextPage(docPtr, pagePtr)

    internal fun getBookmarkTitle(bookmarkPtr: Long): String = nativeGetBookmarkTitle(bookmarkPtr)

    internal fun saveAsCopy(
        docPtr: Long,
        callback: PdfWriteCallback,
        flags: Int,
    ): Boolean = nativeSaveAsCopy(docPtr, callback, flags)

    internal fun getPageCharCounts(docPtr: Long): IntArray = nativeGetPageCharCounts(docPtr)

    @Suppress("LongParameterList")
    internal fun renderPagesWithMatrix(
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
    internal fun renderPagesSurfaceWithMatrix(
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
