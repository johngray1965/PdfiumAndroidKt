package io.legere.pdfiumandroid.suspend

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.legere.pdfiumandroid.base.BasePDFTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
