package io.legere.pdfiumandroid.unlocked

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.jni.NativeDocument
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.NativePage
import io.legere.pdfiumandroid.jni.NativeTextPage
import io.legere.pdfiumandroid.util.AlreadyClosedBehavior
import io.legere.pdfiumandroid.util.Config
import io.legere.pdfiumandroid.util.pdfiumConfig
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config as RoboConfig

// =================================================================================================
// 1. Abstract Base Class (JUnit 4 Style)
// =================================================================================================

@RunWith(AndroidJUnit4::class)
@RoboConfig(manifest = RoboConfig.NONE)
abstract class PdfTextPageBaseTest {
    @MockK lateinit var mockNativeFactory: NativeFactory

    @MockK lateinit var mockNativeDocument: NativeDocument

    @MockK lateinit var mockNativePage: NativePage

    @MockK lateinit var mockNativeTextPage: NativeTextPage

    lateinit var pdfDocumentU: PdfDocumentU
    lateinit var pdfPage: PdfPageU
    lateinit var pdfTextPage: PdfTextPageU

    // Abstract Configuration Hooks
    abstract fun getBehavior(): AlreadyClosedBehavior

    abstract fun setupClosedState()

    abstract fun isStateClosed(): Boolean

    private fun shouldThrowException() = getBehavior() == AlreadyClosedBehavior.EXCEPTION && isStateClosed()

    private fun shouldReturnDefault() = getBehavior() == AlreadyClosedBehavior.IGNORE && isStateClosed()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Global Config
        pdfiumConfig = Config(alreadyClosedBehavior = getBehavior())

        // Mocks
        every { mockNativeFactory.getNativeDocument() } returns mockNativeDocument
        every { mockNativeFactory.getNativePage() } returns mockNativePage
        every { mockNativeFactory.getNativeTextPage() } returns mockNativeTextPage
        every { mockNativeTextPage.textCountChars(any()) } returns 10

        // Instance creation
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)

        val pageMap = mutableMapOf(0 to PdfDocument.PageCount(100L, 2))
        pdfPage = PdfPageU(pdfDocumentU, 0, 0, pageMap, mockNativeFactory)
        pdfTextPage = PdfTextPageU(pdfDocumentU, 0, 0, pageMap, mockNativeFactory)

        setupClosedState()
    }

    // Helper for JUnit 4 Exception assertions
    private fun assertThrows(block: () -> Unit) {
        try {
            block()
            fail("Expected an Exception to be thrown, but nothing was thrown.")
        } catch (e: Exception) {
            // Success (Check type if needed, e.g., IllegalStateException)
            assertThat(e).isInstanceOf(IllegalStateException::class.java)
        }
    }

    // =============================================================================================
    // TESTS
    // =============================================================================================

    @Test
    fun textPageCountChars_behavior() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageCountChars() }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageCountChars()).isEqualTo(-1)
            return
        }

        val result = pdfTextPage.textPageCountChars()
        assertThat(result).isEqualTo(10)
    }

    @Test
    fun textPageGetTextLegacy_validRange() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageGetTextLegacy(0, 10) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageGetTextLegacy(0, 10)).isNull()
            return
        }

        val expectedString = "Hello World"
        val length = expectedString.length

        every { mockNativeTextPage.textGetText(any(), any(), any(), any()) } answers {
            val buffer = arg<ShortArray>(3)
            for (i in expectedString.indices) {
                if (i < buffer.size) buffer[i] = expectedString[i].code.toShort()
            }
            length + 1
        }
        val result = pdfTextPage.textPageGetTextLegacy(0, length)
        assertThat(result).isEqualTo(expectedString)
    }

    @Test
    fun textPageGetText_validRange() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageGetText(0, 10) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageGetText(0, 10)).isNull()
            return
        }

        val expectedString = "Hello World"
        val length = expectedString.length

        every { mockNativeTextPage.textGetTextByteArray(any(), any(), any(), any()) } answers {
            val buffer = arg<ByteArray>(3)
            val count = thirdArg<Int>()
            val bytes = expectedString.toByteArray(Charsets.UTF_16LE)
            bytes.copyInto(buffer, endIndex = minOf(bytes.size, count * 2))
            count + 1
        }
        val result = pdfTextPage.textPageGetText(0, length)
        assertThat(result).isEqualTo(expectedString)
    }

    @Test
    fun textPageGetUnicode_validIndex() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageGetUnicode(0) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageGetUnicode(0)).isEqualTo(Char.MIN_VALUE)
            return
        }

        val expectedChar = 'H'
        every { mockNativeTextPage.textGetUnicode(any(), any()) } returns expectedChar.code
        val result = pdfTextPage.textPageGetUnicode(0)
        assertThat(result).isEqualTo(expectedChar)
    }

    @Test
    fun textPageGetCharBox_validIndex() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageGetCharBox(0) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageGetCharBox(0)).isNull()
            return
        }

        // PDFium returns L, R, B, T in doubles usually
        every { mockNativeTextPage.textGetCharBox(any(), any()) } returns
            doubleArrayOf(90.0, 100.0, 600.0, 700.0)

        val rect = pdfTextPage.textPageGetCharBox(0)
        // Ensure your mapping logic matches this expectation (L, T, R, B)
        assertThat(rect).isEqualTo(RectF(90.0f, 700.0f, 100.0f, 600.0f))
    }

    @Test
    fun textPageGetCharIndexAtPos_validCoordinates() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageGetCharIndexAtPos(0.0, 0.0, 0.0, 0.0) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageGetCharIndexAtPos(0.0, 0.0, 0.0, 0.0)).isEqualTo(-1)
            return
        }

        every { mockNativeTextPage.textGetCharIndexAtPos(any(), any(), any(), any(), any()) } returns 10
        val result = pdfTextPage.textPageGetCharIndexAtPos(50.0, 50.0, 50.0, 50.0)
        assertThat(result).isEqualTo(10)
    }

    @Test
    fun textPageCountRects_validRange() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageCountRects(0, 100) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageCountRects(0, 100)).isEqualTo(-1)
            return
        }

        every { mockNativeTextPage.textCountRects(any(), any(), any()) } returns 4
        val result = pdfTextPage.textPageCountRects(0, 100)
        assertThat(result).isEqualTo(4)
    }

    @Test
    fun textPageGetRect_validIndex() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageGetRect(0) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageGetRect(0)).isNull()
            return
        }

        every { mockNativeTextPage.textGetRect(any(), any()) } returns doubleArrayOf(90.0, 700.0, 100.0, 600.0)
        val result = pdfTextPage.textPageGetRect(0)
        assertThat(result).isEqualTo(RectF(90.0f, 700.0f, 100.0f, 600.0f))
    }

    @Test
    fun textPageGetRectsForRanges_validInput() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageGetRectsForRanges(intArrayOf()) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageGetRectsForRanges(intArrayOf())).isNull()
            return
        }

        val mockRects = doubleArrayOf(10.0, 20.0, 30.0, 40.0, 50.0, 60.0)
        val mockInts = intArrayOf(5, 0)
        every { mockNativeTextPage.textGetRects(any(), any()) } returns mockRects

        val result = pdfTextPage.textPageGetRectsForRanges(intArrayOf(0))
        assertThat(result).hasSize(1)
        assertThat(result!![0].rect).isEqualTo(RectF(10f, 20f, 30f, 40f))
    }

    @Test
    fun textPageGetBoundedText_validRect() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.textPageGetBoundedText(RectF(), 10) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.textPageGetBoundedText(RectF(), 10)).isNull()
            return
        }

        val expected = "Bounded"
        every { mockNativeTextPage.textGetBoundedText(any(), any(), any(), any(), any(), any()) } answers {
            val buffer = arg<ShortArray>(5)
            expected.forEachIndexed { i, c -> buffer[i] = c.code.toShort() }
            expected.length + 1
        }

        val result = pdfTextPage.textPageGetBoundedText(RectF(0f, 0f, 100f, 100f), 10)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun getFontSize_validIndex() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.getFontSize(5) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.getFontSize(5)).isEqualTo(0.0)
            return
        }

        every { mockNativeTextPage.getFontSize(any(), 5) } returns 12.5
        assertThat(pdfTextPage.getFontSize(5)).isEqualTo(12.5)
    }

    @Test
    fun findStart_validSearch() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.findStart("q", emptySet(), 0) }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.findStart("q", emptySet(), 0)).isNull()
            return
        }

        every { mockNativeTextPage.findStart(any(), any(), any(), any()) } returns 999L
        val result = pdfTextPage.findStart("query", emptySet(), 0)
//        assertThat(result.searchHandle).isEqualTo(999L) the FindResultU tests will really test this
    }

    @Test
    fun loadWebLink_execution() {
        if (shouldThrowException()) {
            assertThrows { pdfTextPage.loadWebLink() }
            return
        } else if (shouldReturnDefault()) {
            assertThat(pdfTextPage.loadWebLink()).isNull()
            return
        }

        every { mockNativeTextPage.loadWebLink(any()) } returns 888L
        val result = pdfTextPage.loadWebLink()
//        assertThat(result.pageLinkPtr).isEqualTo(888L) the PdfPageLinkU tests will really test this
    }

    @Test
    fun close_referenceCounting() {
        // Logic test, generally unaffected by "state" unless setupClosedState() forced a close already
        if (isStateClosed()) return

        val map = mutableMapOf(0 to PdfDocument.PageCount(100L, 2))
        val page = PdfTextPageU(pdfDocumentU, 0, 100L, map, mockNativeFactory)

        every { mockNativeTextPage.closeTextPage(any()) } just runs

        page.close()
        assertThat(map[0]?.count).isEqualTo(1)
        verify(exactly = 0) { mockNativeTextPage.closeTextPage(any()) }

        page.close()
        assertThat(map.containsKey(0)).isFalse()
        verify(exactly = 1) { mockNativeTextPage.closeTextPage(100L) }
    }
}

// =================================================================================================
// 2. Concrete Implementations (The actual Test Classes)
// =================================================================================================

class PdfTextPageHappyPathTest : PdfTextPageBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = false

    override fun setupClosedState() { }

    @Test
    fun `textPageGetTextLegacy empty result`() {
        val expectedString = ""
        val length = 0

        every { mockNativeTextPage.textGetText(any(), any(), any(), any()) } answers {
            val buffer = arg<ShortArray>(3) // Get the ShortArray passed to the function

            // Fill buffer with char codes from the expected string
            // PDFium fills the buffer with UTF-16LE values
            for (i in expectedString.indices) {
                if (i < buffer.size) {
                    buffer[i] = expectedString[i].code.toShort()
                }
            }

            // Return the number of characters written (including null terminator if applicable,
            // but typical PDFium logic returns count of chars written)
            length + 1
        }
        val result = pdfTextPage.textPageGetTextLegacy(0, 0)
        assertThat(result).isEqualTo(expectedString)
    }

    @Test
    fun `textPageGetTextLegacy null pointer exception`() {
        // Verify that textPageGetTextLegacy returns null and logs an error if a NullPointerException occurs (e.g., context null).
        every { mockNativeTextPage.textGetText(any(), any(), any(), any()) } throws NullPointerException()
        val result = pdfTextPage.textPageGetTextLegacy(0, 10)
        assertThat(result).isNull()
    }

    @Test
    fun `textPageGetTextLegacy general exception`() {
        // Verify that textPageGetTextLegacy returns null and logs an error if the native method throws a general exception.
        every { mockNativeTextPage.textGetText(any(), any(), any(), any()) } throws Exception()
        val result = pdfTextPage.textPageGetTextLegacy(0, 10)
        assertThat(result).isNull()
    }

    @Test
    fun `textPageGetText empty result`() {
        // Verify that textPageGetText returns an empty string if the native call returns a non-positive value.
        val expectedString = ""
        val length = expectedString.length

        every { mockNativeTextPage.textGetTextByteArray(any(), any(), any(), any()) } answers {
            val buffer = arg<ByteArray>(3) // Get the ByteArray passed to the function
            val count = thirdArg<Int>()

            // Encode the string to UTF-16LE bytes and copy into the buffer
            // Limit copy to the requested count (count * 2 bytes)
            val bytes = expectedString.toByteArray(Charsets.UTF_16LE)
            bytes.copyInto(buffer, endIndex = minOf(bytes.size, count * 2))

            // Return characters written + null terminator count
            count + 1
        }
        val result = pdfTextPage.textPageGetText(0, length)
        assertThat(result).isEqualTo(expectedString)
    }

    @Test
    fun `textPageGetText exception handling`() {
        // Verify that textPageGetText catches exceptions from native code, logs them, and returns null.
        every { mockNativeTextPage.textGetTextByteArray(any(), any(), any(), any()) } throws Exception()
        val result = pdfTextPage.textPageGetText(0, 10)
        assertThat(result).isNull()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `textPageGetUnicode invalid index`() {
        // Verify behavior when an invalid index is provided (likely returns 0/null char or throws depending on native impl).
        every { mockNativeTextPage.textGetUnicode(any(), any()) } throws IllegalArgumentException()
        val result = pdfTextPage.textPageGetUnicode(0)
        assertThat(result).isNull()
    }

    @Test
    fun `textPageGetCharBox native order correction`() {
        // Verify that textPageGetCharBox returns a correctly populated RectF (left, top, right, bottom) for a valid character index.
        every { mockNativeTextPage.textGetCharBox(any(), any()) } returns
            doubleArrayOf(
                90.314415,
                103.44171,
                699.1206,
                715.3187,
            )

        val rect = pdfTextPage.textPageGetCharBox(0)

        assertThat(rect).isEqualTo(RectF(90.314415f, 715.3187f, 103.44171f, 699.1206f))
    }

    @Test
    fun `textPageGetCharBox exception handling`() {
        // Verify that textPageGetCharBox returns null and logs error if native code throws an exception.
        every { mockNativeTextPage.textGetCharBox(any(), any()) } throws Exception()

        val rect = pdfTextPage.textPageGetCharBox(0)

        assertThat(rect).isNull()
    }

    @Test
    fun `textPageGetCharIndexAtPos no character found`() {
        // Verify that textPageGetCharIndexAtPos returns -1 if no character exists at the given position within tolerance.
        every { mockNativeTextPage.textGetCharIndexAtPos(any(), any(), any(), any(), any()) } returns -1

        val result = pdfTextPage.textPageGetCharIndexAtPos(50.0, 50.0, 50.0, 50.0)

        assertThat(result).isEqualTo(-1)
    }

    @Test
    fun `textPageGetCharIndexAtPos exception handling`() {
        // Verify that textPageGetCharIndexAtPos returns -1 and logs error if native code throws an exception.
        every { mockNativeTextPage.textGetCharIndexAtPos(any(), any(), any(), any(), any()) } throws Exception()

        val result = pdfTextPage.textPageGetCharIndexAtPos(50.0, 50.0, 50.0, 50.0)

        assertThat(result).isEqualTo(-1)
    }

    @Test
    fun `textPageCountRects exception handling`() {
        // Verify that textPageCountRects returns -1 and logs error upon native exception.
        every { mockNativeTextPage.textCountRects(any(), any(), any()) } throws Exception()

        val result = pdfTextPage.textPageCountRects(0, 100)

        assertThat(result).isEqualTo(-1)
    }
}

class PdfTextPageClosedExceptionTest : PdfTextPageBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeTextPage.closeTextPage(any()) } just runs
        pdfTextPage.close() // 2->1
        pdfTextPage.close() // 1->0 (Closed)
    }
}

class PdfTextPageClosedIgnoreTest : PdfTextPageBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.IGNORE

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeTextPage.closeTextPage(any()) } just runs
        pdfTextPage.close()
        pdfTextPage.close()
    }
}

class PdfTextPageDocClosedTest : PdfTextPageBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU.close()
    }
}
