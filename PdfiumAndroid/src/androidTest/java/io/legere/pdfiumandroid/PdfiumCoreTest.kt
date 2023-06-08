package io.legere.pdfiumandroid

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.lang.IllegalStateException

class PdfiumCoreTest {

    @Test
    fun newDocument() {
        InstrumentationRegistry.getInstrumentation().context

        val pdfBytes = getPdfBytes("f01.pdf")

        assertNotNull(pdfBytes)

        val pdfiumCore = PdfiumCore()
        val pdfDocument = pdfiumCore.newDocument(pdfBytes)

        assertNotNull(pdfDocument)
    }

    @Test
    fun getPageCount() {
        InstrumentationRegistry.getInstrumentation().context

        val pdfBytes = getPdfBytes("f01.pdf")

        assertNotNull(pdfBytes)

        val pdfiumCore = PdfiumCore()
        val pdfDocument = pdfiumCore.newDocument(pdfBytes)

        assertNotNull(pdfDocument)

        val pageCount = pdfDocument.getPageCount()

        assert(pageCount == 4) { "Page count should be 4" }
    }

    @Test
    fun getPage() {
        InstrumentationRegistry.getInstrumentation().context

        val pdfBytes = getPdfBytes("f01.pdf")

        assertNotNull(pdfBytes)

        val pdfiumCore = PdfiumCore()
        val pdfDocument = pdfiumCore.newDocument(pdfBytes)

        assertNotNull(pdfDocument)

        val page = pdfDocument.openPage(0)

        assertNotNull(page)
    }

    @Test(expected = IllegalStateException::class)
    fun closeDocument() {
        InstrumentationRegistry.getInstrumentation().context

        val pdfBytes = getPdfBytes("f01.pdf")

        assertNotNull(pdfBytes)

        val pdfiumCore = PdfiumCore()
        var shouldBeClosed: PdfDocument?
        pdfiumCore.newDocument(pdfBytes).use { pdfDocument ->
            assertNotNull(pdfDocument)
            shouldBeClosed = pdfDocument
        }

        // Now it should be closed
        shouldBeClosed?.openPage(0) // This should throw an exception
    }

    private fun getPdfBytes(filename: String) : ByteArray? {
        val appContext = InstrumentationRegistry.getInstrumentation().context
        val assetManager = appContext.assets
        try {
            val input = assetManager.open(filename)
            return input.readBytes()
        } catch (e: Exception) {
            Log.e(PdfiumCoreTest::class.simpleName, "Ugh",  e)
        }
        assetManager.close()
        return null
    }

}
