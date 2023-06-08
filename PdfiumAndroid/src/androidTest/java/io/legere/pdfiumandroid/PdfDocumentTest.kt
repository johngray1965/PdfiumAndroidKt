package io.legere.pdfiumandroid

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertNotNull

import org.junit.After
import org.junit.Before
import org.junit.Test

class PdfDocumentTest {

    lateinit var pdfDocument: PdfDocument

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation().context

        val pdfBytes = getPdfBytes("f01.pdf")

        assertNotNull(pdfBytes)

        pdfDocument = PdfiumCore().newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun getParcelFileDescriptor() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun setParcelFileDescriptor() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun getPageCount() {
        val pageCount = pdfDocument.getPageCount()

        assert(pageCount == 4) { "Page count should be 4" }
    }

    @Test
    fun openPage() {
        val page = pdfDocument.openPage(0)

        assertNotNull(page)
    }

    @Test
    fun openPages() {
        val page = pdfDocument.openPages(0, 3)

        assert(page.size == 4) { "Page count should be 4" }
    }

    @Test
    fun getDocumentMeta() {
        val meta = pdfDocument.getDocumentMeta()

        assertNotNull(meta)
    }

    @Test
    fun getTableOfContents() {
        // I don't think this test document has a table of contents
        val toc = pdfDocument.getTableOfContents()

        assertNotNull(toc)
    }

    @Test
    fun openTextPage() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun openTextPages() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun saveAsCopy() {
        assert(false) { "not implemented yet" }
    }

    @Test
    fun close() {
        assert(false) { "not implemented yet" }
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
