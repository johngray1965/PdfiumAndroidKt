package io.legere.pdfiumandroid

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.util.Size
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfPageTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocument
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        TestCase.assertNotNull(pdfBytes)

        pdfDocument = PdfiumCore().newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun getPageWidth() {
        pdfDocument.openPage(0).use { page ->
            val pageWidth = page.getPageWidth(72)

            assertThat(pageWidth).isEqualTo(612) // 8.5 inches * 72 dpi
        }
    }

    @Test
    fun getPageHeight() {
        pdfDocument.openPage(0).use { page ->
            val pageWidth = page.getPageHeight(72)

            assertThat(pageWidth).isEqualTo(792) // 11 inches * 72 dpi
        }
    }

    @Test
    fun getPageWidthPoint() {
        pdfDocument.openPage(0).use { page ->
            val pageWidth = page.getPageWidthPoint()

            assertThat(pageWidth).isEqualTo(612) // 11 inches * 72 dpi
        }
    }

    @Test
    fun getPageHeightPoint() {
        pdfDocument.openPage(0).use { page ->
            val pageWidth = page.getPageHeightPoint()

            assertThat(pageWidth).isEqualTo(792) // 11 inches * 72 dpi
        }
    }

    @Test
    fun getPageCropBox() {
        pdfDocument.openPage(0).use { page ->
            val cropBox = page.getPageCropBox()

            assertThat(cropBox).isEqualTo(noResultRect)
        }
    }

    @Test
    fun getPageMediaBox() {
        pdfDocument.openPage(0).use { page ->
            val mediaBox = page.getPageMediaBox()

            assertThat(mediaBox).isEqualTo(RectF(0.0f, 0.0f, 612.0f, 792.0f))
        }
    }

    @Test
    fun getPageBleedBox() {
        pdfDocument.openPage(0).use { page ->
            val bleedBox = page.getPageBleedBox()

            assertThat(bleedBox).isEqualTo(noResultRect)
        }
    }

    @Test
    fun getPageTrimBox() {
        pdfDocument.openPage(0).use { page ->
            val trimBox = page.getPageTrimBox()

            assertThat(trimBox).isEqualTo(noResultRect)
        }
    }

    @Test
    fun getPageArtBox() {
        pdfDocument.openPage(0).use { page ->
            val artBox = page.getPageArtBox()

            assertThat(artBox).isEqualTo(noResultRect)
        }
    }

    @Test
    fun getPageBoundingBox() {
        pdfDocument.openPage(0).use { page ->
            val artBox = page.getPageBoundingBox()

            // Note, that looks incorrect, but pdfs coordinate systems starts from bottom left corner
            assertThat(artBox).isEqualTo(RectF(0f, 792f, 612f, 0f))
        }
    }

    @Test
    fun getPageSize() {
        pdfDocument.openPage(0).use { page ->
            val size = page.getPageSize(72)

            assertThat(size).isEqualTo(Size(612, 792))
        }
    }

    @Test
    fun renderPageBitmap() {
        pdfDocument.openPage(0).use { page ->

            val conf = Bitmap.Config.RGB_565 // see other conf types

            val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

            page.renderPageBitmap(bmp, 0, 0, 612, 792, true)

            // How to verify that it's correct?
            // Even if we don't verify the bitmap, we can check that it doesn't crash
        }
    }

    @Test
    fun testRenderPageBitmap() {
        pdfDocument.openPage(0).use { page ->

            val conf = Bitmap.Config.RGB_565 // see other conf types

            val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

            page.renderPageBitmap(bmp, 0, 0, 612, 792, renderAnnot = true, textMask = true)

            // How to verify that it's correct?
            // Even if we don't verify the bitmap, we can check that it doesn't crash
        }
    }

    @Test
    fun getPageLinks() {
        pdfDocument.openPage(0).use { page ->
            val links = page.getPageLinks()

            assertThat(links.size).isEqualTo(0) // The test doc doesn't have links
        }
    }

    @Test
    fun mapPageCoordsToDevice() {
        pdfDocument.openPage(0).use { page ->
            val devicePt = page.mapPageCoordsToDevice(0, 0, 100, 100, 0, 0.0, 0.0)

            assertThat(devicePt).isEqualTo(Point(0, 100))
        }
    }

    @Test
    fun mapDeviceCoordsToPage() {
        pdfDocument.openPage(0).use { page ->
            val devicePt = page.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 0, 0)

            assertThat(devicePt).isEqualTo(PointF(0f, 792.00006f))
        }
    }

    @Test
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

    @Test
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
        var pageAfterClose: PdfPage?
        pdfDocument.openPage(0).use { page ->
            pageAfterClose = page
        }
        pageAfterClose!!.getPageWidth(72)
    }
}
