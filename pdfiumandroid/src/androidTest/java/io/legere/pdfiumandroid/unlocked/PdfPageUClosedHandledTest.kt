package io.legere.pdfiumandroid.unlocked

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.util.AlreadyClosedBehavior
import io.legere.pdfiumandroid.util.Config
import io.mockk.mockk
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("Migrating to non-instrumented tests")
class PdfPageUClosedHandledTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument =
            PdfiumCoreU(
                config =
                    Config(
                        alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE,
                    ),
            ).newDocument(pdfBytes)
        pdfPage = pdfDocument.openPage(0)
        pdfPage.close()
    }

    @Test
    fun getPageWidth() {
        pdfPage.getPageWidth(72)
    }

    @Test
    fun getPageHeight() {
        pdfPage.getPageHeight(72)
    }

    @Test
    fun getPageWidthPoint() {
        pdfPage.getPageWidthPoint()
    }

    @Test
    fun getPageHeightPoint() {
        pdfPage.getPageHeightPoint()
    }

    @Test
    fun getPageMatrix() {
        pdfPage.getPageMatrix()
    }

    @Test
    fun getPageRotation() {
        pdfPage.getPageRotation()
    }

    @Test
    fun getPageCropBox() {
        pdfPage.getPageCropBox()
    }

    @Test
    fun getPageMediaBox() {
        pdfPage.getPageMediaBox()
    }

    @Test
    fun getPageBleedBox() {
        pdfPage.getPageBleedBox()
    }

    @Test
    fun getPageTrimBox() {
        pdfPage.getPageTrimBox()
    }

    @Test
    fun getPageArtBox() {
        pdfPage.getPageArtBox()
    }

    @Test
    fun getPageBoundingBox() {
        pdfPage.getPageBoundingBox()
    }

    @Test
    fun getPageSize() {
        pdfPage.getPageSize(72)
    }

    @Test
    fun renderPage() {
        val surface: Surface = mockk()
        pdfPage.renderPage(surface, Matrix(), RectF(), renderAnnot = true, textMask = true)
    }

    @Test
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

    @Test
    fun renderPageBitmap() {
        val conf = Bitmap.Config.RGB_565 // see other conf types

        val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

        pdfPage.renderPageBitmap(bmp, 0, 0, 612, 792, true)
    }

    @Test
    fun testRenderPageBitmap() {
        val conf = Bitmap.Config.RGB_565 // see other conf types

        val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

        pdfPage.renderPageBitmap(bmp, 0, 0, 612, 792, renderAnnot = true, textMask = true)
    }

    @Test
    fun getPageLinks() {
        pdfPage.getPageLinks()
    }

    @Test
    fun mapPageCoordsToDevice() {
        pdfPage.mapPageCoordsToDevice(0, 0, 100, 100, 0, 0.0, 0.0)
    }

    @Test
    fun mapDeviceCoordsToPage() {
        pdfPage.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 0, 0)
    }

    @Test
    fun mapRectToDevice() {
        pdfPage.mapRectToDevice(0, 0, 100, 100, 0, RectF(0f, 0f, 100f, 100f))
    }

    @Test
    fun mapRectToPage() {
        pdfPage.mapRectToPage(0, 0, 100, 100, 0, Rect(0, 0, 100, 100))
    }

    @Test
    fun close() {
        var pageAfterClose: PdfPageU?
        pdfDocument.openPage(0).use { page ->
            pageAfterClose = page
        }
        pageAfterClose!!.getPageWidth(72)
    }
}
