package io.legere.pdfiumandroid.util

import io.legere.pdfiumandroid.BuildConfig
import io.legere.pdfiumandroid.PdfiumSource

internal class PdfiumNativeSourceBridge(private val source: PdfiumSource) {

    private var buffer: ByteArray? = null

    fun read(position: Long, size: Long): Int =
        try {
            require(size <= Int.MAX_VALUE) { "size is too large" }
            val trimmedSize = size.toInt()

            var buffer = buffer
            if (buffer == null || buffer.size < size) {
                buffer = ByteArray(trimmedSize).also { this.buffer = it }
            }

            val bytesRead = source.read(position, buffer, trimmedSize)
            if (bytesRead <= 0) 0 else bytesRead // Pdfium expects 0 for error while Java/Kotlin usually return a negative value
        } catch (t: Throwable) { // This is to prevent the exception to go to the native code level
            if (BuildConfig.DEBUG) {
                t.printStackTrace()
            }
            0
        }
}
