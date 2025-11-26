package io.legere.pdfiumandroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("Migrating to non-instrumented tests")
class PdfDocumentTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocument
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCore().newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun getPageCount() {
        val pageCount = pdfDocument.getPageCount()

        assertThat(pageCount).isEqualTo(4)
    }

    @Test
    fun openPage() {
        val page = pdfDocument.openPage(0)

        assertThat(page).isNotNull()
    }

    @Test
    fun openPages() {
        val page = pdfDocument.openPages(0, 3)

        assertThat(page.size).isEqualTo(4)
    }

    @Test
    fun getDocumentMeta() {
        val meta = pdfDocument.getDocumentMeta()

        assertThat(meta).isNotNull()
    }

    @Test
    fun getTableOfContents() {
        // I don't think this test document has a table of contents
        val toc = pdfDocument.getTableOfContents()

        assertThat(toc).isNotNull()
        assertThat(toc.size).isEqualTo(0)
    }

    @Test
    fun openTextPage() {
        val page = pdfDocument.openPage(0)
        val textPage = page.openTextPage()
        assertThat(textPage).isNotNull()
    }

//    @Test
//    fun openTextPages() {
//        val textPages = pdfDocument.openTextPages(0, 3)
//        assertThat(textPages.size).isEqualTo(4)
//    }

    @Test
    fun saveAsCopy() {
        pdfDocument.saveAsCopy(
            object : PdfWriteCallback {
                override fun WriteBlock(data: ByteArray?): Int {
                    // assertThat(data?.size).isEqualTo(pdfBytes?.size)
                    // assertThat(data).isEqualTo(pdfBytes)
                    return data?.size ?: 0
                }
            },
        )
    }

    @Test(expected = IllegalStateException::class)
    fun closeDocument() {
        var shouldBeClosed: PdfDocument?
        PdfiumCore().newDocument(pdfBytes).use { pdfDocument ->
            assertThat(pdfDocument).isNotNull()
            shouldBeClosed = pdfDocument
        }

        // Now it should be closed
        shouldBeClosed?.openPage(0) // This should throw an exception
    }
}
