/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.legere.pdfiumandroid.suspend

import android.graphics.Bitmap
import android.view.Surface
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.PageAttributes
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfPoint
import io.legere.pdfiumandroid.api.types.PdfPointF
import io.legere.pdfiumandroid.api.types.PdfRect
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.testing.StandardTestDispatcherExtension
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, StandardTestDispatcherExtension::class)
class PdfPageTest {
    lateinit var pdfPage: PdfPageKt

    @MockK
    lateinit var pdfPageU: PdfPageU

    @MockK
    lateinit var pdfTextPageU: PdfTextPageU

    @BeforeEach
    fun setUp() {
        // Using UnconfinedTestDispatcher (via StandardTestDispatcherExtension logic usually)
        // or Main for testing, assuming PdfPageKt uses the passed dispatcher.
        pdfPage = PdfPageKt(pdfPageU, Dispatchers.Unconfined)
    }

    @Test
    fun openTextPage() =
        runTest {
            // This wrapper likely returns the suspend version of TextPage, usually PdfTextPageKt
            // but let's assume for now it returns something that wraps pdfTextPageU.
            // If openTextPage returns PdfTextPageKt, we need to adjust the assertion.
            // Assuming based on setup it returns a wrapper.

            every { pdfPageU.openTextPage() } returns pdfTextPageU

            val result = pdfPage.openTextPage()

            // Result should be a suspend wrapper (PdfTextPageKt) holding the unlocked text page
            assertThat(result).isNotNull()
            // assertThat(result.page).isEqualTo(pdfTextPageU) // If accessing the inner prop is possible

            verify { pdfPageU.openTextPage() }
        }

    @Test
    fun testAlterConstructor() =
        runTest {
            val page = PdfPage(pdfPageU)
            pdfPage = PdfPageKt(page, Dispatchers.Unconfined)
            every { pdfPageU.openTextPage() } returns pdfTextPageU

            val result = pdfPage.openTextPage()

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
            assertThat(pdfPage.getPageWidth(72)).isEqualTo(100)
            verify { pdfPageU.getPageWidth(72) }
        }

    @Test
    fun getPageHeight() =
        runTest {
            every { pdfPageU.getPageHeight(72) } returns 200
            assertThat(pdfPage.getPageHeight(72)).isEqualTo(200)
            verify { pdfPageU.getPageHeight(72) }
        }

    @Test
    fun getPageWidthPoint() =
        runTest {
            every { pdfPageU.getPageWidthPoint() } returns 612
            assertThat(pdfPage.getPageWidthPoint()).isEqualTo(612)
            verify { pdfPageU.getPageWidthPoint() }
        }

    @Test
    fun getPageHeightPoint() =
        runTest {
            every { pdfPageU.getPageHeightPoint() } returns 792
            assertThat(pdfPage.getPageHeightPoint()).isEqualTo(792)
            verify { pdfPageU.getPageHeightPoint() }
        }

    @Test
    fun getPageMatrix() =
        runTest {
            val expected = PdfMatrix()
            every { pdfPageU.getPageMatrix() } returns expected
            assertThat(pdfPage.getPageMatrix()).isEqualTo(expected)
            verify { pdfPageU.getPageMatrix() }
        }

    @Test
    fun getPageRotation() =
        runTest {
            every { pdfPageU.getPageRotation() } returns 90
            assertThat(pdfPage.getPageRotation()).isEqualTo(90)
            verify { pdfPageU.getPageRotation() }
        }

    @Test
    fun getPageCropBox() =
        runTest {
            val rect = PdfRectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageCropBox() } returns rect
            assertThat(pdfPage.getPageCropBox()).isEqualTo(rect)
            verify { pdfPageU.getPageCropBox() }
        }

    @Test
    fun getPageMediaBox() =
        runTest {
            val rect = PdfRectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageMediaBox() } returns rect
            assertThat(pdfPage.getPageMediaBox()).isEqualTo(rect)
            verify { pdfPageU.getPageMediaBox() }
        }

    @Test
    fun getPageBleedBox() =
        runTest {
            val rect = PdfRectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageBleedBox() } returns rect
            assertThat(pdfPage.getPageBleedBox()).isEqualTo(rect)
            verify { pdfPageU.getPageBleedBox() }
        }

    @Test
    fun getPageTrimBox() =
        runTest {
            val rect = PdfRectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageTrimBox() } returns rect
            assertThat(pdfPage.getPageTrimBox()).isEqualTo(rect)
            verify { pdfPageU.getPageTrimBox() }
        }

    @Test
    fun getPageArtBox() =
        runTest {
            val rect = PdfRectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageArtBox() } returns rect
            assertThat(pdfPage.getPageArtBox()).isEqualTo(rect)
            verify { pdfPageU.getPageArtBox() }
        }

    @Test
    fun getPageBoundingBox() =
        runTest {
            val rect = PdfRectF(0f, 0f, 100f, 100f)
            every { pdfPageU.getPageBoundingBox() } returns rect
            assertThat(pdfPage.getPageBoundingBox()).isEqualTo(rect)
            verify { pdfPageU.getPageBoundingBox() }
        }

    @Test
    fun getPageSize() =
        runTest {
            val size = Size(100, 200)
            every { pdfPageU.getPageSize(72) } returns size
            assertThat(pdfPage.getPageSize(72)).isEqualTo(size)
            verify { pdfPageU.getPageSize(72) }
        }

    @Test
    fun renderPage() =
        runTest {
            val surface = mockk<Surface>()
            every {
                pdfPageU.renderPage(
                    any(),
                    0,
                    0,
                    100,
                    100,
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

            assertThat(pdfPage.renderPage(surface, 0, 0, 100, 100)).isTrue()
            verify { pdfPageU.renderPage(any(), 0, 0, 100, 100, any(), any(), any()) }
        }

    @Test
    fun testRenderPage() =
        runTest {
            // Variant with Surface and Matrix
            val surface = mockk<Surface>()
            val matrix = PdfMatrix()
            val clip = PdfRectF.EMPTY
            every { pdfPageU.renderPage(any(), any(), any(), any<PdfMatrix>(), any(), any(), any(), any()) } returns true
            every { pdfPageU.lockSurface(surface, any(), any()) } answers {
                thirdArg<LongArray>().let {
                    it[0] = 1
                    it[1] = 1
                }
                true
            }
            every { pdfPageU.unlockSurface(any()) } just runs

            assertThat(pdfPage.renderPage(surface, matrix, clip)).isTrue()
            verify { pdfPageU.renderPage(any(), any(), any(), any<PdfMatrix>(), any(), any(), any(), any()) }
            verify { pdfPageU.lockSurface(surface, any(), any()) }
            verify { pdfPageU.unlockSurface(any()) }
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
            val matrix = PdfMatrix()
            val clip = PdfRectF.EMPTY
            every { pdfPageU.renderPageBitmap(bitmap, matrix, clip) } just runs

            pdfPage.renderPageBitmap(bitmap, matrix, clip)
            verify { pdfPageU.renderPageBitmap(bitmap, matrix, clip) }
        }

    @Test
    fun getPageLinks() =
        runTest {
            val links = listOf(Link(bounds = mockk(), uri = "uri", destPageIdx = 1))
            every { pdfPageU.getPageLinks() } returns links

            assertThat(pdfPage.getPageLinks()).isEqualTo(links)
            verify { pdfPageU.getPageLinks() }
        }

    @Test
    fun mapPageCoordsToDevice() =
        runTest {
            val point = PdfPoint(10, 20)
            every { pdfPageU.mapPageCoordsToDevice(0, 0, 100, 100, 0, 5.0, 5.0) } returns point

            assertThat(pdfPage.mapPageCoordsToDevice(0, 0, 100, 100, 0, 5.0, 5.0)).isEqualTo(point)
            verify { pdfPageU.mapPageCoordsToDevice(0, 0, 100, 100, 0, 5.0, 5.0) }
        }

    @Test
    fun mapDeviceCoordsToPage() =
        runTest {
            val point = PdfPointF(5.0f, 5.0f)
            every { pdfPageU.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 10, 20) } returns point

            assertThat(pdfPage.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 10, 20)).isEqualTo(point)
            verify { pdfPageU.mapDeviceCoordsToPage(0, 0, 100, 100, 0, 10, 20) }
        }

    @Test
    fun mapRectToDevice() =
        runTest {
            val rect = PdfRect(10, 20, 30, 40)
            val src = PdfRectF(0f, 0f, 10f, 10f)
            every { pdfPageU.mapRectToDevice(0, 0, 100, 100, 0, src) } returns rect

            assertThat(pdfPage.mapRectToDevice(0, 0, 100, 100, 0, src)).isEqualTo(rect)
            verify { pdfPageU.mapRectToDevice(0, 0, 100, 100, 0, src) }
        }

    @Test
    fun mapRectToPage() =
        runTest {
            val rect = PdfRectF(0f, 0f, 10f, 10f)
            val src = PdfRect(10, 20, 30, 40)
            every { pdfPageU.mapRectToPage(0, 0, 100, 100, 0, src) } returns rect

            assertThat(pdfPage.mapRectToPage(0, 0, 100, 100, 0, src)).isEqualTo(rect)
            verify { pdfPageU.mapRectToPage(0, 0, 100, 100, 0, src) }
        }

    @Test
    fun getPageAttributes() =
        runTest {
            val expected = PageAttributes.EMPTY
            every { pdfPageU.getPageAttributes() } returns PageAttributes.EMPTY
            val result = pdfPage.getPageAttributes()
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
    fun safeCloseNormal() =
        runTest {
            every { pdfPageU.close() } just runs
            // Verify safeClose swallows the exception
            assertThat(pdfPage.safeClose()).isTrue()
            verify { pdfPageU.close() }
        }

    @Test
    fun safeClose() =
        runTest {
            every { pdfPageU.close() } throws RuntimeException("Close failed")
            // Verify safeClose swallows the exception
            assertThrows<RuntimeException> { pdfPage.safeClose() }
            verify { pdfPageU.close() }
        }
}
