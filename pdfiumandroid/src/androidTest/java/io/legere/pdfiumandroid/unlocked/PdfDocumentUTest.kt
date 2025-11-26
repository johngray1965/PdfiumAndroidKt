package io.legere.pdfiumandroid.unlocked

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.base.BasePDFTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("Migrating to non-instrumented tests")
class PdfDocumentUTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        try {
            // Some test close the document, so we need to catch the exception
            pdfDocument.close()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    @Test
    fun getPageCount() {
        val pageCount = pdfDocument.getPageCount()

        Truth.assertThat(pageCount).isEqualTo(4)
    }

    @Test
    fun getPageCharCounts() {
        val pageCharCounts = pdfDocument.getPageCharCounts()

        val expectedValues = intArrayOf(3468, 3723, 3966, 2290)
        println(pageCharCounts)

        Truth.assertThat(pageCharCounts).isEqualTo(expectedValues)
    }

    @Test
    fun openPage() {
        val page = pdfDocument.openPage(0)

        Truth.assertThat(page).isNotNull()
    }

    @Test
    fun openPages() {
        val page = pdfDocument.openPages(0, 3)

        Truth.assertThat(page.size).isEqualTo(4)
    }

    @Test
    fun getDocumentMeta() {
        val meta = pdfDocument.getDocumentMeta()

        println(meta)

        Truth.assertThat(meta).isNotNull()
    }

    @Test
    fun getTableOfContents() {
        pdfBytes = getPdfBytes("1604.05669v1.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)

        val toc = pdfDocument.getTableOfContents()

        println(toc)

        Truth.assertThat(toc).isNotNull()
        Truth.assertThat(toc.size).isEqualTo(2)
    }

    @Test(expected = IllegalStateException::class)
    fun getTableOfContentsClosedDocument() {
        pdfDocument.close()
        pdfDocument.getTableOfContents()
//        try {
//        } catch (e: IllegalStateException) {
//            Truth.assertThat(e).isNotNull()
//        }
    }

    @Test
    fun openTextPage() {
        val page = pdfDocument.openPage(0)
        val textPage = page.openTextPage()
        Truth.assertThat(textPage).isNotNull()
    }

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
            Truth.assertThat(pdfDocument).isNotNull()
            shouldBeClosed = pdfDocument
        }

        // Now it should be closed
        shouldBeClosed?.openPage(0) // This should throw an exception
    }
}
