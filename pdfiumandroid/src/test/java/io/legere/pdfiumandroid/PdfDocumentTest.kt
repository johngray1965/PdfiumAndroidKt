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

import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.Bookmark
import io.legere.pdfiumandroid.api.Meta
import io.legere.pdfiumandroid.api.PdfWriteCallback
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(Lifecycle.PER_CLASS)
class PdfDocumentTest {
    val document: PdfDocumentU = mockk()

    val pdfDocument: PdfDocument = PdfDocument(document)

    @Test
    fun getPageCount() {
        every { document.getPageCount() } returns 10
        assertThat(pdfDocument.getPageCount()).isEqualTo(10)
    }

    @Test
    fun getPageCharCounts() {
        val expected = intArrayOf(10, 20)
        every { document.getPageCharCounts() } returns expected
        assertThat(pdfDocument.getPageCharCounts()).isEqualTo(expected)
    }

    @Test
    fun openPage() {
        val expected = mockk<PdfPageU>()
        every { document.openPage(any()) } returns expected
        assertThat(pdfDocument.openPage(0)?.page).isEqualTo(expected)
    }

    @Test
    fun openPageNull() {
        every { document.openPage(any()) } returns null
        assertThat(pdfDocument.openPage(0)).isNull()
    }

    @Test
    fun deletePage() {
        every { document.deletePage(any()) } just runs
        pdfDocument.deletePage(0)
        verify { document.deletePage(0) }
    }

    @Test
    fun openPages() {
        val expected = listOf(mockk<PdfPageU>(), mockk<PdfPageU>())
        every { document.openPages(any(), any()) } returns expected
        assertThat(pdfDocument.openPages(0, 1).map { it.page }).isEqualTo(expected)
    }

    @Test
    fun renderPages() {
        every { document.renderPages(any(), any(), any(), any()) } returns true
        assertThat(pdfDocument.renderPages(mockk(), emptyList(), emptyList(), emptyList())).isTrue()
    }

    @Test
    fun testRenderPages() {
        every { document.renderPages(any(), any(), any(), any(), any(), any(), any(), any(), any()) } just runs
        pdfDocument.renderPages(1234L, 100, 100, emptyList(), emptyList(), emptyList())
        verify { document.renderPages(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun getDocumentMeta() {
        val expected = Meta()
        every { document.getDocumentMeta() } returns expected
        assertThat(pdfDocument.getDocumentMeta()).isEqualTo(expected)
    }

    @Test
    fun getTableOfContents() {
        val expected = listOf(Bookmark(), Bookmark())
        every { document.getTableOfContents() } returns expected
        assertThat(pdfDocument.getTableOfContents()).isEqualTo(expected)
    }

    @Test
    fun saveAsCopy() {
        every { document.saveAsCopy(any(), any()) } returns true
        assertThat(
            pdfDocument.saveAsCopy(
                object : PdfWriteCallback {
                    override fun WriteBlock(data: ByteArray?): Int {
                        TODO("Not yet implemented")
                    }
                },
            ),
        ).isTrue()
    }

    @Test
    fun close() {
        every { document.close() } just runs
        pdfDocument.close()
        verify { document.close() }
    }
}
