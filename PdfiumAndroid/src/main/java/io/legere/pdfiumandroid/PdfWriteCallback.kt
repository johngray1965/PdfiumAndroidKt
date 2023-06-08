package io.legere.pdfiumandroid

interface PdfWriteCallback {
    @Suppress("FunctionNaming")
    // The name need to be exactly what it is.
    // The native call is looking for is as WriteBlock
    fun WriteBlock(data: ByteArray?): Int
}
