package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
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
class PdfPageLinkKtTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentKt
    private lateinit var pdfPage: PdfPageKt
    private lateinit var pdfTextPage: PdfTextPageKt

    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() =
        runBlocking {
            pdfBytes = getPdfBytes("pdf-test.pdf")

            TestCase.assertNotNull(pdfBytes)

            pdfDocument = PdfiumCoreKt(Dispatchers.Unconfined).newDocument(pdfBytes)
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
    fun testLink(): TestResult =
        runTest {
            val links = pdfTextPage.loadWebLink()
            assertThat(links).isNotNull()
            links.close()
        }

    @Test
    fun testCountWebLinks(): TestResult =
        runTest {
            val links = pdfTextPage.loadWebLink()
            assertThat(links).isNotNull()
            assertThat(links.countWebLinks()).isEqualTo(1)
            links.close()
        }

    @Test
    fun testGetTextRange(): TestResult =
        runTest {
            val links = pdfTextPage.loadWebLink()
            assertThat(links).isNotNull()
            assertThat(links.getTextRange(0)).isEqualTo(Pair(351, 31))
            links.close()
        }

    @Test
    fun testGetUrl(): TestResult =
        runTest {
            val links = pdfTextPage.loadWebLink()
            assertThat(links).isNotNull()
            val (_, count) = links.getTextRange(0)
            assertThat(links.getURL(0, count)).isEqualTo("http://www.education.gov.yk.ca/")
            links.close()
        }

    @Test
    fun testCountRects(): TestResult =
        runTest {
            val links = pdfTextPage.loadWebLink()
            assertThat(links).isNotNull()
            val count = links.countRects(0)
            assertThat(count).isEqualTo(1)
            links.close()
        }

    @Test
    fun testGetRect(): TestResult =
        runTest {
            val links = pdfTextPage.loadWebLink()
            assertThat(links).isNotNull()
            val count = links.getRect(0, 0)
            assertThat(count).isEqualTo(RectF(221.46f, 480.624f, 389.66394f, 469.152f))
            links.close()
        }
}
