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

package io.legere.pdfiumandroid.core.unlocked

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.legere.pdfiumandroid.core.jni.NativePageLink
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
        PdfiumCoreU.resetForTesting()
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
