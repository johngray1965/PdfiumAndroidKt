package io.legere.pdfiumandroid

import android.graphics.RectF
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.util.Size
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PdfPageTest {

    private lateinit var pdfDocument: PdfDocument
    private var pdfBytes: ByteArray? = null

    private val noResultRect = RectF(-1f, -1f, -1f, -1f)

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
    fun getFontSize() {
        pdfDocument.openPage(0).use { page ->
            val fontSize = page.getFontSize(0)

            assertThat(fontSize).isEqualTo(612)
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
        assert(false) { "not implemented yet" }
    }

    @Test
    fun testRenderPage() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun textPageGetFontSize() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun renderPageBitmap() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun testRenderPageBitmap() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun getPageLinks() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun mapPageCoordsToDevice() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun mapDeviceCoordsToPage() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun mapRectToDevice() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun mapRectToPage() {
        assert(false) { "not implemented yet" }
    }

    @Test(expected = IllegalStateException::class)
    fun close() {
        var pageAfterClose: PdfPage? = null
        pdfDocument.openPage(0).use { page ->
            pageAfterClose = page
        }
        pageAfterClose!!.getPageWidth(72)
    }


    private fun getPdfBytes(filename: String) : ByteArray? {
        val appContext = InstrumentationRegistry.getInstrumentation().context
        val assetManager = appContext.assets
        try {
            val input = assetManager.open(filename)
            return input.readBytes()
        } catch (e: Exception) {
            Log.e(PdfiumCoreTest::class.simpleName, "Ugh",  e)
        }
        assetManager.close()
        return null
    }

}
