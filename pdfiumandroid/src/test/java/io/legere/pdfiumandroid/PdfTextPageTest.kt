package io.legere.pdfiumandroid

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.unlocked.FindResultU
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import io.legere.pdfiumandroid.unlocked.PdfTextPageU
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PdfTextPageTest {
    private lateinit var page: PdfTextPage

    @MockK
    private lateinit var pdfTextPageU: PdfTextPageU

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        page = PdfTextPage(pdfTextPageU)
    }

    @Test
    fun textPageCountChars() {
        val expected = 1234
        every { pdfTextPageU.textPageCountChars() } returns expected
        assertThat(page.textPageCountChars()).isEqualTo(expected)
        verify { pdfTextPageU.textPageCountChars() }
    }

    @Test
    fun textPageGetTextLegacy() {
        val expected = "Hello Legacy"
        every { pdfTextPageU.textPageGetTextLegacy(0, 5) } returns expected
        assertThat(page.textPageGetTextLegacy(0, 5)).isEqualTo(expected)
        verify { pdfTextPageU.textPageGetTextLegacy(0, 5) }
    }

    @Test
    fun textPageGetText() {
        val expected = "Hello World"
        every { pdfTextPageU.textPageGetText(0, 11) } returns expected
        assertThat(page.textPageGetText(0, 11)).isEqualTo(expected)
        verify { pdfTextPageU.textPageGetText(0, 11) }
    }

    @Test
    fun textPageGetUnicode() {
        val expected = 'A'
        every { pdfTextPageU.textPageGetUnicode(10) } returns expected
        assertThat(page.textPageGetUnicode(10)).isEqualTo(expected)
        verify { pdfTextPageU.textPageGetUnicode(10) }
    }

    @Test
    fun textPageGetCharBox() {
        val expected = RectF(0f, 0f, 10f, 10f)
        every { pdfTextPageU.textPageGetCharBox(5) } returns expected
        assertThat(page.textPageGetCharBox(5)).isEqualTo(expected)
        verify { pdfTextPageU.textPageGetCharBox(5) }
    }

    @Test
    fun textPageGetCharIndexAtPos() {
        val expected = 42
        every { pdfTextPageU.textPageGetCharIndexAtPos(10.0, 20.0, 5.0, 5.0) } returns expected
        assertThat(page.textPageGetCharIndexAtPos(10.0, 20.0, 5.0, 5.0)).isEqualTo(expected)
        verify { pdfTextPageU.textPageGetCharIndexAtPos(10.0, 20.0, 5.0, 5.0) }
    }

    @Test
    fun textPageCountRects() {
        val expected = 7
        every { pdfTextPageU.textPageCountRects(0, 100) } returns expected
        assertThat(page.textPageCountRects(0, 100)).isEqualTo(expected)
        verify { pdfTextPageU.textPageCountRects(0, 100) }
    }

    @Test
    fun textPageGetRect() {
        val expected = RectF(5f, 5f, 20f, 20f)
        every { pdfTextPageU.textPageGetRect(3) } returns expected
        assertThat(page.textPageGetRect(3)).isEqualTo(expected)
        verify { pdfTextPageU.textPageGetRect(3) }
    }

    @Test
    fun textPageGetRectsForRanges() {
        val expected = listOf(WordRangeRect(5, 0, mockk()))

        every { pdfTextPageU.textPageGetRectsForRanges(any()) } returns expected

        assertThat(page.textPageGetRectsForRanges(intArrayOf(0))).isEqualTo(expected)
        verify { pdfTextPageU.textPageGetRectsForRanges(any()) }
    }

    @Test
    fun textPageGetBoundedText() {
        val rect = RectF(0f, 0f, 100f, 100f)
        val expected = "Bounded"
        every { pdfTextPageU.textPageGetBoundedText(rect, 50) } returns expected
        assertThat(page.textPageGetBoundedText(rect, 50)).isEqualTo(expected)
        verify { pdfTextPageU.textPageGetBoundedText(rect, 50) }
    }

    @Test
    fun getFontSize() {
        val expected = 12.5
        every { pdfTextPageU.getFontSize(1) } returns expected
        assertThat(page.getFontSize(1)).isEqualTo(expected)
        verify { pdfTextPageU.getFontSize(1) }
    }

    @Test
    fun findStart() {
        // The wrapper returns a wrapper (FindResult) around the unlocked result (FindResultU)
        val mockHandle = 123L
        val unlockedResult = FindResultU(mockHandle)

        val foundFlag = setOf(FindFlags.Consecutive)

        every { pdfTextPageU.findStart("query", foundFlag, 0) } returns unlockedResult

        val result = page.findStart("query", foundFlag, 0)

        assertThat(result).isNotNull()
        // Assuming FindResult exposes the underlying handle or comparable property
        // or assuming FindResult wraps FindResultU
        assertThat(result?.findResult).isEqualTo(unlockedResult)
        verify { pdfTextPageU.findStart("query", foundFlag, 0) }
    }

    @Test
    fun `findStart with null`() {
        // The wrapper returns a wrapper (FindResult) around the unlocked result (FindResultU)
        val foundFlag = setOf(FindFlags.Consecutive)

        every { pdfTextPageU.findStart("query", foundFlag, 0) } returns null

        val result = page.findStart("query", foundFlag, 0)

        assertThat(result).isNull()
        // Assuming FindResult exposes the underlying handle or comparable property
        // or assuming FindResult wraps FindResultU
        verify { pdfTextPageU.findStart("query", foundFlag, 0) }
    }

    @Test
    fun loadWebLink() {
        // Similarly, PdfTextPage.loadWebLink() returns PdfLink (wrapper) for PdfLinkU
        val mockLinkU = mockk<PdfPageLinkU>()

        every { pdfTextPageU.loadWebLink() } returns mockLinkU

        val result = page.loadWebLink()

        assertThat(result).isNotNull()
        assertThat(result?.pageLink).isEqualTo(mockLinkU)
        verify { pdfTextPageU.loadWebLink() }
    }

    @Test
    fun `loadWebLink with null`() {
        // Similarly, PdfTextPage.loadWebLink() returns PdfLink (wrapper) for PdfLinkU
        every { pdfTextPageU.loadWebLink() } returns null

        val result = page.loadWebLink()

        assertThat(result).isNull()
        verify { pdfTextPageU.loadWebLink() }
    }

    @Test
    fun close() {
        every { pdfTextPageU.close() } just runs
        page.close()
        verify { pdfTextPageU.close() }
    }
}
