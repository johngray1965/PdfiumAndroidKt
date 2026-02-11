package io.legere.pdfiumandroid.unlocked

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.jni.NativeDocument
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.NativePage
import io.legere.pdfiumandroid.jni.NativeTextPage
import io.legere.pdfiumandroid.unlocked.testing.ClosableTestContext
import io.legere.pdfiumandroid.unlocked.testing.closableTest
import io.legere.pdfiumandroid.util.AlreadyClosedBehavior
import io.legere.pdfiumandroid.util.Config
import io.legere.pdfiumandroid.util.pdfiumConfig
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config as RoboConfig

// =================================================================================================
// 1. Abstract Base Class (JUnit 4 Style)
// =================================================================================================

@RunWith(AndroidJUnit4::class)
@RoboConfig(manifest = RoboConfig.NONE)
abstract class PdfTextPageBaseTest : ClosableTestContext {
    @get:Rule
    val mockkRule: MockKRule = MockKRule(this)

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

    override fun shouldThrowException() = getBehavior() == AlreadyClosedBehavior.EXCEPTION && isStateClosed()

    override fun shouldReturnDefault() = getBehavior() == AlreadyClosedBehavior.IGNORE && isStateClosed()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        PdfiumCoreU.resetForTesting()

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

    // =============================================================================================
    // TESTS
    // =============================================================================================

    @Test
    fun textPageCountChars_behavior() =
        closableTest {
            apiCall = {
                pdfTextPage.textPageCountChars()
            }

            verifyHappy {
                assertThat(it).isEqualTo(10)
            }
            verifyDefault {
                assertThat(it).isEqualTo(-1)
            }
        }

    @Test
    fun textPageGetTextLegacy_validRange() =
        closableTest {
            val expectedString = "Hello World"
            val length = expectedString.length
            setupHappy {
                every { mockNativeTextPage.textGetText(any(), any(), any(), any()) } answers {
                    val buffer = arg<ShortArray>(3)
                    for (i in expectedString.indices) {
                        if (i < buffer.size) buffer[i] = expectedString[i].code.toShort()
                    }
                    length + 1
                }
            }
            apiCall = {
                pdfTextPage.textPageGetTextLegacy(0, length)
            }

            verifyHappy {
                assertThat(it).isEqualTo(expectedString)
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun textPageGetText_validRange() =
        closableTest {
            val expectedString = "Hello World"
            val length = expectedString.length
            setupHappy {
                every { mockNativeTextPage.textGetTextString(any(), any(), any()) } returns expectedString
            }
            apiCall = {
                pdfTextPage.textPageGetText(0, length)
            }

            verifyHappy {
                assertThat(it).isEqualTo(expectedString)
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun textPageGetUnicode_validIndex() =
        closableTest {
            val expectedChar = 'H'
            setupHappy {
                every { mockNativeTextPage.textGetUnicode(any(), any()) } returns expectedChar.code
            }
            apiCall = {
                pdfTextPage.textPageGetUnicode(0)
            }

            verifyHappy {
                assertThat(it).isEqualTo(expectedChar)
            }
            verifyDefault {
                assertThat(it).isEqualTo(Char.MIN_VALUE)
            }
        }

    @Test
    fun textPageGetCharBox_validIndex() =
        closableTest {
            setupHappy {
                every { mockNativeTextPage.textGetCharBox(any(), any()) } returns
                    doubleArrayOf(90.0, 100.0, 600.0, 700.0)
            }
            apiCall = {
                pdfTextPage.textPageGetCharBox(0)
            }

            verifyHappy {
                assertThat(it).isEqualTo(RectF(90.0f, 700.0f, 100.0f, 600.0f))
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun textPageGetCharIndexAtPos_validCoordinates() =
        closableTest {
            setupHappy {
                every { mockNativeTextPage.textGetCharIndexAtPos(any(), any(), any(), any(), any()) } returns 10
            }
            apiCall = {
                pdfTextPage.textPageGetCharIndexAtPos(50.0, 50.0, 50.0, 50.0)
            }

            verifyHappy {
                assertThat(it).isEqualTo(10)
            }
            verifyDefault {
                assertThat(it).isEqualTo(-1)
            }
        }

    @Test
    fun textPageCountRects_validRange() =
        closableTest {
            setupHappy {
                every { mockNativeTextPage.textCountRects(any(), any(), any()) } returns 4
            }
            apiCall = {
                pdfTextPage.textPageCountRects(0, 100)
            }

            verifyHappy {
                assertThat(it).isEqualTo(4)
            }
            verifyDefault {
                assertThat(it).isEqualTo(-1)
            }
        }

    @Test
    fun textPageGetRect_validIndex() =
        closableTest {
            setupHappy {
                every { mockNativeTextPage.textGetRect(any(), any()) } returns
                    floatArrayOf(
                        90.0f,
                        700.0f,
                        100.0f,
                        600.0f,
                    )
            }
            apiCall = {
                pdfTextPage.textPageGetRect(0)
            }

            verifyHappy {
                assertThat(it).isEqualTo(RectF(90.0f, 700.0f, 100.0f, 600.0f))
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun textPageGetRectsForRanges_validInput() =
        closableTest {
            setupHappy {
                val mockRects = floatArrayOf(10.0f, 20.0f, 30.0f, 40.0f, 50.0f, 60.0f)
                every { mockNativeTextPage.textGetRects(any(), any()) } returns mockRects
            }
            apiCall = {
                pdfTextPage.textPageGetRectsForRanges(intArrayOf(0))
            }

            verifyHappy {
                assertThat(it).hasSize(1)
                assertThat(it!![0].rect).isEqualTo(RectF(10f, 20f, 30f, 40f))
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun textPageGetRectsForRanges_invalidInput() =
        closableTest {
            setupHappy {
                every { mockNativeTextPage.textGetRects(any(), any()) } returns null
            }
            apiCall = {
                pdfTextPage.textPageGetRectsForRanges(intArrayOf(0))
            }

            verifyHappy {
                assertThat(it).isNull()
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun textPageGetBoundedText_validRect() =
        closableTest {
            val expected = "Bounded"
            setupHappy {
                every { mockNativeTextPage.textGetBoundedText(any(), any(), any(), any(), any(), any()) } answers {
                    val buffer = arg<ShortArray>(5)
                    expected.forEachIndexed { i, c -> buffer[i] = c.code.toShort() }
                    expected.length + 1
                }
            }
            apiCall = {
                pdfTextPage.textPageGetBoundedText(RectF(0f, 0f, 100f, 100f), 10)
            }

            verifyHappy {
                assertThat(it).isEqualTo(expected)
            }
            verifyDefault {
                assertThat(it).isNull()
            }
        }

    @Test
    fun getFontSize_validIndex() =
        closableTest {
            setupHappy {
                every { mockNativeTextPage.getFontSize(any(), 5) } returns 12.5
            }
            apiCall = {
                pdfTextPage.getFontSize(5)
            }

            verifyHappy {
                assertThat(it).isEqualTo(12.5)
            }
            verifyDefault {
                assertThat(it).isEqualTo(0.0)
            }
        }

    @Test
    fun findStart_validSearch() =
        closableTest {
            setupHappy {
                every { mockNativeTextPage.findStart(any(), any(), any(), any()) } returns 999L
            }
            apiCall = {
                pdfTextPage.findStart("query", emptySet(), 0)
            }

            verifyHappy {
                assertThat(it).isNotNull()
                verify(exactly = 1) { mockNativeTextPage.findStart(any(), any(), any(), any()) }
            }
            verifyDefault {
                assertThat(it).isNull()
                verify(exactly = 0) { mockNativeTextPage.findStart(any(), any(), any(), any()) }
            }
        }

    @Test
    fun loadWebLink_execution() =
        closableTest {
            setupHappy {
                every { mockNativeTextPage.loadWebLink(any()) } returns 888L
            }
            apiCall = {
                pdfTextPage.loadWebLink()
            }

            verifyHappy {
                assertThat(it).isNotNull()
                verify(exactly = 1) { mockNativeTextPage.loadWebLink(any()) }
            }
            verifyDefault {
                assertThat(it).isNull()
                verify(exactly = 0) { mockNativeTextPage.loadWebLink(any()) }
            }
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

    override fun setupClosedState() {
        // empty
    }

    @Test
    fun `textPageGetTextLegacy empty result`() {
        val expectedString = ""
        val length = 0

        every { mockNativeTextPage.textGetText(any(), any(), any(), any()) } answers {
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
        assertThat(result).isNull()
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

class PdfTextPageHappyTest : PdfTextPageBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.EXCEPTION

    override fun isStateClosed() = false

    override fun setupClosedState() {
        every { mockNativeTextPage.closeTextPage(any()) } just runs
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

class PdfTextDocClosedIgnoreTest : PdfTextPageBaseTest() {
    override fun getBehavior() = AlreadyClosedBehavior.IGNORE

    override fun isStateClosed() = true

    override fun setupClosedState() {
        every { mockNativeDocument.closeDocument(any()) } just runs
        pdfDocumentU.close()
    }
}
