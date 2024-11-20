@file:Suppress("unused")

package io.legere.pdfiumandroid.arrow

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import arrow.core.Either
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.util.Size
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfPageKtF represents a single page of a PDF file.
 * @property page the [PdfPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
class PdfPageKtF(
    val page: PdfPage,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    /**
     * Open a text page
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    suspend fun openTextPage(): Either<PdfiumKtFErrors, PdfTextPageKtF> =
        wrapEither(dispatcher) {
            PdfTextPageKtF(page.openTextPage(), dispatcher)
        }

    /**
     * suspend version of [PdfPage.getPageWidth]
     */
    suspend fun getPageWidth(screenDpi: Int): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            page.getPageWidth(screenDpi)
        }

    /**
     * suspend version of [PdfPage.getPageHeight]
     */
    suspend fun getPageHeight(screenDpi: Int): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            page.getPageHeight(screenDpi)
        }

    /**
     * suspend version of [PdfPage.getPageWidthPoint]
     */
    suspend fun getPageWidthPoint(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            page.getPageWidthPoint()
        }

    /**
     * suspend version of [PdfPage.getPageHeightPoint]
     */
    suspend fun getPageHeightPoint(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            page.getPageHeightPoint()
        }

    /**
     * suspend version of [PdfPage.getPageMatrix]
     */
    suspend fun getPageMatrix(): Either<PdfiumKtFErrors, Matrix> =
        wrapEither(dispatcher) {
            page.getPageMatrix() ?: error("Page matrix is null")
        }

    /**
     * suspend version of [PdfPage.getPageRotation]
     */
    suspend fun getPageRotation(): Either<PdfiumKtFErrors, Int> =
        withContext(dispatcher) {
            Either
                .catch {
                    val rotation = page.getPageRotation()
                    if (rotation < 0) {
                        error("Invalid rotation: $rotation")
                    }
                    rotation
                }.mapLeft {
                    exceptionToPdfiumKtFError(it)
                }
        }

    /**
     * suspend version of [PdfPage.getPageCropBox]
     */
    suspend fun getPageCropBox(): Either<PdfiumKtFErrors, RectF> =
        wrapEither(dispatcher) {
            page.getPageCropBox()
        }

    /**
     * suspend version of [PdfPage.getPageMediaBox]
     */
    suspend fun getPageMediaBox(): Either<PdfiumKtFErrors, RectF> =
        wrapEither(dispatcher) {
            page.getPageMediaBox()
        }

    /**
     * suspend version of [PdfPage.getPageBleedBox]
     */
    suspend fun getPageBleedBox(): Either<PdfiumKtFErrors, RectF> =
        wrapEither(dispatcher) {
            page.getPageBleedBox()
        }

    /**
     * suspend version of [PdfPage.getPageTrimBox]
     */
    suspend fun getPageTrimBox(): Either<PdfiumKtFErrors, RectF> =
        wrapEither(dispatcher) {
            page.getPageTrimBox()
        }

    /**
     * suspend version of [PdfPage.getPageArtBox]
     */
    suspend fun getPageArtBox(): Either<PdfiumKtFErrors, RectF> =
        wrapEither(dispatcher) {
            page.getPageArtBox()
        }

    /**
     * suspend version of [PdfPage.getPageBoundingBox]
     */
    suspend fun getPageBoundingBox(): Either<PdfiumKtFErrors, RectF> =
        wrapEither(dispatcher) {
            page.getPageBoundingBox()
        }

    /**
     * suspend version of [PdfPage.getPageSize]
     */
    suspend fun getPageSize(screenDpi: Int): Either<PdfiumKtFErrors, Size> =
        wrapEither(dispatcher) {
            page.getPageSize(screenDpi)
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
    ): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            page.renderPage(surface, startX, startY, drawSizeX, drawSizeY)
            true
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
    ): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            page.renderPageBitmap(bitmap, startX, startY, drawSizeX, drawSizeY, renderAnnot, textMask)
            true
        }

    suspend fun renderPageBitmap(
        bitmap: Bitmap?,
        matrix: Matrix,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
    ): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            page.renderPageBitmap(bitmap, matrix, clipRect, renderAnnot, textMask)
            true
        }

    /**
     * suspend version of [PdfPage.getPageLinks]
     */
    suspend fun getPageLinks(): Either<PdfiumKtFErrors, List<PdfDocument.Link>> =
        wrapEither(dispatcher) {
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
    ): Either<PdfiumKtFErrors, Point> =
        wrapEither(dispatcher) {
            page.mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate, pageX, pageY)
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
    ): Either<PdfiumKtFErrors, PointF> =
        wrapEither(dispatcher) {
            page.mapDeviceCoordsToPage(startX, startY, sizeX, sizeY, rotate, deviceX, deviceY)
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
    ): Either<PdfiumKtFErrors, Rect> =
        wrapEither(dispatcher) {
            page.mapRectToDevice(startX, startY, sizeX, sizeY, rotate, coords)
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
    ): Either<PdfiumKtFErrors, RectF> =
        wrapEither(dispatcher) {
            page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)
        }

    /**
     * Closes the page
     */
    override fun close() {
        page.close()
    }

    fun safeClose(): Either<PdfiumKtFErrors, Boolean> =
        Either
            .catch {
                page.close()
                true
            }.mapLeft { exceptionToPdfiumKtFError(it) }
}
