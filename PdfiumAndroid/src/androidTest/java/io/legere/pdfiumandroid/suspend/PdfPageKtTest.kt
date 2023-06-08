package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.util.Size
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PdfPageKtTest : BasePDFTest() {

    private lateinit var pdfDocument: PdfDocumentKt
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() = runBlocking {
        pdfBytes = getPdfBytes("f01.pdf")

        TestCase.assertNotNull(pdfBytes)

        pdfDocument = PdfiumCoreKt(Dispatchers.Unconfined).newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun getPageWidth() = runTest {
        pdfDocument.openPage(0).use { page ->
            val pageWidth = page.getPageWidth(72)

            assertThat(pageWidth).isEqualTo(612) // 8.5 inches * 72 dpi
        }
    }

    @Test
    fun getPageHeight() = runTest {
        pdfDocument.openPage(0).use { page ->
            val pageWidth = page.getPageHeight(72)

            assertThat(pageWidth).isEqualTo(792) // 11 inches * 72 dpi
        }
    }

    @Test
    fun getPageWidthPoint() = runTest {
        pdfDocument.openPage(0).use { page ->
            val pageWidth = page.getPageWidthPoint()

            assertThat(pageWidth).isEqualTo(612) // 11 inches * 72 dpi
        }
    }

    @Test
    fun getPageHeightPoint() = runTest {
        pdfDocument.openPage(0).use { page ->
            val pageWidth = page.getPageHeightPoint()

            assertThat(pageWidth).isEqualTo(792) // 11 inches * 72 dpi
        }
    }

    @Test
    fun getPageCropBox() = runTest {
        pdfDocument.openPage(0).use { page ->
            val cropBox = page.getPageCropBox()

            assertThat(cropBox).isEqualTo(noResultRect)
        }
    }

    @Test
    fun getPageMediaBox() = runTest {
        pdfDocument.openPage(0).use { page ->
            val mediaBox = page.getPageMediaBox()

            assertThat(mediaBox).isEqualTo(RectF(0.0f, 0.0f, 612.0f, 792.0f))
        }
    }

    @Test
    fun getPageBleedBox() = runTest {
        pdfDocument.openPage(0).use { page ->
            val bleedBox = page.getPageBleedBox()

            assertThat(bleedBox).isEqualTo(noResultRect)
        }
    }

    @Test
    fun getPageTrimBox() = runTest {
        pdfDocument.openPage(0).use { page ->
            val trimBox = page.getPageTrimBox()

            assertThat(trimBox).isEqualTo(noResultRect)
        }
    }

    @Test
    fun getPageArtBox() = runTest {
        pdfDocument.openPage(0).use { page ->
            val artBox = page.getPageArtBox()

            assertThat(artBox).isEqualTo(noResultRect)
        }
    }

    @Test
    fun getPageBoundingBox() = runTest {
        pdfDocument.openPage(0).use { page ->
            val artBox = page.getPageBoundingBox()

            assertThat(artBox).isEqualTo(RectF(0f, 792f, 612f, 0f))
        }
    }

    @Test
    fun getPageSize() = runTest {
        pdfDocument.openPage(0).use { page ->
            val size = page.getPageSize(72)

            assertThat(size).isEqualTo(Size(612, 792))
        }
    }

    @Test
    fun renderPage() = runTest {
        // I really don't know how to test it
    }

    @Test
    fun testRenderPage() = runTest {
        // I really don't know how to test it
    }

    @Test
    fun renderPageBitmap() = runTest {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun testRenderPageBitmap() = runTest {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun getPageLinks() = runTest {
        pdfDocument.openPage(0).use { page ->
            val links = page.getPageLinks()

            assertThat(links.size).isEqualTo(0) // The test doc doesn't have links
        }
    }

    @Test
    fun mapPageCoordsToDevice() = runTest {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun mapDeviceCoordsToPage() = runTest {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun mapRectToDevice() = runTest {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test
    fun mapRectToPage() = runTest {
        assert(notImplementedAssetValue) { "not implemented yet" }
    }

    @Test(expected = IllegalStateException::class)
    fun close() = runTest {
        var pageAfterClose: PdfPageKt?
        pdfDocument.openPage(0).use { page ->
            pageAfterClose = page
        }
        pageAfterClose!!.getPageWidth(72)
    }

    @Test
    fun getPage() = runTest {
        pdfDocument.openPage(0).use { page ->
            assertThat(page).isNotNull()
        }
    }

}
