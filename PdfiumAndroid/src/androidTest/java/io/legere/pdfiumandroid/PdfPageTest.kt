package io.legere.pdfiumandroid

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.util.Size
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PdfPageTest :  BasePDFTest() {

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
    fun renderPage() {
        // I really don't know how to test it
    }

    @Test
    fun testRenderPage() {
        // I really don't know how to test it
    }

    @Test
    fun renderPageBitmap() {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun testRenderPageBitmap() {
        assert(notImplementedAssetValue) { "not implemented yet" }
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
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun mapDeviceCoordsToPage() {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun mapRectToDevice() {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun mapRectToPage() {
        assert(notImplementedAssetValue) { "not implemented yet" }
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
