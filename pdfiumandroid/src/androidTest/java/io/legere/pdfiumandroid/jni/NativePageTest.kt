package io.legere.pdfiumandroid.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.unlocked.PdfPageU
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
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
        pdfPage.close()
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
                0.99808586f,
                0.0f,
                0.0f,
                1.0000008f,
                89.999176f,
                699.1206f,
            )
        assertThat(matrix).isEqualTo(expectedMatrix)
    }
}
