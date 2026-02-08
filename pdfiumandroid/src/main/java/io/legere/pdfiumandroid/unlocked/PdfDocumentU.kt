@file:Suppress("unused")

package io.legere.pdfiumandroid.unlocked

import android.graphics.Matrix
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.view.Surface
import androidx.annotation.OpenForTesting
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfDocument.Bookmark
import io.legere.pdfiumandroid.PdfDocument.Meta
import io.legere.pdfiumandroid.PdfDocument.PageCount
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.PdfTextPage
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.PdfiumSource
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.defaultNativeFactory
import io.legere.pdfiumandroid.unlocked.PdfDocumentU.Companion.FPDF_INCREMENTAL
import io.legere.pdfiumandroid.unlocked.PdfDocumentU.Companion.FPDF_NO_INCREMENTAL
import io.legere.pdfiumandroid.unlocked.PdfDocumentU.Companion.FPDF_REMOVE_SECURITY
import io.legere.pdfiumandroid.util.handleAlreadyClosed
import io.legere.pdfiumandroid.util.matricesToFloatArray
import io.legere.pdfiumandroid.util.rectsToFloatArray
import java.io.Closeable

private const val MAX_RECURSION = 16

/**
 * PdfDocument represents a PDF file and allows you to load pages from it.
 */
@Suppress("TooManyFunctions")
class PdfDocumentU(
    val mNativeDocPtr: Long,
    val nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    private val pageMap = mutableMapOf<Int, PageCount>()
    private val textPageMap = mutableMapOf<Int, PageCount>()

    private val nativeDocument = nativeFactory.getNativeDocument()

    @Volatile
    var isClosed = false
        private set

    var parcelFileDescriptor: ParcelFileDescriptor? = null
    var source: PdfiumSource? = null

    /**
     *  Get the page count of the PDF document
     *  @return the number of pages
     */
    fun getPageCount(): Int {
        if (handleAlreadyClosed(isClosed)) return 0
        return nativeDocument.getPageCount(mNativeDocPtr)
    }

    /**
     *  Get the page character counts for every page of the PDF document
     *  @return an array of character counts
     */
    fun getPageCharCounts(): IntArray {
        if (handleAlreadyClosed(isClosed)) return IntArray(0)
        return nativeDocument.getPageCharCounts(mNativeDocPtr)
    }

    /**
     * Open page and store native pointer in [PdfDocumentU]
     * @param pageIndex the page index
     * @return the opened page [PdfPage]
     * @throws IllegalArgumentException if  document is closed or the page cannot be loaded,
     * RuntimeException if the page cannot be loaded
     */
    @Suppress("ReturnCount")
    fun openPage(pageIndex: Int): PdfPageU? {
        if (handleAlreadyClosed(isClosed)) return null
        if (pageMap.containsKey(pageIndex)) {
            pageMap[pageIndex]?.let {
                it.count++
//                    Timber.d("from cache openPage: pageIndex: $pageIndex, count: ${it.count}")
                return PdfPageU(this, pageIndex, it.pagePtr, pageMap)
            }
        }
//            Timber.d("openPage: pageIndex: $pageIndex")

        val pagePtr = nativeDocument.loadPage(this.mNativeDocPtr, pageIndex)
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
        nativeDocument.deletePage(this.mNativeDocPtr, pageIndex)
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
        val pagesPtr: LongArray = nativeDocument.loadPages(this.mNativeDocPtr, fromIndex, toIndex)
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
        nativeDocument.renderPagesWithMatrix(
            pages.map { it.pagePtr }.toLongArray(),
            bufferPtr,
            drawSizeX,
            drawSizeY,
            matricesToFloatArray(matrices),
            rectsToFloatArray(clipRects),
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
        return nativeDocument.renderPagesSurfaceWithMatrix(
            pages.map { it.pagePtr }.toLongArray(),
            surface,
            matricesToFloatArray(matrices),
            rectsToFloatArray(clipRects),
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
        meta.title = nativeDocument.getDocumentMetaText(mNativeDocPtr, "Title")
        meta.author = nativeDocument.getDocumentMetaText(mNativeDocPtr, "Author")
        meta.subject = nativeDocument.getDocumentMetaText(mNativeDocPtr, "Subject")
        meta.keywords = nativeDocument.getDocumentMetaText(mNativeDocPtr, "Keywords")
        meta.creator = nativeDocument.getDocumentMetaText(mNativeDocPtr, "Creator")
        meta.producer = nativeDocument.getDocumentMetaText(mNativeDocPtr, "Producer")
        meta.creationDate = nativeDocument.getDocumentMetaText(mNativeDocPtr, "CreationDate")
        meta.modDate = nativeDocument.getDocumentMetaText(mNativeDocPtr, "ModDate")
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
        bookmark.title = nativeDocument.getBookmarkTitle(bookmarkPtr)
        bookmark.pageIdx = nativeDocument.getBookmarkDestIndex(mNativeDocPtr, bookmarkPtr)
        tree.add(bookmark)
        val child = nativeDocument.getFirstChildBookmark(mNativeDocPtr, bookmarkPtr)
        if (child != 0L && levelMutable < MAX_RECURSION) {
            println("child: $child, level: $levelMutable")
            recursiveGetBookmark(bookmark.children, child, ++levelMutable)
        }
        val sibling = nativeDocument.getSiblingBookmark(mNativeDocPtr, bookmarkPtr)
        if (sibling != 0L && levelMutable < MAX_RECURSION) {
            println("sibling: $sibling, level: $levelMutable")
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
        val first = nativeDocument.getFirstChildBookmark(this.mNativeDocPtr, 0)
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
    @OpenForTesting
    fun openTextPage(page: PdfPageU): PdfTextPageU {
        check(!isClosed) { "Already closed" }
        if (textPageMap.containsKey(page.pageIndex)) {
            textPageMap[page.pageIndex]?.let {
                it.count++
//                    Timber.d("from cache openTextPage: pageIndex: ${page.pageIndex}, count: ${it.count}")
                return PdfTextPageU(this, page.pageIndex, it.pagePtr, textPageMap, nativeFactory)
            }
        }
//            Timber.d("openTextPage: pageIndex: ${page.pageIndex}")
        val textPagePtr = nativeDocument.loadTextPage(this.mNativeDocPtr, page.pagePtr)
        textPageMap[page.pageIndex] = PageCount(textPagePtr, 1)
        return PdfTextPageU(this, page.pageIndex, textPagePtr, textPageMap, nativeFactory)
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
        val textPagesPtr: LongArray = nativeDocument.loadPages(mNativeDocPtr, fromIndex, toIndex)
        return textPagesPtr.mapIndexed { index: Int, pagePtr: Long ->
            PdfTextPageU(
                this,
                fromIndex + index,
                pagePtr,
                textPageMap,
                nativeFactory,
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
        return nativeDocument.saveAsCopy(mNativeDocPtr, callback, flags)
    }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        if (handleAlreadyClosed(isClosed)) return
        Logger.d(TAG, "PdfDocument.close")
        isClosed = true
        nativeDocument.closeDocument(mNativeDocPtr)
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
