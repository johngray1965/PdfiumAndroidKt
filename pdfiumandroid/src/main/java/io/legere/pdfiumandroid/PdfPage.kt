@file:Suppress("unused")

package io.legere.pdfiumandroid

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import io.legere.pdfiumandroid.util.Size
import io.legere.pdfiumandroid.util.handleAlreadyClosed
import java.io.Closeable

private const val THREE_BY_THREE = 9

/**
 * Represents a single page in a [PdfDocument].
 */
@Suppress("TooManyFunctions")
class PdfPage(
    val doc: PdfDocument,
    val pageIndex: Int,
    val pagePtr: Long,
    private val pageMap: MutableMap<Int, PdfDocument.PageCount>
) : Closeable {

    private var isClosed = false

    private external fun nativeClosePage(pagePtr: Long)
    private external fun nativeClosePages(pagesPtr: LongArray)
    private external fun nativeGetPageWidthPixel(pagePtr: Long, dpi: Int): Int
    private external fun nativeGetPageHeightPixel(pagePtr: Long, dpi: Int): Int
    private external fun nativeGetPageWidthPoint(pagePtr: Long): Int
    private external fun nativeGetPageHeightPoint(pagePtr: Long): Int
    private external fun nativeGetPageMediaBox(pagePtr: Long): FloatArray
    private external fun nativeGetPageCropBox(pagePtr: Long): FloatArray
    private external fun nativeGetPageBleedBox(pagePtr: Long): FloatArray
    private external fun nativeGetPageTrimBox(pagePtr: Long): FloatArray
    private external fun nativeGetPageArtBox(pagePtr: Long): FloatArray
    private external fun nativeGetPageBoundingBox(pagePtr: Long): FloatArray
    private external fun nativeGetDestPageIndex(docPtr: Long, linkPtr: Long): Int?
    private external fun nativeGetLinkURI(docPtr: Long, linkPtr: Long): String?
    private external fun nativeGetLinkRect(docPtr: Long, linkPtr: Long): RectF?

    @Suppress("LongParameterList")
    private external fun nativeRenderPage(
        pagePtr: Long,
        surface: Surface?,
        startX: Int,
        startY: Int,
        drawSizeHor: Int,
        drawSizeVer: Int,
        renderAnnot: Boolean
    )

    @Suppress("LongParameterList")
    private external fun nativeRenderPageBitmap(
        pagePtr: Long,
        bitmap: Bitmap?,
        startX: Int,
        startY: Int,
        drawSizeHor: Int,
        drawSizeVer: Int,
        renderAnnot: Boolean,
        textMask: Boolean
    )

    @Suppress("LongParameterList")
    private external fun nativeRenderPageBitmapWithMatrix(
        pagePtr: Long,
        bitmap: Bitmap?,
        matrix: FloatArray,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false
    )

    private external fun nativeGetPageSizeByIndex(docPtr: Long, pageIndex: Int, dpi: Int): Size
    private external fun nativeGetPageLinks(pagePtr: Long): LongArray

    @Suppress("LongParameterList")
    private external fun nativePageCoordsToDevice(
        pagePtr: Long,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        pageX: Double,
        pageY: Double
    ): Point

    @Suppress("LongParameterList")
    private external fun nativeDeviceCoordsToPage(
        pagePtr: Long,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        deviceX: Int,
        deviceY: Int
    ): PointF

    /**
     * Open a text page
     * @return the opened [PdfTextPage]
     * @throws IllegalArgumentException if document is closed or the page cannot be loaded
     */
    @Suppress("DEPRECATION")
    fun openTextPage(): PdfTextPage = doc.openTextPage(this)

    /**
     * Get page width in pixels.
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return page width in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageWidth(screenDpi: Int): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1

        synchronized(PdfiumCore.lock) {
            return nativeGetPageWidthPixel(pagePtr, screenDpi)
        }
    }

    /**
     * Get page height in pixels.
     * @param screenDpi screen DPI (Dots Per Inch)
     * @return page height in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageHeight(screenDpi: Int): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1

        synchronized(PdfiumCore.lock) {
            return nativeGetPageHeightPixel(pagePtr, screenDpi)
        }
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).
     * @return page width in points
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageWidthPoint(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        synchronized(PdfiumCore.lock) {
            return nativeGetPageWidthPoint(pagePtr)
        }
    }

    /**
     * Get page height in PostScript points (1/72th of an inch)
     * @return page height in points
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageHeightPoint(): Int {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return -1
        synchronized(PdfiumCore.lock) {
            return nativeGetPageHeightPoint(pagePtr)
        }
    }

    /**
     *  Get the page's crop box in PostScript points (1/72th of an inch)
     *  @return page crop box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageCropBox(): RectF {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            val o = nativeGetPageCropBox(pagePtr)
            val r = RectF()
            r.left = o[LEFT]
            r.top = o[TOP]
            r.right = o[RIGHT]
            r.bottom = o[BOTTOM]
            return r
        }
    }

    /**
     *  Get the page's media box in PostScript points (1/72th of an inch)
     *  @return page media box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageMediaBox(): RectF {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            val o = nativeGetPageMediaBox(pagePtr)
            val r = RectF()
            r.left = o[LEFT]
            r.top = o[TOP]
            r.right = o[RIGHT]
            r.bottom = o[BOTTOM]
            return r
        }
    }

    /**
     *  Get the page's bleed box in PostScript points (1/72th of an inch)
     *  @return page bleed box in pointsor RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageBleedBox(): RectF {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            val o = nativeGetPageBleedBox(pagePtr)
            val r = RectF()
            r.left = o[LEFT]
            r.top = o[TOP]
            r.right = o[RIGHT]
            r.bottom = o[BOTTOM]
            return r
        }
    }

    /**
     *  Get the page's trim box in PostScript points (1/72th of an inch)
     *  @return page trim box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageTrimBox(): RectF {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            val o = nativeGetPageTrimBox(pagePtr)
            val r = RectF()
            r.left = o[LEFT]
            r.top = o[TOP]
            r.right = o[RIGHT]
            r.bottom = o[BOTTOM]
            return r
        }
    }

    /**
     *  Get the page's art box in PostScript points (1/72th of an inch)
     *  @return page art box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageArtBox(): RectF {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            val o = nativeGetPageArtBox(pagePtr)
            val r = RectF()
            r.left = o[LEFT]
            r.top = o[TOP]
            r.right = o[RIGHT]
            r.bottom = o[BOTTOM]
            return r
        }
    }

    /**
     *  Get the page's bounding box in PostScript points (1/72th of an inch)
     *  @return page bounding box in points or RectF(-1, -1, -1, -1) if not present
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageBoundingBox(): RectF {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            val o = nativeGetPageBoundingBox(pagePtr)
            val r = RectF()
            r.left = o[LEFT]
            r.top = o[TOP]
            r.right = o[RIGHT]
            r.bottom = o[BOTTOM]
            return r
        }
    }

    /**
     *  Get the page's size in pixels
     *  @return page size in pixels
     *  @throws IllegalStateException If the page or document is closed
     */
    fun getPageSize(screenDpi: Int): Size {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeGetPageSizeByIndex(
                doc.mNativeDocPtr,
                pageIndex,
                screenDpi
            )
        }
    }

    /**
     * Render page fragment on [Surface].<br></br>
     * @param surface Surface on which to render page
     * @param startX left position of the page in the surface
     * @param startY top position of the page in the surface
     * @param drawSizeX horizontal size of the page on the surface
     * @param drawSizeY vertical size of the page on the surface
     * @param renderAnnot whether render annotation
     * @throws IllegalStateException If the page or document is closed
     */
    @Suppress("LongParameterList", "TooGenericExceptionCaught")
    fun renderPage(
        surface: Surface?,
        startX: Int,
        startY: Int,
        drawSizeX: Int,
        drawSizeY: Int,
        renderAnnot: Boolean = false
    ) {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return
        synchronized(PdfiumCore.lock) {
            try {
                // nativeRenderPage(doc.mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
                nativeRenderPage(
                    pagePtr,
                    surface,
                    startX,
                    startY,
                    drawSizeX,
                    drawSizeY,
                    renderAnnot
                )
            } catch (e: NullPointerException) {
                Logger.e(TAG, e, "mContext may be null")
            } catch (e: Exception) {
                Logger.e(TAG, e, "Exception throw from native")
            }
        }
    }

    /**
     * Render page fragment on [Bitmap].<br></br>
     * @param bitmap Bitmap on which to render page
     * @param startX left position of the page in the bitmap
     * @param startY top position of the page in the bitmap
     * @param drawSizeX horizontal size of the page on the bitmap
     * @param drawSizeY vertical size of the page on the bitmap
     * @param renderAnnot whether render annotation
     * @param textMask whether to render text as image mask
     * @throws IllegalStateException If the page or document is closed
     *
     * Supported bitmap configurations:
     *
     *  * ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     *  * RGB_565 - little worse quality, 1/2 the memory usage
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
        textMask: Boolean = false
    ) {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return
        synchronized(PdfiumCore.lock) {
            nativeRenderPageBitmap(
                pagePtr,
                bitmap,
                startX,
                startY,
                drawSizeX,
                drawSizeY,
                renderAnnot,
                textMask
            )
        }
    }

    fun renderPageBitmap(
        bitmap: Bitmap?,
        matrix: Matrix,
        clipRect: RectF,
        renderAnnot: Boolean = false,
        textMask: Boolean = false
    ) {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return
        val matrixValues = FloatArray(THREE_BY_THREE)
        matrix.getValues(matrixValues)
        synchronized(PdfiumCore.lock) {
            nativeRenderPageBitmapWithMatrix(
                pagePtr,
                bitmap,
                floatArrayOf(
                    matrixValues[Matrix.MSCALE_X],
                    matrixValues[Matrix.MSCALE_Y],
                    matrixValues[Matrix.MTRANS_X],
                    matrixValues[Matrix.MTRANS_Y]
                ),
                clipRect,
                renderAnnot,
                textMask
            )
        }
    }

    /** Get all links from given page  */
    fun getPageLinks(): List<PdfDocument.Link> {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return emptyList()
        synchronized(PdfiumCore.lock) {
            val links: MutableList<PdfDocument.Link> =
                ArrayList()
            val linkPtrs = nativeGetPageLinks(pagePtr)
            for (linkPtr in linkPtrs) {
                val index = nativeGetDestPageIndex(doc.mNativeDocPtr, linkPtr)
                val uri = nativeGetLinkURI(doc.mNativeDocPtr, linkPtr)
                val rect = nativeGetLinkRect(doc.mNativeDocPtr, linkPtr)
                if (rect != null && (index != null || uri != null)) {
                    links.add(PdfDocument.Link(rect, index, uri))
                }
            }
            return links
        }
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
        pageY: Double
    ): Point {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        return nativePageCoordsToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY)
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
        deviceY: Int
    ): PointF {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        return nativeDeviceCoordsToPage(
            pagePtr,
            startX,
            startY,
            sizeX,
            sizeY,
            rotate,
            deviceX,
            deviceY
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
        coords: RectF
    ): Rect {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        val leftTop = mapPageCoordsToDevice(
            startX,
            startY,
            sizeX,
            sizeY,
            rotate,
            coords.left.toDouble(),
            coords.top.toDouble()
        )
        val rightBottom = mapPageCoordsToDevice(
            startX,
            startY,
            sizeX,
            sizeY,
            rotate,
            coords.right.toDouble(),
            coords.bottom.toDouble()
        )
        return Rect(
            leftTop.x,
            leftTop.y,
            rightBottom.x,
            rightBottom.y
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
        coords: Rect
    ): RectF {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        val leftTop = mapDeviceCoordsToPage(
            startX,
            startY,
            sizeX,
            sizeY,
            rotate,
            coords.left,
            coords.top
        )
        val rightBottom = mapDeviceCoordsToPage(
            startX,
            startY,
            sizeX,
            sizeY,
            rotate,
            coords.right,
            coords.bottom
        )
        return RectF(leftTop.x, leftTop.y, rightBottom.x, rightBottom.y)
    }

    /**
     * Close the page and release all resources
     */
    override fun close() {
        if (handleAlreadyClosed(isClosed || doc.isClosed)) return

        synchronized(PdfiumCore.lock) {
            pageMap[pageIndex]?.let {
                if (it.count > 1) {
                    it.count--
                    return
                }
            }

            pageMap.remove(pageIndex)

            isClosed = true
            nativeClosePage(pagePtr)
        }
    }

    companion object {
        private const val TAG = "PdfPage"

        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3
    }
}
