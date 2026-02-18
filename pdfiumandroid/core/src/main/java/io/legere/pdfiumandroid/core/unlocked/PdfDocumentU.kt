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

import android.os.ParcelFileDescriptor
import android.view.Surface
import androidx.annotation.OpenForTesting
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.api.handleAlreadyClosed
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfRectF
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

    private val nativeDocument = nativeFactory.getNativeDocument()

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
                    return PdfPageU(this, pageIndex, it.pagePtr, pageMap)
                }
            }
            val pagePtr = nativeDocument.loadPage(this.mNativeDocPtr, pageIndex)
            pageMap[pageIndex] = PageCount(pagePtr, 1)
            return PdfPageU(this, pageIndex, pagePtr, pageMap)
        } catch (e: RuntimeException) {
            Logger.e(TAG, e, "openPage: pageIndex: $pageIndex $e")
            return null
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
        var pageIndex = fromIndex
        for (page in pagesPtr) {
            if (pageIndex > toIndex) break
            pageIndex++
        }
        return pagesPtr.map { PdfPageU(this, pageIndex, it, pageMap) }
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
        matrices: List<PdfMatrix>,
        clipRects: List<PdfRectF>,
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
     * @param matrices The list of transformation [PdfMatrix] for each page, mapping page coordinates
     * to surface coordinates.
     * @param clipRects The list of [PdfRectF] for each page, defining the clipping area in surface coordinates.
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
        matrices: List<PdfMatrix>,
        clipRects: List<PdfRectF>,
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
        isClosed = true
        nativeDocument.closeDocument(mNativeDocPtr)
        parcelFileDescriptor?.close()
        parcelFileDescriptor = null
        source?.close()
        source = null
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
