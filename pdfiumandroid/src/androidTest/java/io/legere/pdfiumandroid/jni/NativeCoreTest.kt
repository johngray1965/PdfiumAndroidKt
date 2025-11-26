package io.legere.pdfiumandroid.jni

import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.base.ByteArrayPdfiumSource
import io.legere.pdfiumandroid.util.PdfiumNativeSourceBridge
import org.junit.Before
import org.junit.Test

class NativeCoreTest : BasePDFTest() {
    val nativeCore = NativeCore()

    @Before
    fun setUp() {
        System.loadLibrary("pdfium")
        System.loadLibrary("pdfiumandroid")
    }

    @Test
    fun openDocument() {
//        val docPtr = nativeCore.openDocument(getPdfPath("f01.pdf"), null)
    }

    @Test
    fun openMemDocument() {
        val pdfBytes = getPdfBytes("f01.pdf")
        val docPtr = nativeCore.openMemDocument(pdfBytes, null)
        assertThat(docPtr).isNotNull()
    }

    @Test
    fun openCustomDocument() {
        val pdfBytes = getPdfBytes("f01.pdf")

        val data = ByteArrayPdfiumSource(pdfBytes!!)

        val nativeSourceBridge = PdfiumNativeSourceBridge(data)
        val docPtr = nativeCore.openCustomDocument(nativeSourceBridge, null, pdfBytes.size.toLong())
        assertThat(docPtr).isNotNull()
    }
}
