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
import io.legere.pdfiumandroid.util.pdfiumConfig
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class PdfTextPageUTest {
    @get:Rule
    val mockkRule: MockKRule = MockKRule(this)

    @MockK
    lateinit var mockNativeFactory: NativeFactory

    @MockK
    lateinit var mockNativeDocument: NativeDocument

    @MockK
    lateinit var mockNativePage: NativePage

    @MockK
    lateinit var mockNativeTextPage: NativeTextPage

    lateinit var pdfDocumentU: PdfDocumentU

    lateinit var pdfPage: PdfPageU

    lateinit var pdfTextPage: PdfTextPageU

    @Before
    fun setUp() {
        pdfiumConfig =
            io.legere.pdfiumandroid.util
                .Config(
                    alreadyClosedBehavior = AlreadyClosedBehavior.EXCEPTION,
                )
        every { mockNativeFactory.getNativeDocument() } returns mockNativeDocument
        every { mockNativeFactory.getNativePage() } returns mockNativePage
        every { mockNativeFactory.getNativeTextPage() } returns mockNativeTextPage
        pdfDocumentU = PdfDocumentU(0, mockNativeFactory)
        pdfPage =
            PdfPageU(
                pdfDocumentU,
                0,
                0,
                pageMap = mutableMapOf(0 to PdfDocument.PageCount(0, 2)),
                nativeFactory = mockNativeFactory,
            )
        pdfTextPage =
            PdfTextPageU(
                pdfDocumentU,
                0,
                0,
                pageMap = mutableMapOf(0 to PdfDocument.PageCount(0, 2)),
                nativeFactory = mockNativeFactory,
            )
    }

    @Test
    fun `textPageCountChars returns valid count`() {
        // Verify that textPageCountChars returns a non-negative integer representing the character count when the page is open and contains text.
        every { mockNativeTextPage.textCountChars(any()) } returns 10
        val result = pdfTextPage.textPageCountChars()
        assertThat(result).isEqualTo(10)
    }

    @Test
    fun `textPageGetTextLegacy valid range`() {
        // Verify that textPageGetTextLegacy returns the correct string for a valid startIndex and length.
        val expectedString = "Hello World"
        val length = expectedString.length

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
        val result = pdfTextPage.textPageGetTextLegacy(0, length)

        assertThat(result).isEqualTo(expectedString)
    }

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
    fun `textPageGetText valid range`() {
        // Verify that textPageGetText returns the correct string using the byte array approach for a valid startIndex and length.
        val expectedString = "Hello World"
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

    @Test
    fun `textPageGetUnicode valid index`() {
        // Verify that textPageGetUnicode returns the correct Char for a valid index.
        // Verify that textPageGetText returns the correct string using the byte array approach for a valid startIndex and length.
        val expectedString = "Hello World"

        every { mockNativeTextPage.textGetUnicode(any(), any()) } answers {
            val index = secondArg<Int>()

            expectedString[index].code
        }
        val result = pdfTextPage.textPageGetUnicode(0)
        assertThat(result).isEqualTo(expectedString[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `textPageGetUnicode invalid index`() {
        // Verify behavior when an invalid index is provided (likely returns 0/null char or throws depending on native impl).
        every { mockNativeTextPage.textGetUnicode(any(), any()) } throws IllegalArgumentException()
        val result = pdfTextPage.textPageGetUnicode(0)
        assertThat(result).isNull()
    }

    @Test
    fun `textPageGetCharBox valid index`() {
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
    fun `textPageGetCharIndexAtPos valid coordinates`() {
        // Verify that textPageGetCharIndexAtPos returns the correct character index for valid x, y coordinates within a character's bounding box.
        every { mockNativeTextPage.textGetCharIndexAtPos(any(), any(), any(), any(), any()) } returns 10

        val result = pdfTextPage.textPageGetCharIndexAtPos(50.0, 50.0, 50.0, 50.0)

        assertThat(result).isEqualTo(10)
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
    fun `textPageCountRects valid range`() {
        // Verify that textPageCountRects returns the correct number of rectangles for a text range spanning multiple lines or blocks.
        val expected = 4
        every { mockNativeTextPage.textCountRects(any(), any(), any()) } returns expected

        val result = pdfTextPage.textPageCountRects(0, 100)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `textPageCountRects exception handling`() {
        // Verify that textPageCountRects returns -1 and logs error upon native exception.
        every { mockNativeTextPage.textCountRects(any(), any(), any()) } throws Exception()

        val result = pdfTextPage.textPageCountRects(0, 100)

        assertThat(result).isEqualTo(-1)
    }

    @Test
    fun `textPageGetRect valid index`() {
        // Verify that textPageGetRect returns a valid RectF for a known rectangle index.
        every { mockNativeTextPage.textGetRect(any(), any()) } returns
            doubleArrayOf(
                90.314415,
                715.3187,
                103.44171,
                699.1206,
            )

        val result = pdfTextPage.textPageGetRect(0)

        assertThat(result).isEqualTo(RectF(90.314415f, 715.3187f, 103.44171f, 699.1206f))
    }

    @Test
    fun `textPageGetRect invalid index`() {
        // Verify behavior (likely exception caught and null returned) when requesting a non-existent rect index.
        every { mockNativeTextPage.textGetRect(any(), any()) } throws Exception()

        val result = pdfTextPage.textPageGetRect(0)

        assertThat(result).isEqualTo(null)
    }

    @Test
    fun `textPageGetRectsForRanges valid input`() {
        // Verify that textPageGetRectsForRanges returns a list of WordRangeRects populated with correct coordinates and range data for valid input array.
        // TODO implement test
    }

    @Test
    fun `textPageGetRectsForRanges null result`() {
        // Verify that textPageGetRectsForRanges returns null if the native method returns null.
        // TODO implement test
    }

    @Test
    fun `textPageGetRectsForRanges parsing logic`() {
        // Verify that the loop correctly parses the flat float/int array from native code into WordRangeRect objects using the correct offsets.
        // TODO implement test
    }

    @Test
    fun `textPageGetBoundedText valid rect`() {
        // Verify that textPageGetBoundedText returns the correct string contained within the specified RectF.
        // TODO implement test
    }

    @Test
    fun `textPageGetBoundedText buffer construction`() {
        // Verify that the string construction from ShortArray/ByteArray correctly handles Little Endian ordering.
        // TODO implement test
    }

    @Test
    fun `getFontSize valid index`() {
        // Verify that getFontSize returns the correct font size double value for a specific character index.
        // TODO implement test
    }

    @Test
    fun `findStart valid search`() {
        // Verify that findStart returns a FindResultU object when a search term is found.
        // TODO implement test
    }

    @Test
    fun `findStart flags integration`() {
        // Verify that the `flags` set is correctly bitwise-ORed and passed to the native function.
        // TODO implement test
    }

    @Test
    fun `loadWebLink execution`() {
        // Verify that loadWebLink returns a valid PdfPageLinkU instance wrapped around the native pointer.
        // TODO implement test
    }

    @Test
    fun `close reference counting decrement`() {
        // Verify that calling close() on a page with count > 1 decrements the count in pageMap and does NOT close the native page.
        // TODO implement test
    }

    @Test
    fun `close final release`() {
        // Verify that calling close() on a page with count == 1 removes it from pageMap, sets isClosed to true, and calls native closeTextPage.
        // TODO implement test
    }

    @Test
    fun `close idempotent`() {
        // Verify that calling close() on an already closed page (isClosed=true) does nothing.
        // TODO implement test
    }
}
