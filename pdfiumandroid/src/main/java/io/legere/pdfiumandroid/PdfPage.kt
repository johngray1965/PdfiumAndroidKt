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

package io.legere.pdfiumandroid

import android.graphics.Bitmap
import android.view.Surface
import androidx.annotation.ColorInt
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.PageAttributes
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfPoint
import io.legere.pdfiumandroid.api.types.PdfPointF
import io.legere.pdfiumandroid.api.types.PdfRect
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.util.wrapLock
import java.io.Closeable

private const val THREE_BY_THREE = 9

private const val LEFT = 0
private const val TOP = 1
private const val RIGHT = 2
private const val BOTTOM = 3

private const val RECT_SIZE = 4

/**
 * Represents a single page in a [PdfDocument].
 */
@Suppress("TooManyFunctions")
class PdfPage internal constructor(
    internal val page: PdfPageU,
) : Closeable {
    val pageIndex: Int
        get() = page.pageIndex

    /**
     * Open a text page
     * @return the opened [PdfTextPage]
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    @Suppress("DEPRECATION")
    fun openTextPage(): PdfTextPage = PdfTextPage(page.openTextPage())

    /**
     * Get page width in pixels.
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return page width in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageWidth(screenDpi: Int): Int =
        wrapLock {
            page.getPageWidth(screenDpi)
        }

    /**
     * Get page height in pixels.
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return page height in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageHeight(screenDpi: Int): Int =
        wrapLock {
            page.getPageHeight(screenDpi)
        }

    /**
     * Get page width in PostScript points (1/72th of an inch).
     * @return page width in points
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageWidthPoint(): Int =
        wrapLock {
            page.getPageWidthPoint()
        }

    /**
     * Get page height in PostScript points (1/72th of an inch)
     * @return page height in points
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageHeightPoint(): Int =
        wrapLock {
            page.getPageHeightPoint()
        }

    @Suppress("LongParameterList", "MagicNumber")
    fun getPageMatrix(): PdfMatrix? =
        wrapLock {
            page.getPageMatrix()
        }

    /**
     * Get page rotation in degrees
     * @return
     *  -1 - Error
     *  0 - No rotation.
     *  1 - Rotated 90 degrees clockwise.
     *  2 - Rotated 180 degrees clockwise.
     *  3 - Rotated 270 degrees clockwise.
     *
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageRotation(): Int =
        wrapLock {
            page.getPageRotation()
        }

    /**
     *  Get the page's crop box in PostScript points (1/72th of an inch)
     *  @return page crop box in points or PdfRectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageCropBox(): PdfRectF =
        wrapLock {
            page.getPageCropBox()
        }

    /**
     *  Get the page's media box in PostScript points (1/72th of an inch)
     *  @return page media box in points or PdfRectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageMediaBox(): PdfRectF =
        wrapLock {
            page.getPageMediaBox()
        }

    /**
     *  Get the page's bleed box in PostScript points (1/72th of an inch)
     *  @return page bleed box in pointsor PdfRectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageBleedBox(): PdfRectF =
        wrapLock {
            page.getPageBleedBox()
        }

    /**
     *  Get the page's trim box in PostScript points (1/72th of an inch)
     *  @return page trim box in points or PdfRectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageTrimBox(): PdfRectF =
        wrapLock {
            page.getPageTrimBox()
        }

    /**
     *  Get the page's art box in PostScript points (1/72th of an inch)
     *  @return page art box in points or PdfRectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageArtBox(): PdfRectF =
        wrapLock {
            page.getPageArtBox()
        }

    /**
     *  Get the page's bounding box in PostScript points (1/72th of an inch)
     *  @return page bounding box in points or PdfRectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageBoundingBox(): PdfRectF =
        wrapLock {
            page.getPageBoundingBox()
        }

    /**
     *  Get the page's size in pixels
     *  @return page size in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageSize(screenDpi: Int): Size =
        wrapLock {
            page.getPageSize(screenDpi)
        }

    /**
     * Render page fragment on [Surface].<br></br>
     * @param bufferPtr Surface's buffer on which to render page
     * @param startX left position of the page in the surface
     * @param startY top position of the page in the surface
     * @param drawSizeX horizontal size of the page on the surface
     * @param drawSizeY vertical size of the page on the surface
     * @param renderAnnot whether render annotation
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     * You almost always want this to be white (the default)
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList", "TooGenericExceptionCaught", "ReturnCount")
    fun renderPage(
        bufferPtr: Long,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false,
        @ColorInt
        canvasColor: Int = 0xFF848484.toInt(),
        @ColorInt
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean =
        wrapLock {
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
        }

    /**
     * Render page fragment on [Surface].<br></br>
     * @param bufferPtr Surface's buffer on which to render page
     * @param matrix The matrix to map the page to the surface
     * @param clipRect The rectangle to clip the page to
     * @param renderAnnot whether render annotation
     * @param textMask whether to render text as image mask - currently ignored
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     * You almost always want this to be white (the default)
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList")
    fun renderPage(
        bufferPtr: Long,
        drawSizeX: Int,
        drawSizeY: Int,
        matrix: PdfMatrix,
        clipRect: PdfRectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean =
        wrapLock {
            page.renderPage(
                bufferPtr,
                drawSizeX,
                drawSizeY,
                matrix,
                clipRect,
                renderAnnot,
                textMask,
                canvasColor,
                pageBackgroundColor,
            )
        }

    @Suppress("LongParameterList")
    fun renderPage(
        surface: Surface,
        matrix: PdfMatrix,
        clipRect: PdfRectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean =
        wrapLock {
            page.renderPage(
                surface,
                matrix,
                clipRect,
                renderAnnot,
                textMask,
                canvasColor,
                pageBackgroundColor,
            )
        }

    /**
     * Render page fragment on [Bitmap].<br></br>
     * @param bitmap Bitmap on which to render page
     * @param startX left position of the page in the bitmap
     * @param startY top position of the page in the bitmap
     * @param drawSizeX horizontal size of the page on the bitmap
     * @param drawSizeY vertical size of the page on the bitmap
     * @param renderAnnot whether render annotation
     * @param textMask whether to render text as image mask. Currently ignored
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     * You almost always want this to be white (the default)
     * @throws IllegalStateException If the page or document is closed
     *
     * Supported bitmap configurations:
     *
     *  * ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     *  * RGB_565 - little worse quality, 1/2 the memory usage.  Much more expensive to render
     *
     */
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        bitmap: Bitmap?,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ) = wrapLock {
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
     * Render page fragment on [Bitmap].<br></br>
     * @param bitmap Bitmap on which to render page
     * @param matrix The matrix to map the page to the surface
     * @param clipRect The rectangle to clip the page to
     * @param renderAnnot whether render annotation
     * @param textMask whether to render text as image mask. Currently ignored
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     * You almost always want this to be white (the default)
     * @throws IllegalStateException If the page or document is closed
     *
     * Supported bitmap configurations:
     *
     *  * ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     *  * RGB_565 - little worse quality, 1/2 the memory usage.  Much more expensive to render
     *
     */
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        bitmap: Bitmap?,
        matrix: PdfMatrix,
        clipRect: PdfRectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ) = wrapLock {
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

    /** Get all links from given page  */
    fun getPageLinks(): List<Link> =
        wrapLock {
            page.getPageLinks()
        }

    /**
     * Map page coordinates to device screen coordinates
     *
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param pageX     X value in page coordinates
     * @param pageY     Y value in page coordinate
     * @return mapped coordinates
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList")
    fun mapPageCoordsToDevice(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        pageX: Double,
        pageY: Double,
    ): PdfPoint =
        wrapLock {
            page.mapPageCoordsToDevice(
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                pageX,
                pageY,
            )
        }

    /**
     * Map device screen coordinates to page coordinates
     *
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param deviceX   X value in page coordinates
     * @param deviceY   Y value in page coordinate
     * @return mapped coordinates
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList")
    fun mapDeviceCoordsToPage(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        deviceX: Int,
        deviceY: Int,
    ): PdfPointF =
        wrapLock {
            page.mapDeviceCoordsToPage(
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                deviceX,
                deviceY,
            )
        }

    /**
     * maps a rectangle from page space to device space
     *
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param coords    rectangle to map
     *
     * @return mapped coordinates
     *
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList")
    fun mapRectToDevice(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: PdfRectF,
    ): PdfRect =
        wrapLock {
            page.mapRectToDevice(
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                coords,
            )
        }

    /**
     * Maps a rectangle from device space to page space
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param coords    rectangle to map
     * @return mapped coordinates
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList")
    fun mapRectToPage(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: PdfRect,
    ): PdfRectF =
        wrapLock {
            page.mapRectToPage(
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                coords,
            )
        }

    /**
     * Get all attributes of a page in a single call.
     * @return [io.legere.pdfiumandroid.api.PageAttributes]
     */
    fun getPageAttributes(): PageAttributes =
        wrapLock {
            page.getPageAttributes()
        }

    /**
     * Close the page and release all resources
     */
    override fun close() {
        wrapLock {
            page.close()
        }
    }
}
