package io.legere.pdfiumandroid.unlocked

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.WordRangeRect
import io.legere.pdfiumandroid.base.BasePDFTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfTextPageUTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private lateinit var pdfTextPage: PdfTextPageU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        pdfPage = pdfDocument.openPage(0)
        pdfTextPage = pdfPage.openTextPage()
    }

    @After
    fun tearDown() {
        pdfTextPage.close()
        pdfPage.close()
        pdfDocument.close()
    }

    @Test
    fun textPageCountChars() {
        val charCount = pdfTextPage.textPageCountChars()

        Truth.assertThat(charCount).isEqualTo(3468)
    }

    @Test
    fun textPageGetTextLegacy() {
        val text = pdfTextPage.textPageGetTextLegacy(0, 100)

        Truth.assertThat(text?.length).isEqualTo(100)
    }

    @Test
    fun textPageGetText() {
        val text = pdfTextPage.textPageGetText(0, 100)

        Truth.assertThat(text?.length).isEqualTo(100)
    }

    @Test
    fun textPageGetUnicode() {
        val char = pdfTextPage.textPageGetUnicode(0)
        Truth.assertThat(char).isEqualTo('T')
    }

    @Test
    fun textPageGetCharBox() {
        val rect = pdfTextPage.textPageGetCharBox(0)

        Truth.assertThat(rect).isEqualTo(RectF(90.314415f, 715.3187f, 103.44171f, 699.1206f))
    }

    @Test
    fun textPageGetCharIndexAtPos() {
        val characterToLookup = 0
        val rect = pdfTextPage.textPageGetCharBox(characterToLookup)

        val pos =
            pdfTextPage.textPageGetCharIndexAtPos(
                rect?.centerX()?.toDouble() ?: 0.0,
                rect?.centerY()?.toDouble() ?: 0.0,
                // Shouldn't need much since we're in the middle of the rect
                1.0,
                1.0,
            )

        Truth.assertThat(pos).isEqualTo(characterToLookup)
    }

    @Test
    fun textPageCountRects() {
        val rectCount = pdfTextPage.textPageCountRects(0, 100)

        Truth.assertThat(rectCount).isEqualTo(4)
    }

    @Test
    fun textPageGetRect() {
        val rect = pdfTextPage.textPageGetRect(0)

        Truth.assertThat(rect).isEqualTo(RectF(0f, 0f, 0f, 0f))
    }

    @Test
    fun textPageGetRectsForRanges() {
        val text = pdfTextPage.textPageGetRectsForRanges(intArrayOf(0, 100))

        val expected =
            listOf(
                WordRangeRect(rangeStart = 0, rangeLength = 100, rect = RectF(90.314415f, 715.3187f, 382.33905f, 698.71454f)),
                WordRangeRect(rangeStart = 0, rangeLength = 100, rect = RectF(90.39967f, 692.642f, 274.37787f, 683.92334f)),
                WordRangeRect(rangeStart = 0, rangeLength = 100, rect = RectF(90.672745f, 682.1487f, 146.53024f, 673.65796f)),
                WordRangeRect(
                    rangeStart = 0,
                    rangeLength = 100,
                    rect = RectF(90.22673f, 647.3405f, 100.42227f, 638.6405f),
                ),
            )

        Truth.assertThat(text).isEqualTo(expected)
    }

    @Test
    fun textPageGetBoundedText() {
        val text = pdfTextPage.textPageGetBoundedText(RectF(0f, 97f, 100f, 100f), 100)

        Truth.assertThat(text).isEqualTo("Do")
    }

    @Test
    fun getFontSize() {
        val fontSize = pdfTextPage.getFontSize(0)

        // We get 0, but that doesn't seem right
        Truth.assertThat(fontSize).isEqualTo(22.559999465942383)
    }

    @Test
    fun findStart() {
        val findWhat = "children's"
        val startIndex = 0
        pdfTextPage.findStart(findWhat, emptySet(), startIndex)?.use { findHandle ->
            var result = findHandle.findNext()
            Truth.assertThat(result).isTrue()
            var index = findHandle.getSchResultIndex()
            var count = findHandle.getSchCount()
            var text = pdfTextPage.textPageGetText(index, count)
            Truth.assertThat(index).isEqualTo(1525)
            Truth.assertThat(count).isEqualTo(10)
            Truth.assertThat(text).isEqualTo(findWhat)
            result = findHandle.findNext()
            Truth.assertThat(result).isTrue()
            index = findHandle.getSchResultIndex()
            count = findHandle.getSchCount()
            text = pdfTextPage.textPageGetText(index, count)
            Truth.assertThat(index).isEqualTo(2761)
            Truth.assertThat(count).isEqualTo(10)
            Truth.assertThat(text).isEqualTo(findWhat)
            result = findHandle.findNext()
            Truth.assertThat(result).isFalse()
        }
    }

    @Test
    fun loadWebLink() {
    }
}
