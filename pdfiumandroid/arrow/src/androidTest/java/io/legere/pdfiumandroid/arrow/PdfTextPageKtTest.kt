package io.legere.pdfiumandroid.arrow

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.raise.either
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestResult
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

                        assertThat(charCount).isEqualTo(3468)
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

                        assertThat(text?.length).isEqualTo(100)
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

                        assertThat(char).isEqualTo('T')
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

                        assertThat(rect)
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

                        assertThat(pos).isEqualTo(characterToLookup)
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

                        assertThat(rectCount).isEqualTo(4)
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

                        assertThat(rect).isEqualTo(RectF(0f, 0f, 0f, 0f))
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

                        assertThat(text).isEqualTo("Do")
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

                        assertThat(fontSize).isEqualTo(22.559999465942383)
                    }
                }
            }
        }

    @Test
    fun findStart(): TestResult =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        val findWhat = "children's"
                        val startIndex = 0
                        textPage.findStart(findWhat, emptySet(), startIndex).bind().use { findHandle ->
                            var result = findHandle.findNext()
                            assertThat(result.bind()).isTrue()
                            var index = findHandle.getSchResultIndex().bind()
                            var count = findHandle.getSchCount().bind()
                            var text = textPage.textPageGetText(index, count).bind()
                            assertThat(index).isEqualTo(1525)
                            assertThat(count).isEqualTo(10)
                            assertThat(text).isEqualTo(findWhat)
                            result = findHandle.findNext()
                            assertThat(result.bind()).isTrue()
                            index = findHandle.getSchResultIndex().bind()
                            count = findHandle.getSchCount().bind()
                            text = textPage.textPageGetText(index, count).bind()
                            assertThat(index).isEqualTo(2761)
                            assertThat(count).isEqualTo(10)
                            assertThat(text).isEqualTo(findWhat)
                            result = findHandle.findNext()
                            assertThat(result.bind()).isFalse()
                        }
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
                assertThat(it).isInstanceOf(PdfiumKtFErrors.AlreadyClosed::class.java)
            }
        }

    @Test
    fun getPage() =
        runTest {
            either {
                pdfDocument.openPage(0).bind().use { page ->
                    page.openTextPage().bind().use { textPage ->
                        assertThat(textPage.page).isNotNull()
                    }
                }
            }
        }
}
