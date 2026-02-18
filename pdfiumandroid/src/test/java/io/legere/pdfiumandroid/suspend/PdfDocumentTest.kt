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

import android.view.Surface
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.testing.StandardTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, StandardTestDispatcherExtension::class)
@TestInstance(Lifecycle.PER_CLASS)
class PdfDocumentTest {
    val pdfDocumentU: PdfDocumentU = mockk()

    val pdfDocument = PdfDocumentKt(document = pdfDocumentU, dispatcher = Dispatchers.Main)

    val surface: Surface = mockk()

    @Test
    fun getPageCount() =
        runTest {
            val expected = 10
            coEvery { pdfDocumentU.getPageCount() } returns expected
            val result = pdfDocument.getPageCount()
            assertThat(result).isEqualTo(expected)
            coVerify { pdfDocumentU.getPageCount() }
        }

    @Test
    fun getPageCharCounts() =
        runTest {
            val expected = intArrayOf(5, 10, 15)
            coEvery { pdfDocumentU.getPageCharCounts() } returns expected
            val result = pdfDocument.getPageCharCounts()
            assertThat(result).isEqualTo(expected)
            coVerify { pdfDocumentU.getPageCharCounts() }
        }

    @Test
    fun openPage() =
        runTest {
            val expected = mockk<PdfPageU>()
            coEvery { pdfDocumentU.openPage(any()) } returns expected
            val result = pdfDocument.openPage(1)
            assertThat(result?.page).isEqualTo(expected)
            coVerify { pdfDocumentU.openPage(any()) }
        }

    @Test
    fun `openPage - fails`() =
        runTest {
            coEvery { pdfDocumentU.openPage(any()) } returns null
            val result = pdfDocument.openPage(1)
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
            val result = pdfDocument.openPages(1, 2)
            assertThat(result[0].page).isEqualTo(expected[0])
            coVerify { pdfDocumentU.openPages(any(), any()) }
        }

    @Test
    fun renderPages() =
        runTest {
            coEvery { pdfDocumentU.renderPages(any(), any(), any(), any(), any()) } returns true
            val result =
                pdfDocument.renderPages(
                    surface,
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    false,
                    renderCoroutinesDispatcher = Dispatchers.Main,
                )
            assertThat(result).isTrue()
            coVerify { pdfDocumentU.renderPages(any(), any(), any(), any(), any()) }
        }

    @Test
    fun getDocumentMeta() =
        runTest {
            val expected = mockk<Meta>()
            coEvery { pdfDocumentU.getDocumentMeta() } returns expected
            val result = pdfDocument.getDocumentMeta()
            assertThat(result).isEqualTo(expected)
            coVerify { pdfDocumentU.getDocumentMeta() }
        }

    @Test
    fun getTableOfContents() =
        runTest {
            val expected = listOf(mockk<Bookmark>())
            coEvery { pdfDocumentU.getTableOfContents() } returns expected
            val result = pdfDocument.getTableOfContents()
            assertThat(result).isEqualTo(expected)
            coVerify { pdfDocumentU.getTableOfContents() }
        }

    @Test
    fun openTextPage() =
        runTest {
            val expected = mockk<PdfTextPageU>()

            val page = mockk<PdfPageKt>()
            coEvery { pdfDocumentU.openTextPage(any()) } returns expected
            coEvery { page.page } returns mockk()

            val result = pdfDocument.openTextPage(page)
            assertThat(result.page).isEqualTo(expected)
            coVerify { pdfDocumentU.openTextPage(any()) }
        }

    @Test
    fun openTextPages() =
        runTest {
            val expected = listOf(mockk<PdfTextPageU>())
            coEvery { pdfDocumentU.openTextPages(any(), any()) } returns expected
            val result = pdfDocument.openTextPages(1, 2)
            assertThat(result[0].page).isEqualTo(expected[0])
            coVerify { pdfDocumentU.openTextPages(any(), any()) }
        }

    @Test
    fun saveAsCopy() =
        runTest {
            val callback: PdfWriteCallback = mockk()
            coEvery { pdfDocumentU.saveAsCopy(any()) } returns true
            val result = pdfDocument.saveAsCopy(callback)
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
        val result = pdfDocument.safeClose()
        assertThat(result).isTrue()
        verify { pdfDocumentU.close() }
    }

    @Test
    fun `safeClose - fails`() {
        every { pdfDocumentU.close() } throws IllegalStateException()
        val result = pdfDocument.safeClose()
        assertThat(result).isFalse()
        verify { pdfDocumentU.close() }
    }
}
