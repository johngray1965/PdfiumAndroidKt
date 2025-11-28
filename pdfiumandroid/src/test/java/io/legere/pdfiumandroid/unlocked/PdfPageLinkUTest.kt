package io.legere.pdfiumandroid.unlocked

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.jni.NativeFactory
import io.legere.pdfiumandroid.jni.NativePageLink
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
import org.robolectric.annotation.Config
import java.lang.NullPointerException
import kotlin.Exception

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class PdfPageLinkUTest {
    @get:Rule
    val mockkRule: MockKRule = MockKRule(this)

    lateinit var pdfPageLink: PdfPageLinkU

    @MockK lateinit var mockNativeFactory: NativeFactory

    @MockK lateinit var mockNativePageLink: NativePageLink

    @Before
    fun setUp() {
        every { mockNativeFactory.getNativePageLink() } returns mockNativePageLink
        pdfPageLink = PdfPageLinkU(124L, mockNativeFactory)
    }

    @Test
    fun countWebLinks() {
        every { mockNativePageLink.countWebLinks(any()) } returns 42
        assertThat(pdfPageLink.countWebLinks()).isEqualTo(42)
    }

    @Test
    fun getURLHappy() {
        val expectedString = "Hello World"
        val length = expectedString.length
        every { mockNativePageLink.getURL(any(), any(), any(), any()) } answers {
            val buffer = arg<ByteArray>(3)
            val count = thirdArg<Int>()
            val bytes = expectedString.toByteArray(Charsets.UTF_16LE)
            println("Buffer size: ${buffer.size}, bytes size: ${bytes.size}, count: $count, length: $length")
            bytes.copyInto(buffer, endIndex = minOf(bytes.size, length * 2))
            count + 1
        }
        val result = pdfPageLink.getURL(10, length)
        assertThat(result).isEqualTo(expectedString)
    }

    @Test
    fun getURLNegativeCount() {
        val expectedString = "Hello World"
        val length = expectedString.length
        every { mockNativePageLink.getURL(any(), any(), any(), any()) } answers {
            val buffer = arg<ByteArray>(3)
            val count = thirdArg<Int>()
            val bytes = expectedString.toByteArray(Charsets.UTF_16LE)
            println("Buffer size: ${buffer.size}, bytes size: ${bytes.size}, count: $count, length: $length")
            bytes.copyInto(buffer, endIndex = minOf(bytes.size, length * 2))
            -1
        }
        val result = pdfPageLink.getURL(10, length)
        assertThat(result).isEqualTo("")
    }

    @Test
    fun getURLNullPointerException() {
        val expectedString = "Hello World"
        val length = expectedString.length
        every { mockNativePageLink.getURL(any(), any(), any(), any()) } throws NullPointerException()
        val result = pdfPageLink.getURL(10, length)
        assertThat(result).isNull()
    }

    @Test
    fun getURLGeneralException() {
        val expectedString = "Hello World"
        val length = expectedString.length
        every { mockNativePageLink.getURL(any(), any(), any(), any()) } throws Exception()
        val result = pdfPageLink.getURL(10, length)
        assertThat(result).isNull()
    }

    @Test
    fun countRects() {
        every { mockNativePageLink.countRects(any(), any()) } returns 1
        assertThat(pdfPageLink.countRects(10)).isEqualTo(1)
    }

    @Test
    fun getRect() {
        every { mockNativePageLink.getRect(any(), any(), any()) } returns floatArrayOf(10f, 20f, 30f, 40f)
        val result = pdfPageLink.getRect(10, 20)
        assertThat(result).isEqualTo(RectF(10f, 20f, 30f, 40f))
    }

    @Test
    fun getTextRange() {
        every { mockNativePageLink.getTextRange(any(), any()) } returns intArrayOf(1, 2)
        val result = pdfPageLink.getTextRange(10)
        assertThat(result.first).isEqualTo(1)
        assertThat(result.second).isEqualTo(2)
    }

    @Test
    fun close() {
        every { mockNativePageLink.closePageLink(any()) } just runs
        pdfPageLink.close()
        verify { mockNativePageLink.closePageLink(any()) }
    }
}
