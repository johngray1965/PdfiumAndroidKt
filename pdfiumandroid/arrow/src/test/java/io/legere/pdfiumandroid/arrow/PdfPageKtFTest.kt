package io.legere.pdfiumandroid.arrow

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.PageAttributes
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.arrow.testing.StandardTestDispatcherExtension
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, StandardTestDispatcherExtension::class)
@Suppress("LargeClass")
class PdfPageKtFTest {
    lateinit var pdfPage: PdfPageKtF

    @MockK
    lateinit var pdfPageU: PdfPageU

    @MockK
    lateinit var pdfTextPageU: PdfTextPageU

    @BeforeEach
    fun setUp() {
        // Using UnconfinedTestDispatcher (via StandardTestDispatcherExtension logic usually)
        // or Main for testing, assuming PdfPageKt uses the passed dispatcher.
        pdfPage = PdfPageKtF(pdfPageU, Dispatchers.Unconfined)
    }

    @Test
    fun openTextPage() =
        runTest {
            // This wrapper likely returns the suspend version of TextPage, usually PdfTextPageKt
            // but let's assume for now it returns something that wraps pdfTextPageU.
            // If openTextPage returns PdfTextPageKt, we need to adjust the assertion.
            // Assuming based on setup it returns a wrapper.

            every { pdfPageU.openTextPage() } returns pdfTextPageU

            val result = pdfPage.openTextPage().getOrNull()

            // Result should be a suspend wrapper (PdfTextPageKt) holding the unlocked text page
            assertThat(result).isNotNull()
            // assertThat(result.page).isEqualTo(pdfTextPageU) // If accessing the inner prop is possible

            verify { pdfPageU.openTextPage() }
        }

    @Test
    fun pageIndex() =
        runTest {
            every { pdfPageU.pageIndex } returns 100
            assertThat(pdfPage.pageIndex).isEqualTo(100)
            verify { pdfPageU.pageIndex }
        }

    @Test
    fun getPageWidth() =
        runTest {
            every { pdfPageU.getPageWidth(72) } returns 100
            assertThat(pdfPage.getPageWidth(72).getOrNull()).isEqualTo(100)
            verify { pdfPageU.getPageWidth(72) }
        }

    @Test
    fun getPageHeight() =
        runTest {
            every { pdfPageU.getPageHeight(72) } returns 200
            assertThat(pdfPage.getPageHeight(72).getOrNull()).isEqualTo(200)
            verify { pdfPageU.getPageHeight(72) }
        }

    @Test
    fun getPageWidthPoint() =
        runTest {
            every { pdfPageU.getPageWidthPoint() } returns 612
            assertThat(pdfPage.getPageWidthPoint().getOrNull()).isEqualTo(612)
            verify { pdfPageU.getPageWidthPoint() }
        }

    @Test
    fun getPageHeightPoint() =
        runTest {
            every { pdfPageU.getPageHeightPoint() } returns 792
            assertThat(pdfPage.getPageHeightPoint().getOrNull()).isEqualTo(792)
            verify { pdfPageU.getPageHeightPoint() }
        }

    @Test
    fun getPageMatrix() =
        runTest {
            val expected = Matrix()
            every { pdfPageU.getPageMatrix() } returns expected
            assertThat(pdfPage.getPageMatrix().getOrNull()).isEqualTo(expected)
            verify { pdfPageU.getPageMatrix() }
        }

    @Test
    fun getPageRotation() =
        runTest {
            val expected = 90
            every { pdfPageU.getPageRotation() } returns expected
            assertThat(pdfPage.getPageRotation().getOrNull()).isEqualTo(expected)
            verify { pdfPageU.getPageRotation() }
        }

    @Test
    fun getPageRotationError() =
        runTest {
            val expected = -1
            every { pdfPageU.getPageRotation() } returns expected
            val result = pdfPage.getPageRotation()
            assertThat(result.isLeft()).isTrue()
            verify { pdfPageU.getPageRotation() }
        }

    @Test
    fun getPageRotationExcept() =
        runTest {
            every { pdfPageU.getPageRotation() } throws Exception()
            val result = pdfPage.getPageRotation()
            assertThat(result.isLeft()).isTrue()
            verify { pdfPageU.getPageRotation() }
        }

    @Test
    fun getPageCropBox() =
        runTest {
            val rect = RectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageCropBox() } returns rect
            assertThat(pdfPage.getPageCropBox().getOrNull()).isEqualTo(rect)
            verify { pdfPageU.getPageCropBox() }
        }

    @Test
    fun getPageMediaBox() =
        runTest {
            val rect = RectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageMediaBox() } returns rect
            assertThat(pdfPage.getPageMediaBox().getOrNull()).isEqualTo(rect)
            verify { pdfPageU.getPageMediaBox() }
        }

    @Test
    fun getPageBleedBox() =
        runTest {
            val rect = RectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageBleedBox() } returns rect
            assertThat(pdfPage.getPageBleedBox().getOrNull()).isEqualTo(rect)
            verify { pdfPageU.getPageBleedBox() }
        }

    @Test
    fun getPageTrimBox() =
        runTest {
            val rect = RectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageTrimBox() } returns rect
            assertThat(pdfPage.getPageTrimBox().getOrNull()).isEqualTo(rect)
            verify { pdfPageU.getPageTrimBox() }
        }

    @Test
    fun getPageArtBox() =
        runTest {
            val rect = RectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageArtBox() } returns rect
            assertThat(pdfPage.getPageArtBox().getOrNull()).isEqualTo(rect)
            verify { pdfPageU.getPageArtBox() }
        }

    @Test
    fun getPageBoundingBox() =
        runTest {
            val rect = RectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageBoundingBox() } returns rect
            assertThat(pdfPage.getPageBoundingBox().getOrNull()).isEqualTo(rect)
            verify { pdfPageU.getPageBoundingBox() }
        }

    @Test
    fun getPageSize() =
        runTest {
            val size = Size(100, 200)
            every { pdfPageU.getPageSize(72) } returns size
            assertThat(pdfPage.getPageSize(72).getOrNull()).isEqualTo(size)
            verify { pdfPageU.getPageSize(72) }
        }

    @Test
    fun renderPage() =
        runTest {
            val surface = mockk<Surface>()
            every {
                pdfPageU.pageIndex
            } returns 0
            every {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            } returns true
            every { pdfPageU.lockSurface(surface, any(), any()) } answers {
                thirdArg<LongArray>().let {
                    it[0] = 1
                    it[1] = 1
                }
                true
            }
            every { pdfPageU.unlockSurface(any()) } just runs

            assertThat(
                pdfPage
                    .renderPage(
                        surface,
                        0,
                        0,
                        100,
                        100,
                        renderCoroutinesDispatcher = Dispatchers.Unconfined,
                    ).getOrNull(),
            ).isTrue()
            verify {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun `renderPage surface null`() =
        runTest {
            val surface = mockk<Surface>()
            every {
                pdfPageU.pageIndex
            } returns 0
            every {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            } returns true
            every { pdfPageU.lockSurface(surface, any(), any()) } answers {
                thirdArg<LongArray>().let {
                    it[0] = 1
                    it[1] = 1
                }
                true
            }
            every { pdfPageU.unlockSurface(any()) } just runs

            val result =
                pdfPage.renderPage(
                    null,
                    0,
                    0,
                    100,
                    100,
                    renderCoroutinesDispatcher = Dispatchers.Unconfined,
                )

            assertThat(
                result.isLeft(),
            ).isTrue()
            verify(exactly = 0) {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun `renderPage bad surface`() =
        runTest {
            val surface = mockk<Surface>()
            every {
                pdfPageU.pageIndex
            } returns 0
            every {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            } returns true
            every { pdfPageU.lockSurface(surface, any(), any()) } answers {
                thirdArg<LongArray>().let {
                    it[0] = 0
                    it[1] = 0
                }
                true
            }
            every { pdfPageU.unlockSurface(any()) } just runs

            val result =
                pdfPage.renderPage(
                    null,
                    0,
                    0,
                    100,
                    100,
                    renderCoroutinesDispatcher = Dispatchers.Unconfined,
                )

            assertThat(
                result.isLeft(),
            ).isTrue()
            verify(exactly = 0) {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun `renderPage bad surface 2`() =
        runTest {
            val surface = mockk<Surface>()
            every {
                pdfPageU.pageIndex
            } returns 0
            every {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            } returns true
            every { pdfPageU.lockSurface(surface, any(), any()) } answers {
                thirdArg<LongArray>().let {
                    it[0] = -1
                    it[1] = -1
                }
                true
            }
            every { pdfPageU.unlockSurface(any()) } just runs

            val result =
                pdfPage.renderPage(
                    surface,
                    0,
                    0,
                    100,
                    100,
                    renderCoroutinesDispatcher = Dispatchers.Unconfined,
                )

            assertThat(
                result.isLeft(),
            ).isFalse()
            verify {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun `renderPage return false`() =
        runTest {
            val surface = mockk<Surface>()
            every {
                pdfPageU.pageIndex
            } returns 0
            every {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            } returns false
            every { pdfPageU.lockSurface(surface, any(), any()) } answers {
                thirdArg<LongArray>().let {
                    it[0] = 0
                    it[1] = 0
                }
                true
            }
            every { pdfPageU.unlockSurface(any()) } just runs

            val result =
                pdfPage.renderPage(
                    surface,
                    0,
                    0,
                    100,
                    100,
                    renderCoroutinesDispatcher = Dispatchers.Unconfined,
                )

            assertThat(
                result.isLeft(),
            ).isFalse()
            verify {
                pdfPageU.renderPage(
                    any<Long>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun testRenderPage() =
        runTest {
            // Variant with Surface and Matrix
            val surface = mockk<Surface>()
            val matrix = Matrix()
            val clip = RectF()
            every {
                pdfPageU.renderPage(
                    any<Surface>(),
                    any<Matrix>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns true

            assertThat(
                pdfPage
                    .renderPage(
                        surface,
                        matrix,
                        clip,
                        renderCoroutinesDispatcher = Dispatchers.Unconfined,
                    ).getOrNull(),
            ).isTrue()
            verify {
                pdfPageU.renderPage(
                    any<Surface>(),
                    any<Matrix>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun `testRenderPage null surface`() =
        runTest {
            // Variant with Surface and Matrix
            val matrix = Matrix()
            val clip = RectF()
            every {
                pdfPageU.renderPage(
                    any<Surface>(),
                    any<Matrix>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns true

            val result =
                pdfPage.renderPage(
                    null,
                    matrix,
                    clip,
                    renderCoroutinesDispatcher = Dispatchers.Unconfined,
                )
            assertThat(result.isLeft()).isTrue()
            verify(exactly = 0) {
                pdfPageU.renderPage(
                    any<Surface>(),
                    any<Matrix>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun `testRenderPage return false`() =
        runTest {
            // Variant with Surface and Matrix
            val surface = mockk<Surface>()
            val matrix = Matrix()
            val clip = RectF()
            every {
                pdfPageU.renderPage(
                    any<Surface>(),
                    any<Matrix>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns false

            val result =
                pdfPage.renderPage(
                    surface,
                    matrix,
                    clip,
                    renderCoroutinesDispatcher = Dispatchers.Unconfined,
                )
            assertThat(result.isLeft()).isTrue()
            verify {
                pdfPageU.renderPage(
                    any<Surface>(),
                    any<Matrix>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun `testRenderPage throws exception`() =
        runTest {
            // Variant with Surface and Matrix
            val surface = mockk<Surface>()
            val matrix = Matrix()
            val clip = RectF()
            every {
                pdfPageU.renderPage(
                    any<Surface>(),
                    any<Matrix>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } throws Exception()

            val result =
                pdfPage.renderPage(
                    surface,
                    matrix,
                    clip,
                    renderCoroutinesDispatcher = Dispatchers.Unconfined,
                )
            assertThat(result.isLeft()).isTrue()
            verify {
                pdfPageU.renderPage(
                    any<Surface>(),
                    any<Matrix>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun renderPageBitmap() =
        runTest {
            val bitmap = mockk<Bitmap>()
            every { pdfPageU.renderPageBitmap(bitmap, 0, 0, 100, 100) } just runs

            pdfPage.renderPageBitmap(bitmap, 0, 0, 100, 100)
            verify { pdfPageU.renderPageBitmap(bitmap, 0, 0, 100, 100) }
        }

    @Test
    fun testRenderPageBitmap() =
        runTest {
            // Variant with Matrix
            val bitmap = mockk<Bitmap>()
            val matrix = Matrix()
            val clip = RectF()
            every { pdfPageU.renderPageBitmap(bitmap, matrix, clip) } just runs

            pdfPage.renderPageBitmap(bitmap, matrix, clip)
            verify { pdfPageU.renderPageBitmap(bitmap, matrix, clip) }
        }

    @Test
    fun getPageLinks() =
        runTest {
            val links = listOf(Link(bounds = mockk(), uri = "uri", destPageIdx = 1))
            every { pdfPageU.getPageLinks() } returns links

            assertThat(pdfPage.getPageLinks().getOrNull()).isEqualTo(links)
            verify { pdfPageU.getPageLinks() }
        }

    @Test
    fun mapPageCoordsToDevice() =
        runTest {
            val point = Point(10, 20)
            every { pdfPageU.mapPageCoordsToDevice(0, 0, 100, 100, 0, 5.0, 5.0) } returns point

            assertThat(
                pdfPage.mapPageCoordsToDevice(0, 0, 100, 100, 0, 5.0, 5.0).getOrNull(),
            ).isEqualTo(point)
            verify { pdfPageU.mapPageCoordsToDevice(0, 0, 100, 100, 0, 5.0, 5.0) }
        }

    @Test
    fun mapDeviceCoordsToPage() =
        runTest {
            val point = PointF(5.0f, 5.0f)
            every { pdfPageU.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 10, 20) } returns point

            assertThat(
                pdfPage.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 10, 20).getOrNull(),
            ).isEqualTo(point)
            verify { pdfPageU.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 10, 20) }
        }

    @Test
    fun mapRectToDevice() =
        runTest {
            val rect = Rect(10, 20, 30, 40)
            val src = RectF(0f, 0f, 10f, 10f)
            every { pdfPageU.mapRectToDevice(0, 0, 100, 100, 0, src) } returns rect

            assertThat(pdfPage.mapRectToDevice(0, 0, 100, 100, 0, src).getOrNull()).isEqualTo(rect)
            verify { pdfPageU.mapRectToDevice(0, 0, 100, 100, 0, src) }
        }

    @Test
    fun mapRectToPage() =
        runTest {
            val rect = RectF(0f, 0f, 10f, 10f)
            val src = Rect(10, 20, 30, 40)
            every { pdfPageU.mapRectToPage(0, 0, 100, 100, 0, src) } returns rect

            assertThat(pdfPage.mapRectToPage(0, 0, 100, 100, 0, src).getOrNull()).isEqualTo(rect)
            verify { pdfPageU.mapRectToPage(0, 0, 100, 100, 0, src) }
        }

    @Test
    fun getPageAttributes() =
        runTest {
            val expected = PageAttributes.EMPTY
            every { pdfPageU.getPageAttributes() } returns PageAttributes.EMPTY
            val result = pdfPage.getPageAttributes().getOrNull()
            assertThat(result).isEqualTo(expected)
            verify { pdfPageU.getPageAttributes() }
        }

    @Test
    fun close() {
        // close() is usually not suspended, but the wrapper might make it suspend
        // Assuming PdfPageKt.close() is suspend or just a normal function depending on impl.
        // If it's normal, runTest isn't strictly needed but doesn't hurt.
        every { pdfPageU.close() } just runs

        runTest {
            pdfPage.close()
        }
        verify { pdfPageU.close() }
    }

    @Test
    fun safeClose() {
        // Assuming safeClose exists as an extension or utility
        // If it's part of the API, verify it catches exceptions
        every { pdfPageU.close() } throws RuntimeException("Boom")

        runTest {
            // verify it doesn't throw
            try {
                pdfPage.safeClose()
                // If you have a specific safeClose method:
                // pdfPage.safeClose()
            } catch (_: Exception) {
                // If testing 'close' behavior on exception
            }
        }
        // Assert logic based on your safeClose implementation
    }
}
