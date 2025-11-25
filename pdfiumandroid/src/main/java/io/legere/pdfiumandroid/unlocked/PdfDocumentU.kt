@file:Suppress("unused")

package io.legere.pdfiumandroid.unlocked

import android.graphics.Matrix
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.view.Surface
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfDocument.Bookmark
import io.legere.pdfiumandroid.PdfDocument.Meta
import io.legere.pdfiumandroid.PdfDocument.PageCount
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.PdfTextPage
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.PdfiumSource
import io.legere.pdfiumandroid.unlocked.PdfDocumentU.Companion.FPDF_INCREMENTAL
import io.legere.pdfiumandroid.unlocked.PdfDocumentU.Companion.FPDF_NO_INCREMENTAL
import io.legere.pdfiumandroid.unlocked.PdfDocumentU.Companion.FPDF_REMOVE_SECURITY
import io.legere.pdfiumandroid.util.handleAlreadyClosed
import java.io.Closeable

private const val MAX_RECURSION = 16
private const val THREE_BY_THREE = 9

/**
 * PdfDocument represents a PDF file and allows you to load pages from it.
 */
@Suppress("TooManyFunctions")
class PdfDocumentU(
    val mNativeDocPtr: Long,
) : Closeable {
    private val pageMap = mutableMapOf<Int, PageCount>()
    private val textPageMap = mutableMapOf<Int, PageCount>()

    @Volatile
    var isClosed = false
        private set

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

    var parcelFileDescriptor: ParcelFileDescriptor? = null
    var source: PdfiumSource? = null

    /**
     *  Get the page count of the PDF document
     *  @return the number of pages
     */
    fun getPageCount(): Int {
        if (handleAlreadyClosed(isClosed)) return 0
        return nativeGetPageCount(mNativeDocPtr)
    }

    /**
     *  Get the page character counts for every page of the PDF document
     *  @return an array of character counts
     */
    fun getPageCharCounts(): IntArray {
        if (handleAlreadyClosed(isClosed)) return IntArray(0)
        return nativeGetPageCharCounts(mNativeDocPtr)
    }

    /**
     * Open page and store native pointer in [PdfDocumentU]
     * @param pageIndex the page index
     * @return the opened page [PdfPage]
     * @throws IllegalArgumentException if  document is closed or the page cannot be loaded,
     * RuntimeException if the page cannot be loaded
     */
    fun openPage(pageIndex: Int): PdfPageU {
        check(!isClosed) { "Already closed" }
        if (pageMap.containsKey(pageIndex)) {
            pageMap[pageIndex]?.let {
                it.count++
//                    Timber.d("from cache openPage: pageIndex: $pageIndex, count: ${it.count}")
                return PdfPageU(this, pageIndex, it.pagePtr, pageMap)
            }
        }
//            Timber.d("openPage: pageIndex: $pageIndex")

        val pagePtr = nativeLoadPage(this.mNativeDocPtr, pageIndex)
        pageMap[pageIndex] = PageCount(pagePtr, 1)
        return PdfPageU(this, pageIndex, pagePtr, pageMap)
    }

    /**
     * Delete page
     * @param pageIndex the page index
     * @throws IllegalArgumentException if document is closed
     */
    fun deletePage(pageIndex: Int) {
        if (handleAlreadyClosed(isClosed)) return
        nativeDeletePage(this.mNativeDocPtr, pageIndex)
    }

    /**
     * Open range of pages and store native pointers in [PdfDocumentU]
     * @param fromIndex the start index of the range
     * @param toIndex the end index of the range
     * @return the opened pages [PdfPage]
     * @throws IllegalArgumentException if document is closed or the pages cannot be loaded
     */
    fun openPages(
        fromIndex: Int,
        toIndex: Int,
    ): List<PdfPageU> {
        if (handleAlreadyClosed(isClosed)) return emptyList()
        val pagesPtr: LongArray = nativeLoadPages(this.mNativeDocPtr, fromIndex, toIndex)
        var pageIndex = fromIndex
        for (page in pagesPtr) {
            if (pageIndex > toIndex) break
            pageIndex++
        }
        return pagesPtr.map { PdfPageU(this, pageIndex, it, pageMap) }
    }

    /**
     * Render page fragment on [Surface].<br></br>
     * @param bufferPtr Surface's buffer on which to render page
     * @param pages The pages to render
     * @param matrices The matrices to map the pages to the surface
     * @param clipRects The rectangles to clip the pages to
     * @param renderAnnot whether render annotation
     * @param textMask whether to render text as image mask - currently ignored
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     * You almost always want this to be white (the default)
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList")
    fun renderPages(
        bufferPtr: Long,
        drawSizeX: Int,
        drawSizeY: Int,
        pages: List<PdfPageU>,
        matrices: List<Matrix>,
        clipRects: List<RectF>,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ) {
        if (handleAlreadyClosed(isClosed || pages.any { it.isClosed })) return
        val matrixFloats =
            matrices
                .flatMap { matrix ->
                    val matrixValues = FloatArray(THREE_BY_THREE)
                    matrix.getValues(matrixValues)
                    listOf(
                        matrixValues[Matrix.MSCALE_X],
                        matrixValues[Matrix.MTRANS_X],
                        matrixValues[Matrix.MTRANS_Y],
                    )
                }.toFloatArray()
        val clipFloats =
            clipRects
                .flatMap { rect ->
                    listOf(
                        rect.left,
                        rect.top,
                        rect.right,
                        rect.bottom,
                    )
                }.toFloatArray()
        nativeRenderPagesWithMatrix(
            pages.map { it.pagePtr }.toLongArray(),
            bufferPtr,
            drawSizeX,
            drawSizeY,
            matrixFloats,
            clipFloats,
            renderAnnot,
            textMask,
            canvasColor,
            pageBackgroundColor,
        )
    }

    @Suppress("LongParameterList")
    fun renderPages(
        surface: Surface,
        pages: List<PdfPageU>,
        matrices: List<Matrix>,
        clipRects: List<RectF>,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean {
        if (handleAlreadyClosed(isClosed || pages.any { it.isClosed })) return false
        val matrixFloats =
            matrices
                .flatMap { matrix ->
                    val matrixValues = FloatArray(THREE_BY_THREE)
                    matrix.getValues(matrixValues)
                    listOf(
                        matrixValues[Matrix.MSCALE_X],
                        matrixValues[Matrix.MTRANS_X],
                        matrixValues[Matrix.MTRANS_Y],
                    )
                }.toFloatArray()
        val clipFloats =
            clipRects
                .flatMap { rect ->
                    listOf(
                        rect.left,
                        rect.top,
                        rect.right,
                        rect.bottom,
                    )
                }.toFloatArray()
        return nativeRenderPagesSurfaceWithMatrix(
            pages.map { it.pagePtr }.toLongArray(),
            surface,
            matrixFloats,
            clipFloats,
            renderAnnot,
            textMask,
            canvasColor,
            pageBackgroundColor,
        )
    }

    /**
     * Get metadata for given document
     * @return the [Meta] data
     * @throws IllegalArgumentException if document is closed
     */
    fun getDocumentMeta(): Meta {
        if (handleAlreadyClosed(isClosed)) return Meta()
        val meta = Meta()
        meta.title = nativeGetDocumentMetaText(mNativeDocPtr, "Title")
        meta.author = nativeGetDocumentMetaText(mNativeDocPtr, "Author")
        meta.subject = nativeGetDocumentMetaText(mNativeDocPtr, "Subject")
        meta.keywords = nativeGetDocumentMetaText(mNativeDocPtr, "Keywords")
        meta.creator = nativeGetDocumentMetaText(mNativeDocPtr, "Creator")
        meta.producer = nativeGetDocumentMetaText(mNativeDocPtr, "Producer")
        meta.creationDate = nativeGetDocumentMetaText(mNativeDocPtr, "CreationDate")
        meta.modDate = nativeGetDocumentMetaText(mNativeDocPtr, "ModDate")
        return meta
    }

    internal fun recursiveGetBookmark(
        tree: MutableList<Bookmark>,
        bookmarkPtr: Long,
        level: Long,
    ) {
        if (handleAlreadyClosed(isClosed)) return
        var levelMutable = level
        val bookmark = Bookmark()
        bookmark.mNativePtr = bookmarkPtr
        bookmark.title = nativeGetBookmarkTitle(bookmarkPtr)
        bookmark.pageIdx = nativeGetBookmarkDestIndex(mNativeDocPtr, bookmarkPtr)
        tree.add(bookmark)
        val child = nativeGetFirstChildBookmark(mNativeDocPtr, bookmarkPtr)
        if (child != 0L && levelMutable < MAX_RECURSION) {
            recursiveGetBookmark(bookmark.children, child, levelMutable++)
        }
        val sibling = nativeGetSiblingBookmark(mNativeDocPtr, bookmarkPtr)
        if (sibling != 0L && levelMutable < MAX_RECURSION) {
            recursiveGetBookmark(tree, sibling, levelMutable)
        }
    }

    /**
     * Get table of contents (bookmarks) for given document
     * @return the [Bookmark] list
     * @throws IllegalArgumentException if document is closed
     */
    fun getTableOfContents(): List<Bookmark> {
        if (handleAlreadyClosed(isClosed)) return emptyList()
        val topLevel: MutableList<Bookmark> =
            ArrayList()
        val first = nativeGetFirstChildBookmark(this.mNativeDocPtr, 0)
        if (first != 0L) {
            recursiveGetBookmark(topLevel, first, 1)
        }
        return topLevel
    }

    /**
     * Open a text page
     * @param page the [PdfPage]
     * @return the opened [PdfTextPage]
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    @Deprecated("Use PdfPage.openTextPage instead", ReplaceWith("page.openTextPage()"))
    internal fun openTextPage(page: PdfPageU): PdfTextPageU {
        check(!isClosed) { "Already closed" }
        if (textPageMap.containsKey(page.pageIndex)) {
            textPageMap[page.pageIndex]?.let {
                it.count++
//                    Timber.d("from cache openTextPage: pageIndex: ${page.pageIndex}, count: ${it.count}")
                return PdfTextPageU(this, page.pageIndex, it.pagePtr, textPageMap)
            }
        }
//            Timber.d("openTextPage: pageIndex: ${page.pageIndex}")
        val textPagePtr = nativeLoadTextPage(this.mNativeDocPtr, page.pagePtr)
        textPageMap[page.pageIndex] = PageCount(textPagePtr, 1)
        return PdfTextPageU(this, page.pageIndex, textPagePtr, textPageMap)
    }

    /**
     * Open a range of text pages
     * @param fromIndex the start index of the range
     * @param toIndex the end index of the range
     * @return the opened [PdfTextPage] list
     * @throws IllegalArgumentException if document is closed or the pages cannot be loaded
     */
    fun openTextPages(
        fromIndex: Int,
        toIndex: Int,
    ): List<PdfTextPageU> {
        if (handleAlreadyClosed(isClosed)) return emptyList()
        val textPagesPtr: LongArray = nativeLoadPages(mNativeDocPtr, fromIndex, toIndex)
        return textPagesPtr.mapIndexed { index: Int, pagePtr: Long ->
            PdfTextPageU(
                this,
                fromIndex + index,
                pagePtr,
                textPageMap,
            )
        }
    }

    /**
     * Save document as a copy
     * @param callback the [PdfWriteCallback] to be called with the data
     * @param flags must be one of [FPDF_INCREMENTAL], [FPDF_NO_INCREMENTAL] or [FPDF_REMOVE_SECURITY]
     * @return true if the document was successfully saved
     * @throws IllegalArgumentException if document is closed
     */
    fun saveAsCopy(
        callback: PdfWriteCallback,
        flags: Int = FPDF_NO_INCREMENTAL,
    ): Boolean {
        if (handleAlreadyClosed(isClosed)) return false
        return nativeSaveAsCopy(mNativeDocPtr, callback, flags)
    }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        if (handleAlreadyClosed(isClosed)) return
        Logger.d(TAG, "PdfDocument.close")
        isClosed = true
        nativeCloseDocument(mNativeDocPtr)
        parcelFileDescriptor?.close()
        parcelFileDescriptor = null
        source?.close()
        source = null
    }

    companion object {
        private val TAG = PdfDocumentU::class.java.name

        const val FPDF_INCREMENTAL = 1
        const val FPDF_NO_INCREMENTAL = 2
        const val FPDF_REMOVE_SECURITY = 3
    }
}
