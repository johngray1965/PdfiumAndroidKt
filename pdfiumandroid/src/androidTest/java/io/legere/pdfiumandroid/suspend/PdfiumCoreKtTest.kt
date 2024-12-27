package io.legere.pdfiumandroid.suspend

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.base.ByteArrayPdfiumSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfiumCoreKtTest : BasePDFTest() {
    @Test
    fun newDocument() =
        runTest {
            val pdfBytes = getPdfBytes("f01.pdf")

            assertThat(pdfBytes).isNotNull()

            val pdfiumCore = PdfiumCoreKt(Dispatchers.Unconfined)
            val pdfDocument = pdfiumCore.newDocument(pdfBytes)

            assertThat(pdfDocument).isNotNull()
        }

    @Test
    fun newDocumentWitCustomSource() =
        runTest {
            val pdfBytes = getPdfBytes("f01.pdf")

            assertThat(pdfBytes).isNotNull()

            val pdfiumCore = PdfiumCoreKt(Dispatchers.Unconfined)
            val pdfDocument = pdfiumCore.newDocument(ByteArrayPdfiumSource(pdfBytes!!))

            assertThat(pdfDocument).isNotNull()
        }
}
