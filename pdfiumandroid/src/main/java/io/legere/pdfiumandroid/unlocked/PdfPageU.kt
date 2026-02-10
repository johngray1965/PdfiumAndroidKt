@file:Suppress("unused")

package io.legere.pdfiumandroid.unlocked

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import androidx.annotation.ColorInt
import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfTextPage
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.NativePage
import io.legere.pdfiumandroid.jni.defaultNativeFactory
import io.legere.pdfiumandroid.util.PageAttributes
import io.legere.pdfiumandroid.util.Size
import io.legere.pdfiumandroid.util.floatArrayToMatrix
import io.legere.pdfiumandroid.util.floatArrayToRect
import io.legere.pdfiumandroid.util.handleAlreadyClosed
import io.legere.pdfiumandroid.util.matrixToFloatArray
import io.legere.pdfiumandroid.util.rectToFloatArray
import java.io.Closeable

private const val RECT_SIZE = 4

/**
 * Represents a single page in a [PdfDocument].
 */
@Suppress("TooManyFunctions")
class PdfPageU(
    val doc: PdfDocumentU,
    val pageIndex: Int,
    val pagePtr: Long,
    private val pageMap: MutableMap<Int, PdfDocument.PageCount>,
    nativeFactory: NativeFactory = defaultNativeFactory,
) : Closeable {
    @Volatile
    internal var isClosed = false

    private val nativePage: NativePage = nativeFactory.getNativePage()

    /**
     * Open a text page
     * @return the opened [PdfTextPage]
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    @Suppress("DEPRECATION")
    fun openTextPage(): PdfTextPageU = doc.openTextPage(this)

    /**
     * Get page width in pixels.
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
     * @return page width in points
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageWidthPoint(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativePage.getPageWidthPoint(pagePtr)
    }

    /**
     * Get page height in PostScript points (1/72th of an inch)
     * @return page height in points
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageHeightPoint(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativePage.getPageHeightPoint(pagePtr)
    }

    @Suppress("LongParameterList", "MagicNumber")
    fun getPageMatrix(): Matrix? {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return null

        return floatArrayToMatrix(nativePage.getPageMatrix(pagePtr))
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
    fun getPageRotation(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        return nativePage.getPageRotation(pagePtr)
    }

    /**
     *  Get the page's crop box in PostScript points (1/72th of an inch)
     *  @return page crop box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageCropBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageCropBox(pagePtr))
    }

    /**
     *  Get the page's media box in PostScript points (1/72th of an inch)
     *  @return page media box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageMediaBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageMediaBox(pagePtr))
    }

    /**
     *  Get the page's bleed box in PostScript points (1/72th of an inch)
     *  @return page bleed box in pointsor RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageBleedBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageBleedBox(pagePtr))
    }

    /**
     *  Get the page's trim box in PostScript points (1/72th of an inch)
     *  @return page trim box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageTrimBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageTrimBox(pagePtr))
    }

    /**
     *  Get the page's art box in PostScript points (1/72th of an inch)
     *  @return page art box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageArtBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageArtBox(pagePtr))
    }

    /**
     *  Get the page's bounding box in PostScript points (1/72th of an inch)
     *  @return page bounding box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageBoundingBox(): RectF {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return RectF(-1f, -1f, -1f, -1f)
        return floatArrayToRect(nativePage.getPageBoundingBox(pagePtr))
    }

    /**
     *  Get the page's size in pixels
     *  @return page size in pixels
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

    /** Get all links from given page  */
    fun getPageLinks(): List<PdfDocument.Link> {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return emptyList()
        val links: MutableList<PdfDocument.Link> =
            ArrayList()
        val linkPtrs = nativePage.getPageLinks(pagePtr)
        for (linkPtr in linkPtrs) {
            val index = nativePage.getDestPageIndex(doc.mNativeDocPtr, linkPtr)
            val uri = nativePage.getLinkURI(doc.mNativeDocPtr, linkPtr)
            val rect = nativePage.getLinkRect(doc.mNativeDocPtr, linkPtr)
            if (rect.size == RECT_SIZE && (index != -1 || uri != null)) {
                links.add(
                    PdfDocument.Link(
                        rect.let { rectFloats ->
                            floatArrayToRect(rectFloats)
                        },
                        index,
                        uri,
                    ),
                )
            }
        }
        return links
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
    ): Point {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return Point(-1, -1)
        return nativePage.pageCoordsToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY).let {
            Point(it[0], it[1])
        }
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
     */
    @Suppress("MagicNumber")
    fun getPageAttributes(): PageAttributes {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) {
            return PageAttributes(0, 0, 0, RectF(), RectF(), RectF(), RectF(), RectF(), RectF(), Matrix(), emptyList())
        }
        val data = nativePage.getPageAttributes(pagePtr)
        return PageAttributes(
            pageWidth = data[0].toInt(),
            pageHeight = data[1].toInt(),
            pageRotation = data[2].toInt(),
            mediaBox = RectF(data[3], data[4], data[5], data[6]),
            cropBox = RectF(data[7], data[8], data[9], data[10]),
            bleedBox = RectF(data[11], data[12], data[13], data[14]),
            trimBox = RectF(data[15], data[16], data[17], data[18]),
            artBox = RectF(data[19], data[20], data[21], data[22]),
            boundingBox = RectF(data[23], data[24], data[25], data[26]),
            pageMatrix = floatArrayToMatrix(floatArrayOf(data[27], data[28], data[29], data[30], data[31], data[32])),
            links = getPageLinks(),
        )
    }

    /**
     * Close the page and release all resources
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

    fun lockSurface(
        surface: Surface,
        dimensions: IntArray,
        ptrs: LongArray,
    ): Boolean =
        synchronized(PdfiumCore.lock) {
            nativePage.lockSurface(surface, dimensions, ptrs)
        }

    fun unlockSurface(ptrs: LongArray) =
        synchronized(PdfiumCore.lock) {
            nativePage.unlockSurface(ptrs)
        }

    companion object {
        private const val TAG = "PdfPage"
    }
}
