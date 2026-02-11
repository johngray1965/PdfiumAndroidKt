@file:Suppress("unused")

package io.legere.pdfiumandroid

import android.graphics.Matrix
import android.graphics.RectF
import android.view.Surface
import io.legere.pdfiumandroid.PdfDocument.Companion.FPDF_INCREMENTAL
import io.legere.pdfiumandroid.PdfDocument.Companion.FPDF_NO_INCREMENTAL
import io.legere.pdfiumandroid.PdfDocument.Companion.FPDF_REMOVE_SECURITY
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import java.io.Closeable

private const val MAX_RECURSION = 16
private const val THREE_BY_THREE = 9

/**
 * PdfDocument represents a PDF file and allows you to load pages from it.
 */
@Suppress("TooManyFunctions")
class PdfDocument(
    val document: PdfDocumentU,
) : Closeable {
    /**
     *  Get the page count of the PDF document
     *  @return the number of pages
     */
    fun getPageCount(): Int =
        PdfiumCore.lock.withLockBlocking {
            document.getPageCount()
        }

    /**
     *  Get the page character counts for every page of the PDF document
     *  @return an array of character counts
     */
    fun getPageCharCounts(): IntArray =
        PdfiumCore.lock.withLockBlocking {
            document.getPageCharCounts()
        }

    /**
     * Open page and store native pointer in [PdfDocument]
     * @param pageIndex the page index
     * @return the opened page [PdfPage]
     * @throws IllegalArgumentException if  document is closed or the page cannot be loaded,
     * RuntimeException if the page cannot be loaded
     */
    fun openPage(pageIndex: Int): PdfPage? =
        PdfiumCore.lock.withLockBlocking {
            document.openPage(pageIndex)?.let { PdfPage(it) }
        }

    /**
     * Delete page
     * @param pageIndex the page index
     * @throws IllegalArgumentException if document is closed
     */
    fun deletePage(pageIndex: Int) {
        PdfiumCore.lock.withLockBlocking {
            document.deletePage(pageIndex)
        }
    }

    /**
     * Open range of pages and store native pointers in [PdfDocument]
     * @param fromIndex the start index of the range
     * @param toIndex the end index of the range
     * @return the opened pages [PdfPage]
     * @throws IllegalArgumentException if document is closed or the pages cannot be loaded
     */
    fun openPages(
        fromIndex: Int,
        toIndex: Int,
    ): List<PdfPage> =
        PdfiumCore.lock.withLockBlocking {
            document.openPages(fromIndex, toIndex).map { PdfPage(it) }
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
        pages: List<PdfPage>,
        matrices: List<Matrix>,
        clipRects: List<RectF>,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ) {
        PdfiumCore.lock.withLockBlocking {
            document.renderPages(
                bufferPtr,
                drawSizeX,
                drawSizeY,
                pages.map { it.page },
                matrices,
                clipRects,
                renderAnnot,
                textMask,
                canvasColor,
                pageBackgroundColor,
            )
        }
    }

    @Suppress("LongParameterList")
    fun renderPages(
        surface: Surface,
        pages: List<PdfPage>,
        matrices: List<Matrix>,
        clipRects: List<RectF>,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean =
        PdfiumCore.lock.withLockBlocking {
            document.renderPages(
                surface,
                pages.map { it.page },
                matrices,
                clipRects,
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
    fun getDocumentMeta(): Meta =
        PdfiumCore.lock.withLockBlocking {
            document.getDocumentMeta()
        }

    /**
     * Get table of contents (bookmarks) for given document
     * @return the [Bookmark] list
     * @throws IllegalArgumentException if document is closed
     */
    fun getTableOfContents(): List<Bookmark> =
        PdfiumCore.lock.withLockBlocking {
            document.getTableOfContents()
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
    ): Boolean =
        PdfiumCore.lock.withLockBlocking {
            document.saveAsCopy(callback, flags)
        }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        PdfiumCore.lock.withLockBlocking {
            document.close()
        }
    }

    data class Meta(
        var title: String? = null,
        var author: String? = null,
        var subject: String? = null,
        var keywords: String? = null,
        var creator: String? = null,
        var producer: String? = null,
        var creationDate: String? = null,
        var modDate: String? = null,
    )

    data class Bookmark(
        val children: MutableList<Bookmark> = ArrayList(),
        var title: String? = null,
        var pageIdx: Long = 0,
        var mNativePtr: Long = 0,
    )

    class Link(
        val bounds: RectF,
        val destPageIdx: Int?,
        val uri: String?,
    )

    data class PageCount(
        val pagePtr: Long,
        var count: Int,
    )

    companion object {
        private val TAG = PdfDocument::class.java.name

        const val FPDF_INCREMENTAL = 1
        const val FPDF_NO_INCREMENTAL = 2
        const val FPDF_REMOVE_SECURITY = 3
    }
}
