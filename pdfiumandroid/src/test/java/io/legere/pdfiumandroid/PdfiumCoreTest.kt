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
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.Link
import io.legere.pdfiumandroid.api.LockManager
import io.legere.pdfiumandroid.api.LockManagerReentrantLock
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.testing.ClosableTestContext
import io.legere.pdfiumandroid.testing.nullableTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class PdfiumCoreBasicTest {
    @get:Rule
    val mockkRule: MockKRule = MockKRule(this)

    lateinit var pdfiumCore: PdfiumCore

    @MockK
    lateinit var pdfiumCoreU: PdfiumCoreU

    @MockK
    lateinit var document: PdfDocument

    @Before
    fun setUp() {
        pdfiumCore = PdfiumCore(coreInternal = pdfiumCoreU)
    }

    @After
    fun tearDown() {
        pdfiumCore.setLockManager(LockManagerReentrantLock())
    }

    @Test
    fun newDocument() {
        val document = mockk<PdfDocumentU>()
        every { pdfiumCoreU.newDocument(any() as ParcelFileDescriptor, any()) } returns document
        val result = pdfiumCore.newDocument(mockk<ParcelFileDescriptor>())
        assertThat(result.document).isEqualTo(document)
        verify { pdfiumCoreU.newDocument(any() as ParcelFileDescriptor, any()) }
    }

    @Test
    fun testNewDocument1() {
        val document = mockk<PdfDocumentU>()
        val data = ByteArray(10)
        every { pdfiumCoreU.newDocument(any() as ByteArray, any()) } returns document
        val result = pdfiumCore.newDocument(data)
        assertThat(result.document).isEqualTo(document)
        verify { pdfiumCoreU.newDocument(any() as ByteArray, any()) }
    }

    @Test
    fun testNewDocument3() {
        val document = mockk<PdfDocumentU>()
        every { pdfiumCoreU.newDocument(any() as PdfiumSource, any()) } returns document
        val result = pdfiumCore.newDocument(mockk<PdfiumSource>())
        assertThat(result.document).isEqualTo(document)
        verify { pdfiumCoreU.newDocument(any() as PdfiumSource, any()) }
    }

    @Test
    fun getPageCount() {
        every { document.getPageCount() } returns 42
        val result = pdfiumCore.getPageCount(document)
        assertThat(result).isEqualTo(42)
        verify { document.getPageCount() }
    }

    @Test
    fun closeDocument() {
        every { document.close() } returns Unit
        pdfiumCore.closeDocument(document)
        verify { document.close() }
    }

    @Test
    fun getTableOfContents() {
        val bookmarks = listOf(mockk<Bookmark>())
        every { pdfiumCore.getTableOfContents(document) } returns bookmarks
        val result = pdfiumCore.getTableOfContents(document)
        assertThat(result).isEqualTo(bookmarks)
        verify { pdfiumCore.getTableOfContents(document) }
    }

    @Test
    fun openTextPage() {
        pdfiumCore.openTextPage(document, 0)
    }

    @Test
    fun openPage() {
        pdfiumCore.openPage(document, 0)
    }

    @Test
    fun testOpenPage() {
        pdfiumCore.openTextPage(document, 0)
    }

    @Test
    fun getDocumentMeta() {
        val meta = mockk<Meta>()
        every { pdfiumCore.getDocumentMeta(document) } returns meta
        val results = pdfiumCore.getDocumentMeta(document)
        assertThat(results).isEqualTo(meta)
        verify { pdfiumCore.getDocumentMeta(document) }
    }
}

@Suppress("DEPRECATION")
abstract class PdfiumCoreBaseTest : ClosableTestContext {
    @get:Rule
    val mockkRule: MockKRule = MockKRule(this)

    lateinit var pdfiumCore: PdfiumCore

    @MockK
    lateinit var pdfiumCoreU: PdfiumCoreU

    @MockK
    lateinit var document: PdfDocument

    @MockK
    lateinit var page: PdfPage

    @MockK
    lateinit var textPage: PdfTextPage

    @Before
    fun setUp() {
        pdfiumCore = PdfiumCore(coreInternal = pdfiumCoreU)
        setupRules()
    }

    @Test
    fun getPageMediaBox() =
        nullableTest {
            val mediaBox = mockk<RectF>()
            setup {
                every { page.getPageMediaBox() } returns mediaBox
                every { page.close() } just runs
            }

            apiCall = { pdfiumCore.getPageMediaBox(document, 0) }

            verifyHappy {
                assertThat(it).isEqualTo(mediaBox)
                verify { page.getPageMediaBox() }
                verify { page.close() }
                verify { document.openPage(any()) }
            }

            verifyDefault {
                assertThat(it).isEqualTo(RectF(-1f, -1f, -1f, -1f))
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun textPageCountChars() =
        nullableTest {
            setup {
                every { textPage.textPageCountChars() } returns 1234
                every { page.close() } just runs
                every { textPage.close() } just runs
            }

            apiCall = { pdfiumCore.textPageCountChars(document, 0) }

            verifyHappy {
                assertThat(it).isEqualTo(1234)
                verify { document.openPage(any()) }
                verify { page.openTextPage() }
                verify { textPage.textPageCountChars() }
                verify { page.close() }
                verify { textPage.close() }
            }

            verifyDefault {
                assertThat(it).isEqualTo(-1)
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun textPageGetText() =
        nullableTest {
            setup {
                every { textPage.textPageGetText(any(), any()) } returns "Yo"
                every { page.close() } just runs
                every { textPage.close() } just runs
            }

            apiCall = { pdfiumCore.textPageGetText(document, 0, 0, 0) }

            verifyHappy {
                assertThat(it).isEqualTo("Yo")
                verify { document.openPage(any()) }
                verify { page.openTextPage() }
                verify { textPage.textPageGetText(any(), any()) }
                verify { page.close() }
                verify { textPage.close() }
            }

            verifyDefault {
                assertThat(it).isNull()
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun getPageWidthPoint() =
        nullableTest {
            setup {
                every { page.getPageWidthPoint() } returns 123
                every { page.close() } just runs
                every { textPage.close() } just runs
            }

            apiCall = { pdfiumCore.getPageWidthPoint(document, 0) }

            verifyHappy {
                assertThat(it).isEqualTo(123)
                verify { document.openPage(any()) }
                verify { page.getPageWidthPoint() }
                verify { page.close() }
            }

            verifyDefault {
                assertThat(it).isEqualTo(-1)
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun getPageHeightPoint() =
        nullableTest {
            setup {
                every { page.getPageHeightPoint() } returns 123
                every { page.close() } just runs
                every { textPage.close() } just runs
            }

            apiCall = { pdfiumCore.getPageHeightPoint(document, 0) }

            verifyHappy {
                assertThat(it).isEqualTo(123)
                verify { document.openPage(any()) }
                verify { page.getPageHeightPoint() }
                verify { page.close() }
            }

            verifyDefault {
                assertThat(it).isEqualTo(-1)
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun renderPageBitmap() =
        nullableTest {
            setup {
                every {
                    page.renderPageBitmap(
                        any<Bitmap>(),
                        any<Int>(),
                        any<Int>(),
                        any<Int>(),
                        any<Int>(),
                        any<Boolean>(),
                    )
                } just runs
                every { page.close() } just runs
            }

            apiCall = { pdfiumCore.renderPageBitmap(document, mockk(), 0, 0, 0, 0, 0) }

            verifyHappy {
                verify {
                    page.renderPageBitmap(
                        any<Bitmap>(),
                        any<Int>(),
                        any<Int>(),
                        any<Int>(),
                        any<Int>(),
                        any<Boolean>(),
                    )
                }
                verify { page.close() }
                verify { document.openPage(any()) }
            }

            verifyDefault {
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun testRenderPageBitmap() =
        nullableTest {
            setup {
                every {
                    page.renderPageBitmap(
                        any<Bitmap>(),
                        any<Int>(),
                        any<Int>(),
                        any<Int>(),
                        any<Int>(),
                        any<Boolean>(),
                        any<Boolean>(),
                    )
                } just runs
                every { page.close() } just runs
            }

            apiCall = {
                listOf(false, true).forEach { renderAnnot ->
                    listOf(false, true).forEach { textMask ->
                        pdfiumCore.renderPageBitmap(
                            document,
                            mockk<Bitmap>(),
                            0,
                            0,
                            0,
                            0,
                            0,
                            renderAnnot = renderAnnot,
                            textMask = textMask,
                        )
                    }
                }
            }

            verifyHappy {
                verify {
                    page.renderPageBitmap(
                        any<Bitmap>(),
                        any<Int>(),
                        any<Int>(),
                        any<Int>(),
                        any<Int>(),
                        any<Boolean>(),
                        any<Boolean>(),
                    )
                }
                verify { page.close() }
                verify { document.openPage(any()) }
            }

            verifyDefault {
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun textPageGetRect() =
        nullableTest {
            val rect = mockk<RectF>()
            setup {
                every { page.openTextPage() } returns textPage
                every { textPage.textPageGetRect(any()) } returns rect
                every { page.close() } just runs
                every { textPage.close() } just runs
            }

            apiCall = { pdfiumCore.textPageGetRect(document, 0, 0) }

            verifyHappy {
                assertThat(it).isEqualTo(rect)
                verify { document.openPage(any()) }
                verify { page.openTextPage() }
                verify { textPage.textPageGetRect(any()) }
                verify { page.close() }
                verify { textPage.close() }
            }

            verifyDefault {
                assertThat(it).isNull()
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun textPageGetBoundedText() =
        nullableTest {
            setup {
                every { textPage.textPageGetBoundedText(any(), any()) } returns "Yo"
                every { page.close() } just runs
                every { textPage.close() } just runs
            }

            apiCall = { pdfiumCore.textPageGetBoundedText(document, 0, mockk(), 0) }

            verifyHappy {
                assertThat(it).isEqualTo("Yo")
                verify { document.openPage(any()) }
                verify { page.openTextPage() }
                verify { textPage.textPageGetBoundedText(any(), any()) }
                verify { page.close() }
                verify { textPage.close() }
            }

            verifyDefault {
                assertThat(it).isNull()
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun mapRectToPage() =
        nullableTest {
            val expected = mockk<RectF>()
            setup {
                every { page.mapRectToPage(any(), any(), any(), any(), any(), any()) } returns expected
                every { page.close() } just runs
            }

            apiCall = { pdfiumCore.mapRectToPage(document, 0, 0, 0, 0, 0, 0, mockk()) }

            verifyHappy {
                assertThat(it).isEqualTo(expected)
                verify { page.mapRectToPage(any(), any(), any(), any(), any(), any()) }
                verify { page.close() }
                verify { document.openPage(any()) }
            }

            verifyDefault {
                assertThat(it).isEqualTo(RectF(-1f, -1f, -1f, -1f))
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun textPageCountRects() =
        nullableTest {
            setup {
                every { textPage.textPageCountRects(any(), any()) } returns 124
                every { page.close() } just runs
                every { textPage.close() } just runs
            }

            apiCall = { pdfiumCore.textPageCountRects(document, 0, 0, 0) }

            verifyHappy {
                assertThat(it).isEqualTo(124)
                verify { document.openPage(any()) }
                verify { page.openTextPage() }
                verify { textPage.textPageCountRects(any(), any()) }
                verify { page.close() }
                verify { textPage.close() }
            }

            verifyDefault {
                assertThat(it).isEqualTo(-1)
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun getPageLinks() =
        nullableTest {
            val expected = listOf(mockk<Link>())
            setup {
                every { page.getPageLinks() } returns expected
                every { page.close() } just runs
            }

            apiCall = { pdfiumCore.getPageLinks(document, 0) }

            verifyHappy {
                assertThat(it).isEqualTo(expected)
                verify { page.getPageLinks() }
                verify { page.close() }
                verify { document.openPage(any()) }
            }

            verifyDefault {
                assertThat(it).isEmpty()
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun mapPageCoordsToDevice() =
        nullableTest {
            val expected = mockk<Point>()
            setup {
                every {
                    page.mapPageCoordsToDevice(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns expected
                every { page.close() } just runs
            }

            apiCall = {
                pdfiumCore.mapPageCoordsToDevice(document, 0, 0, 0, 0, 0, 0, 0.0, 0.0)
            }

            verifyHappy {
                assertThat(it).isEqualTo(expected)
                verify { page.mapPageCoordsToDevice(any(), any(), any(), any(), any(), any(), any()) }
                verify { page.close() }
                verify { document.openPage(any()) }
            }

            verifyDefault {
                assertThat(it).isEqualTo(Point(-1, -1))
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun mapRectToDevice() =
        nullableTest {
            val expected = mockk<Rect>()
            setup {
                every { page.mapRectToDevice(any(), any(), any(), any(), any(), any()) } returns expected
                every { page.close() } just runs
            }

            apiCall = {
                pdfiumCore.mapRectToDevice(document, 0, 0, 0, 0, 0, 0, mockk())
            }

            verifyHappy {
                assertThat(it).isEqualTo(expected)
                verify { page.mapRectToDevice(any(), any(), any(), any(), any(), any()) }
                verify { page.close() }
                verify { document.openPage(any()) }
            }

            verifyDefault {
                assertThat(it).isEqualTo(Rect(-1, -1, -1, -1))
                verify { document.openPage(any()) }
            }
        }

    @Test
    fun setLockManager() =
        nullableTest {
            val expected = mockk<LockManager>()
            setup {
                every { page.close() } just runs
            }

            apiCall = {
                pdfiumCore.setLockManager(expected)
            }

            verifyHappy {
                assertThat(PdfiumCoreU.lock).isEqualTo(expected)
            }

            verifyDefault {
                assertThat(PdfiumCoreU.lock).isEqualTo(expected)
            }
        }
}

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class PdfiumCoreHappyPathTest : PdfiumCoreBaseTest() {
    override fun shouldReturnDefault(): Boolean = false

    override fun setupRules() {
        every { document.openPage(any()) } returns page
        every { page.openTextPage() } returns textPage
    }
}

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class PdfiumCoreNullPageTest : PdfiumCoreBaseTest() {
    override fun shouldReturnDefault(): Boolean = true

    override fun setupRules() {
        every { document.openPage(any()) } returns null
    }
}
