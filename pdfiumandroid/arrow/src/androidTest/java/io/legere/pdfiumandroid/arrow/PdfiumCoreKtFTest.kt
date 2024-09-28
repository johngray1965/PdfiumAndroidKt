package io.legere.pdfiumandroid.arrow

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfiumCoreKtFTest : BasePDFTest() {
    @Test
    fun newDocument() =
        runTest {
            val pdfBytes = getPdfBytes("f01.pdf")

            Assert.assertNotNull(pdfBytes)

            val pdfiumCore = PdfiumCoreKtF(Dispatchers.Unconfined)
            val pdfDocument = pdfiumCore.newDocument(pdfBytes)

            Assert.assertNotNull(pdfDocument)
        }
}
