package io.legere.pdfiumandroid

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PdfTextPageTest : BasePDFTest() {

    private lateinit var pdfDocument: PdfDocument
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        TestCase.assertNotNull(pdfBytes)

        pdfDocument = PdfiumCore().newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun textPageCountChars() {
        pdfDocument.openTextPage(0).use { textPage ->
            val charCount = textPage.textPageCountChars()

            assertThat(charCount).isEqualTo(3468)
        }
    }

    @Test
    fun textPageGetText() {
        pdfDocument.openTextPage(0).use { textPage ->
            val text = textPage.textPageGetText(0, 100)

            assertThat(text?.length).isEqualTo(100)
        }
    }

    @Test
    fun textPageGetUnicode() {
        pdfDocument.openTextPage(0).use { textPage ->
            val char = textPage.textPageGetUnicode(0)

            assertThat(char).isEqualTo('T')
        }
    }

    @Test
    fun textPageGetCharBox() {
        pdfDocument.openTextPage(0).use { textPage ->
            val rect = textPage.textPageGetCharBox(0)

            assertThat(rect).isEqualTo( RectF(90.314415f, 715.3187f, 103.44171f, 699.1206f))
        }
    }

    @Test
    fun textPageGetCharIndexAtPos() {
        pdfDocument.openTextPage(0).use { textPage ->
            val characterToLookup = 0
            val rect = textPage.textPageGetCharBox(characterToLookup)

            val pos = textPage.textPageGetCharIndexAtPos(
                rect?.centerX()?.toDouble() ?: 0.0,
                rect?.centerY()?.toDouble() ?: 0.0,
                1.0, // Shouldn't need much since we're in the middle of the rect
                1.0
            )

            assertThat(pos).isEqualTo(characterToLookup)
        }
    }

    @Test
    fun textPageCountRects() {
        pdfDocument.openTextPage(0).use { textPage ->
            val rectCount = textPage.textPageCountRects(0, 100)

            assertThat(rectCount).isEqualTo( 4)
        }
    }

    @Test
    fun textPageGetRect() {
        pdfDocument.openTextPage(0).use { textPage ->
            val rect = textPage.textPageGetRect(0)

            assertThat(rect).isEqualTo( RectF(0f, 0f, 0f, 0f))
        }
    }

    @Test
    fun textPageGetBoundedText() {
        pdfDocument.openTextPage(0).use { textPage ->
            val text = textPage.textPageGetBoundedText(RectF(0f, 97f, 100f, 100f), 100)

            assertThat(text).isEqualTo( "Do")
        }
    }

    @Test
    fun getFontSize() {
        pdfDocument.openTextPage(0).use { textPage ->
            val fontSize = textPage.getFontSize(0)

            // We get 0, but that doesn't seem right
            assertThat(fontSize).isEqualTo(22.559999465942383)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun close() {
        var pageAfterClose: PdfTextPage?
        pdfDocument.openTextPage(0).use { textPage ->
            pageAfterClose = textPage
        }
        pageAfterClose!!.textPageCountChars()
    }

    @Test
    fun getDoc() {
        pdfDocument.openTextPage(0).use { textPage ->
            assertThat(textPage.doc).isNotNull()
        }
    }

    @Test
    fun getPageIndex() {
        pdfDocument.openTextPage(0).use { textPage ->
            assertThat(textPage.pageIndex).isEqualTo(0)
        }
    }

    @Test
    fun getPagePtr() {
        pdfDocument.openTextPage(0).use { textPage ->
            assertThat(textPage.pagePtr).isNotNull()
        }
    }
}
