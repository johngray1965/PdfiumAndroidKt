@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.util.Size
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfPageKt represents a single page of a PDF file.
 * @property page the [PdfPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
class PdfPageKt(val page: PdfPage, private val dispatcher: CoroutineDispatcher) : Closeable {
    /**
     * Open a text page
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    suspend fun openTextPage(): PdfTextPageKt {
        return withContext(dispatcher) {
            PdfTextPageKt(page.openTextPage(), dispatcher)
        }
    }

    /**
     * suspend version of [PdfPage.getPageWidth]
     */
    suspend fun getPageWidth(screenDpi: Int): Int {
        return withContext(dispatcher) {
            page.getPageWidth(screenDpi)
        }
    }

    /**
     * suspend version of [PdfPage.getPageHeight]
     */
    suspend fun getPageHeight(screenDpi: Int): Int {
        return withContext(dispatcher) {
            page.getPageHeight(screenDpi)
        }
    }

    /**
     * suspend version of [PdfPage.getPageWidthPoint]
     */
    suspend fun getPageWidthPoint(): Int {
        return withContext(dispatcher) {
            page.getPageWidthPoint()
        }
    }

    /**
     * suspend version of [PdfPage.getPageHeightPoint]
     */
    suspend fun getPageHeightPoint(): Int {
        return withContext(dispatcher) {
            page.getPageHeightPoint()
        }
    }

    /**
     * suspend version of [PdfPage.getPageCropBox]
     */
    suspend fun getPageCropBox(): RectF {
        return withContext(dispatcher) {
            page.getPageCropBox()
        }
    }

    /**
     * suspend version of [PdfPage.getPageMediaBox]
     */
    suspend fun getPageMediaBox(): RectF {
        return withContext(dispatcher) {
            page.getPageMediaBox()
        }
    }

    /**
     * suspend version of [PdfPage.getPageBleedBox]
     */
    suspend fun getPageBleedBox(): RectF {
        return withContext(dispatcher) {
            page.getPageBleedBox()
        }
    }

    /**
     * suspend version of [PdfPage.getPageTrimBox]
     */
    suspend fun getPageTrimBox(): RectF {
        return withContext(dispatcher) {
            page.getPageTrimBox()
        }
    }

    /**
     * suspend version of [PdfPage.getPageArtBox]
     */
    suspend fun getPageArtBox(): RectF {
        return withContext(dispatcher) {
            page.getPageArtBox()
        }
    }

    /**
     * suspend version of [PdfPage.getPageBoundingBox]
     */
    suspend fun getPageBoundingBox(): RectF {
        return withContext(dispatcher) {
            page.getPageBoundingBox()
        }
    }

    /**
     * suspend version of [PdfPage.getPageSize]
     */
    suspend fun getPageSize(screenDpi: Int): Size {
        return withContext(dispatcher) {
            page.getPageSize(screenDpi)
        }
    }

    /**
     * suspend version of [PdfPage.renderPage]
     */
    @Suppress("LongParameterList")
    suspend fun renderPage(
        surface: Surface?,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
    ) {
        return withContext(dispatcher) {
            page.renderPage(surface, startX, startY, drawSizeX, drawSizeY)
        }
    }

    @Suppress("LongParameterList")
    /**
     * suspend version of [PdfPage.renderPageBitmap]
     */
    suspend fun renderPageBitmap(
        bitmap: Bitmap,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
    ) {
        return withContext(dispatcher) {
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot, textMask)
        }
    }

    suspend fun renderPageBitmap(
        bitmap: Bitmap?,
        matrix: Matrix,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
    ) {
        return withContext(dispatcher) {
            page.renderPageBitmap(bitmap, matrix, clipRect, renderAnnot, textMask)
        }
    }

    /**
     * suspend version of [PdfPage.getPageLinks]
     */
    suspend fun getPageLinks(): List<PdfDocument.Link> {
        return withContext(dispatcher) {
            page.getPageLinks()
        }
    }

    /**
     * suspend version of [PdfPage.mapPageCoordsToDevice]
     */
    @Suppress("LongParameterList")
    suspend fun mapPageCoordsToDevice(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        pageX: Double,
        pageY: Double,
    ): Point {
        return withContext(dispatcher) {
            page.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)
        }
    }

    @Suppress("LongParameterList")
    /**
     * suspend version of [PdfPage.mapDeviceCoordsToPage]
     */
    suspend fun mapDeviceCoordsToPage(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        deviceX: Int,
        deviceY: Int,
    ): PointF {
        return withContext(dispatcher) {
            page.mapDeviceCoordsToPage(startX, startY, sizeX, sizeY, rotate, deviceX, deviceY)
        }
    }

    @Suppress("LongParameterList")
    /**
     * suspend version of [PdfPage.mapRectToDevice]
     */
    suspend fun mapRectToDevice(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: RectF,
    ): Rect {
        return withContext(dispatcher) {
            page.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)
        }
    }

    @Suppress("LongParameterList")
    /**
     * suspend version of [PdfPage.mapRectToPage]
     */
    suspend fun mapRectToPage(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: Rect,
    ): RectF {
        return withContext(dispatcher) {
            page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)
        }
    }

    /**
     * Closes the page
     */
    override fun close() {
        page.close()
    }

    fun safeClose(): Boolean {
        return try {
            page.close()
            true
        } catch (e: IllegalStateException) {
            Logger.e("PdfPageKt", e, "PdfPageKt.safeClose")
            false
        }
    }
}
