package io.legere.pdfiumandroid

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
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
        val expected = mockk<RectF>()
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
