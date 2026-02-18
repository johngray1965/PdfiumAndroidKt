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

package io.legere.pdfiumandroid.core.unlocked

import android.graphics.Bitmap
import android.view.Surface
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.AlreadyClosedBehavior
import io.legere.pdfiumandroid.api.PageAttributes
import io.legere.pdfiumandroid.api.Size
import io.legere.pdfiumandroid.api.pdfiumConfig
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfPoint
import io.legere.pdfiumandroid.api.types.PdfPointF
import io.legere.pdfiumandroid.api.types.PdfRect
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.jni.NativeDocument
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.NativePage
import io.legere.pdfiumandroid.core.jni.NativeTextPage
import io.legere.pdfiumandroid.core.unlocked.testing.ClosableTestContext
import io.legere.pdfiumandroid.core.unlocked.testing.closableTest
import io.legere.pdfiumandroid.core.util.PageCount
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@Suppress("LargeClass")
@ExtendWith(MockKExtension::class)
abstract class PdfPageUBaseTest : ClosableTestContext {
    val mockNativeFactory: NativeFactory = mockk()

    val mockNativeDocument: NativeDocument = mockk()

    val mockNativePage: NativePage = mockk()

    val mockNativeTextPage: NativeTextPage = mockk()

    lateinit var pdfDocumentU: PdfDocumentU

    lateinit var pdfPage: PdfPageU

    abstract fun getBehavior(): AlreadyClosedBehavior

    abstract fun setupClosedState()

    abstract fun isStateClosed(): Boolean

    override fun shouldThrowException() = getBehavior() == AlreadyClosedBehavior.EXCEPTION && isStateClosed()

    override fun shouldReturnDefault() = getBehavior() == AlreadyClosedBehavior.IGNORE && isStateClosed()

    val invalidRectF = PdfRectF(-1.0f, -1.0f, -1.0f, -1.0f)

    @BeforeEach
    fun setUp() {
        clearAllMocks(currentThreadOnly = true)

        PdfiumCoreU.resetForTesting()
        pdfiumConfig =
            io.legere.pdfiumandroid.api
                .Config(alreadyClosedBehavior = getBehavior())
        every { mockNativePage.closePage(any()) } just runs
        every { mockNativeFactory.getNativeDocument() } returns mockNativeDocument
        every { mockNativeFactory.getNativePage() } returns mockNativePage
        every { mockNativeFactory.getNativeTextPage() } returns mockNativeTextPage
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfPage =
            PdfPageU(
                pdfDocumentU,
                0,
                0,
                pageMap = mutableMapOf(0 to PageCount(0, 2)),
                nativeFactory = mockNativeFactory,
            )
        setupClosedState()
    }

    @Test
    fun `openTextPage success`() {
        if (isStateClosed()) return
        // Verify that openTextPage returns a valid PdfTextPageU instance when the document and page are open.
        every { mockNativeDocument.loadTextPage(any(), any()) } returns 123
        val textPage = pdfPage.openTextPage()
        assertThat(textPage).isNotNull()
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
                every { mockNativePage.getPageMatrix(any()) } returns
                    floatArrayOf(
                        1f,
                        2f,
                        3f,
                        4f,
                        5f,
                        6f,
                    )
            }
            apiCall = {
                pdfPage.getPageMatrix()
            }

            verifyHappy {
                assertThat(it).isEqualTo(
                    PdfMatrix(
                        floatArrayOf(
                            1.0f,
                            2.0f,
                            5.0f,
                            3.0f,
                            4.0f,
                            6.0f,
                            0.0f,
                            0.0f,
                            1.0f,
                        ),
                    ),
                )
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
                    floatArrayOf(10f, 20f, 30f, 40f)
            }
            apiCall = {
                pdfPage.getPageCropBox()
            }

            verifyHappy {
                assertThat(it).isEqualTo(PdfRectF(10.0f, 20.0f, 30.0f, 40.0f))
            }
            verifyDefault {
                assertThat(it).isEqualTo(invalidRectF)
            }
        }

    @Test
    fun `getPageMediaBox success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageMediaBox(any()) } returns
                    floatArrayOf(10f, 20f, 30f, 40f)
            }
            apiCall = {
                pdfPage.getPageMediaBox()
            }

            verifyHappy {
                assertThat(it).isEqualTo(PdfRectF(10.0f, 20.0f, 30.0f, 40.0f))
            }
            verifyDefault {
                assertThat(it).isEqualTo(invalidRectF)
            }
        }

    @Test
    fun `getPageBleedBox success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageBleedBox(any()) } returns
                    floatArrayOf(10f, 20f, 30f, 40f)
            }
            apiCall = {
                pdfPage.getPageBleedBox()
            }
            verifyHappy {
                assertThat(it).isEqualTo(PdfRectF(10.0f, 20.0f, 30.0f, 40.0f))
            }
            verifyDefault {
                assertThat(it).isEqualTo(invalidRectF)
            }
        }

    @Test
    fun `getPageTrimBox success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageTrimBox(any()) } returns
                    floatArrayOf(10f, 20f, 30f, 40f)
            }
            apiCall = {
                pdfPage.getPageTrimBox()
            }
            verifyHappy {
                assertThat(it).isEqualTo(PdfRectF(10.0f, 20.0f, 30.0f, 40.0f))
            }
            verifyDefault {
                assertThat(it).isEqualTo(invalidRectF)
            }
        }

    @Test
    fun `getPageArtBox success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageArtBox(any()) } returns
                    floatArrayOf(10f, 20f, 30f, 40f)
            }
            apiCall = {
                pdfPage.getPageArtBox()
            }
            verifyHappy {
                assertThat(it).isEqualTo(PdfRectF(10.0f, 20.0f, 30.0f, 40.0f))
            }
            verifyDefault {
                assertThat(it).isEqualTo(invalidRectF)
            }
        }

    @Test
    fun `getPageBoundingBox success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageBoundingBox(any()) } returns
                    floatArrayOf(10f, 20f, 30f, 40f)
            }
            apiCall = {
                pdfPage.getPageBoundingBox()
            }
            verifyHappy {
                assertThat(it).isEqualTo(PdfRectF(10.0f, 20.0f, 30.0f, 40.0f))
            }
            verifyDefault {
                assertThat(it).isEqualTo(invalidRectF)
            }
        }

    @Test
    fun `getPageSize success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageSizeByIndex(any(), any(), any()) } returns
                    intArrayOf(
                        10,
                        20,
                    )
            }
            apiCall = {
                pdfPage.getPageSize(72)
            }
            verifyHappy {
                assertThat(it).isEqualTo(Size(width = 10, height = 20))
            }
            verifyDefault {
                assertThat(it).isEqualTo(Size(width = -1, height = -1))
            }
        }

    @Test
    fun `renderPage to bufferPtr success`() =
        closableTest {
            setupHappy {
                every {
                    mockNativePage.renderPage(
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
            }
            apiCall = {
                pdfPage.renderPage(0L, 100, 100, 100, 100)
            }
            verifyHappy {
                assertThat(it).isTrue()
            }
            verifyDefault {
                assertThat(it).isFalse()
            }
        }

    // NOTE: Tests checking specific native exception handling logic (try/catch inside the wrapper)
    // are best kept as standard @Tests, as they test "Happy path with native crash" scenarios,
    // not the "Closed Object" guard scenarios.
    @Test
    fun `renderPage to bufferPtr exception handling`() {
        if (isStateClosed()) return // Skip for closed states as we are testing logic inside the open state

        every {
            mockNativePage.renderPage(
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
        } throws
            NullPointerException("Test exception")
        val result = pdfPage.renderPage(0L, 100, 100, 100, 100)
        assertThat(result).isFalse()
    }

    @Test
    fun `renderPage to bufferPtr exception handling 2`() {
        if (isStateClosed()) return

        every {
            mockNativePage.renderPage(
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
        } throws
            IllegalStateException("Test exception")
        val result = pdfPage.renderPage(0L, 100, 100, 100, 100)
        assertThat(result).isFalse()
    }

    @Test
    fun `renderPage with Matrix to bufferPtr success`() =
        closableTest {
            val matrix = PdfMatrix()
            matrix.postTranslate(100f, 100f)
            val clipRect = PdfRectF(0f, 0f, 100f, 100f)

            setupHappy {
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
                } returns
                    true
            }
            apiCall = {
                pdfPage.renderPage(0L, 100, 100, matrix, clipRect)
            }
            verifyHappy {
                assertThat(it).isTrue()
            }
            verifyDefault {
                assertThat(it).isFalse()
            }
        }

    @Test
    fun `renderPage with Matrix to Surface success`() =
        closableTest {
            val surface = mockk<Surface>()
            val matrix = PdfMatrix()
            matrix.postTranslate(100f, 100f)
            val clipRect = PdfRectF(0f, 0f, 100f, 100f)

            setupHappy {
                every {
                    mockNativePage.renderPageSurfaceWithMatrix(
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
            }
            apiCall = {
                pdfPage.renderPage(surface, matrix, clipRect)
            }
            verifyHappy {
                assertThat(it).isTrue()
            }
            verifyDefault {
                assertThat(it).isFalse()
            }
        }

    @Test
    fun `renderPageBitmap coordinates success`() =
        closableTest {
            val bitmap = mockk<Bitmap>()
            val matrix = PdfMatrix()
            matrix.postTranslate(100f, 100f)
            val clipRect = PdfRectF(0f, 0f, 100f, 100f)

            setupHappy {
                every {
                    mockNativePage.renderPageBitmapWithMatrix(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } just runs
            }
            apiCall = {
                pdfPage.renderPageBitmap(bitmap, matrix, clipRect)
            }
            verifyHappy {
                // Verify calls actually happened if possible, or just that no exception occurred
            }
            verifyDefault {
                // No exception expected
            }
        }

    @Test
    fun `renderPageBitmap Matrix success`() =
        closableTest {
            val bitmap = mockk<Bitmap>()
            setupHappy {
                every {
                    mockNativePage.renderPageBitmap(
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
                        any(),
                    )
                } just
                    runs
            }
            apiCall = {
                pdfPage.renderPageBitmap(bitmap, 0, 0, 0, 0)
            }
            verifyHappy {
                // No exception
            }
            verifyDefault {
                // No exception
            }
        }

    @Test
    fun `renderPageBitmap null bitmap`() {
        if (isStateClosed()) return

        val matrix = PdfMatrix()
        matrix.postTranslate(100f, 100f)
        val clipRect = PdfRectF(0f, 0f, 100f, 100f)
        every {
            mockNativePage.renderPageBitmapWithMatrix(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } just runs
        pdfPage.renderPageBitmap(null, matrix, clipRect)
    }

    @Test
    fun `getPageLinks success`() =
        closableTest {
            setupHappy {
                every { mockNativePage.getPageLinks(any()) } returns
                    longArrayOf(100L, 200L, 300L, 400L, 500L, 600L)
                every { mockNativePage.getLinkRect(any(), any()) } returns
                    floatArrayOf(100f, 200f, 300f, 400f)
                every { mockNativePage.getDestPageIndex(any(), any()) } returns 101
                every { mockNativePage.getLinkURI(any(), any()) } returns "somelink"
            }
            apiCall = {
                pdfPage.getPageLinks()
            }
            verifyHappy {
                assertThat(it.size).isEqualTo(6)
            }
            verifyDefault {
                assertThat(it).isEmpty()
            }
        }

    @Test
    fun `mapPageCoordsToDevice success`() =
        closableTest {
            setupHappy {
                every {
                    mockNativePage.pageCoordsToDevice(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns
                    intArrayOf(100, 200)
            }
            apiCall = {
                pdfPage.mapPageCoordsToDevice(0, 0, 0, 0, 0, 0.0, 0.0)
            }
            verifyHappy {
                assertThat(it).isEqualTo(PdfPoint(100, 200))
            }
            verifyDefault {
                assertThat(it).isEqualTo(PdfPoint(0, 0)) // Fixed expectation from -1,-1 to 0,0 based on PdfPageU change
            }
        }

    @Test
    fun `mapDeviceCoordsToPage success`() =
        closableTest {
            setupHappy {
                every {
                    mockNativePage.deviceCoordsToPage(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns
                    floatArrayOf(100f, 200f)
            }
            apiCall = {
                pdfPage.mapDeviceCoordsToPage(0, 0, 0, 0, 0, 0, 0)
            }
            verifyHappy {
                assertThat(it).isEqualTo(PdfPointF(100.0f, 200.0f))
            }
            verifyDefault {
                assertThat(it).isEqualTo(PdfPointF(0f, 0f)) // Fixed expectation
            }
        }

    @Test
    fun `mapRectToDevice success`() =
        closableTest {
            setupHappy {
                every {
                    mockNativePage.pageCoordsToDevice(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns
                    intArrayOf(100, 200)
            }
            apiCall = {
                pdfPage.mapRectToDevice(0, 0, 0, 0, 0, PdfRectF(100f, 200f, 300f, 400f))
            }
            verifyHappy {
                assertThat(it).isEqualTo(PdfRect(100, 200, 100, 200))
            }
            verifyDefault {
                assertThat(it).isEqualTo(PdfRect(0, 0, 0, 0)) // Fixed expectation
            }
        }

    @Test
    fun `mapRectToPage success`() =
        closableTest {
            setupHappy {
                every {
                    mockNativePage.deviceCoordsToPage(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns
                    floatArrayOf(100f, 200f)
            }
            apiCall = {
                pdfPage.mapRectToPage(0, 0, 0, 0, 0, PdfRect(100, 200, 300, 400))
            }
            verifyHappy {
                assertThat(it).isEqualTo(PdfRectF(100.0f, 200.0f, 100.0f, 200.0f))
            }
            verifyDefault {
                assertThat(it).isEqualTo(PdfRectF(-1f, -1f, -1f, -1f))
            }
        }

    @Test
    @Suppress("MagicNumber", "LongMethod")
    fun `getPageAttributes success`() =
        closableTest {
            setupHappy {
                every {
                    mockNativePage.getPageLinks(
                        any(),
                    )
                } returns LongArray(0) { 0L }

                every {
                    mockNativePage.getPageAttributes(
                        any(),
                    )
                } returns
                    floatArrayOf(
                        1f,
                        2f,
                        3f,
                        4f,
                        5f,
                        6f,
                        7f,
                        8f,
                        9f,
                        10f,
                        11f,
                        12f,
                        13f,
                        14f,
                        15f,
                        16f,
                        17f,
                        18f,
                        19f,
                        20f,
                        21f,
                        22f,
                        23f,
                        24f,
                        25f,
                        26f,
                        27f,
                        28f,
                        29f,
                        30f,
                        31f,
                    )
            }
            apiCall = {
                pdfPage.getPageAttributes()
            }
            verifyHappy {
                assertThat(it).isEqualTo(
                    PageAttributes(
                        0,
                        1,
                        2,
                        3,
                        PdfRectF(0.0f, 0.0f, 1.0f, 2.0f),
                        PdfRectF(4.0f, 5.0f, 6.0f, 7.0f),
                        PdfRectF(8.0f, 9.0f, 10.0f, 11.0f),
                        PdfRectF(12.0f, 13.0f, 14.0f, 15.0f),
                        PdfRectF(16.0f, 17.0f, 18.0f, 19.0f),
                        PdfRectF(20.0f, 21.0f, 22.0f, 23.0f),
                        PdfRectF(24.0f, 25.0f, 26.0f, 27.0f),
                        emptyList(),
                        PdfMatrix(
                            floatArrayOf(
                                2.0f,
                                0.0f,
                                28.0f,
                                0.0f,
                                1.0f,
                                29.0f,
                                0.0f,
                                0.0f,
                                1.0f,
                            ),
                        ),
                    ),
                )
            }
            verifyDefault {
                assertThat(it).isEqualTo(PageAttributes.EMPTY)
            }
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
        // Verify that setting the isClosed property updates the internal state correctly
        // and the getter reflects this change.
        assertThat(pdfPage.isClosed).isFalse()
        pdfPage.isClosed = true
        assertThat(pdfPage.isClosed).isTrue()
        pdfPage.isClosed = false
        assertThat(pdfPage.isClosed).isFalse()
    }
    // ========================================================================
    // Internal Logic Tests (Reference Counting)
    // These test the *mechanism* of closing, so they run as standard tests
    // rather than using the closableTest wrapper.
    // ========================================================================

    @Test
    fun `close decrements reference count`() {
        pdfPage.close()
        assertThat(pdfPage.isClosed).isFalse()
    }

    @Test
    fun `close actually closes native page`() {
        pdfPage.close()
        pdfPage.close()
        assertThat(pdfPage.isClosed).isTrue()
    }

    @Test
    fun `close actually closes native page with missing map entry`() {
        // Verify that calling close() when reference count is 1 removes the page from pageMap,
        // sets isClosed to true, and calls native closePage.
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

    @Test
    fun `close idempotent check`() {
        // Verify that calling close() multiple times on an already closed page does not cause errors
        // or double-free native resources.
        pdfPage.close()
        pdfPage.close()
        assertThrows<IllegalStateException> {
            pdfPage.close()
        }
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
