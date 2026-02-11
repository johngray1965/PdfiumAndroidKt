package io.legere.pdfiumandroid.jni

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.WordRangeRect
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.unlocked.PdfPageU
import io.legere.pdfiumandroid.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
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
        Truth.assertThat(charCount).isEqualTo(3415)
    }

    @Test
    fun textGetCharBox() {
        val rect = nativeTextPage.textGetCharBox(pageTextPtr, 0)

        Truth.assertThat(rect).isEqualTo(
            doubleArrayOf(
                90.31439971923828,
                103.44169616699219,
                699.1204833984375,
                715.318603515625,
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
                                    val r = RectF()
                                    r.left = data[i + 0]
                                    r.top = data[i + 1]
                                    r.right = data[i + 2]
                                    r.bottom = data[i + 3]
                                    val rangeStart = data[i + 4].toInt()
                                    val rangeLength = data[i + 5].toInt()
                                    WordRangeRect(rangeStart, rangeLength, r).let {
                                        wordRangeRects.add(it)
                                    }
                                }
                                wordRangeRects
                            }

                        Truth.assertThat(result).isNotNull()
                        Truth.assertThat(result?.size).isEqualTo(1237)
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
