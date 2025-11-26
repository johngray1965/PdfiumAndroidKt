package io.legere.pdfiumandroid.unlocked

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.base.BasePDFTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfTextPageUClosedDocumentNotHandledTest : BasePDFTest() {
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

        pdfDocument.close()
    }

    @After
    fun tearDown() {
        try {
            pdfTextPage.close()
            pdfPage.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun textPageCountChars() {
        pdfTextPage.textPageCountChars()
    }

    @Test(expected = IllegalStateException::class)
    fun textPageGetTextLegacy() {
        pdfTextPage.textPageGetTextLegacy(0, 100)
    }

    @Test(expected = IllegalStateException::class)
    fun textPageGetText() {
        pdfTextPage.textPageGetText(0, 100)
    }

    @Test(expected = IllegalStateException::class)
    fun textPageGetUnicode() {
        pdfTextPage.textPageGetUnicode(0)
    }

    @Test(expected = IllegalStateException::class)
    fun textPageGetCharBox() {
        pdfTextPage.textPageGetCharBox(0)
    }

    @Test(expected = IllegalStateException::class)
    fun textPageGetCharIndexAtPos() {
        val characterToLookup = 0
        pdfTextPage.textPageGetCharBox(characterToLookup)
    }

    @Test(expected = IllegalStateException::class)
    fun textPageCountRects() {
        pdfTextPage.textPageCountRects(0, 100)
    }

    @Test(expected = IllegalStateException::class)
    fun textPageGetRect() {
        pdfTextPage.textPageGetRect(0)
    }

    @Test(expected = IllegalStateException::class)
    fun textPageGetRectsForRanges() {
        pdfTextPage.textPageGetRectsForRanges(intArrayOf(0, 100))
    }

    @Test(expected = IllegalStateException::class)
    fun textPageGetBoundedText() {
        pdfTextPage.textPageGetBoundedText(RectF(0f, 97f, 100f, 100f), 100)
    }

    @Test(expected = IllegalStateException::class)
    fun getFontSize() {
        pdfTextPage.getFontSize(0)
    }

    @Test(expected = IllegalStateException::class)
    fun findStart() {
        val findWhat = "children's"
        val startIndex = 0
        pdfTextPage.findStart(findWhat, emptySet(), startIndex)
    }

    @Test(expected = IllegalStateException::class)
    fun loadWebLink() {
    }
}
