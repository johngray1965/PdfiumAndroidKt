package io.legere.pdfiumandroid

interface PdfWriteCallback {
    // The name need to be exactly what it is.
    // The native call is looking for is as WriteBlock
    @Suppress("FunctionNaming")
    fun WriteBlock(data: ByteArray?): Int
}
