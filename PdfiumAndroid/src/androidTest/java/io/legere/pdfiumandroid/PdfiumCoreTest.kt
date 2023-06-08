package io.legere.pdfiumandroid

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test

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
