package io.legere.pdfiumandroid

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val charCount = textPage.textPageCountChars()

                assertThat(charCount).isEqualTo(3468)
            }
        }
    }

    @Test
    fun textPageGetText() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val text = textPage.textPageGetText(0, 100)

                assertThat(text?.length).isEqualTo(100)
            }
        }
    }

    @Test
    fun textPageGetUnicode() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val char = textPage.textPageGetUnicode(0)
                assertThat(char).isEqualTo('T')
            }
        }
    }

    @Test
    fun textPageGetCharBox() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val rect = textPage.textPageGetCharBox(0)

                assertThat(rect).isEqualTo(RectF(90.314415f, 715.3187f, 103.44171f, 699.1206f))
            }
        }
    }

    @Test
    fun textPageGetCharIndexAtPos() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val characterToLookup = 0
                val rect = textPage.textPageGetCharBox(characterToLookup)

                val pos =
                    textPage.textPageGetCharIndexAtPos(
                        rect?.centerX()?.toDouble() ?: 0.0,
                        rect?.centerY()?.toDouble() ?: 0.0,
                        // Shouldn't need much since we're in the middle of the rect
                        1.0,
                        1.0,
                    )

                assertThat(pos).isEqualTo(characterToLookup)
            }
        }
    }

    @Test
    fun textPageCountRects() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val rectCount = textPage.textPageCountRects(0, 100)

                assertThat(rectCount).isEqualTo(4)
            }
        }
    }

    @Test
    fun textPageGetRect() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val rect = textPage.textPageGetRect(0)

                assertThat(rect).isEqualTo(RectF(0f, 0f, 0f, 0f))
            }
        }
    }

    @Test
    fun textPageGetBoundedText() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val text = textPage.textPageGetBoundedText(RectF(0f, 97f, 100f, 100f), 100)

                assertThat(text).isEqualTo("Do")
            }
        }
    }

    @Test
    fun getFontSize() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val fontSize = textPage.getFontSize(0)

                // We get 0, but that doesn't seem right
                assertThat(fontSize).isEqualTo(22.559999465942383)
            }
        }
    }

    @Test
    fun findStart() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                val findWhat = "children's"
                val startIndex = 0
                textPage.findStart(findWhat, emptySet(), startIndex)?.use { findHandle ->
                    var result = findHandle.findNext()
                    assertThat(result).isTrue()
                    var index = findHandle.getSchResultIndex()
                    var count = findHandle.getSchCount()
                    var text = textPage.textPageGetText(index, count)
                    assertThat(index).isEqualTo(1525)
                    assertThat(count).isEqualTo(10)
                    assertThat(text).isEqualTo(findWhat)
                    result = findHandle.findNext()
                    assertThat(result).isTrue()
                    index = findHandle.getSchResultIndex()
                    count = findHandle.getSchCount()
                    text = textPage.textPageGetText(index, count)
                    assertThat(index).isEqualTo(2761)
                    assertThat(count).isEqualTo(10)
                    assertThat(text).isEqualTo(findWhat)
                    result = findHandle.findNext()
                    assertThat(result).isFalse()
                }
            }
        }
    }

    @Test(expected = IllegalStateException::class)
    fun close() {
        var pageAfterClose: PdfTextPage?
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                pageAfterClose = textPage
            }
            pageAfterClose!!.textPageCountChars()
        }
    }

    @Test
    fun getDoc() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                assertThat(textPage.doc).isNotNull()
            }
        }
    }

    @Test
    fun getPageIndex() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                assertThat(textPage.pageIndex).isEqualTo(0)
            }
        }
    }

    @Test
    fun getPagePtr() {
        pdfDocument.openPage(0).use { page ->
            page.openTextPage().use { textPage ->
                assertThat(textPage.pagePtr).isNotNull()
            }
        }
    }
}
