package io.legere.pdfiumandroid.util

import io.legere.pdfiumandroid.Logger
import io.legere.pdfiumandroid.PdfiumSource

/**
 * A bridge class used internally to provide a custom [PdfiumSource] to the native PDFium library.
 * This class facilitates reading data from a Kotlin/Java [PdfiumSource] when the native code
 * requires a custom callback mechanism for data access.
 *
 * It is primarily used by [io.legere.pdfiumandroid.unlocked.PdfiumCoreU] for `openCustomDocument`.
 *
 * @property source The [PdfiumSource] implementation that provides the actual PDF data.
 */
class PdfiumNativeSourceBridge(
    private val source: PdfiumSource,
) {
    private var buffer: ByteArray? = null

    /**
     * Reads a specified amount of data from the [PdfiumSource] at a given position.
     * This method is called by the native PDFium library.
     *
     * @param position The starting offset in the source from which to read data.
     * @param size The number of bytes to read.
     * @return The number of bytes actually read, or 0 if an error occurred or end of stream is reached.
     */
    @Suppress("TooGenericExceptionCaught")
    fun read(
        position: Long,
        size: Long,
    ): Int =
        try {
            require(size <= Int.MAX_VALUE) { "size is too large" }
            val trimmedSize = size.toInt()

            var buffer = buffer
            if (buffer == null || buffer.size < size) {
                buffer = ByteArray(trimmedSize).also { this.buffer = it }
            }

            val bytesRead = source.read(position, buffer, trimmedSize)
            // Pdfium expects 0 for error while Java/Kotlin usually return a negative value
            if (bytesRead <= 0) 0 else bytesRead
        } catch (t: Throwable) {
            // This is to prevent the exception to go to the native code level
            Logger.e("PdfiumNativeSourceBridge", t, "read failed")
            0
        }
}
