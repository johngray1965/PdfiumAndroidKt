@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import androidx.annotation.Keep
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.PageAttributes
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.util.wrapLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfPageKt represents a single page of a PDF file.
 * @property page the [PdfPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
@Keep
class PdfPageKt(
    internal val page: PdfPageU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    constructor(page: PdfPage, dispatcher: CoroutineDispatcher) : this(
        page.page,
        dispatcher,
    )

    val pageIndex: Int
        get() = page.pageIndex

    /**
     * Open a text page
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    suspend fun openTextPage(): PdfTextPageKt =
        wrapSuspend(dispatcher) {
            PdfTextPageKt(page.openTextPage(), dispatcher)
        }

    /**
     * suspend version of [PdfPage.getPageWidth]
     */
    suspend fun getPageWidth(screenDpi: Int): Int =
        wrapSuspend(dispatcher) {
            page.getPageWidth(screenDpi)
        }

    /**
     * suspend version of [PdfPage.getPageHeight]
     */
    suspend fun getPageHeight(screenDpi: Int): Int =
        wrapSuspend(dispatcher) {
            page.getPageHeight(screenDpi)
        }

    /**
     * suspend version of [PdfPage.getPageWidthPoint]
     */
    suspend fun getPageWidthPoint(): Int =
        wrapSuspend(dispatcher) {
            page.getPageWidthPoint()
        }

    /**
     * suspend version of [PdfPage.getPageHeightPoint]
     */
    suspend fun getPageHeightPoint(): Int =
        wrapSuspend(dispatcher) {
            page.getPageHeightPoint()
        }

    /**
     * suspend version of [PdfPage.getPageMatrix]
     */
    suspend fun getPageMatrix(): Matrix? =
        wrapSuspend(dispatcher) {
            page.getPageMatrix()
        }

    /**
     * suspend version of [PdfPage.getPageRotation]
     */
    suspend fun getPageRotation(): Int =
        wrapSuspend(dispatcher) {
            page.getPageRotation()
        }

    /**
     * suspend version of [PdfPage.getPageCropBox]
     */
    @Suppress("LongParameterList")
    suspend fun getPageCropBox(): RectF =
        wrapSuspend(dispatcher) {
            page.getPageCropBox()
        }

    /**
     * suspend version of [PdfPage.getPageMediaBox]
     */
    suspend fun getPageMediaBox(): RectF =
        wrapSuspend(dispatcher) {
            page.getPageMediaBox()
        }

    /**
     * suspend version of [PdfPage.getPageBleedBox]
     */
    suspend fun getPageBleedBox(): RectF =
        wrapSuspend(dispatcher) {
            page.getPageBleedBox()
        }

    /**
     * suspend version of [PdfPage.getPageTrimBox]
     */
    suspend fun getPageTrimBox(): RectF =
        wrapSuspend(dispatcher) {
            page.getPageTrimBox()
        }

    /**
     * suspend version of [PdfPage.getPageArtBox]
     */
    suspend fun getPageArtBox(): RectF =
        wrapSuspend(dispatcher) {
            page.getPageArtBox()
        }

    /**
     * suspend version of [PdfPage.getPageBoundingBox]
     */
    suspend fun getPageBoundingBox(): RectF =
        wrapSuspend(dispatcher) {
            page.getPageBoundingBox()
        }

    /**
     * suspend version of [PdfPage.getPageSize]
     */
    suspend fun getPageSize(screenDpi: Int): Size =
        wrapSuspend(dispatcher) {
            page.getPageSize(screenDpi)
        }

    /**
     * suspend version of [PdfPage.renderPage]
     */
    @Suppress("LongParameterList", "ComplexMethod", "ComplexCondition")
    suspend fun renderPage(
        surface: Surface?,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean {
        var retValue: Boolean
        PdfiumCore.surfaceMutex.withLock {
            withContext(dispatcher) {
                val sizes = IntArray(2)
                val pointers = LongArray(2)
                surface?.let {
                    page.lockSurface(
                        it,
                        sizes,
                        pointers,
                    )
                }
                val nativeWindow = pointers[0]
                val bufferPtr = pointers[1]
                if (bufferPtr != 0L && bufferPtr != -1L && nativeWindow != 0L && nativeWindow != -1L) {
                    try {
                        retValue =
                            page.renderPage(
                                bufferPtr,
                                startX,
                                startY,
                                drawSizeX,
                                drawSizeY,
                                renderAnnot,
                                canvasColor,
                                pageBackgroundColor,
                            )
                    } finally {
                        surface?.let {
                            page.unlockSurface(longArrayOf(nativeWindow, bufferPtr))
                        }
                    }
                } else {
                    retValue = false
                }
            }
        }
        return retValue
    }

    /**
     * suspend version of [PdfPage.renderPage]
     */
    @Suppress("LongParameterList", "ComplexMethod", "ComplexCondition")
    suspend fun renderPage(
        surface: Surface?,
        matrix: Matrix,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean {
        var retValue: Boolean
        PdfiumCore.surfaceMutex.withLock {
            withContext(dispatcher) {
                val sizes = IntArray(2)
                val pointers = LongArray(2)
                surface?.let {
                    page.lockSurface(
                        it,
                        sizes,
                        pointers,
                    )
                }
                val nativeWindow = pointers[0]
                val bufferPtr = pointers[1]
                val surfaceWidth = sizes[0]
                val surfaceHeight = sizes[1]
                if (bufferPtr != 0L && bufferPtr != -1L && nativeWindow != 0L && nativeWindow != -1L) {
                    try {
                        retValue =
                            page.renderPage(
                                bufferPtr,
                                surfaceWidth,
                                surfaceHeight,
                                matrix,
                                clipRect,
                                renderAnnot,
                                textMask,
                                canvasColor,
                                pageBackgroundColor,
                            )
                    } finally {
                        surface?.let {
                            page.unlockSurface(longArrayOf(nativeWindow, bufferPtr))
                        }
                    }
                } else {
                    retValue = false
                }
            }
        }
        return retValue
    }

    /**
     * suspend version of [PdfPage.renderPageBitmap]
     */
    @Suppress("LongParameterList")
    suspend fun renderPageBitmap(
        bitmap: Bitmap,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ) = wrapSuspend(dispatcher) {
        page.renderPageBitmap(
            bitmap,
            startX,
            startY,
            drawSizeX,
            drawSizeY,
            renderAnnot,
            textMask,
            canvasColor,
            pageBackgroundColor,
        )
    }

    /**
     * suspend version of [PdfPage.renderPageBitmap]
     */
    @Suppress("LongParameterList")
    suspend fun renderPageBitmap(
        bitmap: Bitmap?,
        matrix: Matrix,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ) = wrapSuspend(dispatcher) {
        page.renderPageBitmap(
            bitmap,
            matrix,
            clipRect,
            renderAnnot,
            textMask,
            canvasColor,
            pageBackgroundColor,
        )
    }

    /**
     * suspend version of [PdfPage.getPageLinks]
     */
    suspend fun getPageLinks(): List<Link> =
        wrapSuspend(dispatcher) {
            page.getPageLinks()
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
    ): Point =
        wrapSuspend(dispatcher) {
            page.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)
        }

    /**
     * suspend version of [PdfPage.mapDeviceCoordsToPage]
     */
    @Suppress("LongParameterList")
    suspend fun mapDeviceCoordsToPage(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        deviceX: Int,
        deviceY: Int,
    ): PointF =
        wrapSuspend(dispatcher) {
            page.mapDeviceCoordsToPage(startX, startY, sizeX, sizeY, rotate, deviceX, deviceY)
        }

    /**
     * suspend version of [PdfPage.mapRectToDevice]
     */
    @Suppress("LongParameterList")
    suspend fun mapRectToDevice(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: RectF,
    ): Rect =
        wrapSuspend(dispatcher) {
            page.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)
        }

    /**
     * suspend version of [PdfPage.mapRectToPage]
     */
    @Suppress("LongParameterList")
    suspend fun mapRectToPage(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: Rect,
    ): RectF =
        wrapSuspend(dispatcher) {
            page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)
        }

    /**
     * suspend version of [PdfPage.getPageAttributes]
     */
    suspend fun getPageAttributes(): PageAttributes =
        wrapSuspend(dispatcher) {
            page.getPageAttributes()
        }

    /**
     * Closes the page
     */
    override fun close() {
        wrapLock {
            page.close()
        }
    }

    fun safeClose(): Boolean =
        try {
            wrapLock {
                page.close()
            }
            true
        } catch (e: IllegalStateException) {
            Logger.e("PdfPageKt", e, "PdfPageKt.safeClose")
            false
        }
}
