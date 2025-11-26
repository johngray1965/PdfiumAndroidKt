package io.legere.pdfiumandroid.jni

import android.graphics.Bitmap
import android.view.Surface
import dalvik.annotation.optimization.FastNative

@Suppress("TooManyFunctions")
class NativePage {
    internal fun closePage(pagePtr: Long) = nativeClosePage(pagePtr)

    internal fun closePages(pagesPtr: LongArray) = nativeClosePages(pagesPtr)

    internal fun getDestPageIndex(
        docPtr: Long,
        linkPtr: Long,
    ) = nativeGetDestPageIndex(docPtr, linkPtr)

    internal fun getLinkURI(
        docPtr: Long,
        linkPtr: Long,
    ) = nativeGetLinkURI(docPtr, linkPtr)

    internal fun getLinkRect(
        docPtr: Long,
        linkPtr: Long,
    ) = nativeGetLinkRect(docPtr, linkPtr)

    internal fun lockSurface(
        surface: Surface,
        dimensions: IntArray,
        ptrs: LongArray,
    ) = nativeLockSurface(surface, dimensions, ptrs)

    internal fun unlockSurface(ptrs: LongArray) = nativeUnlockSurface(ptrs)

    @Suppress("LongParameterList")
    internal fun renderPage(
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
    internal fun renderPageWithMatrix(
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
    internal fun renderPageSurface(
        pagePtr: Long,
        surface: Surface,
        startX: Int,
        startY: Int,
        renderAnnot: Boolean,
        canvasColor: Int,
        pageBackgroundColor: Int,
    ) = nativeRenderPageSurface(pagePtr, surface, startX, startY, renderAnnot, canvasColor, pageBackgroundColor)

    @Suppress("LongParameterList")
    internal fun renderPageSurfaceWithMatrix(
        pagePtr: Long,
        surface: Surface,
        matrix: FloatArray,
        clipRect: FloatArray,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
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
    internal fun renderPageBitmap(
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
    internal fun renderPageBitmapWithMatrix(
        pagePtr: Long,
        bitmap: Bitmap?,
        matrix: FloatArray,
        clipRect: FloatArray,
        renderAnnot: Boolean = false,
        textMask: Boolean = false,
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

    internal fun getPageSizeByIndex(
        docPtr: Long,
        pageIndex: Int,
        dpi: Int,
    ) = nativeGetPageSizeByIndex(docPtr, pageIndex, dpi)

    internal fun getPageLinks(pagePtr: Long) = nativeGetPageLinks(pagePtr)

    @Suppress("LongParameterList")
    internal fun pageCoordsToDevice(
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
    internal fun deviceCoordsToPage(
        pagePtr: Long,
        startX: Int,
        startY: Int,
        sizeX: Int,
        sizeY: Int,
        rotate: Int,
        deviceX: Int,
        deviceY: Int,
    ) = nativeDeviceCoordsToPage(pagePtr, startX, startY, sizeX, sizeY, rotate, deviceX, deviceY)

    internal fun getPageWidthPixel(
        pagePtr: Long,
        dpi: Int,
    ) = nativeGetPageWidthPixel(pagePtr, dpi)

    internal fun getPageHeightPixel(
        pagePtr: Long,
        dpi: Int,
    ) = nativeGetPageHeightPixel(pagePtr, dpi)

    internal fun getPageWidthPoint(pagePtr: Long) = nativeGetPageWidthPoint(pagePtr)

    internal fun getPageHeightPoint(pagePtr: Long) = nativeGetPageHeightPoint(pagePtr)

    internal fun getPageRotation(pagePtr: Long) = nativeGetPageRotation(pagePtr)

    internal fun getPageMediaBox(pagePtr: Long) = nativeGetPageMediaBox(pagePtr)

    internal fun getPageCropBox(pagePtr: Long) = nativeGetPageCropBox(pagePtr)

    internal fun getPageBleedBox(pagePtr: Long) = nativeGetPageBleedBox(pagePtr)

    internal fun getPageTrimBox(pagePtr: Long) = nativeGetPageTrimBox(pagePtr)

    internal fun getPageArtBox(pagePtr: Long) = nativeGetPageArtBox(pagePtr)

    internal fun getPageBoundingBox(pagePtr: Long) = nativeGetPageBoundingBox(pagePtr)

    internal fun getPageMatrix(pagePtr: Long) = nativeGetPageMatrix(pagePtr)

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
            renderAnnot: Boolean = false,
            textMask: Boolean = false,
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
            renderAnnot: Boolean = false,
            textMask: Boolean = false,
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
            renderAnnot: Boolean = false,
            textMask: Boolean = false,
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
    }
}
