@file:Suppress("unused")

package io.legere.pdfiumandroid

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.Surface
import io.legere.pdfiumandroid.util.Size
import java.io.Closeable

@Suppress("TooManyFunctions")
class PdfPage(
    private val doc: PdfDocument,
    private val pageIndex: Int,
    val pagePtr: Long
) : Closeable {

    var isClosed = false
        private set

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
        pagePtr: Long, surface: Surface?, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    @Suppress("LongParameterList")
    private external fun nativeRenderPageBitmap(
        pagePtr: Long, bitmap: Bitmap?, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean, textMask: Boolean
    )
    private external fun nativeGetPageSizeByIndex(docPtr: Long, pageIndex: Int, dpi: Int): Size
    private external fun nativeGetPageLinks(pagePtr: Long): LongArray

    @Suppress("LongParameterList")
    private external fun nativePageCoordsToDevice(
        pagePtr: Long, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point

    @Suppress("LongParameterList")
    private external fun nativeDeviceCoordsToPage(
        pagePtr: Long, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, deviceX: Int, deviceY: Int
    ): PointF

    /**
     * Get page width in pixels. <br></br>
     * This method requires page to be opened.
     */
    fun getPageWidth(screenDpi: Int): Int {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeGetPageWidthPixel(pagePtr, screenDpi)
        }
    }

    /**
     * Get page height in pixels. <br></br>
     * This method requires page to be opened.
     */
    fun getPageHeight(screenDpi: Int): Int {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeGetPageHeightPixel(pagePtr, screenDpi)
        }
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getPageWidthPoint(): Int {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeGetPageWidthPoint(pagePtr)
        }
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getPageHeightPoint(): Int {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            return nativeGetPageHeightPoint(pagePtr)
        }
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
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
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
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
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
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
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
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
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
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
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
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
     * Get size of page in pixels.<br></br>
     * This method does not require given page to be opened.
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
     * Page must be opened before rendering.
     */
    @Suppress("LongParameterList")
    fun renderPage(surface: Surface?,
            startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, screenDpi: Int) {
        check(!isClosed && !doc.isClosed) { "Already closed" }
            renderPage(surface, startX, startY, drawSizeX, drawSizeY, screenDpi, false )
    }

    /**
     * Render page fragment on [Surface]. This method allows to render annotations.<br></br>
     * Page must be opened before rendering.
     */
    @Suppress("LongParameterList", "TooGenericExceptionCaught")
    fun renderPage(
        surface: Surface?,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
        screenDpi: Int,
        renderAnnot: Boolean
    ) {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            try {
                //nativeRenderPage(doc.mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
                nativeRenderPage(
                    pagePtr, surface, screenDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null", e)
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native", e)
            }
        }
    }

    /**
     * Render page fragment on [Bitmap].<br></br>
     * Page must be opened before rendering.
     *
     *
     * Supported bitmap configurations:
     *
     *  * ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     *  * RGB_565 - little worse quality, twice less memory usage
     *
     */
    @Suppress("LongParameterList")
    fun renderPageBitmap(bitmap: Bitmap?,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, screenDpi: Int, textMask: Boolean
    ) {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        renderPageBitmap(
            bitmap,
            startX,
            startY,
            drawSizeX,
            drawSizeY,
            screenDpi,
            false,
            textMask
        )
    }

    /**
     * Render page fragment on [Bitmap]. This method allows to render annotations.<br></br>
     * Page must be opened before rendering.
     *
     *
     * For more info see [PdfiumCore.renderPageBitmap]
     */
    @Suppress("LongParameterList")
    fun renderPageBitmap(bitmap: Bitmap?,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, screenDpi: Int,
        renderAnnot: Boolean, textMask: Boolean
    ) {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        synchronized(PdfiumCore.lock) {
            nativeRenderPageBitmap(
                pagePtr, bitmap, screenDpi,
                startX, startY, drawSizeX, drawSizeY, renderAnnot, textMask
            )
        }
    }

    /** Get all links from given page  */
    fun getPageLinks(): List<PdfDocument.Link> {
        check(!isClosed && !doc.isClosed) { "Already closed" }
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
     */
    @Suppress("LongParameterList")
    fun mapPageCoordsToDevice(startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
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
     */
    @Suppress("LongParameterList")
    fun mapDeviceCoordsToPage(startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, deviceX: Int, deviceY: Int
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
     * @see PdfiumCore.mapPageCoordsToDevice
     * @return mapped coordinates
     */
    @Suppress("LongParameterList")
    fun mapRectToDevice(startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, coords: RectF
    ): Rect {
        check(!isClosed && !doc.isClosed) { "Already closed" }
        val leftTop = mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate,
            coords.left.toDouble(), coords.top.toDouble()
        )
        val rightBottom = mapPageCoordsToDevice(startX, startY, sizeX, sizeY, rotate,
            coords.right.toDouble(), coords.bottom.toDouble()
        )
        return Rect(
            leftTop.x,
            leftTop.y,
            rightBottom.x,
            rightBottom.y
        )
    }

    /**
     * @see PdfiumCore.mapDeviceCoordsToPage
     * @return mapped coordinates
     */
    @Suppress("LongParameterList")
    fun mapRectToPage(startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, coords: Rect
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



    override fun close() {
        if (isClosed) return
        synchronized(PdfiumCore.lock) {
            isClosed = true
            nativeClosePage(pagePtr)
        }
    }

    companion object {
        private const val TAG =  "PdfPage"

        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3
    }

}
