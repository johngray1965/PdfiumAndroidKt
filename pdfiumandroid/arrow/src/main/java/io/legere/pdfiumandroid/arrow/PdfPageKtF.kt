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

package io.legere.pdfiumandroid.arrow

import android.graphics.Bitmap
import android.view.Surface
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.PageAttributes
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfPoint
import io.legere.pdfiumandroid.api.types.PdfPointF
import io.legere.pdfiumandroid.api.types.PdfRect
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.util.wrapLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * PdfPageKtF represents a single page of a PDF file.
 * @property page the [PdfPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
class PdfPageKtF internal constructor(
    internal val page: PdfPageU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    val pageIndex: Int
        get() = page.pageIndex

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
    suspend fun getPageMatrix(): Either<PdfiumKtFErrors, PdfMatrix> =
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
    suspend fun getPageCropBox(): Either<PdfiumKtFErrors, PdfRectF> =
        wrapEither(dispatcher) {
            page.getPageCropBox()
        }

    /**
     * suspend version of [PdfPage.getPageMediaBox]
     */
    suspend fun getPageMediaBox(): Either<PdfiumKtFErrors, PdfRectF> =
        wrapEither(dispatcher) {
            page.getPageMediaBox()
        }

    /**
     * suspend version of [PdfPage.getPageBleedBox]
     */
    suspend fun getPageBleedBox(): Either<PdfiumKtFErrors, PdfRectF> =
        wrapEither(dispatcher) {
            page.getPageBleedBox()
        }

    /**
     * suspend version of [PdfPage.getPageTrimBox]
     */
    suspend fun getPageTrimBox(): Either<PdfiumKtFErrors, PdfRectF> =
        wrapEither(dispatcher) {
            page.getPageTrimBox()
        }

    /**
     * suspend version of [PdfPage.getPageArtBox]
     */
    suspend fun getPageArtBox(): Either<PdfiumKtFErrors, PdfRectF> =
        wrapEither(dispatcher) {
            page.getPageArtBox()
        }

    /**
     * suspend version of [PdfPage.getPageBoundingBox]
     */
    suspend fun getPageBoundingBox(): Either<PdfiumKtFErrors, PdfRectF> =
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
    @Suppress("LongParameterList", "ComplexCondition")
    suspend fun renderPage(
        surface: Surface?,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
        renderCoroutinesDispatcher: CoroutineDispatcher,
    ): Either<PdfiumKtFErrors, Boolean> {
        val sizes = IntArray(2)
        val pointers = LongArray(2)
        return withContext(renderCoroutinesDispatcher) {
            PdfiumCore.surfaceMutex.withLock {
                surface?.let {
                    page.lockSurface(
                        it,
                        sizes,
                        pointers,
                    )
                    val nativeWindow = pointers[0]
                    val bufferPtr = pointers[1]
                    val surfaceWidth = sizes[0]
                    val surfaceHeight = sizes[1]
                    Logger.d(
                        "PdfPageKtF",
                        "page: ${page.pageIndex}, surfaceWidth: $surfaceWidth, " +
                            "surfaceHeight: $surfaceHeight, nativeWindow: $nativeWindow, " +
                            "bufferPtr: $bufferPtr, nativeWindow: $nativeWindow",
                    )
                    if (bufferPtr == 0L || bufferPtr == -1L || nativeWindow == 0L || nativeWindow == -1L) {
                        PdfiumKtFErrors.ConstraintError.left()
                    }
                    val result =
                        page.renderPage(
                            bufferPtr,
                            startX,
                            startY,
                            drawSizeX,
                            drawSizeY,
                            canvasColor = canvasColor,
                            pageBackgroundColor = pageBackgroundColor,
                        )
                    page.unlockSurface(longArrayOf(nativeWindow, bufferPtr))
                    if (!result) {
                        PdfiumKtFErrors.ConstraintError.left()
                    }
                    true.right()
                } ?: PdfiumKtFErrors.ConstraintError.left()
            }
        }
    }

    /**
     * suspend version of [PdfPage.renderPage]
     */
    @Suppress("LongParameterList")
    suspend fun renderPage(
        surface: Surface?,
        matrix: PdfMatrix,
        clipRect: PdfRectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
        renderCoroutinesDispatcher: CoroutineDispatcher,
    ): Either<PdfiumKtFErrors, Boolean> {
        return withContext(renderCoroutinesDispatcher) {
            PdfiumCore.surfaceMutex.withLock {
                Either
                    .catch {
                        return@withContext surface?.let {
                            val retValue =
                                page.renderPage(
                                    surface,
                                    matrix,
                                    clipRect,
                                    renderAnnot,
                                    textMask,
                                    canvasColor,
                                    pageBackgroundColor,
                                )
                            if (!retValue) {
                                PdfiumKtFErrors.ConstraintError.left()
                            } else {
                                true.right()
                            }
                        } ?: PdfiumKtFErrors.ConstraintError.left()
                    }.mapLeft {
                        exceptionToPdfiumKtFError(it)
                    }
            }
        }
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
    ): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
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
            true
        }

    /**
     * suspend version of [PdfPage.renderPageBitmap]
     */
    @Suppress("LongParameterList")
    suspend fun renderPageBitmap(
        bitmap: Bitmap?,
        matrix: PdfMatrix,
        clipRect: PdfRectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Either<PdfiumKtFErrors, Boolean> =
        wrapEither(dispatcher) {
            page.renderPageBitmap(bitmap, matrix, clipRect, renderAnnot, textMask, canvasColor, pageBackgroundColor)
            true
        }

    /**
     * suspend version of [PdfPage.getPageLinks]
     */
    suspend fun getPageLinks(): Either<PdfiumKtFErrors, List<Link>> =
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
    ): Either<PdfiumKtFErrors, PdfPoint> =
        wrapEither(dispatcher) {
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
    ): Either<PdfiumKtFErrors, PdfPointF> =
        wrapEither(dispatcher) {
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
        coords: PdfRectF,
    ): Either<PdfiumKtFErrors, PdfRect> =
        wrapEither(dispatcher) {
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
        coords: PdfRect,
    ): Either<PdfiumKtFErrors, PdfRectF> =
        wrapEither(dispatcher) {
            page.mapRectToPage(startX, startY, sizeX, sizeY, rotate, coords)
        }

    /**
     * suspend version of [PdfPage.getPageAttributes]
     */
    suspend fun getPageAttributes(): Either<PdfiumKtFErrors, PageAttributes> =
        wrapEither(dispatcher) {
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

    fun safeClose(): Either<PdfiumKtFErrors, Boolean> =
        Either
            .catch {
                wrapLock {
                    page.close()
                }
                true
            }.mapLeft { exceptionToPdfiumKtFError(it) }
}
