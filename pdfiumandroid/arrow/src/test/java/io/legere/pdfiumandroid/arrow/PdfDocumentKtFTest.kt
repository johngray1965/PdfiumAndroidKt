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

package io.legere.pdfiumandroid.arrow

import android.view.Surface
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.arrow.testing.StandardTestDispatcherExtension
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, StandardTestDispatcherExtension::class)
class PdfDocumentKtFTest {
    private lateinit var pdfDocument: PdfDocumentKtF

    @MockK
    private lateinit var pdfDocumentU: PdfDocumentU

    @BeforeEach
    fun setUp() {
        pdfDocument = PdfDocumentKtF(document = pdfDocumentU, dispatcher = Dispatchers.Main)
    }

    @Test
    fun getPageCount() =
        runTest {
            val expected = 10
            coEvery { pdfDocumentU.getPageCount() } returns expected
            val result = pdfDocument.getPageCount().getOrNull()
            assertThat(result).isEqualTo(expected)
            coVerify { pdfDocumentU.getPageCount() }
        }

    @Test
    fun getPageCharCounts() =
        runTest {
            val expected = intArrayOf(5, 10, 15)
            coEvery { pdfDocumentU.getPageCharCounts() } returns expected
            val result = pdfDocument.getPageCharCounts().getOrNull()
            assertThat(result).isEqualTo(expected)
            coVerify { pdfDocumentU.getPageCharCounts() }
        }

    @Test
    fun openPage() =
        runTest {
            val expected = mockk<PdfPageU>()
            coEvery { pdfDocumentU.openPage(any()) } returns expected
            val result = pdfDocument.openPage(1).getOrNull()
            assertThat(result?.page).isEqualTo(expected)
            coVerify { pdfDocumentU.openPage(any()) }
        }

    @Test
    fun `openPage - fails`() =
        runTest {
            coEvery { pdfDocumentU.openPage(any()) } returns null
            val result = pdfDocument.openPage(1).getOrNull()
            assertThat(result?.page).isNull()
            coVerify { pdfDocumentU.openPage(any()) }
        }

    @Test
    fun deletePage() =
        runTest {
            coEvery { pdfDocumentU.deletePage(any()) } returns Unit
            pdfDocument.deletePage(1)
            coVerify { pdfDocumentU.deletePage(any()) }
        }

    @Test
    fun openPages() =
        runTest {
            val expected = listOf(mockk<PdfPageU>())
            coEvery { pdfDocumentU.openPages(any(), any()) } returns expected
            val result = pdfDocument.openPages(1, 2).getOrNull()
            assertThat(result?.get(0)?.page).isEqualTo(expected[0])
            coVerify { pdfDocumentU.openPages(any(), any()) }
        }

    @Test
    fun renderPagesFalseFalse() =
        runTest {
            renderPagesWithOptions(renderPages = false, textMask = false)
        }

    @Test
    fun renderPagesTrueFalse() =
        runTest {
            renderPagesWithOptions(renderPages = true, textMask = false)
        }

    @Test
    fun renderPagesFalseTrue() =
        runTest {
            renderPagesWithOptions(renderPages = false, textMask = true)
        }

    @Test
    fun renderPagesTrueTrue() =
        runTest {
            renderPagesWithOptions(renderPages = true, textMask = true)
        }

    private suspend fun renderPagesWithOptions(
        renderPages: Boolean,
        textMask: Boolean,
    ) {
        coEvery {
            pdfDocumentU.renderPages(
                any<Surface>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns true
        val result =
            pdfDocument.renderPages(
                mockk<Surface>(),
                emptyList(),
                emptyList(),
                emptyList(),
                renderAnnot = renderPages,
                textMask = textMask,
                canvasColor = 0,
                pageBackgroundColor = 0,
                renderCoroutinesDispatcher = Dispatchers.Main,
            )
        assertThat(result).isTrue()
        coVerify {
            pdfDocumentU.renderPages(
                any<Surface>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }
    }

    @Test
    fun renderPagesDefault() =
        runTest {
            coEvery {
                pdfDocumentU.renderPages(
                    any<Surface>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns true
            val result =
                pdfDocument.renderPages(
                    mockk<Surface>(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    renderCoroutinesDispatcher = Dispatchers.Main,
                )
            assertThat(result).isTrue()
            coVerify {
                pdfDocumentU.renderPages(
                    any<Surface>(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

    @Test
    fun getDocumentMeta() =
        runTest {
            val expected = mockk<Meta>()
            coEvery { pdfDocumentU.getDocumentMeta() } returns expected
            val result = pdfDocument.getDocumentMeta().getOrNull()
            assertThat(result).isEqualTo(expected)
            coVerify { pdfDocumentU.getDocumentMeta() }
        }

    @Test
    fun getTableOfContents() =
        runTest {
            val expected = listOf(mockk<Bookmark>())
            coEvery { pdfDocumentU.getTableOfContents() } returns expected
            val result = pdfDocument.getTableOfContents().getOrNull()
            assertThat(result).isEqualTo(expected)
            coVerify { pdfDocumentU.getTableOfContents() }
        }

//    @Test
//    fun openTextPage() =
//        runTest {
//            val expected = mockk<PdfTextPageU>()
//
//            val page = mockk<PdfPageKt>()
//            coEvery { pdfDocumentU.openTextPage(any()) } returns expected
//            coEvery { page.page } returns mockk()
//
//            val result = pdfDocument.openTextPage(page)
//            assertThat(result.page).isEqualTo(expected)
//            coVerify { pdfDocumentU.openTextPage(any()) }
//        }

    @Test
    fun openTextPages() =
        runTest {
            val expected = listOf(mockk<PdfTextPageU>())
            coEvery { pdfDocumentU.openTextPages(any(), any()) } returns expected
            val result = pdfDocument.openTextPages(1, 2).getOrNull()
            assertThat(result?.get(0)?.page).isEqualTo(expected[0])
            coVerify { pdfDocumentU.openTextPages(any(), any()) }
        }

    @Test
    fun saveAsCopy() =
        runTest {
            val callback: PdfWriteCallback = mockk()
            coEvery { pdfDocumentU.saveAsCopy(any()) } returns true
            val result = pdfDocument.saveAsCopy(callback).getOrNull()
            assertThat(result).isTrue()
            coVerify { pdfDocumentU.saveAsCopy(any()) }
        }

    @Test
    fun close() {
        every { pdfDocumentU.close() } returns Unit
        pdfDocument.close()
        verify { pdfDocumentU.close() }
    }

    @Test
    fun safeClose() {
        every { pdfDocumentU.close() } returns Unit
        val result = pdfDocument.safeClose().getOrNull()
        assertThat(result).isTrue()
        verify { pdfDocumentU.close() }
    }

    @Test
    fun `safeClose - fails`() {
        every { pdfDocumentU.close() } throws IllegalStateException()
        val result = pdfDocument.safeClose().getOrNull()
        assertThat(result).isNull()
        verify { pdfDocumentU.close() }
    }
}
