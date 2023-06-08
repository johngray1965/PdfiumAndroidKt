package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.base.BasePDFTest
import org.junit.Assert.assertNotNull
import org.junit.Test

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
