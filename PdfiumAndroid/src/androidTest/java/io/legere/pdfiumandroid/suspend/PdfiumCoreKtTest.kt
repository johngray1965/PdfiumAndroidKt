package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.base.BasePDFTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class PdfiumCoreKtTest : BasePDFTest() {

    @Test
    fun newDocument() = runTest {
        val pdfBytes = getPdfBytes("f01.pdf")

        Assert.assertNotNull(pdfBytes)

        val pdfiumCore = PdfiumCoreKt(Dispatchers.Unconfined)
        val pdfDocument = pdfiumCore.newDocument(pdfBytes)

        Assert.assertNotNull(pdfDocument)
    }

}
