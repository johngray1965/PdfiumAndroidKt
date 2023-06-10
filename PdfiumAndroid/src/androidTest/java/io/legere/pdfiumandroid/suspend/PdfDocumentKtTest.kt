package io.legere.pdfiumandroid.suspend

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.base.BasePDFTest
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfDocumentKtTest : BasePDFTest() {

    private lateinit var pdfDocument: PdfDocumentKt
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() = runBlocking {
        pdfBytes = getPdfBytes("f01.pdf")

        TestCase.assertNotNull(pdfBytes)

        pdfDocument = PdfiumCoreKt(Dispatchers.Unconfined).newDocument(pdfBytes)
    }

    @After
    fun tearDown() = runTest {
        pdfDocument.close()
    }

    @Test
    fun getPageCount() = runTest {
        val pageCount = pdfDocument.getPageCount()

        assert(pageCount == 4) { "Page count should be 4" }
    }

    @Test
    fun openPage() = runTest {
        val page = pdfDocument.openPage(0)

        TestCase.assertNotNull(page)
    }

    @Test
    fun openPages() = runTest {
        val page = pdfDocument.openPages(0, 3)

        assert(page.size == 4) { "Page count should be 4" }
    }

    @Test
    fun getDocumentMeta() = runTest {
        val meta = pdfDocument.getDocumentMeta()

        TestCase.assertNotNull(meta)
    }

    @Test
    fun getTableOfContents() = runTest {
        // I don't think this test document has a table of contents
        val toc = pdfDocument.getTableOfContents()

        TestCase.assertNotNull(toc)
        Truth.assertThat(toc.size).isEqualTo(0)
    }

    @Test
    fun openTextPage() = runTest {
        val textPage = pdfDocument.openTextPage(0)
        TestCase.assertNotNull(textPage)
    }

    @Test
    fun openTextPages() = runTest {
        val textPages = pdfDocument.openTextPages(0, 3)
        Truth.assertThat(textPages.size).isEqualTo(4)
    }

    @Test
    fun saveAsCopy() = runTest {
        pdfDocument.saveAsCopy(object : PdfWriteCallback {
            override fun WriteBlock(data: ByteArray?): Int {
                // Truth.assertThat(data?.size).isEqualTo(pdfBytes?.size)
                // Truth.assertThat(data).isEqualTo(pdfBytes)
                return data?.size ?: 0
            }
        })
    }

    @Test(expected = IllegalStateException::class)
    fun close() = runTest {
        var documentAfterClose: PdfDocumentKt?
        PdfiumCoreKt(Dispatchers.Unconfined).newDocument(pdfBytes).use {
            documentAfterClose = it
        }
        documentAfterClose?.openPage(0)
    }
}
