package io.legere.pdfiumandroid.unlocked

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.mockk.mockk
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("Migrating to non-instrumented tests")
class PdfPageUClosedDocumentNotHandledTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        pdfPage = pdfDocument.openPage(0)
        pdfDocument.close()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageWidth() {
        pdfPage.getPageWidth(72)
    }

    @Test(expected = IllegalStateException::class)
    fun getPageHeight() {
        pdfPage.getPageHeight(72)
    }

    @Test(expected = IllegalStateException::class)
    fun getPageWidthPoint() {
        pdfPage.getPageWidthPoint()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageHeightPoint() {
        pdfPage.getPageHeightPoint()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageMatrix() {
        pdfPage.getPageMatrix()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageRotation() {
        pdfPage.getPageRotation()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageCropBox() {
        pdfPage.getPageCropBox()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageMediaBox() {
        pdfPage.getPageMediaBox()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageBleedBox() {
        pdfPage.getPageBleedBox()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageTrimBox() {
        pdfPage.getPageTrimBox()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageArtBox() {
        pdfPage.getPageArtBox()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageBoundingBox() {
        pdfPage.getPageBoundingBox()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageSize() {
        pdfPage.getPageSize(72)
    }

    @Test(expected = IllegalStateException::class)
    fun renderPage() {
        val surface: Surface = mockk()
        pdfPage.renderPage(surface, Matrix(), RectF(), renderAnnot = true, textMask = true)
    }

    @Test(expected = IllegalStateException::class)
    fun testRenderPage() {
        pdfPage.renderPage(
            bufferPtr = 0L,
            startX = 0,
            startY = 0,
            drawSizeX = 100,
            drawSizeY = 100,
            renderAnnot = true,
        )
    }

//    @Test(expected = IllegalStateException::class)
//    fun testRenderPage1() {
//        pdfPage.renderPage(
//            bufferPtr = 0L,
//            drawSizeX = 0,
//            drawSizeY = 0,
//            matrix = Matrix(),
//            clipRect = RectF(),
//            renderAnnot = true,
//            textMask = true,
//            canvasColor = TODO(),
//            pageBackgroundColor = TODO(),
//        )
//    }

    @Test(expected = IllegalStateException::class)
    fun renderPageBitmap() {
        pdfDocument.openPage(0).use { page ->

            val conf = Bitmap.Config.RGB_565 // see other conf types

            val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

            page.renderPageBitmap(bmp, 0, 0, 612, 792, true)

            // How to verify that it's correct?
            // Even if we don't verify the bitmap, we can check that it doesn't crash
        }
    }

    @Test(expected = IllegalStateException::class)
    fun testRenderPageBitmap() {
        pdfDocument.openPage(0).use { page ->

            val conf = Bitmap.Config.RGB_565 // see other conf types

            val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

            page.renderPageBitmap(bmp, 0, 0, 612, 792, renderAnnot = true, textMask = true)

            // How to verify that it's correct?
            // Even if we don't verify the bitmap, we can check that it doesn't crash
        }
    }

    @Test(expected = IllegalStateException::class)
    fun getPageLinks() {
        pdfDocument.openPage(0).use { page ->
            val links = page.getPageLinks()

            Truth.assertThat(links.size).isEqualTo(0) // The test doc doesn't have links
        }
    }

    @Test(expected = IllegalStateException::class)
    fun mapPageCoordsToDevice() {
        pdfDocument.openPage(0).use { page ->
            val devicePt = page.mapPageCoordsToDevice(0, 0, 100, 100, 0, 0.0, 0.0)

            assertThat(devicePt).isEqualTo(Point(0, 100))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun mapDeviceCoordsToPage() {
        pdfDocument.openPage(0).use { page ->
            val devicePt = page.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 0, 0)

            assertThat(devicePt).isEqualTo(PointF(0f, 792.00006f))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun mapRectToDevice() {
        pdfDocument.openPage(0).use { page ->
            val devicePt = page.mapRectToDevice(0, 0, 100, 100, 0, RectF(0f, 0f, 100f, 100f))

            assertThat(devicePt).isEqualTo(
                Rect(
                    // 0f in coords to 0f in device
                    0,
                    // 0f in corrds in at the bottom, the bottom of the device is 100f
                    100,
                    // 100f in coords = 100f/(8.5*72) * 100f = 16f
                    16,
                    // 100f in coords = 100 - 100f/(11*72) * 100f = 87f
                    87,
                ),
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun mapRectToPage() {
        pdfDocument.openPage(0).use { page ->
            val devicePt = page.mapRectToPage(0, 0, 100, 100, 0, Rect(0, 0, 100, 100))

            assertThat(devicePt).isEqualTo(
                RectF(0.0f, 792.00006f, 612.0f, 0.0f),
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun close() {
        var pageAfterClose: PdfPageU?
        pdfDocument.openPage(0).use { page ->
            pageAfterClose = page
        }
        pageAfterClose!!.getPageWidth(72)
    }
}
