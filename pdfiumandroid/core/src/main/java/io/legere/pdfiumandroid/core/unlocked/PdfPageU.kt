@file:Suppress("unused")

package io.legere.pdfiumandroid.core.unlocked

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import androidx.annotation.ColorInt
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.PageAttributes
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.api.handleAlreadyClosed
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.NativePageContract
import io.legere.pdfiumandroid.core.jni.defaultNativeFactory
import io.legere.pdfiumandroid.core.util.PageCount
import io.legere.pdfiumandroid.core.util.floatArrayToMatrix
import io.legere.pdfiumandroid.core.util.floatArrayToRect
import io.legere.pdfiumandroid.core.util.matrixToFloatArray
import io.legere.pdfiumandroid.core.util.rectToFloatArray
import io.legere.pdfiumandroid.core.util.wrapLock
import java.io.Closeable

private const val RECT_SIZE = 4

/**
 * Represents an **unlocked** single page in a [PdfDocumentU].
 * This class is for **internal use only** within the PdfiumAndroid library.
 * Direct use from outside the library is not recommended as it bypasses thread-safety mechanisms.
 *
 * @property doc The parent [PdfDocumentU] this page belongs to.
 * @property pageIndex The 0-based index of this page within the document.
 * @property pagePtr The native pointer to the FPDF_PAGE object.
 * @property pageMap A mutable map used internally to track open page counts.
 * @property nativePage The native interface for page operations.
 */
@Suppress("TooManyFunctions")
class PdfPageU(
    val doc: PdfDocumentU,
    val pageIndex: Int,
    val pagePtr: Long,
    private val pageMap: MutableMap<Int, PageCount>,
    nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    @Volatile
    var isClosed = false

    private val nativePage: NativePageContract = nativeFactory.getNativePage()

    /**
     * Open a text page.
     * For internal use only.
     *
     * @return the opened [PdfTextPageU]
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    @Suppress("DEPRECATION")
    fun openTextPage(): PdfTextPageU = doc.openTextPage(this)

    /**
     * Get page width in pixels.
     * For internal use only.
     *
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return page width in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageWidth(screenDpi: Int): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativePage.getPageWidthPixel(pagePtr, screenDpi)
    }

    /**
     * Get page height in pixels.
     * For internal use only.
     *
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return page height in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageHeight(screenDpi: Int): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativePage.getPageHeightPixel(pagePtr, screenDpi)
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @return page width in points
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageWidthPoint(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativePage.getPageWidthPoint(pagePtr)
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @return page height in points
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageHeightPoint(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativePage.getPageHeightPoint(pagePtr)
    }

    /**
     * Get the page's transformation matrix.
     * For internal use only.
     *
     * @return A [Matrix] representing the page's transformation, or `null` if an error occurs.
     */
    @Suppress("LongParameterList", "MagicNumber")
    fun getPageMatrix(): Matrix? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null

        return floatArrayToMatrix(nativePage.getPageMatrix(pagePtr))
    }

    /**
     * Get page rotation in degrees.
     * For internal use only.
     *
     * @return
     *  -1 - Error
     *  0 - No rotation.
     *  1 - Rotated 90 degrees clockwise.
     *  2 - Rotated 180 degrees clockwise.
     *  3 - Rotated 270 degrees clockwise.
     *
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageRotation(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativePage.getPageRotation(pagePtr)
    }

    /**
     * Get the page's crop box in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @return page crop box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageCropBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageCropBox(pagePtr))
    }

    /**
     * Get the page's media box in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @return page media box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageMediaBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageMediaBox(pagePtr))
    }

    /**
     * Get the page's bleed box in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @return page bleed box in pointsor RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageBleedBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageBleedBox(pagePtr))
    }

    /**
     * Get the page's trim box in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @return page trim box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageTrimBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageTrimBox(pagePtr))
    }

    /**
     * Get the page's art box in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @return page art box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageArtBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageArtBox(pagePtr))
    }

    /**
     * Get the page's bounding box in PostScript points (1/72th of an inch).
     * For internal use only.
     *
     * @return page bounding box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageBoundingBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageBoundingBox(pagePtr))
    }

    /**
     * Get the page's size in pixels.
     * For internal use only.
     *
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return page size in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageSize(screenDpi: Int): Size {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return Size(-1, -1)
        return nativePage
            .getPageSizeByIndex(
                doc.mNativeDocPtr,
                pageIndex,
                screenDpi,
            ).let {
                Size(it[0], it[1])
            }
    }

    /**
     * Render page fragment on [Surface]'s buffer.
     * For internal use only.
     *
     * @param bufferPtr Surface's buffer on which to render page
     * @param startX left position of the page in the surface
     * @param startY top position of the page in the surface
     * @param drawSizeX horizontal size of the page on the surface
     * @param drawSizeY vertical size of the page on the surface
     * @param renderAnnot whether render annotation
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     * You almost always want this to be white (the default)
     * @return `true` if rendering was successful, `false` otherwise.
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
    ): Boolean {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return false
        try {
            // nativePage.renderPage(doc.mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
            return nativePage.renderPage(
                pagePtr,
                bufferPtr,
                startX,
                startY,
                drawSizeX,
                drawSizeY,
                renderAnnot,
                canvasColor,
                pageBackgroundColor,
            )
        } catch (e: NullPointerException) {
            Logger.e(TAG, e, "mContext may be null")
        } catch (e: Exception) {
            Logger.e(TAG, e, "Exception throw from native")
        }
        return false
    }

    /**
     * Render page fragment on [Surface]'s buffer with a transformation matrix.
     * For internal use only.
     *
     * @param bufferPtr Surface's buffer on which to render page
     * @param matrix The matrix to map the page to the surface
     * @param clipRect The rectangle to clip the page to
     * @param renderAnnot whether render annotation
     * @param textMask whether to render text as image mask - currently ignored
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     * You almost always want this to be white (the default)
     * @return `true` if rendering was successful, `false` otherwise.
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList")
    fun renderPage(
        bufferPtr: Long,
        drawSizeX: Int,
        drawSizeY: Int,
        matrix: Matrix,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return false
        return nativePage.renderPageWithMatrix(
            pagePtr,
            bufferPtr,
            drawSizeX,
            drawSizeY,
            matrixToFloatArray(matrix),
            rectToFloatArray(clipRect),
            renderAnnot,
            textMask,
            canvasColor,
            pageBackgroundColor,
        )
    }

    /**
     * Render page fragment directly on a [Surface] with a transformation matrix.
     * For internal use only.
     *
     * @param surface The [Surface] on which to render the page.
     * @param matrix The matrix to map the page to the surface.
     * @param clipRect The rectangle to clip the page to.
     * @param renderAnnot whether render annotation.
     * @param textMask whether to render text as image mask - currently ignored.
     * @param canvasColor The color to fill the canvas with. Use 0 to not fill the canvas.
     * @param pageBackgroundColor The color for the page background. Use 0 to not fill the background.
     * You almost always want this to be white (the default).
     * @return `true` if rendering was successful, `false` otherwise.
     * @throws IllegalStateException If the page or document is closed.
     */
    @Suppress("LongParameterList")
    fun renderPage(
        surface: Surface,
        matrix: Matrix,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ): Boolean {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return false
        return nativePage.renderPageSurfaceWithMatrix(
            pagePtr,
            surface,
            matrixToFloatArray(matrix),
            rectToFloatArray(clipRect),
            renderAnnot,
            textMask,
            canvasColor,
            pageBackgroundColor,
        )
    }

    /**
     * Render page fragment on a [Bitmap].
     * For internal use only.
     *
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
    ) {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return
        nativePage.renderPageBitmap(
            doc.mNativeDocPtr,
            pagePtr,
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
     * Render page fragment on a [Bitmap] with a transformation matrix.
     * For internal use only.
     *
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
     */
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        bitmap: Bitmap?,
        matrix: Matrix,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int = 0xFF848484.toInt(),
        pageBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    ) {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return
        nativePage.renderPageBitmapWithMatrix(
            pagePtr,
            bitmap,
            matrixToFloatArray(matrix),
            rectToFloatArray(clipRect),
            renderAnnot,
            textMask,
            canvasColor,
            pageBackgroundColor,
        )
    }

    /**
     * Get all links from given page.
     * For internal use only.
     *
     * @return A list of [io.legere.pdfiumandroid.api.Link] found on the page.
     * @throws IllegalStateException If the page or document is closed.
     */
    fun getPageLinks(): List<Link> {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return emptyList()
        val linkPtrs = nativePage.getPageLinks(pagePtr)
        val links =
            Array(linkPtrs.size) { i ->
                val linkPtr = linkPtrs[i]
                val index = nativePage.getDestPageIndex(doc.mNativeDocPtr, linkPtr)
                val uri = nativePage.getLinkURI(doc.mNativeDocPtr, linkPtr)
                val rect = nativePage.getLinkRect(doc.mNativeDocPtr, linkPtr)
                Link(
                    floatArrayToRect(rect),
                    index,
                    uri,
                )
            }
        return links.toList()
    }

    /**
     * Map page coordinates to device screen coordinates.
     * For internal use only.
     *
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 270 degrees clockwise)
     * @param pageX     X value in page coordinates
     * @param pageY     Y value in page coordinate
     * @return mapped coordinates as a [Point]
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
    ): Point {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return Point(-1, -1)
        return nativePage
            .pageCoordsToDevice(
                pagePtr,
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                pageX,
                pageY,
            ).let {
                Point(it[0], it[1])
            }
    }

    /**
     * Map device screen coordinates to page coordinates.
     * For internal use only.
     *
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 270 degrees clockwise)
     * @param deviceX   X value in page coordinates
     * @param deviceY   Y value in page coordinate
     * @return mapped coordinates as a [PointF]
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
    ): PointF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return PointF(-1f, -1f)
        return nativePage
            .deviceCoordsToPage(
                pagePtr,
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                deviceX,
                deviceY,
            ).let {
                PointF(it[0], it[1])
            }
    }

    /**
     * Maps a rectangle from page space to device space.
     * For internal use only.
     *
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 270 degrees clockwise)
     * @param coords    rectangle to map
     *
     * @return mapped coordinates as a [Rect]
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
        coords: RectF,
    ): Rect {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return Rect(-1, -1, -1, -1)
        val leftTop =
            mapPageCoordsToDevice(
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                coords.left.toDouble(),
                coords.top.toDouble(),
            )
        val rightBottom =
            mapPageCoordsToDevice(
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                coords.right.toDouble(),
                coords.bottom.toDouble(),
            )
        return Rect(
            leftTop.x,
            leftTop.y,
            rightBottom.x,
            rightBottom.y,
        )
    }

    /**
     * Maps a rectangle from device space to page space.
     * For internal use only.
     *
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 270 degrees clockwise)
     * @param coords    rectangle to map
     * @return mapped coordinates as a [RectF]
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList")
    fun mapRectToPage(
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        coords: Rect,
    ): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        val leftTop =
            mapDeviceCoordsToPage(
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                coords.left,
                coords.top,
            )
        val rightBottom =
            mapDeviceCoordsToPage(
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                coords.right,
                coords.bottom,
            )
        return RectF(leftTop.x, leftTop.y, rightBottom.x, rightBottom.y)
    }

    /**
     * Get all attributes of a page in a single call.
     * For internal use only.
     *
     * @return A [io.legere.pdfiumandroid.api.PageAttributes] object containing various properties of the page.
     * @throws IllegalStateException If the page or document is closed.
     */
    @Suppress("MagicNumber")
    fun getPageAttributes(): PageAttributes {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) {
            return PageAttributes.EMPTY
        }
        val data = nativePage.getPageAttributes(pagePtr)

        val pageWidth = data[0]
        val pageHeight = data[1]
        val pageWidthInt = pageWidth.toInt()
        val pageHeightInt = pageHeight.toInt()
        val pageRotation = data[2].toInt()
        val left = data[27]
        val top = data[28]
        val right = data[29]
        val bottom = data[30]

        val key = PdfDocumentU.MatrixKey(pageWidthInt, pageHeightInt, pageRotation, right.toInt(), bottom.toInt())

        val pageRectF = RectF(0f, 0f, pageWidth, pageHeight)
        val mappedRect = RectF(left, top, right, bottom)

        val matrix =
            doc.getCachedMatrix(key) { m ->
                calculateRectTranslateMatrix(
                    pageRectF,
                    mappedRect,
                    result = m,
                )
            }

        return PageAttributes(
            page = pageIndex,
            pageWidth = pageWidthInt,
            pageHeight = pageHeightInt,
            pageRotation = pageRotation,
            rect = pageRectF,
            mediaBox = RectF(data[3], data[4], data[5], data[6]),
            cropBox = RectF(data[7], data[8], data[9], data[10]),
            bleedBox = RectF(data[11], data[12], data[13], data[14]),
            trimBox = RectF(data[15], data[16], data[17], data[18]),
            artBox = RectF(data[19], data[20], data[21], data[22]),
            boundingBox = RectF(data[23], data[24], data[25], data[26]),
            pageMatrix = matrix,
            links = getPageLinks(),
        )
    }

    /**
     * Close the page and release all resources.
     * For internal use only.
     */
    override fun close() {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return

        pageMap[pageIndex]?.let {
            if (it.count > 1) {
                it.count--
                return
            }

            pageMap.remove(pageIndex)

            isClosed = true
            nativePage.closePage(pagePtr)
        } ?: run {
            isClosed = true
            nativePage.closePage(pagePtr)
        }
    }

    /**
     * Locks the surface and retrieves its dimensions and buffer pointers.
     * For internal use only.
     *
     * @param surface The [Surface] to lock.
     * @param dimensions An [IntArray] to store the width and height of the locked surface.
     * @param ptrs A [LongArray] to store the native window and buffer pointers.
     * @return `true` if the surface was successfully locked, `false` otherwise.
     */
    fun lockSurface(
        surface: Surface,
        dimensions: IntArray,
        ptrs: LongArray,
    ): Boolean =
        wrapLock {
            nativePage.lockSurface(surface, dimensions, ptrs)
        }

    /**
     * Unlocks the surface and releases the native window.
     * For internal use only.
     *
     * @param ptrs A [LongArray] containing the native window and buffer pointers obtained from [lockSurface].
     */
    fun unlockSurface(ptrs: LongArray) =
        wrapLock {
            nativePage.unlockSurface(ptrs)
        }

    /**
     * @suppress
     */
    companion object {
        private const val TAG = "PdfPage"

        /**
         * Calculates a transformation matrix to map a source rectangle to a destination rectangle.
         * For internal use only.
         *
         * @param from The source [RectF].
         * @param to The destination [RectF].
         * @param result The [Matrix] object to store the calculated transformation. Will be reset before calculation.
         */
        fun calculateRectTranslateMatrix(
            from: RectF?,
            to: RectF?,
            result: Matrix?,
        ) {
            if (from == null || to == null || result == null) {
                return
            }
            if (from.width() == 0f || from.height() == 0f) {
                return
            }
            result.reset()
            result.postTranslate(-from.left, -from.top)
            result.postScale(to.width() / from.width(), to.height() / from.height())
            result.postTranslate(to.left, to.top)
        }
    }
}
