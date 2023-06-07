package io.legere.pdfiumandroid

interface PdfWriteCallback {
    fun writeBlock(data: ByteArray?): Int
}
