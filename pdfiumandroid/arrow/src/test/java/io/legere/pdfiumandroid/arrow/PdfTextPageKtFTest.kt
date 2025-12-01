package io.legere.pdfiumandroid.arrow

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.FindFlags
import io.legere.pdfiumandroid.WordRangeRect
import io.legere.pdfiumandroid.arrow.testing.StandardTestDispatcherExtension
import io.legere.pdfiumandroid.unlocked.FindResultU
import io.legere.pdfiumandroid.unlocked.PdfPageLinkU
import io.legere.pdfiumandroid.unlocked.PdfTextPageU
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, StandardTestDispatcherExtension::class)
class PdfTextPageKtFTest {
    lateinit var pdfTextPage: PdfTextPageKtF

    @MockK
    lateinit var pdfTextPageU: PdfTextPageU

    @BeforeEach
    fun setUp() {
        // Using UnconfinedTestDispatcher (via StandardTestDispatcherExtension logic usually)
        // or Main for testing, assuming PdfPageKt uses the passed dispatcher.
        pdfTextPage = PdfTextPageKtF(pdfTextPageU, Dispatchers.Unconfined)
    }

    @Test
    fun textPageCountChars() =
        runTest {
            val expected = 124
            every { pdfTextPageU.textPageCountChars() } returns expected
            assertThat(pdfTextPage.textPageCountChars().getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.textPageCountChars() }
        }

    @Test
    fun textPageGetText() =
        runTest {
            val expected = "Hello World"
            every { pdfTextPageU.textPageGetText(0, 11) } returns expected
            assertThat(pdfTextPage.textPageGetText(0, 11).getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.textPageGetText(0, 11) }
        }

    @Test
    fun textPageGetUnicode() =
        runTest {
            val expected = 'A'
            every { pdfTextPageU.textPageGetUnicode(5) } returns expected
            assertThat(pdfTextPage.textPageGetUnicode(5).getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.textPageGetUnicode(5) }
        }

    @Test
    fun textPageGetCharBox() =
        runTest {
            val expected = RectF(0f, 0f, 10f, 10f)
            every { pdfTextPageU.textPageGetCharBox(1) } returns expected
            assertThat(pdfTextPage.textPageGetCharBox(1).getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.textPageGetCharBox(1) }
        }

    @Test
    fun textPageGetCharIndexAtPos() =
        runTest {
            val expected = 42
            every { pdfTextPageU.textPageGetCharIndexAtPos(10.0, 10.0, 5.0, 5.0) } returns expected
            assertThat(pdfTextPage.textPageGetCharIndexAtPos(10.0, 10.0, 5.0, 5.0).getOrNull()).isEqualTo(
                expected,
            )
            verify { pdfTextPageU.textPageGetCharIndexAtPos(10.0, 10.0, 5.0, 5.0) }
        }

    @Test
    fun textPageCountRects() =
        runTest {
            val expected = 10
            every { pdfTextPageU.textPageCountRects(0, 100) } returns expected
            assertThat(pdfTextPage.textPageCountRects(0, 100).getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.textPageCountRects(0, 100) }
        }

    @Test
    fun textPageGetRect() =
        runTest {
            val expected = RectF(5f, 5f, 15f, 15f)
            every { pdfTextPageU.textPageGetRect(3) } returns expected
            assertThat(pdfTextPage.textPageGetRect(3).getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.textPageGetRect(3) }
        }

    @Test
    fun textPageGetRectsForRanges() =
        runTest {
            val starts = intArrayOf(0)
            val expected = listOf(WordRangeRect(5, 0, RectF(0f, 0f, 10f, 10f)))

            every { pdfTextPageU.textPageGetRectsForRanges(starts) } returns expected

            assertThat(pdfTextPage.textPageGetRectsForRanges(starts).getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.textPageGetRectsForRanges(starts) }
        }

    @Test
    fun textPageGetBoundedText() =
        runTest {
            val rect = RectF(0f, 0f, 100f, 100f)
            val expected = "Bounded"
            every { pdfTextPageU.textPageGetBoundedText(rect, 100) } returns expected

            assertThat(pdfTextPage.textPageGetBoundedText(rect, 100).getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.textPageGetBoundedText(rect, 100) }
        }

    @Test
    fun getFontSize() =
        runTest {
            val expected = 14.0
            every { pdfTextPageU.getFontSize(2) } returns expected
            assertThat(pdfTextPage.getFontSize(2).getOrNull()).isEqualTo(expected)
            verify { pdfTextPageU.getFontSize(2) }
        }

    @Test
    fun findStart() =
        runTest {
            // The suspend version returns a FindResultKt (wrapper) around FindResultU
            val mockHandle = 999L
            val flags = setOf(FindFlags.MatchCase, FindFlags.MatchWholeWord)
            val unlockedResult = FindResultU(mockHandle)
            every { pdfTextPageU.findStart("search", flags, 0) } returns unlockedResult

            val result = pdfTextPage.findStart("search", flags, 0).getOrNull()

            assertThat(result).isNotNull()
            // Assuming FindResultKt exposes underlying handle or properties
            assertThat(result?.findResult).isEqualTo(unlockedResult)
            verify { pdfTextPageU.findStart("search", flags, 0) }
        }

    @Test
    fun loadWebLink() =
        runTest {
            val mockLink = PdfPageLinkU(888L)
            every { pdfTextPageU.loadWebLink() } returns mockLink

            val result = pdfTextPage.loadWebLink().getOrNull()

            assertThat(result).isNotNull()
            assertThat(result?.pageLink).isEqualTo(mockLink)
            verify { pdfTextPageU.loadWebLink() }
        }

    @Test
    fun close() =
        runTest {
            every { pdfTextPageU.close() } just runs
            pdfTextPage.close()
            verify { pdfTextPageU.close() }
        }

    @Test
    fun safeClose() =
        runTest {
            every { pdfTextPageU.close() } throws RuntimeException("Close failed")
            // Verify safeClose swallows the exception
            val result = pdfTextPage.safeClose().getOrNull()
            assertThat(result).isNull()

            verify { pdfTextPageU.close() }
        }
}
