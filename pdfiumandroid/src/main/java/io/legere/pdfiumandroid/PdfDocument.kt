/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package io.legere.pdfiumandroid

import android.graphics.Matrix
import android.graphics.RectF
import android.view.Surface
import io.legere.pdfiumandroid.PdfDocument.Companion.FPDF_INCREMENTAL
import io.legere.pdfiumandroid.PdfDocument.Companion.FPDF_NO_INCREMENTAL
import io.legere.pdfiumandroid.PdfDocument.Companion.FPDF_REMOVE_SECURITY
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.util.wrapLock
import java.io.Closeable

private const val MAX_RECURSION = 16
private const val THREE_BY_THREE = 9

/**
 * PdfDocument represents a PDF file and allows you to load pages from it.
 */
@Suppress("TooManyFunctions")
class PdfDocument internal constructor(
    internal val document: PdfDocumentU,
) : Closeable {
    @Deprecated(
        "Moved to io.legere.pdfiumandroid.api.Meta",
        ReplaceWith("Meta", "io.legere.pdfiumandroid.api.Meta"),
    )
    typealias Meta = io.legere.pdfiumandroid.api.Meta

    @Deprecated(
        "Moved to io.legere.pdfiumandroid.api.Bookmark",
        ReplaceWith("Bookmark", "io.legere.pdfiumandroid.api.Bookmark"),
    )
    typealias Bookmark = io.legere.pdfiumandroid.api.Bookmark

    @Deprecated(
        "Moved to io.legere.pdfiumandroid.api.Link",
        ReplaceWith("Link", "io.legere.pdfiumandroid.api.Link"),
    )
    typealias Link = io.legere.pdfiumandroid.api.Link

    /**
     *  Get the page count of the PDF document
     *  @return the number of pages
     */
    fun getPageCount(): Int =
        wrapLock {
            document.getPageCount()
        }

    /**
     *  Get the page character counts for every page of the PDF document
     *  @return an array of character counts
     */
    fun getPageCharCounts(): IntArray =
        wrapLock {
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
        wrapLock {
            document.openPage(pageIndex)?.let { PdfPage(it) }
        }

    /**
     * Delete page
     * @param pageIndex the page index
     * @throws IllegalArgumentException if document is closed
     */
    fun deletePage(pageIndex: Int) {
        wrapLock {
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
        wrapLock {
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
        wrapLock {
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
        wrapLock {
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
    fun getDocumentMeta(): io.legere.pdfiumandroid.api.Meta =
        wrapLock {
            document.getDocumentMeta()
        }

    /**
     * Get table of contents (bookmarks) for given document
     * @return the [Bookmark] list
     * @throws IllegalArgumentException if document is closed
     */
    fun getTableOfContents(): List<io.legere.pdfiumandroid.api.Bookmark> =
        wrapLock {
            document.getTableOfContents()
        }

    /**
     * Save document as a copy
     * @param callback the [io.legere.pdfiumandroid.api.PdfWriteCallback] to be called with the data
     * @param flags must be one of [FPDF_INCREMENTAL], [FPDF_NO_INCREMENTAL] or [FPDF_REMOVE_SECURITY]
     * @return true if the document was successfully saved
     * @throws IllegalArgumentException if document is closed
     */
    fun saveAsCopy(
        callback: PdfWriteCallback,
        flags: Int = FPDF_NO_INCREMENTAL,
    ): Boolean =
        wrapLock {
            document.saveAsCopy(callback, flags)
        }

    /**
     * Close the document
     * @throws IllegalArgumentException if document is closed
     */
    override fun close() {
        wrapLock {
            document.close()
        }
    }

    /**
     * @suppress
     */
    companion object {
        private val TAG = PdfDocument::class.java.name

        const val FPDF_INCREMENTAL = 1
        const val FPDF_NO_INCREMENTAL = 2
        const val FPDF_REMOVE_SECURITY = 3
    }
}
