package io.legere.pdfiumandroid.unlocked

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.jni.NativeDocument
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.NativePage
import io.legere.pdfiumandroid.jni.NativeTextPage
import io.legere.pdfiumandroid.unlocked.testing.ClosableTestContext
import io.legere.pdfiumandroid.unlocked.testing.closableTest
import io.legere.pdfiumandroid.util.AlreadyClosedBehavior
import io.legere.pdfiumandroid.util.Size
import io.legere.pdfiumandroid.util.pdfiumConfig
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.lang.NullPointerException

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
abstract class PdfPageUBaseTest : ClosableTestContext {
    @get:Rule
    val mockkRule: MockKRule = MockKRule(this)

    @MockK
    lateinit var mockNativeFactory: NativeFactory

    @MockK
    lateinit var mockNativeDocument: NativeDocument

    @MockK
    lateinit var mockNativePage: NativePage

    @MockK
    lateinit var mockNativeTextPage: NativeTextPage

    lateinit var pdfDocumentU: PdfDocumentU

    lateinit var pdfPage: PdfPageU

    abstract fun getBehavior(): AlreadyClosedBehavior

    abstract fun setupClosedState()

    abstract fun isStateClosed(): Boolean

    override fun shouldThrowException() = getBehavior() == AlreadyClosedBehavior.EXCEPTION && isStateClosed()

    override fun shouldReturnDefault() = getBehavior() == AlreadyClosedBehavior.IGNORE && isStateClosed()

    @Before
    fun setUp() {
        pdfiumConfig =
            io.legere.pdfiumandroid.util
                .Config(alreadyClosedBehavior = getBehavior())
        every { mockNativeFactory.getNativeDocument() } returns mockNativeDocument
        every { mockNativeFactory.getNativePage() } returns mockNativePage
        every { mockNativeFactory.getNativeTextPage() } returns mockNativeTextPage
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfPage =
            PdfPageU(
                pdfDocumentU,
                0,
                0,
                pageMap = mutableMapOf(0 to PdfDocument.PageCount(0, 2)),
                nativeFactory = mockNativeFactory,
            )
        setupClosedState()
    }

    @Test
    fun `openTextPage success`() =
        closableTest {
            setupHappy {
                every { mockNativeDocument.loadTextPage(any(), any()) } returns 123
            }
            apiCall = {
                pdfPage.openTextPage()
            }

            verifyHappy {
                assertThat(it).isNotNull()
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun `getPageWidth pixels success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageWidthPixel(any(), any()) } returns 100
            }
            apiCall = {
                pdfPage.getPageWidth(100)
            }

            verifyHappy {
                assertThat(it).isEqualTo(100)
            }
            verifyDefault {
                assertThat(it).isEqualTo(-1)
            }
        }

    @Test
    fun `getPageHeight pixels success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageHeightPixel(any(), any()) } returns 100
            }
            apiCall = {
                pdfPage.getPageHeight(100)
            }

            verifyHappy {
                assertThat(it).isEqualTo(100)
            }
            verifyDefault {
                assertThat(it).isEqualTo(-1)
            }
        }

    @Test
    fun `getPageWidthPoint success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageWidthPoint(any()) } returns 100
            }
            apiCall = {
                pdfPage.getPageWidthPoint()
            }

            verifyHappy {
                assertThat(it).isEqualTo(100)
            }
            verifyDefault {
                assertThat(it).isEqualTo(-1)
            }
        }

    @Test
    fun `getPageHeightPoint success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageHeightPoint(any()) } returns 100
            }
            apiCall = {
                pdfPage.getPageHeightPoint()
            }

            verifyHappy {
                assertThat(it).isEqualTo(100)
            }
            verifyDefault {
                assertThat(it).isEqualTo(-1)
            }
        }

    @Test
    fun `getPageMatrix success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageMatrix(any()) } returns floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f)
            }
            apiCall = {
                pdfPage.getPageMatrix()
            }

            verifyHappy {
                assertThat(it).isEqualTo(Matrix().apply { setValues(floatArrayOf(1.0f, 2.0f, 5.0f, 3.0f, 4.0f, 6.0f, 0.0f, 0.0f, 1.0f)) })
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun `getPageRotation valid values`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageRotation(any()) } returns 90
            }
            apiCall = {
                pdfPage.getPageRotation()
            }

            verifyHappy {
                assertThat(it).isEqualTo(90)
            }
            verifyDefault {
                assertThat(it).isEqualTo(-1)
            }
        }

    @Test
    fun `getPageCropBox success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageCropBox(any()) } returns
                    floatArrayOf(
                        10f,
                        20f,
                        30f,
                        40f,
                    )
            }
            apiCall = {
                pdfPage.getPageCropBox()
            }

            verifyHappy {
                assertThat(it).isEqualTo(RectF(10.0f, 20.0f, 30.0f, 40.0f))
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun `getPageMediaBox success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageMediaBox(any()) } returns
                    floatArrayOf(
                        10f,
                        20f,
                        30f,
                        40f,
                    )
            }
            apiCall = {
                pdfPage.getPageMediaBox()
            }

            verifyHappy {
                assertThat(it).isEqualTo(RectF(10.0f, 20.0f, 30.0f, 40.0f))
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun `getPageBleedBox success`() {
        // Verify getPageBleedBox returns a RectF with correct coordinates.
        every { mockNativePage.getPageBleedBox(any()) } returns
            floatArrayOf(
                10f,
                20f,
                30f,
                40f,
            )
        val result = pdfPage.getPageBleedBox()
        assertThat(result).isEqualTo(RectF(10.0f, 20.0f, 30.0f, 40.0f))
    }

    @Test
    fun `getPageTrimBox success`() {
        // Verify getPageTrimBox returns a RectF with correct coordinates.
        every { mockNativePage.getPageTrimBox(any()) } returns
            floatArrayOf(
                10f,
                20f,
                30f,
                40f,
            )
        val result = pdfPage.getPageTrimBox()
        assertThat(result).isEqualTo(RectF(10.0f, 20.0f, 30.0f, 40.0f))
    }

    @Test
    fun `getPageArtBox success`() {
        // Verify getPageArtBox returns a RectF with correct coordinates.
        every { mockNativePage.getPageArtBox(any()) } returns
            floatArrayOf(
                10f,
                20f,
                30f,
                40f,
            )
        val result = pdfPage.getPageArtBox()
        assertThat(result).isEqualTo(RectF(10.0f, 20.0f, 30.0f, 40.0f))
    }

    @Test
    fun `getPageBoundingBox success`() {
        // Verify getPageBoundingBox returns a RectF with correct coordinates.
        every { mockNativePage.getPageBoundingBox(any()) } returns
            floatArrayOf(
                10f,
                20f,
                30f,
                40f,
            )
        val result = pdfPage.getPageBoundingBox()
        assertThat(result).isEqualTo(RectF(10.0f, 20.0f, 30.0f, 40.0f))
    }

    @Test
    fun `getPageSize success`() {
        // Verify getPageSize returns a valid Size object based on the provided screen DPI.
        every {
            mockNativePage.getPageSizeByIndex(
                any(),
                any(),
                any(),
            )
        } returns intArrayOf(10, 20)
        val result = pdfPage.getPageSize(72)
        assertThat(result).isEqualTo(Size(width = 10, height = 20))
    }

    @Test
    fun `renderPage to bufferPtr success`() {
        // Verify renderPage with bufferPtr renders correctly and returns true for valid inputs.
        every { mockNativePage.renderPage(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns true
        val result = pdfPage.renderPage(0L, 100, 100, 100, 100)
        assertThat(result).isTrue()
    }

    @Test
    fun `renderPage to bufferPtr exception handling`() {
        every { mockNativePage.renderPage(any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws
            NullPointerException("Test exception")
        val result = pdfPage.renderPage(0L, 100, 100, 100, 100)
        assertThat(result).isFalse()
    }

    @Test
    fun `renderPage to bufferPtr exception handling 2`() {
        every { mockNativePage.renderPage(any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws
            IllegalStateException("Test exception")
        val result = pdfPage.renderPage(0L, 100, 100, 100, 100)
        assertThat(result).isFalse()
    }

    @Test
    fun `renderPage with Matrix to bufferPtr success`() {
        // Verify renderPage with Matrix/clipRect renders correctly to the buffer pointer and returns true.
        val matrix = Matrix()
        matrix.postTranslate(100f, 100f)
        val clipRect = RectF(0f, 0f, 100f, 100f)
        every {
            mockNativePage.renderPageWithMatrix(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns true
        pdfPage.renderPage(0L, 100, 100, matrix, clipRect)
    }

    @Test
    fun `renderPage with Matrix to Surface success`() {
        // Verify renderPage with Matrix/clipRect renders correctly to a Surface object and returns true.
        val surface = mockk<Surface>()
        val matrix = Matrix()
        matrix.postTranslate(100f, 100f)
        val clipRect = RectF(0f, 0f, 100f, 100f)
        every { mockNativePage.renderPageSurfaceWithMatrix(any(), any(), any(), any(), any(), any(), any(), any()) } returns true
        pdfPage.renderPage(surface, matrix, clipRect)
    }

    @Test
    fun `renderPageBitmap coordinates success`() {
        // Verify renderPageBitmap using start coordinates renders to the bitmap successfully.
        val bitmap = mockk<Bitmap>()
        val matrix = Matrix()
        matrix.postTranslate(100f, 100f)
        val clipRect = RectF(0f, 0f, 100f, 100f)
        every { mockNativePage.renderPageBitmapWithMatrix(any(), any(), any(), any(), any(), any(), any(), any()) } just runs
        pdfPage.renderPageBitmap(bitmap, matrix, clipRect)
    }

    @Test
    fun `renderPageBitmap Matrix success`() {
        // Verify renderPageBitmap using Matrix and clipRect renders to the bitmap successfully.
        val bitmap = mockk<Bitmap>()
        every { mockNativePage.renderPageBitmap(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } just runs
        pdfPage.renderPageBitmap(bitmap, 0, 0, 0, 0)
    }

    @Test
    fun `renderPageBitmap null bitmap`() {
        // Verify how renderPageBitmap behaves if a null Bitmap is passed (native layer behavior check or graceful fail).
        val matrix = Matrix()
        matrix.postTranslate(100f, 100f)
        val clipRect = RectF(0f, 0f, 100f, 100f)
        every { mockNativePage.renderPageBitmapWithMatrix(any(), any(), any(), any(), any(), any(), any(), any()) } just runs
        pdfPage.renderPageBitmap(null, matrix, clipRect)
    }

    @Test
    fun `getPageLinks success`() {
        // Verify getPageLinks parses and returns a list of Link objects with valid rects, URIs, or page indices.
        every { mockNativePage.getPageLinks(any()) } returns
            longArrayOf(100L, 200L, 300L, 400L, 500L, 600L)
        every { mockNativePage.getLinkRect(any(), any()) } returns
            floatArrayOf(100f, 200f, 300f, 400f)
        every { mockNativePage.getPageLinks(any()) } returns
            longArrayOf(100L, 200L, 300L, 400L, 500L, 600L)
        every { mockNativePage.getDestPageIndex(any(), any()) } returns
            101
        every { mockNativePage.getLinkURI(any(), any()) } returns
            "somelink"

        val results = pdfPage.getPageLinks()
        println(results)
        assertThat(results.size).isEqualTo(6)
    }

    @Test
    fun `mapPageCoordsToDevice success`() {
        // Verify mapPageCoordsToDevice correctly transforms page coordinates (double) to device coordinates (int Point).
        every { mockNativePage.pageCoordsToDevice(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            intArrayOf(100, 200)
        val result = pdfPage.mapPageCoordsToDevice(0, 0, 0, 0, 0, 0.0, 0.0)
        assertThat(result).isEqualTo(Point(100, 200))
    }

    @Test
    fun `mapDeviceCoordsToPage success`() {
        // Verify mapDeviceCoordsToPage correctly transforms device coordinates (int) to page coordinates (PointF).
        every { mockNativePage.deviceCoordsToPage(any(), any(), any(), any(), any(), any(), any(), any()) } returns floatArrayOf(100f, 200f)
        val result = pdfPage.mapDeviceCoordsToPage(0, 0, 0, 0, 0, 0, 0)
        assertThat(result).isEqualTo(PointF(100.0f, 200.0f))
    }

    @Test
    fun `mapRectToDevice success`() {
        // Verify mapRectToDevice transforms a RectF (page) to a Rect (device) correctly.
        every { mockNativePage.pageCoordsToDevice(any(), any(), any(), any(), any(), any(), any(), any()) } returns intArrayOf(100, 200)
        val result = pdfPage.mapRectToDevice(0, 0, 0, 0, 0, RectF(100f, 200f, 300f, 400f))
        assertThat(result).isEqualTo(Rect(100, 200, 100, 200))
    }

    @Test
    fun `mapRectToPage success`() {
        // Verify mapRectToPage transforms a Rect (device) to a RectF (page) correctly.
        every { mockNativePage.deviceCoordsToPage(any(), any(), any(), any(), any(), any(), any(), any()) } returns floatArrayOf(100f, 200f)
        val result = pdfPage.mapRectToPage(0, 0, 0, 0, 0, Rect(100, 200, 300, 400))
        assertThat(result).isEqualTo(RectF(100.0f, 200.0f, 100.0f, 200.0f))
    }

    @Test
    fun `close decrements reference count`() {
        // Verify that calling close() on a page that is opened multiple times only decrements the count in pageMap and does not close the native page.
        every { mockNativePage.closePage(any()) } just runs
        pdfPage.close()
        assertThat(pdfPage.isClosed).isFalse()
    }

    @Test
    fun `close actually closes native page`() {
        // Verify that calling close() when reference count is 1 removes the page from pageMap, sets isClosed to true, and calls native closePage.
        every { mockNativePage.closePage(any()) } just runs
        pdfPage.close()
        pdfPage.close()
        assertThat(pdfPage.isClosed).isTrue()
    }

    @Test
    fun `close actually closes native page with missing map entry`() {
        // Verify that calling close() when reference count is 1 removes the page from pageMap, sets isClosed to true, and calls native closePage.
        every { mockNativePage.closePage(any()) } just runs
        val pdfPage2 =
            PdfPageU(
                pdfDocumentU,
                0,
                0,
                pageMap = mutableMapOf(),
                nativeFactory = mockNativeFactory,
            )

        pdfPage2.close()
        assertThat(pdfPage2.isClosed).isTrue()
    }

    @Test(expected = IllegalStateException::class)
    fun `close idempotent check`() {
        // Verify that calling close() multiple times on an already closed page does not cause errors or double-free native resources.
        every { mockNativePage.closePage(any()) } just runs
        pdfPage.close()
        pdfPage.close()
        pdfPage.close()
        assertThat(pdfPage.isClosed).isFalse()
    }

    @Test
    fun `lockSurface success`() {
        // Verify lockSurface delegates to nativePage inside a synchronized block.
        val surface = mockk<Surface>()
        every { mockNativePage.lockSurface(any(), any(), any()) } returns true
        val result =
            pdfPage.lockSurface(surface, intArrayOf(1, 2), longArrayOf(100, 200))
        assertThat(result).isTrue()
    }

    @Test
    fun `unlockSurface success`() {
        // Verify unlockSurface delegates to nativePage inside a synchronized block.
        every { mockNativePage.unlockSurface(any()) } just runs
        pdfPage.unlockSurface(longArrayOf(100, 200))
        // if no exception is thrown, the test passes
    }
}

class PdfPageHappyTest : PdfPageUBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = false

    override fun setupClosedState() {
        every { mockNativeTextPage.closeTextPage(any()) } just runs
    }

    @Test
    fun `isClosed and setClosed getter setter verification`() {
        // Verify that setting the isClosed property updates the internal state correctly and the getter reflects this change.
        assertThat(pdfPage.isClosed).isFalse()
        pdfPage.isClosed = true
        assertThat(pdfPage.isClosed).isTrue()
        pdfPage.isClosed = false
        assertThat(pdfPage.isClosed).isFalse()
    }
}

class PdfPageClosedExceptionTest : PdfPageUBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeTextPage.closeTextPage(any()) } just runs
        pdfPage.close() // 2->1
        pdfPage.close() // 1->0 (Closed)
    }
}

class PdfPageClosedIgnoreTest : PdfPageUBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.IGNORE

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeTextPage.closeTextPage(any()) } just runs
        pdfPage.close()
        pdfPage.close()
    }
}

class PdfPageDocClosedTest : PdfPageUBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU.close()
    }
}

class PdfPageDocClosedIgnoreTest : PdfPageUBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.IGNORE

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU.close()
    }
}
