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

package io.legere.pdfiumandroid

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.view.Surface
import com.google.common.truth.Truth.assertThat
import io.legere.geokt.FloatRectValues
import io.legere.geokt.IntRectValues
import io.legere.geokt.KtImmutableMatrix
import io.legere.geokt.KtImmutablePoint
import io.legere.geokt.KtImmutablePointF
import io.legere.geokt.KtImmutableRect
import io.legere.geokt.KtImmutableRectF
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.PageAttributes
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(Lifecycle.PER_CLASS)
@Suppress("LargeClass")
class PdfPageTest {
    val page: PdfPageU =
        mockk {
            every {
                renderPageBitmap(
                    any<Bitmap>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Int>(),
                    any<Boolean>(),
                    any<Boolean>(),
                    any<Int>(),
                    any<Int>(),
                )
            } just runs
            every {
                renderPageBitmap(
                    any<Bitmap>(),
                    any<KtImmutableMatrix>(),
                    any<KtImmutableRectF>(),
                    any<Boolean>(),
                    any<Boolean>(),
                    any<Int>(),
                    any<Int>(),
                )
            } just runs
        }

    val pdfTextPageU: PdfTextPageU = mockk()

    val pdfPage: PdfPage = PdfPage(page)

    @Test
    fun pageIndex() =
        runTest {
            every { page.pageIndex } returns 100
            assertThat(pdfPage.pageIndex).isEqualTo(100)
            verify { page.pageIndex }
        }

    @Test
    fun openTextPage() {
        every { page.openTextPage() } returns pdfTextPageU
        val result = pdfPage.openTextPage()
        assertThat(result.textPage).isEqualTo(pdfTextPageU)
        verify { page.openTextPage() }
    }

    @Test
    fun getPageWidth() {
        every { page.getPageWidth(any()) } returns 800
        val result = pdfPage.getPageWidth(100)
        assertThat(result).isEqualTo(800)
        verify { page.getPageWidth(100) }
    }

    @Test
    fun getPageHeight() {
        val expected = 1200
        every { page.getPageHeight(any()) } returns expected
        val result = pdfPage.getPageHeight(100)
        assertThat(result).isEqualTo(expected)
        verify { page.getPageHeight(100) }
    }

    @Test
    fun getPageWidthPoint() {
        val expected = 800
        every { page.getPageWidthPoint() } returns expected
        val result = pdfPage.getPageWidthPoint()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageWidthPoint() }
    }

    @Test
    fun getPageHeightPoint() {
        val expected = 1200
        every { page.getPageHeightPoint() } returns expected
        val result = pdfPage.getPageHeightPoint()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageHeightPoint() }
    }

    @Test
    fun getPageMatrix() {
        val expected = mockk<KtImmutableMatrix>()
        every { page.getPageMatrix() } returns expected
        val result = pdfPage.getPageMatrix()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageMatrix() }
    }

    @Test
    fun getPageRotation() {
        val expected = 90
        every { page.getPageRotation() } returns expected
        val result = pdfPage.getPageRotation()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageRotation() }
    }

    @Test
    fun getPageCropBox() {
        val expected = mockk<KtImmutableRectF>()
        every { page.getPageCropBox() } returns expected
        val result = pdfPage.getPageCropBox()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageCropBox() }
    }

    @Test
    fun getPageMediaBox() {
        val expected = mockk<KtImmutableRectF>()
        every { page.getPageMediaBox() } returns expected
        val result = pdfPage.getPageMediaBox()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageMediaBox() }
    }

    @Test
    fun getPageBleedBox() {
        val expected = mockk<KtImmutableRectF>()
        every { page.getPageBleedBox() } returns expected
        val result = pdfPage.getPageBleedBox()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageBleedBox() }
    }

    @Test
    fun getPageTrimBox() {
        val expected = mockk<KtImmutableRectF>()
        every { page.getPageTrimBox() } returns expected
        val result = pdfPage.getPageTrimBox()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageTrimBox() }
    }

    @Test
    fun getPageArtBox() {
        val expected = mockk<KtImmutableRectF>()
        every { page.getPageArtBox() } returns expected
        val result = pdfPage.getPageArtBox()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageArtBox() }
    }

    @Test
    fun getPageBoundingBox() {
        val expected = mockk<KtImmutableRectF>()
        every { page.getPageBoundingBox() } returns expected
        val result = pdfPage.getPageBoundingBox()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageBoundingBox() }
    }

    @Test
    fun getPageSize() {
        val expected = mockk<Size>()
        every { page.getPageSize(any()) } returns expected
        val result = pdfPage.getPageSize(100)
        assertThat(result).isEqualTo(expected)
        verify { page.getPageSize(any()) }
    }

    @Test
    fun testRenderPage() {
        val expected = true
        every {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        listOf(false, true).forEach { renderAnnot ->
            val result = pdfPage.renderPage(1L, 0, 0, 0, 0, renderAnnot)
            assertThat(result).isEqualTo(expected)
        }
        verify {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPageWithDefaults() {
        val expected = true
        every {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        pdfPage.renderPage(1L, 0, 0, 0, 0)
        verify {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPage1() {
        val expected = true
        every {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                val result =
                    pdfPage.renderPage(1L, 0, 0, mockk<KtImmutableMatrix>(), mockk<KtImmutableRectF>(), renderAnnot, textMask)
                assertThat(result).isEqualTo(expected)
            }
        }
        verify {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPage1WithAndroidTypes() {
        val expected = true
        every {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                val result =
                    pdfPage.renderPage(1L, 0, 0, mockk<Matrix>(relaxed = true), mockk<RectF>(), renderAnnot, textMask)
                assertThat(result).isEqualTo(expected)
            }
        }
        verify {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPage2() {
        val expected = true
        every {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                val result =
                    pdfPage.renderPage(
                        1L,
                        0,
                        0,
                        mockk<KtImmutableMatrix>(),
                        mockk<KtImmutableRectF>(),
                        renderAnnot,
                        textMask,
                    )
                assertThat(result).isEqualTo(expected)
            }
        }
        verify {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPage2WithDefaults() {
        val expected = true
        every {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        val result =
            pdfPage.renderPage(1L, 0, 0, mockk<KtImmutableMatrix>(), mockk<KtImmutableRectF>())
        assertThat(result).isEqualTo(expected)
        verify {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPage2WithAndroidTypes() {
        val expected = true
        every {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                val result =
                    pdfPage.renderPage(
                        1L,
                        0,
                        0,
                        mockk<Matrix>(relaxed = true),
                        mockk<RectF>(),
                        renderAnnot,
                        textMask,
                    )
                assertThat(result).isEqualTo(expected)
            }
        }
        verify {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPage2WithDefaultsWithAndroidTypes() {
        val expected = true
        every {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        val result =
            pdfPage.renderPage(1L, 0, 0, mockk<Matrix>(relaxed = true), mockk<RectF>())
        assertThat(result).isEqualTo(expected)
        verify {
            page.renderPage(
                any<Long>(),
                any<Int>(),
                any<Int>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPageToSurface() {
        val expected = true
        every {
            page.renderPage(
                any<Surface>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                val result =
                    pdfPage.renderPage(
                        mockk<Surface>(),
                        mockk<KtImmutableMatrix>(),
                        mockk<KtImmutableRectF>(),
                        renderAnnot,
                        textMask,
                    )
                assertThat(result).isEqualTo(expected)
            }
        }
        verify {
            page.renderPage(
                any<Surface>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPageToSurfaceWithDefaults() {
        val expected = true
        every {
            page.renderPage(
                any<Surface>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        val result =
            pdfPage.renderPage(
                mockk<Surface>(),
                mockk<KtImmutableMatrix>(),
                mockk<KtImmutableRectF>(),
            )
        assertThat(result).isEqualTo(expected)
        verify {
            page.renderPage(
                any<Surface>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPageToSurfaceWithAndroidTypes() {
        val expected = true
        every {
            page.renderPage(
                any<Surface>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                val result =
                    pdfPage.renderPage(
                        mockk<Surface>(),
                        mockk<Matrix>(relaxed = true),
                        mockk<RectF>(),
                        renderAnnot,
                        textMask,
                    )
                assertThat(result).isEqualTo(expected)
            }
        }
        verify {
            page.renderPage(
                any<Surface>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPageToSurfaceWithDefaultsWithAndroidTypes() {
        val expected = true
        every {
            page.renderPage(
                any<Surface>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        } returns
            expected
        // every { page.renderPage(any<Long>(), any(), any(), any(), any(), any(), any()) } returns expected
        val result =
            pdfPage.renderPage(
                mockk<Surface>(),
                mockk<Matrix>(relaxed = true),
                mockk<RectF>(),
            )
        assertThat(result).isEqualTo(expected)
        verify {
            page.renderPage(
                any<Surface>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun renderPageBitmap() {
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                pdfPage.renderPageBitmap(
                    mockk<Bitmap>(),
                    0,
                    0,
                    0,
                    0,
                    renderAnnot = renderAnnot,
                    textMask = textMask,
                )
            }
        }
        verify {
            page.renderPageBitmap(
                any<Bitmap>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun renderPageBitmapWithDefaults() {
        pdfPage.renderPageBitmap(
            mockk<Bitmap>(),
            0,
            0,
            0,
            0,
        )
        verify {
            page.renderPageBitmap(
                any<Bitmap>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPageBitmap() {
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                pdfPage.renderPageBitmap(
                    mockk<Bitmap>(),
                    mockk<KtImmutableMatrix>(),
                    mockk<KtImmutableRectF>(),
                    renderAnnot,
                    textMask,
                )
            }
        }
        verify {
            page.renderPageBitmap(
                any<Bitmap>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPageBitmapWithDefaults() {
        pdfPage.renderPageBitmap(
            mockk<Bitmap>(),
            mockk<KtImmutableMatrix>(),
            mockk<KtImmutableRectF>(),
        )
        verify {
            page.renderPageBitmap(
                any<Bitmap>(),
                any<KtImmutableMatrix>(),
                any<KtImmutableRectF>(),
                any<Boolean>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
            )
        }
    }

    @Test
    fun testRenderPageBitmapWithAndroidTypes() {
        listOf(false, true).forEach { renderAnnot ->
            listOf(false, true).forEach { textMask ->
                pdfPage.renderPageBitmap(
                    mockk<Bitmap>(),
                    mockk<Matrix>(relaxed = true),
                    mockk<RectF>(),
                    renderAnnot,
                    textMask,
                )
            }
        }
    }

    @Test
    fun testRenderPageBitmapWithDefaultsWithAndroidTypes() {
        pdfPage.renderPageBitmap(
            mockk<Bitmap>(),
            mockk<Matrix>(relaxed = true),
            mockk<RectF>(),
        )
    }

    @Test
    fun getPageLinks() {
        val expected = listOf(Link(KtImmutableRectF.EMPTY, 0, ""))
        every { page.getPageLinks() } returns expected
        val result = pdfPage.getPageLinks()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageLinks() }
    }

    @Test
    fun mapPageCoordsToDevice() {
        val expected = KtImmutablePoint.ZERO
        every { page.mapPageCoordsToDevice(any(), any(), any(), any(), any(), any(), any()) } returns expected
        val result = pdfPage.mapPageCoordsToDevice(0, 0, 0, 0, 0, 0.0, 0.0)
        assertThat(result).isEqualTo(expected)
        verify { page.mapPageCoordsToDevice(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun mapDeviceCoordsToPage() {
        val expected = KtImmutablePointF.ZERO
        every { page.mapDeviceCoordsToPage(any(), any(), any(), any(), any(), any(), any()) } returns expected
        val result = pdfPage.mapDeviceCoordsToPage(0, 0, 0, 0, 0, 0, 0)
        assertThat(result).isEqualTo(expected)
        verify { page.mapDeviceCoordsToPage(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun mapRectToDevice() {
        val expected = KtImmutableRect.EMPTY
        every { page.mapRectToDevice(any(), any(), any(), any(), any(), any()) } returns expected
        val result = pdfPage.mapRectToDevice(0, 0, 0, 0, 0, mockk<FloatRectValues>())
        assertThat(result).isEqualTo(expected)
        verify { page.mapRectToDevice(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun mapRectToDeviceWithAndroidTypes() {
        val expected = KtImmutableRect.EMPTY
        every { page.mapRectToDevice(any(), any(), any(), any(), any(), any()) } returns expected
        // a deprecated wrapper, it doesn't matter in this case
        pdfPage.mapRectToDevice(0, 0, 0, 0, 0, mockk<RectF>())
    }

    @Test
    fun mapRectToPage() {
        val expected = KtImmutableRectF.EMPTY
        every { page.mapRectToPage(any(), any(), any(), any(), any(), any()) } returns expected
        val result = pdfPage.mapRectToPage(0, 0, 0, 0, 0, mockk<IntRectValues>())
        assertThat(result).isEqualTo(expected)
        verify { page.mapRectToPage(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun mapRectToPageWithAndroidTypes() {
        val expected = KtImmutableRectF.EMPTY
        every { page.mapRectToPage(any(), any(), any(), any(), any(), any()) } returns expected
        // a deprecated wrapper, it doesn't matter in this case
        pdfPage.mapRectToPage(0, 0, 0, 0, 0, mockk<Rect>())
    }

    @Test
    fun getPageAttributes() {
        val expected = PageAttributes.EMPTY
        every { page.getPageAttributes() } returns PageAttributes.EMPTY
        val result = pdfPage.getPageAttributes()
        assertThat(result).isEqualTo(expected)
        verify { page.getPageAttributes() }
    }

    @Test
    fun close() {
        every { page.close() } returns Unit
        pdfPage.close()
        verify { page.close() }
    }
}
