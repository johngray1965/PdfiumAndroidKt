package io.legere.pdfiumandroid.jni

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.unlocked.PdfPageU
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.util.matrixToFloatArray
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativePageTest : BasePDFTest() {
    private val nativePage = defaultNativeFactory.getNativePage()
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private var pdfBytes: ByteArray? = null

    private var pagePtr: Long = 0

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        pdfPage = pdfDocument.openPage(0)!!
        pagePtr = pdfPage.pagePtr
    }

    @After
    fun tearDown() {
        try {
            pdfPage.close()
        } catch (e: Exception) {
            println("Exception: $e")
        }
        pdfDocument.close()
    }

    @Test
    fun getPageSizeByIndex() {
        val size =
            nativePage
                .getPageSizeByIndex(
                    pdfDocument.mNativeDocPtr,
                    0,
                    72,
                )

        assertThat(size).isEqualTo(intArrayOf(612, 792))
    }

    @Test
    fun getPageLinks() {
        val links = nativePage.getPageLinks(pagePtr)

        Truth.assertThat(links.size).isEqualTo(0) // The test doc doesn't have links
    }

    @Test
    fun pageCoordsToDevice() {
        val devicePt = nativePage.pageCoordsToDevice(pagePtr, 0, 0, 100, 100, 0, 0.0, 0.0)

        assertThat(devicePt).isEqualTo(intArrayOf(0, 100))
    }

    @Test
    fun deviceCoordsToPage() {
        val devicePt = nativePage.deviceCoordsToPage(pagePtr, 0, 0, 100, 100, 0, 0, 0)

        assertThat(devicePt).isEqualTo(floatArrayOf(0f, 792.00006f))
    }

    @Test
    fun getPageWidthPixel() {
        val pageWidth = nativePage.getPageWidthPixel(pagePtr, 72)
        assertThat(pageWidth).isEqualTo(612) // 8.5 inches * 72 dpi
    }

    @Test
    fun getPageHeightPixel() {
        val pageHeight = nativePage.getPageHeightPixel(pagePtr, 72)

        Truth.assertThat(pageHeight).isEqualTo(792) // 11 inches * 72 dpi
    }

    @Test
    fun getPageWidthPoint() {
        val pageWidthPoint = nativePage.getPageWidthPoint(pagePtr)

        Truth.assertThat(pageWidthPoint).isEqualTo(612) // 11 inches * 72 dpi
    }

    @Test
    fun getPageHeightPoint() {
        val pageHeightPoint = nativePage.getPageHeightPoint(pagePtr)

        Truth.assertThat(pageHeightPoint).isEqualTo(792) // 11 inches * 72 dpi
    }

    @Test
    fun getPageRotation() {
        val rotation = nativePage.getPageRotation(pagePtr)

        assertThat(rotation).isEqualTo(0)
    }

    @Test
    fun getPageMediaBox() {
        val mediaBox = nativePage.getPageMediaBox(pagePtr)

        assertThat(mediaBox).isEqualTo(floatArrayOf(0.0f, 0.0f, 612.0f, 792.0f))
    }

    @Test
    fun getPageCropBox() {
        val cropBox = nativePage.getPageCropBox(pagePtr)

        assertThat(cropBox).isEqualTo(noResultFloatArray)
    }

    @Test
    fun getPageBleedBox() {
        val bleedBox = nativePage.getPageBleedBox(pagePtr)

        assertThat(bleedBox).isEqualTo(noResultFloatArray)
    }

    @Test
    fun getPageTrimBox() {
        val trimBox = nativePage.getPageTrimBox(pagePtr)

        assertThat(trimBox).isEqualTo(noResultFloatArray)
    }

    @Test
    fun getPageArtBox() {
        val artBox = nativePage.getPageArtBox(pagePtr)

        assertThat(artBox).isEqualTo(noResultFloatArray)
    }

    @Test
    fun getPageBoundingBox() {
        val boundingBox = nativePage.getPageBoundingBox(pagePtr)

        // Note, that looks incorrect, but pdfs coordinate systems starts from bottom left corner
        assertThat(boundingBox).isEqualTo(floatArrayOf(0f, 792f, 612f, 0f))
    }

    @Test
    fun getPageMatrix() {
        val matrix = nativePage.getPageMatrix(pagePtr)

        val expectedMatrix =
            floatArrayOf(
                0.99808574f,
                0.0f,
                0.0f,
                1.0000007f,
                89.99916f,
                699.1205f,
            )
        assertThat(matrix).isEqualTo(expectedMatrix)
    }

    @Test
    fun getPageAttributes() {
        val attribs = pdfPage.getPageAttributes()

        assertThat(attribs.pageWidth).isEqualTo(612)
        assertThat(attribs.pageHeight).isEqualTo(792)
        assertThat(attribs.pageRotation).isEqualTo(0)
        assertThat(attribs.mediaBox).isEqualTo(android.graphics.RectF(0.0f, 0.0f, 612.0f, 792.0f))
        assertThat(attribs.cropBox).isEqualTo(noResultRect)
        assertThat(attribs.bleedBox).isEqualTo(noResultRect)
        assertThat(attribs.trimBox).isEqualTo(noResultRect)
        assertThat(attribs.artBox).isEqualTo(noResultRect)
        assertThat(attribs.boundingBox).isEqualTo(android.graphics.RectF(0f, 792f, 612f, 0f))

        val matrixValues = FloatArray(9)
        attribs.pageMatrix.getValues(matrixValues)

        // FPDF_GetPageMatrix with 0,0,612,792,0 should give:
        // a=1, b=0, c=0, d=-1, e=0, f=792
        // MSCALE_X (a), MSKEW_X (c), MTRANS_X (e)
        // MSKEW_Y (b), MSCALE_Y (d), MTRANS_Y (f)
        // MPERSP_0 (0), MPERSP_1 (0), MPERSP_2 (1)

        val expectedValues =
            floatArrayOf(
                1.0f,
                0.0f,
                0.0f,
                0.0f,
                -1.0f,
                792.0f,
                0.0f,
                0.0f,
                1.0f,
            )

        assertThat(matrixValues).isEqualTo(expectedValues)
        assertThat(attribs.links).isEmpty()
    }

    @Test
    fun renderPageBitmap() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        pdfPage.renderPageBitmap(bitmap, 0, 0, 100, 100)

        // Check if a pixel is not transparent
        assertThat(bitmap.getPixel(50, 50)).isNotEqualTo(0)
    }

    @Test
    fun renderPageBitmapWithMatrix() {
        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
        val clipRect = floatArrayOf(0f, 0f, 100f, 100f)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        nativePage.renderPageBitmapWithMatrix(
            pdfPage.pagePtr,
            bitmap,
            matrixToFloatArray(matrix),
            clipRect,
            renderAnnot = false,
            textMask = false,
            canvasColor = 0,
            pageBackgroundColor = 0,
        )

        // Check if a pixel is not transparent
        assertThat(bitmap.getPixel(50, 50)).isNotEqualTo(0)
    }

    @Test
    fun renderPageBitmapWithMatrixWithDefaults() {
        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
        val clipRect = floatArrayOf(0f, 0f, 100f, 100f)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        nativePage.renderPageBitmapWithMatrix(
            pdfPage.pagePtr,
            bitmap,
            matrixToFloatArray(matrix),
            clipRect,
            canvasColor = 0,
            pageBackgroundColor = 0,
        )

        // Check if a pixel is not transparent
        assertThat(bitmap.getPixel(50, 50)).isNotEqualTo(0)
    }

    @Test
    fun renderPageBitmapWithMatrixWithAnnotations() {
        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
        val clipRect = floatArrayOf(0f, 0f, 100f, 100f)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        nativePage.renderPageBitmapWithMatrix(
            pdfPage.pagePtr,
            bitmap,
            matrixToFloatArray(matrix),
            clipRect,
            renderAnnot = true,
            textMask = true,
            canvasColor = 0,
            pageBackgroundColor = 0,
        )

        // Check if a pixel is not transparent
        assertThat(bitmap.getPixel(50, 50)).isNotEqualTo(0)
    }

    @Test
    fun renderPageSurface() {
        val surfaceTexture = SurfaceTexture(10)
        surfaceTexture.setDefaultBufferSize(100, 100)
        val surface = Surface(surfaceTexture)

        val result =
            nativePage.renderPageSurface(
                pdfPage.pagePtr,
                surface,
                0,
                0,
                renderAnnot = false,
                canvasColor = 0,
                pageBackgroundColor = 0,
            )

        assertThat(result).isTrue()
        surface.release()
        surfaceTexture.release()
    }

    @Test
    fun renderPageSurfaceWithMatrix() {
        fun renderPageSurfaceWithOptions(
            renderAnnot: Boolean,
            textMask: Boolean,
        ) {
            val surfaceTexture = SurfaceTexture(11)
            surfaceTexture.setDefaultBufferSize(100, 100)
            val surface = Surface(surfaceTexture)

            val matrix = Matrix()
            matrix.postScale(0.5f, 0.5f)
            val clipRect = floatArrayOf(0f, 0f, 100f, 100f)

            val result =
                nativePage.renderPageSurfaceWithMatrix(
                    pdfPage.pagePtr,
                    surface,
                    matrixToFloatArray(matrix),
                    clipRect,
                    renderAnnot = renderAnnot,
                    textMask = textMask,
                    canvasColor = 0,
                    pageBackgroundColor = 0,
                )

            assertThat(result).isTrue()
            surface.release()
            surfaceTexture.release()
        }

        renderPageSurfaceWithOptions(renderAnnot = false, textMask = false)
        renderPageSurfaceWithOptions(renderAnnot = true, textMask = false)
        renderPageSurfaceWithOptions(renderAnnot = false, textMask = true)
        renderPageSurfaceWithOptions(renderAnnot = true, textMask = true)
    }

    @Test
    fun renderPageSurfaceWithMatrixWithDefaults() {
        val surfaceTexture = SurfaceTexture(11)
        surfaceTexture.setDefaultBufferSize(100, 100)
        val surface = Surface(surfaceTexture)

        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
        val clipRect = floatArrayOf(0f, 0f, 100f, 100f)

        val result =
            nativePage.renderPageSurfaceWithMatrix(
                pdfPage.pagePtr,
                surface,
                matrixToFloatArray(matrix),
                clipRect,
                canvasColor = 0,
                pageBackgroundColor = 0,
            )

        assertThat(result).isTrue()
        surface.release()
        surfaceTexture.release()
    }

    @Test
    fun renderPageBufferRenderAnnotationTrue() {
        renderPageBufferWithOptions(true)
    }

    @Test
    fun renderPageBufferRenderAnnotationFalse() {
        renderPageBufferWithOptions(false)
    }

    private fun renderPageBufferWithOptions(renderAnnot: Boolean) {
        val surfaceTexture = SurfaceTexture(12)
        surfaceTexture.setDefaultBufferSize(100, 100)
        val surface = Surface(surfaceTexture)

        val dimensions = IntArray(2)
        val ptrs = LongArray(2)
        nativePage.lockSurface(surface, dimensions, ptrs)
        val bufferPtr = ptrs[1]

        assertThat(bufferPtr).isNotEqualTo(0L)

        val result =
            nativePage.renderPage(
                pdfPage.pagePtr,
                bufferPtr,
                0,
                0,
                dimensions[0],
                dimensions[1],
                renderAnnot = renderAnnot,
                canvasColor = 0,
                pageBackgroundColor = 0,
            )

        assertThat(result).isTrue()

        nativePage.unlockSurface(ptrs)
        surface.release()
        surfaceTexture.release()
    }

    @Test
    fun renderPageWithMatrixBufferFalseFalse() {
        renderPageWithMatrixBufferWithOptions(renderAnnot = false, textMask = false)
    }

    @Test
    fun renderPageWithMatrixBufferFalseTrue() {
        renderPageWithMatrixBufferWithOptions(renderAnnot = false, textMask = true)
    }

    @Test
    fun renderPageWithMatrixBufferTrueFalse() {
        renderPageWithMatrixBufferWithOptions(renderAnnot = true, textMask = false)
    }

    @Test
    fun renderPageWithMatrixBufferTrueTrue() {
        renderPageWithMatrixBufferWithOptions(renderAnnot = true, textMask = true)
    }

    @Test
    fun renderPageWithMatrixBufferWithDefaults() {
        val surfaceTexture = SurfaceTexture(13)
        surfaceTexture.setDefaultBufferSize(100, 100)
        val surface = Surface(surfaceTexture)

        val dimensions = IntArray(2)
        val ptrs = LongArray(2)
        nativePage.lockSurface(surface, dimensions, ptrs)
        val bufferPtr = ptrs[1]

        assertThat(bufferPtr).isNotEqualTo(0L)

        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
        val clipRect = floatArrayOf(0f, 0f, 100f, 100f)

        val result =
            nativePage.renderPageWithMatrix(
                pdfPage.pagePtr,
                bufferPtr,
                dimensions[0],
                dimensions[1],
                matrixToFloatArray(matrix),
                clipRect,
                canvasColor = 0,
                pageBackgroundColor = 0,
            )

        assertThat(result).isTrue()

        nativePage.unlockSurface(ptrs)
        surface.release()
        surfaceTexture.release()
    }

    private fun renderPageWithMatrixBufferWithOptions(
        renderAnnot: Boolean,
        textMask: Boolean,
    ) {
        val surfaceTexture = SurfaceTexture(13)
        surfaceTexture.setDefaultBufferSize(100, 100)
        val surface = Surface(surfaceTexture)

        val dimensions = IntArray(2)
        val ptrs = LongArray(2)
        nativePage.lockSurface(surface, dimensions, ptrs)
        val bufferPtr = ptrs[1]

        assertThat(bufferPtr).isNotEqualTo(0L)

        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
        val clipRect = floatArrayOf(0f, 0f, 100f, 100f)

        val result =
            nativePage.renderPageWithMatrix(
                pdfPage.pagePtr,
                bufferPtr,
                dimensions[0],
                dimensions[1],
                matrixToFloatArray(matrix),
                clipRect,
                renderAnnot = renderAnnot,
                textMask = textMask,
                canvasColor = 0,
                pageBackgroundColor = 0,
            )

        assertThat(result).isTrue()

        nativePage.unlockSurface(ptrs)
        surface.release()
        surfaceTexture.release()
    }

    @Test
    fun renderPageWithMatrixBufferWithAnnotations() {
        val surfaceTexture = SurfaceTexture(13)
        surfaceTexture.setDefaultBufferSize(100, 100)
        val surface = Surface(surfaceTexture)

        val dimensions = IntArray(2)
        val ptrs = LongArray(2)
        nativePage.lockSurface(surface, dimensions, ptrs)
        val bufferPtr = ptrs[1]

        assertThat(bufferPtr).isNotEqualTo(0L)

        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
        val clipRect = floatArrayOf(0f, 0f, 100f, 100f)

        val result =
            nativePage.renderPageWithMatrix(
                pdfPage.pagePtr,
                bufferPtr,
                dimensions[0],
                dimensions[1],
                matrixToFloatArray(matrix),
                clipRect,
                renderAnnot = true,
                textMask = true,
                canvasColor = 0,
                pageBackgroundColor = 0,
            )

        assertThat(result).isTrue()

        nativePage.unlockSurface(ptrs)
        surface.release()
        surfaceTexture.release()
    }

    @Test
    fun closePages() {
        val pdfPage2 = pdfDocument.openPage(1)!!
        val pagePtr2 = pdfPage2.pagePtr
        val pages2close = longArrayOf(pagePtr, pagePtr2)
        println("Before Closing pages: ${pages2close.joinToString()}")
        nativePage.closePages(pages2close)
        println("After Closing pages: ${pages2close.joinToString()}")
        // reopen the page so it's not closed
        pdfPage = pdfDocument.openPage(0)!!
        pagePtr = pdfPage.pagePtr
    }
}
