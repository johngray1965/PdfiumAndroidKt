package io.legere.pdfiumandroid.arrow

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.raise.either
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.util.Size
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfPageKtFTest : BasePDFTest() {

    private lateinit var pdfDocument: PdfDocumentKtF
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() = runBlocking {
        pdfBytes = getPdfBytes("f01.pdf")

        TestCase.assertNotNull(pdfBytes)

        pdfDocument = PdfiumCoreKtF(Dispatchers.Unconfined).newDocument(pdfBytes).getOrNull()!!
    }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun getPageWidth() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val pageWidth = page.getPageWidth(72).bind()

                assertThat(pageWidth).isEqualTo(612) // 8.5 inches * 72 dpi
            }
        }
    }

    @Test
    fun getPageHeight() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val pageWidth = page.getPageHeight(72).bind()

                assertThat(pageWidth).isEqualTo(792) // 11 inches * 72 dpi
            }
        }
    }

    @Test
    fun getPageWidthPoint() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val pageWidth = page.getPageWidthPoint().bind()

                assertThat(pageWidth).isEqualTo(612) // 11 inches * 72 dpi
            }
        }
    }

    @Test
    fun getPageHeightPoint() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val pageWidth = page.getPageHeightPoint().bind()

                assertThat(pageWidth).isEqualTo(792) // 11 inches * 72 dpi
            }
        }
    }

    @Test
    fun getPageCropBox() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val cropBox = page.getPageCropBox().bind()

                assertThat(cropBox).isEqualTo(noResultRect)
            }
        }
    }

    @Test
    fun getPageMediaBox() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val mediaBox = page.getPageMediaBox().bind()

                assertThat(mediaBox).isEqualTo(RectF(0.0f, 0.0f, 612.0f, 792.0f))
            }
        }
    }

    @Test
    fun getPageBleedBox() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val bleedBox = page.getPageBleedBox().bind()

                assertThat(bleedBox).isEqualTo(noResultRect)
            }
        }
    }

    @Test
    fun getPageTrimBox() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val trimBox = page.getPageTrimBox().bind()

                assertThat(trimBox).isEqualTo(noResultRect)
            }
        }
    }

    @Test
    fun getPageArtBox() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val artBox = page.getPageArtBox().bind()

                assertThat(artBox).isEqualTo(noResultRect)
            }
        }
    }

    @Test
    fun getPageBoundingBox() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val artBox = page.getPageBoundingBox().bind()

                assertThat(artBox).isEqualTo(RectF(0f, 792f, 612f, 0f))
            }
        }
    }

    @Test
    fun getPageSize() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val size = page.getPageSize(72).bind()

                assertThat(size).isEqualTo(Size(612, 792))
            }
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
        either {
            pdfDocument.openPage(0).bind().use { page ->

                val conf = Bitmap.Config.RGB_565 // see other conf types

                val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

                page.renderPageBitmap(bmp, 0, 0, 612, 792)

                // How to verify that it's correct?
                // Even if we don't verify the bitmap, we can check that it doesn't crash
            }
        }
    }

    @Test
    fun testRenderPageBitmap() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->

                val conf = Bitmap.Config.RGB_565 // see other conf types

                val bmp = Bitmap.createBitmap(612, 792, conf) // this creates a MUTABLE bitmap

                page.renderPageBitmap(bmp, 0, 0, 612, 792, renderAnnot = true, textMask = true)

                // How to verify that it's correct?
                // Even if we don't verify the bitmap, we can check that it doesn't crash
            }
        }
    }

    @Test
    fun getPageLinks() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val links = page.getPageLinks().bind()

                assertThat(links.size).isEqualTo(0) // The test doc doesn't have links
            }
        }
    }

    @Test
    fun mapPageCoordsToDevice() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val devicePt = page.mapPageCoordsToDevice(0, 0, 100, 100, 0, 0.0, 0.0).bind()

                assertThat(devicePt).isEqualTo(Point(0, 100))
            }
        }
    }

    @Test
    fun mapDeviceCoordsToPage() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val devicePt = page.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 0, 0).bind()

                assertThat(devicePt).isEqualTo(PointF(0f, 792.00006f))
            }
        }
    }

    @Test
    fun mapRectToDevice() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val devicePt = page.mapRectToDevice(0, 0, 100, 100, 0, RectF(0f, 0f, 100f, 100f)).bind()

                assertThat(devicePt).isEqualTo(
                    Rect(
                        0, // 0f in coords to 0f in device
                        100, // 0f in corrds in at the bottom, the bottom of the device is 100f
                        16, // 100f in coords = 100f/(8.5*72) * 100f = 16f
                        87
                    ) // 100f in coords = 100 - 100f/(11*72) * 100f = 87f
                )
            }
        }
    }

    @Test
    fun mapRectToPage() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                val devicePt = page.mapRectToPage(0, 0, 100, 100, 0, Rect(0, 0, 100, 100)).bind()

                assertThat(devicePt).isEqualTo(
                    RectF(0.0f, 792.00006f, 612.0f, 0.0f)
                )
            }
        }
    }

    fun close() = runTest {
        either {
            var pageAfterClose: PdfPageKtF?
            pdfDocument.openPage(0).bind().use { page ->
                pageAfterClose = page
            }
            pageAfterClose!!.getPageWidth(72)
        }.mapLeft {
            assertThat(it).isInstanceOf(PdfiumKtFErrors.AlreadyClosed::class.java)
        }
    }

    @Test
    fun getPage() = runTest {
        either {
            pdfDocument.openPage(0).bind().use { page ->
                assertThat(page).isNotNull()
            }
        }
    }
}
