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

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.arrow.testing.StandardTestDispatcherExtension
import io.legere.pdfiumandroid.core.unlocked.PdfPageLinkU
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
class PdfPageLinkKtFTest {
    lateinit var pdfPageLink: PdfPageLinkKtF

    @MockK
    lateinit var pdfPageLinkU: PdfPageLinkU

    @BeforeEach
    fun setUp() {
        pdfPageLink = PdfPageLinkKtF(pdfPageLinkU, Dispatchers.Unconfined)
    }

    @Test
    fun countWebLinks() =
        runTest {
            val expected = 23
            every { pdfPageLinkU.countWebLinks() } returns expected
            val result = pdfPageLink.countWebLinks().getOrNull()
            assertThat(result).isEqualTo(expected)
            verify {
                pdfPageLinkU.countWebLinks()
            }
        }

    @Test
    fun getURL() =
        runTest {
            val expected = "testing"
            every { pdfPageLinkU.getURL(any(), any()) } returns expected
            val result = pdfPageLink.getURL(0, 10).getOrNull()
            assertThat(result).isEqualTo(expected)
            verify {
                pdfPageLinkU.getURL(any(), any())
            }
        }

    @Test
    fun countRects() =
        runTest {
            val expected = 29
            every { pdfPageLinkU.countRects(any()) } returns expected
            val result = pdfPageLink.countRects(0).getOrNull()
            assertThat(result).isEqualTo(expected)
            verify {
                pdfPageLinkU.countRects(any())
            }
        }

    @Test
    fun getRect() =
        runTest {
            val expected = mockk<RectF>()
            every { pdfPageLinkU.getRect(any(), any()) } returns expected
            val result = pdfPageLink.getRect(0, 1).getOrNull()
            assertThat(result).isEqualTo(expected)
            verify {
                pdfPageLinkU.getRect(any(), any())
            }
        }

    @Test
    fun getTextRange() =
        runTest {
            val expected = Pair(1, 2)
            every { pdfPageLinkU.getTextRange(any()) } returns expected
            val result = pdfPageLink.getTextRange(0).getOrNull()
            assertThat(result).isEqualTo(expected)
            verify {
                pdfPageLinkU.getTextRange(any())
            }
        }

    @Test
    fun close() {
        every { pdfPageLinkU.close() } returns Unit
        pdfPageLink.close()
        verify {
            pdfPageLinkU.close()
        }
    }
}
