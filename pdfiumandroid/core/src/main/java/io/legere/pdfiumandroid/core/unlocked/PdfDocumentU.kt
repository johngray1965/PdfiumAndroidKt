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

@file:Suppress("unused")

package io.legere.pdfiumandroid.core.unlocked

import android.graphics.Matrix
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.view.Surface
import androidx.annotation.OpenForTesting
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.ImmutableMatrix
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.api.handleAlreadyClosed
import io.legere.pdfiumandroid.api.pdfiumConfig
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.defaultNativeFactory
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU.Companion.FPDF_INCREMENTAL
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU.Companion.FPDF_NO_INCREMENTAL
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU.Companion.FPDF_REMOVE_SECURITY
import io.legere.pdfiumandroid.core.util.PageCount
import io.legere.pdfiumandroid.core.util.matricesToFloatArray
import io.legere.pdfiumandroid.core.util.rectsToFloatArray
import java.io.Closeable

private const val MAX_RECURSION = 16

/**
 * Represents an **unlocked** PDF document and provides raw access to its pages and metadata.
 * This class is for **internal use only** within the PdfiumAndroid library.
 * Direct use from outside the library is not recommended as it bypasses thread-safety mechanisms.
 *
 * @property mNativeDocPtr The native pointer to the FPDF_DOCUMENT object.
 * @property nativeFactory The factory to provide native interface implementations.
 */
@Suppress("TooManyFunctions")
class PdfDocumentU(
    val mNativeDocPtr: Long,
    val nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    private val pageMap = mutableMapOf<Int, PageCount>()
    private val textPageMap = mutableMapOf<Int, PageCount>()

    /**
     * Indexes of pages that every holder has closed but which are still open natively, in the order
     * they were released. Their [PageCount.count] is 0, so reopening one costs nothing and the next
     * release past [io.legere.pdfiumandroid.api.Config.pageRetentionCount] evicts the oldest.
     */
    private val retainedPages = LinkedHashSet<Int>()

    /**
     * Represents a key for caching transformation matrices.
     * For internal use only.
     *
     * @property pageWidth The width of the page in pixels.
     * @property pageHeight The height of the page in pixels.
     * @property rotation The rotation of the page.
     * @property right The right boundary for the matrix calculation.
     * @property bottom The bottom boundary for the matrix calculation.
     */
    internal data class MatrixKey(
        val pageWidth: Int,
        val pageHeight: Int,
        val rotation: Int,
        val right: Int,
        val bottom: Int,
    )

    private val matrixCache = mutableMapOf<MatrixKey, ImmutableMatrix>()

    /**
     * Retrieves a cached transformation matrix or computes and caches a new one.
     * For internal use only.
     *
     * @param key The [MatrixKey] used to identify the cached matrix.
     * @param calculate A lambda function that takes a [Matrix] and computes its values if not cached.
     * @return The cached or newly computed [Matrix].
     */
    internal fun getCachedMatrix(
        key: MatrixKey,
        calculate: (Matrix) -> Unit,
    ): ImmutableMatrix =
        matrixCache.getOrPut(key) {
            ImmutableMatrix(Matrix().also(calculate))
        }

    private val nativeDocument = nativeFactory.getNativeDocument()

    // Only needed when this document has to close pages itself, which a document whose pages the
    // caller closes never does. Resolved on first use, unsynchronized like the rest of this layer.
    private val nativePage by lazy(LazyThreadSafetyMode.NONE) { nativeFactory.getNativePage() }
    private val nativeTextPage by lazy(LazyThreadSafetyMode.NONE) { nativeFactory.getNativeTextPage() }

    @Volatile
    var isClosed = false
        private set

    var parcelFileDescriptor: ParcelFileDescriptor? = null
    var source: PdfiumSource? = null

    /**
     * Get the page count of the PDF document.
     * For internal use only.
     *
     * @return the number of pages
     * @throws IllegalStateException if document is closed
     */
    fun getPageCount(): Int {
        if (handleAlreadyClosed(isClosed)) return 0
        return nativeDocument.getPageCount(mNativeDocPtr)
    }

    /**
     * Get the page character counts for every page of the PDF document.
     * For internal use only.
     *
     * @return an array of character counts
     * @throws IllegalStateException if document is closed
     */
    fun getPageCharCounts(): IntArray {
        if (handleAlreadyClosed(isClosed)) return IntArray(0)
        return nativeDocument.getPageCharCounts(mNativeDocPtr)
    }

    /**
     * Open page and store native pointer in [PdfDocumentU].
     * For internal use only.
     *
     * @param pageIndex the page index
     * @return the opened page [PdfPageU], or `null` if the document is closed or the page cannot be loaded.
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded,
     * RuntimeException if the page cannot be loaded
     */
    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    fun openPage(pageIndex: Int): PdfPageU? {
        if (handleAlreadyClosed(isClosed)) return null
        try {
            if (pageMap.containsKey(pageIndex)) {
                pageMap[pageIndex]?.let {
                    it.count++
                    // The page has a holder again, so it is no longer a candidate for eviction
                    retainedPages.remove(pageIndex)
                    return PdfPageU(this, pageIndex, it.pagePtr, pageMap, nativeFactory)
                }
            }
            val pagePtr = nativeDocument.loadPage(this.mNativeDocPtr, pageIndex)
            pageMap[pageIndex] = PageCount(pagePtr, 1)
            return PdfPageU(this, pageIndex, pagePtr, pageMap, nativeFactory)
        } catch (e: RuntimeException) {
            Logger.e(TAG, e, "openPage: pageIndex: $pageIndex $e")
            return null
        }
    }

    /**
     * Get a page's size in pixels without opening it.
     * For internal use only.
     *
     * PDFium reads the size off the document itself, so this avoids the cost of loading the page.
     * Prefer it over opening a page when the size is all that is wanted.
     *
     * @param pageIndex the page index
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return page size in pixels, or `Size(-1, -1)` if the document is closed
     * @throws IllegalStateException if document is closed
     */
    fun getPageSize(
        pageIndex: Int,
        screenDpi: Int,
    ): Size {
        if (handleAlreadyClosed(isClosed)) return Size(-1, -1)
        return nativePage
            .getPageSizeByIndex(mNativeDocPtr, pageIndex, screenDpi)
            .let { Size(it[0], it[1]) }
    }

    /**
     * Get the size of every page in the document, in pixels, without opening any of them.
     * For internal use only.
     *
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return the pages' sizes in pixels, in page order, or an empty list if the document is closed
     * @throws IllegalStateException if document is closed
     */
    fun getPageSizes(screenDpi: Int): List<Size> {
        if (handleAlreadyClosed(isClosed)) return emptyList()
        return (0..<nativeDocument.getPageCount(mNativeDocPtr)).map { pageIndex ->
            nativePage
                .getPageSizeByIndex(mNativeDocPtr, pageIndex, screenDpi)
                .let { Size(it[0], it[1]) }
        }
    }

    /**
     * Delete page.
     * For internal use only.
     *
     * @param pageIndex the page index
     * @throws IllegalArgumentException if document is closed
     */
    fun deletePage(pageIndex: Int) {
        if (handleAlreadyClosed(isClosed)) return
        nativeDocument.deletePage(this.mNativeDocPtr, pageIndex)
    }

    /**
     * Open range of pages and store native pointers in [PdfDocumentU].
     * For internal use only.
     *
     * @param fromIndex the start index of the range
     * @param toIndex the end index of the range
     * @return the opened pages [PdfPageU] list, or an empty list if the document is closed
     * or the pages cannot be loaded.
     * @throws IllegalArgumentException if document is closed or the pages cannot be loaded
     */
    fun openPages(
        fromIndex: Int,
        toIndex: Int,
    ): List<PdfPageU> {
        if (handleAlreadyClosed(isClosed)) return emptyList()
        val pagesPtr: LongArray = nativeDocument.loadPages(this.mNativeDocPtr, fromIndex, toIndex)
        return pagesPtr.mapIndexed { offset, loadedPtr ->
            val pageIndex = fromIndex + offset
            val open = pageMap[pageIndex]
            if (open == null) {
                pageMap[pageIndex] = PageCount(loadedPtr, 1)
                PdfPageU(this, pageIndex, loadedPtr, pageMap, nativeFactory)
            } else {
                // The native call loads a fresh handle for every index in the range, so when the
                // page is already open, drop the duplicate and hand back the one being tracked.
                nativePage.closePage(loadedPtr)
                open.count++
                retainedPages.remove(pageIndex)
                PdfPageU(this, pageIndex, open.pagePtr, pageMap, nativeFactory)
            }
        }
    }

    /**
     * Offer a page, or a text page, whose last holder has just closed it to the retention pool, so
     * that reopening it does not have to load it again. Evicts, and closes, whatever has been
     * released for longest beyond [io.legere.pdfiumandroid.api.Config.pageRetentionCount].
     *
     * For internal use only.
     *
     * @param pageIndex the index of the page or text page being released
     * @return `true` if it was retained and must be left open, `false` if the caller should close
     * it now
     */
    internal fun retainOnRelease(pageIndex: Int): Boolean {
        val retentionCount = pdfiumConfig.pageRetentionCount
        if (retentionCount <= 0) return false

        // A text page is loaded from its page, so the two are evicted together and the index only
        // becomes a candidate once nothing holds either of them.
        if (isFullyReleased(pageIndex)) {
            retainedPages.remove(pageIndex)
            retainedPages.add(pageIndex)

            while (retainedPages.size > retentionCount) {
                val oldest = retainedPages.first()
                retainedPages.remove(oldest)
                evict(oldest)
            }
        }
        return true
    }

    private fun isFullyReleased(pageIndex: Int): Boolean =
        (pageMap[pageIndex]?.count ?: 0) == 0 && (textPageMap[pageIndex]?.count ?: 0) == 0

    /** Close and forget the page at [pageIndex], text page first so it never outlives its page. */
    private fun evict(pageIndex: Int) {
        textPageMap.remove(pageIndex)?.let { nativeTextPage.closeTextPage(it.pagePtr) }
        pageMap.remove(pageIndex)?.let { nativePage.closePage(it.pagePtr) }
    }

    /**
     * Render multiple page fragments on a [Surface]'s buffer.
     * For internal use only.
     *
     * @param bufferPtr Surface's buffer on which to render pages.
     * @param drawSizeX horizontal size of the rendering area on the surface.
     * @param drawSizeY vertical size of the rendering area on the surface.
     * @param pages The list of [PdfPageU] to render.
     * @param matrices The list of transformation [Matrix] for each page, mapping page coordinates
     * to surface coordinates.
     * @param clipRects The list of [RectF] for each page, defining the clipping area in surface coordinates.
     * @param renderAnnot whether to render annotations.
     * @param textMask whether to render text as an image mask - currently ignored.
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     *                            You almost always want this to be white (the default).
     * @throws IllegalStateException If the page or document is closed.
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

    /**
     * Render multiple page fragments directly on a [Surface].
     * For internal use only.
     *
     * @param surface The [Surface] on which to render the pages.
     * @param pages The list of [PdfPageU] to render.
     * @param matrices The list of transformation [Matrix] for each page, mapping page coordinates
     * to surface coordinates.
     * @param clipRects The list of [RectF] for each page, defining the clipping area in surface coordinates.
     * @param renderAnnot whether to render annotations.
     * @param textMask whether to render text as an image mask - currently ignored.
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     *                            You almost always want this to be white (the default).
     * @return `true` if rendering was successful, `false` otherwise.
     * @throws IllegalStateException If the page or document is closed.
     */
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
     * Get metadata for given document.
     * For internal use only.
     *
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

    /**
     * Recursively retrieves bookmarks from the PDF document.
     * For internal use only.
     *
     * @param tree The mutable list to populate with [Bookmark] objects.
     * @param bookmarkPtr The native pointer to the current FPDF_BOOKMARK object.
     * @param level The current recursion level to prevent stack overflow.
     */
    internal fun recursiveGetBookmark(
        tree: MutableList<Bookmark>,
        bookmarkPtr: Long,
        level: Long,
    ) {
        if (handleAlreadyClosed(isClosed)) return
        var currentPtr = bookmarkPtr
        while (currentPtr != 0L) {
            val bookmark = Bookmark()
            bookmark.mNativePtr = currentPtr
            bookmark.title = nativeDocument.getBookmarkTitle(currentPtr)
            bookmark.pageIdx = nativeDocument.getBookmarkDestIndex(mNativeDocPtr, currentPtr)
            tree.add(bookmark)
            val child = nativeDocument.getFirstChildBookmark(mNativeDocPtr, currentPtr)
            if (child != 0L && level < MAX_RECURSION) {
                recursiveGetBookmark(bookmark.children, child, level + 1)
            }
            currentPtr = nativeDocument.getSiblingBookmark(mNativeDocPtr, currentPtr)
        }
    }

    /**
     * Get table of contents (bookmarks) for given document.
     * For internal use only.
     *
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
     * Open a text page.
     * For internal use only. Prefer [PdfPageU.openTextPage].
     * @deprecated
     * @param page the [PdfPageU]
     * @return the opened [PdfTextPageU]
     * @throws IllegalStateException if document is closed or the page cannot be loaded
     */
    @Deprecated("Use PdfPage.openTextPage instead", ReplaceWith("page.openTextPage()"))
    @OpenForTesting
    fun openTextPage(page: PdfPageU): PdfTextPageU {
        check(!isClosed) { "Already closed" }
        if (textPageMap.containsKey(page.pageIndex)) {
            textPageMap[page.pageIndex]?.let {
                it.count++
                // The text page has a holder again, so its index is no longer evictable
                retainedPages.remove(page.pageIndex)
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
     * Open a range of text pages.
     * For internal use only.
     *
     * @param fromIndex the start index of the range
     * @param toIndex the end index of the range
     * @return the opened [PdfTextPageU] list, or an empty list if the document is closed or the pages cannot be loaded.
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
     * Save document as a copy.
     * For internal use only.
     *
     * @param callback the [io.legere.pdfiumandroid.api.PdfWriteCallback] to be called with the data
     * @param flags must be one of [FPDF_INCREMENTAL], [FPDF_NO_INCREMENTAL] or [FPDF_REMOVE_SECURITY]
     * @return `true` if the document was successfully saved, `false` otherwise.
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
     * Close the document and release all resources.
     * For internal use only.
     *
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        if (handleAlreadyClosed(isClosed)) return
        closeOpenPages()
        isClosed = true
        nativeDocument.closeDocument(mNativeDocPtr)
        parcelFileDescriptor?.close()
        parcelFileDescriptor = null
        source?.close()
        source = null
        matrixCache.clear()
    }

    /**
     * Close every native page this document still owns, whether it is retained or a page a caller
     * never closed. PDFium requires a page to outlive neither its text page nor its document, so
     * text pages go first and both go before the document itself is closed.
     */
    private fun closeOpenPages() {
        if (textPageMap.isNotEmpty()) {
            textPageMap.values.forEach { nativeTextPage.closeTextPage(it.pagePtr) }
            textPageMap.clear()
        }
        if (pageMap.isNotEmpty()) {
            pageMap.values.forEach { nativePage.closePage(it.pagePtr) }
            pageMap.clear()
        }
        retainedPages.clear()
    }

    /**
     * @suppress
     */
    companion object {
        private val TAG = PdfDocumentU::class.java.name

        /** Flag for incremental save. */
        const val FPDF_INCREMENTAL = 1

        /** Flag for non-incremental save. */
        const val FPDF_NO_INCREMENTAL = 2

        /** Flag to remove security from the document during save. */
        const val FPDF_REMOVE_SECURITY = 3
    }
}
