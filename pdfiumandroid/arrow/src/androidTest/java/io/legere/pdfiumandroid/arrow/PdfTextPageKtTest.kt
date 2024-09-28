package io.legere.pdfiumandroid.arrow

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.raise.either
import com.google.common.truth.Truth
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfTextPageKtTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentKtF
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() =
        runBlocking {
            pdfBytes = getPdfBytes("f01.pdf")

            TestCase.assertNotNull(pdfBytes)

            pdfDocument = PdfiumCoreKtF(Dispatchers.Unconfined).newDocument(pdfBytes).getOrNull()!!
        }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun textPageCountChars() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val charCount = textPage.textPageCountChars().bind()

                        Truth.assertThat(charCount).isEqualTo(3468)
                    }
                }
            }
        }

    @Test
    fun textPageGetText() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val text = textPage.textPageGetText(0, 100).bind()

                        Truth.assertThat(text?.length).isEqualTo(100)
                    }
                }
            }
        }

    @Test
    fun textPageGetUnicode() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val char = textPage.textPageGetUnicode(0).bind()

                        Truth.assertThat(char).isEqualTo('T')
                    }
                }
            }
        }

    @Test
    fun textPageGetCharBox() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val rect = textPage.textPageGetCharBox(0).bind()

                        Truth
                            .assertThat(rect)
                            .isEqualTo(RectF(90.314415f, 715.3187f, 103.44171f, 699.1206f))
                    }
                }
            }
        }

    @Test
    fun textPageGetCharIndexAtPos() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val characterToLookup = 0
                        val rect = textPage.textPageGetCharBox(characterToLookup).bind()

                        val pos =
                            textPage
                                .textPageGetCharIndexAtPos(
                                    rect?.centerX()?.toDouble() ?: 0.0,
                                    rect?.centerY()?.toDouble() ?: 0.0,
                                    // Shouldn't need much since we're in the middle of the rect
                                    1.0,
                                    1.0,
                                ).bind()

                        Truth.assertThat(pos).isEqualTo(characterToLookup)
                    }
                }
            }
        }

    @Test
    fun textPageCountRects() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val rectCount = textPage.textPageCountRects(0, 100).bind()

                        Truth.assertThat(rectCount).isEqualTo(4)
                    }
                }
            }
        }

    @Test
    fun textPageGetRect() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val rect = textPage.textPageGetRect(0).bind()

                        Truth.assertThat(rect).isEqualTo(RectF(0f, 0f, 0f, 0f))
                    }
                }
            }
        }

    @Test
    fun textPageGetBoundedText() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val text = textPage.textPageGetBoundedText(RectF(0f, 97f, 100f, 100f), 100).bind()

                        Truth.assertThat(text).isEqualTo("Do")
                    }
                }
            }
        }

    @Test
    fun getFontSize() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val fontSize = textPage.getFontSize(0).bind()

                        Truth.assertThat(fontSize).isEqualTo(22.559999465942383)
                    }
                }
            }
        }

    fun close() =
        runTest {
            either {
                var pageAfterClose: PdfTextPageKtF?
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        pageAfterClose = textPage
                    }
                }
                pageAfterClose!!.textPageCountChars()
            }.mapLeft {
                Truth.assertThat(it).isInstanceOf(PdfiumKtFErrors.AlreadyClosed::class.java)
            }
        }

    @Test
    fun getPage() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        Truth.assertThat(textPage.page).isNotNull()
                    }
                }
            }
        }
}
