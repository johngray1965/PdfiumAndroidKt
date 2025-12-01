package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.testing.StandardTestDispatcherExtension
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
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
class PdfPageLinkTest {
    lateinit var pdfPageLink: PdfPageLinkKt

    @MockK
    lateinit var pdfPageLinkU: PdfPageLinkU

    @BeforeEach
    fun setUp() {
        pdfPageLink = PdfPageLinkKt(pdfPageLinkU, Dispatchers.Unconfined)
    }

    @Test
    fun countWebLinks() =
        runTest {
            val expected = 23
            every { pdfPageLinkU.countWebLinks() } returns expected
            val result = pdfPageLink.countWebLinks()
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
            val result = pdfPageLink.getURL(0, 10)
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
            val result = pdfPageLink.countRects(0)
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
            val result = pdfPageLink.getRect(0, 1)
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
            val result = pdfPageLink.getTextRange(0)
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
