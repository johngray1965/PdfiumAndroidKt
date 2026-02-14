package io.legere.pdfiumandroid.jni

import android.graphics.Bitmap
import android.view.Surface
import dalvik.annotation.optimization.FastNative

/**
 * Contract for native PDFium page operations.
 * This interface defines the JNI methods for interacting with PDF pages.
 * Implementations of this contract are intended for **internal use only**
 * within the PdfiumAndroid library to abstract native calls.
 */
@Suppress("TooManyFunctions")
interface NativePageContract {
    /**
     * Closes a native PDF page.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page to close.
     */
    fun closePage(pagePtr: Long)

    /**
     * Closes multiple native PDF pages.
     * This is a JNI method.
     *
     * @param pagesPtr An array of native pointers (long) to the PDF pages to close.
     */
    fun closePages(pagesPtr: LongArray)

    /**
     * Gets the page index of a destination linked from a PDF link.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param linkPtr The native pointer (long) to the PDF link.
     * @return The 0-based page index of the destination, or -1 if not found.
     */
    fun getDestPageIndex(
        docPtr: Long,
        linkPtr: Long,
    ): Int

    /**
     * Gets the URI of a web link on a PDF page.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param linkPtr The native pointer (long) to the PDF link.
     * @return The URI string, or `null` if it's not a URI link or an error occurs.
     */
    fun getLinkURI(
        docPtr: Long,
        linkPtr: Long,
    ): String?

    /**
     * Gets the bounding rectangle of a link on a PDF page.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param linkPtr The native pointer (long) to the PDF link.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the rectangle.
     */
    fun getLinkRect(
        docPtr: Long,
        linkPtr: Long,
    ): FloatArray

    /**
     * Locks an Android [Surface] for rendering and retrieves its dimensions and native buffer pointers.
     * This is a JNI method.
     *
     * @param surface The [Surface] to lock.
     * @param dimensions An [IntArray] of size 2 to receive the width and height [width, height].
     * @param ptrs A [LongArray] of size 2 to receive the native window pointer and the buffer pointer.
     * @return `true` if the surface was successfully locked, `false` otherwise.
     */
    fun lockSurface(
        surface: Surface,
        dimensions: IntArray,
        ptrs: LongArray,
    ): Boolean

    /**
     * Unlocks an Android [Surface] and releases native resources.
     * This is a JNI method.
     *
     * @param ptrs A [LongArray] containing the native window pointer and buffer pointer obtained from [lockSurface].
     */
    fun unlockSurface(ptrs: LongArray)

    /**
     * Renders a fragment of a PDF page onto a pre-locked [Surface] buffer.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page to render.
     * @param bufferPtr The native pointer (long) to the locked [Surface] buffer's pixel data.
     * @param startX The X coordinate of the left edge of the rendering area in device pixels.
     * @param startY The Y coordinate of the top edge of the rendering area in device pixels.
     * @param drawSizeHor The horizontal size of the rendering area in device pixels.
     * @param drawSizeVer The vertical size of the rendering area in device pixels.
     * @param renderAnnot `true` to render annotations, `false` otherwise.
     * @param canvasColor The ARGB color to fill the canvas background. Use 0 for no fill.
     * @param pageBackgroundColor The ARGB color to fill the page background. Use 0 for no fill.
     * @return `true` if rendering was successful, `false` otherwise.
     */
    @Suppress("LongParameterList")
    fun renderPage(
        pagePtr: Long,
        bufferPtr: Long,
        startX: Int,
        startY: Int,
        drawSizeHor: Int,
        drawSizeVer: Int,
        renderAnnot: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ): Boolean

    /**
     * Renders a fragment of a PDF page onto a pre-locked [Surface] buffer using a transformation matrix.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page to render.
     * @param bufferPtr The native pointer (long) to the locked [Surface] buffer's pixel data.
     * @param drawSizeHor The horizontal size of the rendering area in device pixels.
     * @param drawSizeVer The vertical size of the rendering area in device pixels.
     * @param matrix A `FloatArray` of 6 elements representing the 2x3 transformation matrix.
     * @param clipRect A `FloatArray` of 4 elements [left, top, right, bottom] defining the clipping rectangle.
     * @param renderAnnot `true` to render annotations, `false` otherwise.
     * @param textMask `true` to render text as an image mask, `false` otherwise. (Currently ignored by Pdfium)
     * @param canvasColor The ARGB color to fill the canvas background. Use 0 for no fill.
     * @param pageBackgroundColor The ARGB color to fill the page background. Use 0 for no fill.
     * @return `true` if rendering was successful, `false` otherwise.
     */
    @Suppress("LongParameterList")
    fun renderPageWithMatrix(
        pagePtr: Long,
        bufferPtr: Long,
        drawSizeHor: Int,
        drawSizeVer: Int,
        matrix: FloatArray,
        clipRect: FloatArray,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ): Boolean

    /**
     * Renders a fragment of a PDF page directly onto an Android [Surface].
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page to render.
     * @param surface The [Surface] to render onto.
     * @param startX The X coordinate of the left edge of the rendering area in device pixels.
     * @param startY The Y coordinate of the top edge of the rendering area in device pixels.
     * @param renderAnnot `true` to render annotations, `false` otherwise.
     * @param canvasColor The ARGB color to fill the canvas background. Use 0 for no fill.
     * @param pageBackgroundColor The ARGB color to fill the page background. Use 0 for no fill.
     * @return `true` if rendering was successful, `false` otherwise.
     */
    @Suppress("LongParameterList")
    fun renderPageSurface(
        pagePtr: Long,
        surface: Surface,
        startX: Int,
        startY: Int,
        renderAnnot: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ): Boolean

    /**
     * Renders a fragment of a PDF page directly onto an Android [Surface] using a transformation matrix.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page to render.
     * @param surface The [Surface] to render onto.
     * @param matrix A `FloatArray` of 6 elements representing the 2x3 transformation matrix.
     * @param clipRect A `FloatArray` of 4 elements [left, top, right, bottom] defining the clipping rectangle.
     * @param renderAnnot `true` to render annotations, `false` otherwise.
     * @param textMask `true` to render text as an image mask, `false` otherwise. (Currently ignored by Pdfium)
     * @param canvasColor The ARGB color to fill the canvas background. Use 0 for no fill.
     * @param pageBackgroundColor The ARGB color to fill the page background. Use 0 for no fill.
     * @return `true` if rendering was successful, `false` otherwise.
     */
    @Suppress("LongParameterList")
    fun renderPageSurfaceWithMatrix(
        pagePtr: Long,
        surface: Surface,
        matrix: FloatArray,
        clipRect: FloatArray,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ): Boolean

    /**
     * Renders a fragment of a PDF page onto an Android [Bitmap].
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param pagePtr The native pointer (long) to the PDF page to render.
     * @param bitmap The [Bitmap] to render onto. Supported formats: `ARGB_8888`, `RGB_565`.
     * @param startX The X coordinate of the left edge of the rendering area in device pixels.
     * @param startY The Y coordinate of the top edge of the rendering area in device pixels.
     * @param drawSizeHor The horizontal size of the rendering area in device pixels.
     * @param drawSizeVer The vertical size of the rendering area in device pixels.
     * @param renderAnnot `true` to render annotations, `false` otherwise.
     * @param textMask `true` to render text as an image mask, `false` otherwise. (Currently ignored by Pdfium)
     * @param canvasColor The ARGB color to fill the canvas background. Use 0 for no fill.
     * @param pageBackgroundColor The ARGB color to fill the page background. Use 0 for no fill.
     */
    @Suppress("LongParameterList")
    fun renderPageBitmap(
        docPtr: Long,
        pagePtr: Long,
        bitmap: Bitmap?,
        startX: Int,
        startY: Int,
        drawSizeHor: Int,
        drawSizeVer: Int,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    )

    /**
     * Renders a fragment of a PDF page onto an Android [Bitmap] using a transformation matrix.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page to render.
     * @param bitmap The [Bitmap] to render onto. Supported formats: `ARGB_8888`, `RGB_565`.
     * @param matrix A `FloatArray` of 6 elements representing the 2x3 transformation matrix.
     * @param clipRect A `FloatArray` of 4 elements [left, top, right, bottom] defining the clipping rectangle.
     * @param renderAnnot `true` to render annotations, `false` otherwise.
     * @param textMask `true` to render text as an image mask, `false` otherwise. (Currently ignored by Pdfium)
     * @param canvasColor The ARGB color to fill the canvas background. Use 0 for no fill.
     * @param pageBackgroundColor The ARGB color to fill the page background. Use 0 for no fill.
     */
    @Suppress("LongParameterList")
    fun renderPageBitmapWithMatrix(
        pagePtr: Long,
        bitmap: Bitmap?,
        matrix: FloatArray,
        clipRect: FloatArray,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
        canvasColor: Int,
        pageBackgroundColor: Int,
    )

    /**
     * Gets the width and height of a PDF page by its index in pixels.
     * This is a JNI method.
     *
     * @param docPtr The native pointer (long) to the PDF document.
     * @param pageIndex The 0-based index of the page.
     * @param dpi The screen DPI (Dots Per Inch) for pixel conversion.
     * @return An [IntArray] of size 2 containing [width, height] in pixels.
     */
    fun getPageSizeByIndex(
        docPtr: Long,
        pageIndex: Int,
        dpi: Int,
    ): IntArray

    /**
     * Retrieves a list of native pointers to links on a PDF page.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A [LongArray] of native pointers to PDF links.
     */
    fun getPageLinks(pagePtr: Long): LongArray

    /**
     * Converts page coordinates to device (pixel) coordinates.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @param startX The X coordinate of the left edge of the display area in device coordinates.
     * @param startY The Y coordinate of the top edge of the display area in device coordinates.
     * @param sizeX The horizontal size (in pixels) for displaying the page.
     * @param sizeY The vertical size (in pixels) for displaying the page.
     * @param rotate Page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (180 degrees), 3 (270 degrees clockwise).
     * @param pageX The X value in page coordinates.
     * @param pageY The Y value in page coordinates.
     * @return An [IntArray] of size 2 containing [deviceX, deviceY].
     */
    @Suppress("LongParameterList")
    fun pageCoordsToDevice(
        pagePtr: Long,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        pageX: Double,
        pageY: Double,
    ): IntArray

    /**
     * Converts device (pixel) coordinates to page coordinates.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @param startX The X coordinate of the left edge of the display area in device coordinates.
     * @param startY The Y coordinate of the top edge of the display area in device coordinates.
     * @param sizeX The horizontal size (in pixels) for displaying the page.
     * @param sizeY The vertical size (in pixels) for displaying the page.
     * @param rotate Page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (180 degrees), 3 (270 degrees clockwise).
     * @param deviceX The X value in device coordinates.
     * @param deviceY The Y value in device coordinates.
     * @return A [FloatArray] of size 2 containing [pageX, pageY].
     */
    @Suppress("LongParameterList")
    fun deviceCoordsToPage(
        pagePtr: Long,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        deviceX: Int,
        deviceY: Int,
    ): FloatArray

    /**
     * Gets the width of a PDF page in pixels.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @param dpi The screen DPI (Dots Per Inch) for pixel conversion.
     * @return The page width in pixels.
     */
    fun getPageWidthPixel(
        pagePtr: Long,
        dpi: Int,
    ): Int

    /**
     * Gets the height of a PDF page in pixels.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @param dpi The screen DPI (Dots Per Inch) for pixel conversion.
     * @return The page height in pixels.
     */
    fun getPageHeightPixel(
        pagePtr: Long,
        dpi: Int,
    ): Int

    /**
     * Gets the width of a PDF page in PostScript points (1/72th of an inch).
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return The page width in points.
     */
    fun getPageWidthPoint(pagePtr: Long): Int

    /**
     * Gets the height of a PDF page in PostScript points (1/72th of an inch).
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return The page height in points.
     */
    fun getPageHeightPoint(pagePtr: Long): Int

    /**
     * Gets the rotation of a PDF page.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return The page rotation (0, 1, 2, or 3 for 0, 90, 180, 270 degrees clockwise).
     */
    fun getPageRotation(pagePtr: Long): Int

    /**
     * Gets the MediaBox of a PDF page in PostScript points.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the MediaBox.
     */
    fun getPageMediaBox(pagePtr: Long): FloatArray

    /**
     * Gets the CropBox of a PDF page in PostScript points.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the CropBox.
     */
    fun getPageCropBox(pagePtr: Long): FloatArray

    /**
     * Gets the BleedBox of a PDF page in PostScript points.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the BleedBox.
     */
    fun getPageBleedBox(pagePtr: Long): FloatArray

    /**
     * Gets the TrimBox of a PDF page in PostScript points.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the TrimBox.
     */
    fun getPageTrimBox(pagePtr: Long): FloatArray

    /**
     * Gets the ArtBox of a PDF page in PostScript points.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the ArtBox.
     */
    fun getPageArtBox(pagePtr: Long): FloatArray

    /**
     * Gets the BoundingBox of a PDF page in PostScript points.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A `FloatArray` of 4 elements [left, top, right, bottom] representing the BoundingBox.
     */
    fun getPageBoundingBox(pagePtr: Long): FloatArray

    /**
     * Gets the transformation matrix of a PDF page.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A `FloatArray` of 6 elements [a, b, c, d, e, f] representing the 2x3 matrix.
     */
    fun getPageMatrix(pagePtr: Long): FloatArray

    /**
     * Gets all relevant attributes of a PDF page in a single call.
     * This is a JNI method.
     *
     * @param pagePtr The native pointer (long) to the PDF page.
     * @return A `FloatArray` containing various page attributes (width, height, rotation, bounding boxes, etc.).
     */
    fun getPageAttributes(pagePtr: Long): FloatArray
}

@Suppress("TooManyFunctions")
internal class NativePage : NativePageContract {
    override fun closePage(pagePtr: Long) = nativeClosePage(pagePtr)

    override fun closePages(pagesPtr: LongArray) = nativeClosePages(pagesPtr)

    override fun getDestPageIndex(
        docPtr: Long,
        linkPtr: Long,
    ) = nativeGetDestPageIndex(docPtr, linkPtr)

    override fun getLinkURI(
        docPtr: Long,
        linkPtr: Long,
    ) = nativeGetLinkURI(docPtr, linkPtr)

    override fun getLinkRect(
        docPtr: Long,
        linkPtr: Long,
    ) = nativeGetLinkRect(docPtr, linkPtr)

    override fun lockSurface(
        surface: Surface,
        dimensions: IntArray,
        ptrs: LongArray,
    ) = nativeLockSurface(surface, dimensions, ptrs)

    override fun unlockSurface(ptrs: LongArray) = nativeUnlockSurface(ptrs)

    @Suppress("LongParameterList")
    override fun renderPage(
        pagePtr: Long,
        bufferPtr: Long,
        startX: Int,
        startY: Int,
        drawSizeHor: Int,
        drawSizeVer: Int,
        renderAnnot: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ) = nativeRenderPage(
        pagePtr,
        bufferPtr,
        startX,
        startY,
        drawSizeHor,
        drawSizeVer,
        renderAnnot,
        canvasColor,
        pageBackgroundColor,
    )

    @Suppress("LongParameterList")
    override fun renderPageWithMatrix(
        pagePtr: Long,
        bufferPtr: Long,
        drawSizeHor: Int,
        drawSizeVer: Int,
        matrix: FloatArray,
        clipRect: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ) = nativeRenderPageWithMatrix(
        pagePtr,
        bufferPtr,
        drawSizeHor,
        drawSizeVer,
        matrix,
        clipRect,
        renderAnnot,
        textMask,
        canvasColor,
        pageBackgroundColor,
    )

    @Suppress("LongParameterList")
    override fun renderPageSurface(
        pagePtr: Long,
        surface: Surface,
        startX: Int,
        startY: Int,
        renderAnnot: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ) = nativeRenderPageSurface(pagePtr, surface, startX, startY, renderAnnot, canvasColor, pageBackgroundColor)

    @Suppress("LongParameterList")
    override fun renderPageSurfaceWithMatrix(
        pagePtr: Long,
        surface: Surface,
        matrix: FloatArray,
        clipRect: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ) = nativeRenderPageSurfaceWithMatrix(
        pagePtr,
        surface,
        matrix,
        clipRect,
        renderAnnot,
        textMask,
        canvasColor,
        pageBackgroundColor,
    )

    @Suppress("LongParameterList")
    override fun renderPageBitmap(
        docPtr: Long,
        pagePtr: Long,
        bitmap: Bitmap?,
        startX: Int,
        startY: Int,
        drawSizeHor: Int,
        drawSizeVer: Int,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ) = nativeRenderPageBitmap(
        docPtr,
        pagePtr,
        bitmap,
        startX,
        startY,
        drawSizeHor,
        drawSizeVer,
        renderAnnot,
        textMask,
        canvasColor,
        pageBackgroundColor,
    )

    @Suppress("LongParameterList")
    override fun renderPageBitmapWithMatrix(
        pagePtr: Long,
        bitmap: Bitmap?,
        matrix: FloatArray,
        clipRect: FloatArray,
        renderAnnot: Boolean,
        textMask: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ) = nativeRenderPageBitmapWithMatrix(
        pagePtr,
        bitmap,
        matrix,
        clipRect,
        renderAnnot,
        textMask,
        canvasColor,
        pageBackgroundColor,
    )

    override fun getPageSizeByIndex(
        docPtr: Long,
        pageIndex: Int,
        dpi: Int,
    ) = nativeGetPageSizeByIndex(docPtr, pageIndex, dpi)

    override fun getPageLinks(pagePtr: Long) = nativeGetPageLinks(pagePtr)

    @Suppress("LongParameterList")
    override fun pageCoordsToDevice(
        pagePtr: Long,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        pageX: Double,
        pageY: Double,
    ) = nativePageCoordsToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY)

    @Suppress("LongParameterList")
    override fun deviceCoordsToPage(
        pagePtr: Long,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        deviceX: Int,
        deviceY: Int,
    ) = nativeDeviceCoordsToPage(pagePtr, startX, startY, sizeX, sizeY, rotate, deviceX, deviceY)

    override fun getPageWidthPixel(
        pagePtr: Long,
        dpi: Int,
    ) = nativeGetPageWidthPixel(pagePtr, dpi)

    override fun getPageHeightPixel(
        pagePtr: Long,
        dpi: Int,
    ) = nativeGetPageHeightPixel(pagePtr, dpi)

    override fun getPageWidthPoint(pagePtr: Long) = nativeGetPageWidthPoint(pagePtr)

    override fun getPageHeightPoint(pagePtr: Long) = nativeGetPageHeightPoint(pagePtr)

    override fun getPageRotation(pagePtr: Long) = nativeGetPageRotation(pagePtr)

    override fun getPageMediaBox(pagePtr: Long) = nativeGetPageMediaBox(pagePtr)

    override fun getPageCropBox(pagePtr: Long) = nativeGetPageCropBox(pagePtr)

    override fun getPageBleedBox(pagePtr: Long) = nativeGetPageBleedBox(pagePtr)

    override fun getPageTrimBox(pagePtr: Long) = nativeGetPageTrimBox(pagePtr)

    override fun getPageArtBox(pagePtr: Long) = nativeGetPageArtBox(pagePtr)

    override fun getPageBoundingBox(pagePtr: Long) = nativeGetPageBoundingBox(pagePtr)

    override fun getPageMatrix(pagePtr: Long) = nativeGetPageMatrix(pagePtr)

    override fun getPageAttributes(pagePtr: Long) = nativeGetPageAttributes(pagePtr)

    /**
     * @suppress
     */
    companion object {
        @JvmStatic
        private external fun nativeClosePage(pagePtr: Long)

        @JvmStatic
        private external fun nativeClosePages(pagesPtr: LongArray)

        @JvmStatic
        private external fun nativeGetDestPageIndex(
            docPtr: Long,
            linkPtr: Long,
        ): Int

        @JvmStatic
        private external fun nativeGetLinkURI(
            docPtr: Long,
            linkPtr: Long,
        ): String?

        @JvmStatic
        private external fun nativeGetLinkRect(
            docPtr: Long,
            linkPtr: Long,
        ): FloatArray

        @JvmStatic
        private external fun nativeLockSurface(
            surface: Surface,
            dimensions: IntArray,
            ptrs: LongArray,
        ): Boolean

        @JvmStatic
        private external fun nativeUnlockSurface(ptrs: LongArray)

        @Suppress("LongParameterList")
        @JvmStatic
        private external fun nativeRenderPage(
            pagePtr: Long,
            bufferPtr: Long,
            startX: Int,
            startY: Int,
            drawSizeHor: Int,
            drawSizeVer: Int,
            renderAnnot: Boolean,
            canvasColor: Int,
            pageBackgroundColor: Int,
        ): Boolean

        @Suppress("LongParameterList")
        @JvmStatic
        private external fun nativeRenderPageWithMatrix(
            pagePtr: Long,
            bufferPtr: Long,
            drawSizeHor: Int,
            drawSizeVer: Int,
            matrix: FloatArray,
            clipRect: FloatArray,
            renderAnnot: Boolean,
            textMask: Boolean,
            canvasColor: Int,
            pageBackgroundColor: Int,
        ): Boolean

        @Suppress("LongParameterList")
        @JvmStatic
        private external fun nativeRenderPageSurface(
            pagePtr: Long,
            surface: Surface,
            startX: Int,
            startY: Int,
            renderAnnot: Boolean,
            canvasColor: Int,
            pageBackgroundColor: Int,
        ): Boolean

        @Suppress("LongParameterList")
        @JvmStatic
        private external fun nativeRenderPageSurfaceWithMatrix(
            pagePtr: Long,
            surface: Surface,
            matrix: FloatArray,
            clipRect: FloatArray,
            renderAnnot: Boolean,
            textMask: Boolean,
            canvasColor: Int,
            pageBackgroundColor: Int,
        ): Boolean

        @Suppress("LongParameterList")
        @JvmStatic
        private external fun nativeRenderPageBitmap(
            docPtr: Long,
            pagePtr: Long,
            bitmap: Bitmap?,
            startX: Int,
            startY: Int,
            drawSizeHor: Int,
            drawSizeVer: Int,
            renderAnnot: Boolean,
            textMask: Boolean,
            canvasColor: Int,
            pageBackgroundColor: Int,
        )

        @Suppress("LongParameterList")
        @JvmStatic
        private external fun nativeRenderPageBitmapWithMatrix(
            pagePtr: Long,
            bitmap: Bitmap?,
            matrix: FloatArray,
            clipRect: FloatArray,
            renderAnnot: Boolean,
            textMask: Boolean,
            canvasColor: Int,
            pageBackgroundColor: Int,
        )

        @JvmStatic
        private external fun nativeGetPageSizeByIndex(
            docPtr: Long,
            pageIndex: Int,
            dpi: Int,
        ): IntArray

        @JvmStatic
        private external fun nativeGetPageLinks(pagePtr: Long): LongArray

        @Suppress("LongParameterList")
        @JvmStatic
        @FastNative
        private external fun nativePageCoordsToDevice(
            pagePtr: Long,
            startX: Int,
            startY: Int,
            sizeX: Int,
            sizeY: Int,
            rotate: Int,
            pageX: Double,
            pageY: Double,
        ): IntArray

        @Suppress("LongParameterList")
        @JvmStatic
        @FastNative
        private external fun nativeDeviceCoordsToPage(
            pagePtr: Long,
            startX: Int,
            startY: Int,
            sizeX: Int,
            sizeY: Int,
            rotate: Int,
            deviceX: Int,
            deviceY: Int,
        ): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeGetPageWidthPixel(
            pagePtr: Long,
            dpi: Int,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeGetPageHeightPixel(
            pagePtr: Long,
            dpi: Int,
        ): Int

        @JvmStatic
        @FastNative
        private external fun nativeGetPageWidthPoint(pagePtr: Long): Int

        @JvmStatic
        @FastNative
        private external fun nativeGetPageHeightPoint(pagePtr: Long): Int

        @JvmStatic
        @FastNative
        private external fun nativeGetPageRotation(pagePtr: Long): Int

        @JvmStatic
        @FastNative
        private external fun nativeGetPageMediaBox(pagePtr: Long): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeGetPageCropBox(pagePtr: Long): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeGetPageBleedBox(pagePtr: Long): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeGetPageTrimBox(pagePtr: Long): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeGetPageArtBox(pagePtr: Long): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeGetPageBoundingBox(pagePtr: Long): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeGetPageMatrix(pagePtr: Long): FloatArray

        @JvmStatic
        @FastNative
        private external fun nativeGetPageAttributes(pagePtr: Long): FloatArray
    }
}
