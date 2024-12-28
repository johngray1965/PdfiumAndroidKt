package io.legere.pdfiumandroid.arrow

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.raise.either
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.arrow.base.BasePDFTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfPageLinkKtFTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentKtF
    private lateinit var pdfPage: PdfPageKtF
    private lateinit var pdfTextPage: PdfTextPageKtF
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() =
        runBlocking {
            pdfBytes = getPdfBytes("pdf-test.pdf")

            Truth.assertThat(pdfBytes).isNotNull()

            pdfDocument = PdfiumCoreKtF(Dispatchers.Unconfined).newDocument(pdfBytes).getOrNull()!!
            pdfPage = pdfDocument.openPage(0).getOrNull()!!
            pdfTextPage = pdfPage.openTextPage().getOrNull()!!
        }

    @After
    fun tearDown() {
        pdfTextPage.close()
        pdfPage.close()
        pdfDocument.close()
    }

    @Test
    fun testLink(): TestResult =
        runTest {
            either {
                pdfTextPage.loadWebLink().bind().use { links ->
                    Truth.assertThat(links).isNotNull()
                }
            }
        }

    @Test
    fun testCountWebLinks(): TestResult =
        runTest {
            either {
                pdfTextPage.loadWebLink().bind().use { links ->
                    Truth.assertThat(links).isNotNull()
                    Truth.assertThat(links.countWebLinks().bind()).isEqualTo(1)
                }
            }
        }

    @Test
    fun testGetTextRange(): TestResult =
        runTest {
            either {
                pdfTextPage.loadWebLink().bind().use { links ->
                    Truth.assertThat(links).isNotNull()
                    Truth.assertThat(links.getTextRange(0).bind()).isEqualTo(Pair(351, 31))
                }
            }
        }

    @Test
    fun testGetUrl(): TestResult =
        runTest {
            either {
                pdfTextPage.loadWebLink().bind().use { links ->
                    Truth.assertThat(links).isNotNull()
                    val (_, count) = links.getTextRange(0).bind()
                    Truth
                        .assertThat(links.getURL(0, count).bind())
                        .isEqualTo("http://www.education.gov.yk.ca/")
                }
            }
        }

    @Test
    fun testCountRects(): TestResult =
        runTest {
            either {
                pdfTextPage.loadWebLink().bind().use { links ->
                    Truth.assertThat(links).isNotNull()
                    val count = links.countRects(0).bind()
                    Truth.assertThat(count).isEqualTo(1)
                }
            }
        }

    @Test
    fun testGetRect(): TestResult =
        runTest {
            either {
                pdfTextPage.loadWebLink().bind().use { links ->
                    Truth.assertThat(links).isNotNull()
                    val count = links.getRect(0, 0).bind()
                    Truth
                        .assertThat(count)
                        .isEqualTo(RectF(221.46f, 480.624f, 389.66394f, 469.152f))
                }
            }
        }
}
