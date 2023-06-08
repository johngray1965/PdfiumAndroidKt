@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.view.Surface
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.util.Size
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

@Suppress("TooManyFunctions")
class PdfPageKt(val page: PdfPage, private val dispatcher: CoroutineDispatcher) : Closeable {

    suspend fun getPageWidth(screenDpi: Int): Int {
        return withContext(dispatcher) {
            page.getPageWidth(screenDpi)
        }
    }

    suspend fun getPageHeight(screenDpi: Int): Int {
        return withContext(dispatcher) {
            page.getPageHeight(screenDpi)
        }
    }

    suspend fun getPageWidthPoint(): Int {
        return withContext(dispatcher) {
            page.getPageWidthPoint()
        }
    }

    suspend fun getPageHeightPoint(): Int {
        return withContext(dispatcher) {
            page.getPageHeightPoint()
        }
    }

    suspend fun getPageCropBox(): RectF {
        return withContext(dispatcher) {
            page.getPageCropBox()
        }
    }

    suspend fun getPageMediaBox(): RectF {
        return withContext(dispatcher) {
            page.getPageMediaBox()
        }
    }

    suspend fun getPageBleedBox(): RectF {
        return withContext(dispatcher) {
            page.getPageBleedBox()
        }
    }

    suspend fun getPageTrimBox(): RectF {
        return withContext(dispatcher) {
            page.getPageTrimBox()
        }
    }

    suspend fun getPageArtBox(): RectF {
        return withContext(dispatcher) {
            page.getPageArtBox()
        }
    }

    suspend fun getPageBoundingBox(): RectF {
        return withContext(dispatcher) {
            page.getPageBoundingBox()
        }
    }

    suspend fun getPageSize(screenDpi: Int): Size {
        return withContext(dispatcher) {
            page.getPageSize(screenDpi)
        }
    }

    @Suppress("LongParameterList")
    suspend fun renderPage(
        surface: Surface?,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, screenDpi: Int
    ) {
        return withContext(dispatcher) {
            page.renderPage(surface, startX, startY, drawSizeX, drawSizeY, screenDpi)
        }
    }

    @Suppress("LongParameterList")
    suspend fun renderPage(
        surface: Surface?,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, screenDpi: Int,
        renderAnnot: Boolean
    ) {
        return withContext(dispatcher) {
            page.renderPage(surface, startX, startY, drawSizeX, drawSizeY, screenDpi, renderAnnot)
        }
    }

    @Suppress("LongParameterList")
    suspend fun renderPageBitmap(
        bitmap: Bitmap,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, screenDpi: Int, textMask: Boolean
    ) {
        return withContext(dispatcher) {
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, screenDpi, false, textMask)
        }
    }

    @Suppress("LongParameterList")
    suspend fun renderPageBitmap(bitmap: Bitmap?,
                                 startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, screenDpi: Int,
                                 renderAnnot: Boolean, textMask: Boolean
    ) {
        return withContext(dispatcher) {
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, screenDpi, renderAnnot, textMask)
        }
    }

    suspend fun getPageLinks(): List<PdfDocument.Link> {
        return withContext(dispatcher) {
            page.getPageLinks()
        }
    }

    @Suppress("LongParameterList")
    suspend fun mapPageCoordsToDevice(startX: Int, startY: Int, sizeX: Int,
                                      sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point {
        return withContext(dispatcher) {
            page.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)
        }
    }

    @Suppress("LongParameterList")
    suspend fun mapDeviceCoordsToPage(startX: Int, startY: Int, sizeX: Int,
                                      sizeY: Int, rotate: Int, deviceX: Int, deviceY: Int
    ): PointF {
        return withContext(dispatcher) {
            page.mapDeviceCoordsToPage(startX, startY, sizeX, sizeY, rotate, deviceX, deviceY)
        }
    }

    @Suppress("LongParameterList")
    suspend fun mapRectToDevice(startX: Int, startY: Int, sizeX: Int,
                                    sizeY: Int, rotate: Int, coords: RectF
    ): RectF {
        return withContext(dispatcher) {
            page.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)
        }
    }

    @Suppress("LongParameterList")
    suspend fun mapRectToPage(startX: Int, startY: Int, sizeX: Int,
                              sizeY: Int, rotate: Int, coords: RectF
    ): RectF {
        return withContext(dispatcher) {
            page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)
        }
    }

    override fun close() {
        page.close()
    }
}
