package io.legere.pdfiumandroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.legere.pdfiumandroid.base.BasePDFTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfiumCoreTest : BasePDFTest() {

    @Test
    fun newDocument() {
        val pdfBytes = getPdfBytes("f01.pdf")

        assertNotNull(pdfBytes)

        val pdfiumCore = PdfiumCore()
        val pdfDocument = pdfiumCore.newDocument(pdfBytes)

        assertNotNull(pdfDocument)
    }

}
