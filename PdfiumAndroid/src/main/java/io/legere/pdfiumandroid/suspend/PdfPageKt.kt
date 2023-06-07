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

@Suppress("TooManyFunctions")
class PdfPageKt(val page: PdfPage, private val dispatcher: CoroutineDispatcher) {

    suspend fun getPageWidth(): Int {
        return withContext(dispatcher) {
            page.getPageWidth()
        }
    }

    suspend fun getPageHeight(): Int {
        return withContext(dispatcher) {
            page.getPageHeight()
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

    suspend fun getFontSize(charIndex: Int): Double {
        return withContext(dispatcher) {
            page.getFontSize(charIndex)
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

    suspend fun getPageSize(): Size {
        return withContext(dispatcher) {
            page.getPageSize()
        }
    }

    suspend fun renderPage(
        surface: Surface?,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int
    ) {
        return withContext(dispatcher) {
            page.renderPage(surface, startX, startY, drawSizeX, drawSizeY)
        }
    }

    @Suppress("LongParameterList")
    suspend fun renderPage(
        surface: Surface?,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
        renderAnnot: Boolean
    ) {
        return withContext(dispatcher) {
            page.renderPage(surface, startX, startY, drawSizeX, drawSizeY, renderAnnot)
        }
    }


    suspend fun textPageGetFontSize(index: Int): Double {
        return withContext(dispatcher) {
            page.textPageGetFontSize(index)
        }
    }

    @Suppress("LongParameterList")
    suspend fun renderPageBitmap(
        bitmap: Bitmap,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, textMask: Boolean
    ) {
        return withContext(dispatcher) {
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, false, textMask)
        }
    }

    @Suppress("LongParameterList")
    suspend fun renderPageBitmap(bitmap: Bitmap?,
                                 startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
                                 renderAnnot: Boolean, textMask: Boolean
    ) {
        return withContext(dispatcher) {
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot, textMask)
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

    suspend fun close() {
        return withContext(dispatcher) {
            page.close()
        }
    }
}
