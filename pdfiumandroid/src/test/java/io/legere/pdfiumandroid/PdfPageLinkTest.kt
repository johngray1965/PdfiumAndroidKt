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
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.unlocked.PdfPageLinkU
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PdfPageLinkTest {
    lateinit var pdfPageLink: PdfPageLink

    @MockK
    lateinit var pageLink: PdfPageLinkU

    @BeforeEach
    fun setUp() {
        pdfPageLink = PdfPageLink(pageLink)
    }

    @Test
    fun countWebLinks() {
        val expected = 7
        every { pageLink.countWebLinks() } returns expected
        assertThat(pdfPageLink.countWebLinks()).isEqualTo(expected)
        verify { pageLink.countWebLinks() }
    }

    @Test
    fun getURL() {
        val expected = "expected"
        every { pageLink.getURL(any(), any()) } returns expected
        assertThat(pdfPageLink.getURL(0, 10)).isEqualTo(expected)
        verify { pageLink.getURL(0, 10) }
    }

    @Test
    fun countRects() {
        val expected = 7
        every { pageLink.countRects(any()) } returns expected
        assertThat(pdfPageLink.countRects(0)).isEqualTo(expected)
        verify { pageLink.countRects(any()) }
    }

    @Test
    fun getRect() {
        val expected = mockk<PdfRectF>()
        every { pageLink.getRect(any(), any()) } returns expected
        assertThat(pdfPageLink.getRect(0, 1)).isEqualTo(expected)
        verify { pageLink.getRect(any(), any()) }
    }

    @Test
    fun getTextRange() {
        val expected = Pair(1, 2)
        every { pageLink.getTextRange(any()) } returns expected
        assertThat(pdfPageLink.getTextRange(0)).isEqualTo(expected)
        verify { pageLink.getTextRange(any()) }
    }

    @Test
    fun close() {
        every { pageLink.close() } returns Unit
        pdfPageLink.close()
        verify {
            pageLink.close()
        }
    }
}
