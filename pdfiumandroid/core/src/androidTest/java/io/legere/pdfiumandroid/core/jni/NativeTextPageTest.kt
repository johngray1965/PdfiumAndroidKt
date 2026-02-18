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

package io.legere.pdfiumandroid.core.jni

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.WordRangeRect
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.core.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.core.unlocked.PdfPageU
import io.legere.pdfiumandroid.core.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import kotlin.time.measureTime

@RunWith(AndroidJUnit4::class)
class NativeTextPageTest : BasePDFTest() {
    private val nativeTextPage = defaultNativeFactory.getNativeTextPage()
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private lateinit var pdfTextPage: PdfTextPageU
    private var pdfBytes: ByteArray? = null

    private var pageTextPtr: Long = 0

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        pdfPage = pdfDocument.openPage(0)!!
        pdfTextPage = pdfPage.openTextPage()
        pageTextPtr = pdfTextPage.pagePtr
    }

    @After
    fun tearDown() {
        pdfTextPage.close()
        pdfPage.close()
        pdfDocument.close()
    }

    @Test
    fun textCountChars() {
        val charCount = nativeTextPage.textCountChars(pageTextPtr)
        Truth.assertThat(charCount).isEqualTo(3468)
    }

    @Test
    fun textGetCharBox() {
        val rect = nativeTextPage.textGetCharBox(pageTextPtr, 0)

        Truth.assertThat(rect).isEqualTo(
            doubleArrayOf(
                90.31441497802734,
                103.44171142578125,
                699.12060546875,
                715.3187255859375,
            ),
        )
    }

    @Test
    fun textGetRect() {
        val rect = nativeTextPage.textGetRect(pageTextPtr, 0)
        Truth.assertThat(rect).isEqualTo(floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f))
    }

    fun findWordRanges(text: String): List<Pair<Int, Int>> {
        val boundaries = Regex("\\b").findAll(text).map { it.range.first }.toMutableList()
        boundaries.add(text.length)
        return boundaries.zipWithNext { start, end -> Pair(start, end - start) }.filter { it.first < text.length }
    }

    @Test
    fun textGetRects() {
        val length =
            nativeTextPage.textCountChars(pageTextPtr)
        if (length > 0) {
            val bytes = ByteArray(length * 2)
//            val r =
            nativeTextPage.textGetTextByteArray(
                pageTextPtr,
                0,
                length,
                bytes,
            )

            val pageText =
                String(bytes, StandardCharsets.UTF_16LE)
            val wordBoundaries = findWordRanges(pageText)
            val wordRangesArray =
                wordBoundaries
                    .flatMap { listOf(it.first, it.second) }
                    .toIntArray()
            val iterations = 100
            val time =
                measureTime {
                    repeat(iterations) {
                        val data = nativeTextPage.textGetRects(pageTextPtr, wordRangesArray)
                        val result =
                            data?.let {
                                val wordRangeRects = mutableListOf<WordRangeRect>()
                                for (i in data.indices step 6) {
                                    val r = PdfRectF(data[i + 0], data[i + 1], data[i + 2], data[i + 3])
                                    val rangeStart = data[i + 4].toInt()
                                    val rangeLength = data[i + 5].toInt()
                                    WordRangeRect(rangeStart, rangeLength, r).let {
                                        wordRangeRects.add(it)
                                    }
                                }
                                wordRangeRects
                            }

                        Truth.assertThat(result).isNotNull()
                        Truth.assertThat(result?.size).isEqualTo(1238)
                    }
                }
            val averageDuration = (time / iterations)
            println("Total Time: $time, Average Time: $averageDuration")
        }
    }

    @Test
    fun textGetBoundedText() {
        val length = 100
        val buf = ShortArray(length + 1)
        val r =
            nativeTextPage.textGetBoundedText(
                pageTextPtr,
                0.0,
                97.0,
                100.0,
                100.0,
                buf,
            )
        val bytes = ByteArray((r - 1) * 2)
        val bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until r - 1) {
            val s = buf[i]
            bb.putShort(s)
        }
        val text = String(bytes, StandardCharsets.UTF_16LE)
        assertThat(text).isEqualTo("Do")
    }

    @Test
    fun findStart() {
        val result = nativeTextPage.findStart(pageTextPtr, "children's", 0, 0)
        Truth.assertThat(result).isNotNull()
    }

    @Test
    fun textGetCharIndexAtPos() {
        val result = nativeTextPage.textGetCharIndexAtPos(pageTextPtr, 50.0, 50.0, 50.0, 50.0)
        Truth.assertThat(result).isEqualTo(-1)
    }

    @Test
    fun textGetText() {
        val length = 100
        val buf = ShortArray(length + 1)
        val r =
            nativeTextPage.textGetText(
                pageTextPtr,
                0,
                length,
                buf,
            )

        val result =
            if (r <= 0) {
                ""
            } else {
                val bytes = ByteArray((r - 1) * 2)
                val bb = ByteBuffer.wrap(bytes)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until r - 1) {
                    val s = buf[i]
                    bb.putShort(s)
                }
                String(bytes, StandardCharsets.UTF_16LE)
            }
        assertThat(result).startsWith("The 50 Best Videos For Kids")
    }

    @Test
    fun textGetUnicode() {
        val result = nativeTextPage.textGetUnicode(pageTextPtr, 0).toChar()
        Truth.assertThat(result).isEqualTo('T')
    }

    @Test
    fun textCountRects() {
        val result = nativeTextPage.textCountRects(pageTextPtr, 0, 100)
        Truth.assertThat(result).isEqualTo(4)
    }

    @Test
    fun getFontSize() {
        val fontSize = nativeTextPage.getFontSize(pageTextPtr, 0)

        // We get 0, but that doesn't seem right
        assertThat(fontSize).isEqualTo(22.559999465942383)
    }
}
