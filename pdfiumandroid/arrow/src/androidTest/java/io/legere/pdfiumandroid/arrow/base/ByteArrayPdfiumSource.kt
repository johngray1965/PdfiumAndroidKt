package io.legere.pdfiumandroid.arrow.base

import io.legere.pdfiumandroid.PdfiumSource

class ByteArrayPdfiumSource(
    private val array: ByteArray,
) : PdfiumSource {
    override val length: Long
        get() = array.size.toLong()

    override fun read(
        position: Long,
        buffer: ByteArray,
        size: Int,
    ): Int {
        array.copyInto(
            destination = buffer,
            destinationOffset = 0,
            startIndex = position.toInt(),
            endIndex = position.toInt() + size,
        )
        return size
    }

    override fun close() {
        // nothing to close
    }
}
