package io.legere.pdfiumandroid.unlocked

import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfWriteCallback
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.util.AlreadyClosedBehavior
import io.legere.pdfiumandroid.util.Config
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfDocumentUClosedHandledTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        Truth.assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU(config = Config(alreadyClosedBehavior = AlreadyClosedBehavior.IGNORE)).newDocument(pdfBytes)
        pdfDocument.close()
    }

    @Test
    fun getPageCount() {
        val pageCount = pdfDocument.getPageCount()

        Truth.assertThat(pageCount).isEqualTo(0)
    }

    @Test
    fun getPageCharCounts() {
        val pageCharCounts = pdfDocument.getPageCharCounts()

        val expectedValues = intArrayOf() // empty array because the doc was closed

        Truth.assertThat(pageCharCounts).isEqualTo(expectedValues)
    }

    @Test(expected = IllegalStateException::class)
    fun openPage() {
        val page = pdfDocument.openPage(0)

        Truth.assertThat(page).isNotNull()
    }

    @Test
    fun deletePage() {
        pdfDocument.deletePage(0)
    }

    @Test
    fun openPages() {
        val page = pdfDocument.openPages(0, 3)

        Truth.assertThat(page.size).isEqualTo(0)
    }

    @Test
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

    @Test
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

    @Test
    fun getDocumentMeta() {
        val meta = pdfDocument.getDocumentMeta()

        Truth.assertThat(meta).isNotNull()
    }

    @Test
    fun getTableOfContents() {
        pdfDocument.getTableOfContents()
    }

    @Test(expected = IllegalStateException::class)
    fun openTextPage() {
        val page = pdfDocument.openPage(0)
        val textPage = page.openTextPage()
        Truth.assertThat(textPage).isNotNull()
    }

    @Test
    fun openTextPages() {
        pdfDocument.openTextPages(0, 3)
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
