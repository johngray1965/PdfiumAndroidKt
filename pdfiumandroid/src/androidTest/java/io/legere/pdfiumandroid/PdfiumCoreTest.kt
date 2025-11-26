package io.legere.pdfiumandroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.base.ByteArrayPdfiumSource
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("Migrating to non-instrumented tests")
class PdfiumCoreTest : BasePDFTest() {
    @Test
    fun newDocument() {
        val pdfBytes = getPdfBytes("f01.pdf")

        assertThat(pdfBytes).isNotNull()

        val pdfiumCore = PdfiumCore()
        val pdfDocument = pdfiumCore.newDocument(pdfBytes)

        assertThat(pdfDocument).isNotNull()
    }

    @Test
    fun newDocumentWithCustomSource() {
        val pdfBytes = getPdfBytes("f01.pdf")

        assertThat(pdfBytes).isNotNull()

        val pdfiumCore = PdfiumCore()
        val pdfDocument = pdfiumCore.newDocument(ByteArrayPdfiumSource(pdfBytes!!))

        assertThat(pdfDocument).isNotNull()
    }
}
