package io.legere.pdfiumandroid.unlocked

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.base.BasePDFTest
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfPageUClosedPageNotHandledTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        pdfPage = pdfDocument.openPage(0)
        pdfPage.close()
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
        val conf = Bitmap.Config.RGB_565 // see other conf types

        val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

        pdfPage.renderPageBitmap(bmp, 0, 0, 612, 792, true)
    }

    @Test(expected = IllegalStateException::class)
    fun testRenderPageBitmap() {
        val conf = Bitmap.Config.RGB_565 // see other conf types

        val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

        pdfPage.renderPageBitmap(bmp, 0, 0, 612, 792, renderAnnot = true, textMask = true)
    }

    @Test(expected = IllegalStateException::class)
    fun getPageLinks() {
        pdfPage.getPageLinks()
    }

    @Test(expected = IllegalStateException::class)
    fun mapPageCoordsToDevice() {
        pdfPage.mapPageCoordsToDevice(0, 0, 100, 100, 0, 0.0, 0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun mapDeviceCoordsToPage() {
        pdfPage.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 0, 0)
    }

    @Test(expected = IllegalStateException::class)
    fun mapRectToDevice() {
        pdfPage.mapRectToDevice(0, 0, 100, 100, 0, RectF(0f, 0f, 100f, 100f))
    }

    @Test(expected = IllegalStateException::class)
    fun mapRectToPage() {
        pdfPage.mapRectToPage(0, 0, 100, 100, 0, Rect(0, 0, 100, 100))
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
