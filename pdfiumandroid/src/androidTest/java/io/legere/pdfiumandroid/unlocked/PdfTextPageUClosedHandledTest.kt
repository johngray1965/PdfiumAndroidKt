package io.legere.pdfiumandroid.unlocked

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.util.AlreadyClosedBehavior
import io.legere.pdfiumandroid.util.Config
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("Migrating to non-instrumented tests")
class PdfTextPageUClosedHandledTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private lateinit var pdfPage: PdfPageU
    private lateinit var pdfTextPage: PdfTextPageU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument =
            PdfiumCoreU(
                config =
                    Config(
                        alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE,
                    ),
            ).newDocument(pdfBytes)
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
            println(e.message)
        }
    }

    @Test
    fun textPageCountChars() {
        pdfTextPage.textPageCountChars()
    }

    @Test
    fun textPageGetTextLegacy() {
        pdfTextPage.textPageGetTextLegacy(0, 100)
    }

    @Test
    fun textPageGetText() {
        pdfTextPage.textPageGetText(0, 100)
    }

    @Test
    fun textPageGetUnicode() {
        pdfTextPage.textPageGetUnicode(0)
    }

    @Test
    fun textPageGetCharBox() {
        pdfTextPage.textPageGetCharBox(0)
    }

    @Test
    fun textPageGetCharIndexAtPos() {
        val characterToLookup = 0
        pdfTextPage.textPageGetCharBox(characterToLookup)
    }

    @Test
    fun textPageCountRects() {
        pdfTextPage.textPageCountRects(0, 100)
    }

    @Test
    fun textPageGetRect() {
        pdfTextPage.textPageGetRect(0)
    }

    @Test
    fun textPageGetRectsForRanges() {
        pdfTextPage.textPageGetRectsForRanges(intArrayOf(0, 100))
    }

    @Test
    fun textPageGetBoundedText() {
        pdfTextPage.textPageGetBoundedText(RectF(0f, 97f, 100f, 100f), 100)
    }

    @Test
    fun getFontSize() {
        pdfTextPage.getFontSize(0)
    }

    @Test
    fun findStart() {
        val findWhat = "children's"
        val startIndex = 0
        pdfTextPage.findStart(findWhat, emptySet(), startIndex)
    }
}
