package io.legere.pdfiumandroid.unlocked

import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.base.BasePDFTest
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfDocumentUClosedNotHandledTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
        pdfDocument.close()
    }

    @Test(expected = IllegalStateException::class)
    fun getPageCount() {
        val pageCount = pdfDocument.getPageCount()

        Truth.assertThat(pageCount).isEqualTo(4)
    }

    @Test(expected = IllegalStateException::class)
    fun getPageCharCounts() {
        val pageCharCounts = pdfDocument.getPageCharCounts()

        val expectedValues = intArrayOf(3468, 3723, 3966, 2290)
        println(pageCharCounts)

        Truth.assertThat(pageCharCounts).isEqualTo(expectedValues)
    }

    @Test(expected = IllegalStateException::class)
    fun openPage() {
        val page = pdfDocument.openPage(0)

        Truth.assertThat(page).isNotNull()
    }

    @Test(expected = IllegalStateException::class)
    fun deletePage() {
        pdfDocument.deletePage(0)
    }

    @Test(expected = IllegalStateException::class)
    fun openPages() {
        val page = pdfDocument.openPages(0, 3)

        Truth.assertThat(page.size).isEqualTo(4)
    }

    @Test(expected = IllegalStateException::class)
    fun renderPages() {
        pdfDocument.renderPages(
            0L,
            0,
            0,
            emptyList(),
            emptyList(),
            emptyList(),
            renderAnnot = false,
            textMask = false,
            canvasColor = 0,
            pageBackgroundColor = 0,
        )
    }

    @Test(expected = IllegalStateException::class)
    fun testRenderPages() {
        val surface: Surface = mockk()
        pdfDocument.renderPages(
            surface,
            emptyList(),
            emptyList(),
            emptyList(),
            renderAnnot = false,
            textMask = false,
            canvasColor = 0,
            pageBackgroundColor = 0,
        )
    }

    @Test(expected = IllegalStateException::class)
    fun getDocumentMeta() {
        val meta = pdfDocument.getDocumentMeta()

        Truth.assertThat(meta).isNotNull()
    }

    @Test(expected = IllegalStateException::class)
    fun getTableOfContents() {
        pdfDocument.getTableOfContents()
    }

    @Test(expected = IllegalStateException::class)
    fun openTextPage() {
        val page = pdfDocument.openPage(0)
        val textPage = page.openTextPage()
        Truth.assertThat(textPage).isNotNull()
    }

    @Test(expected = IllegalStateException::class)
    fun openTextPages() {
        pdfDocument.openTextPages(0, 3)
    }

    @Test(expected = IllegalStateException::class)
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
