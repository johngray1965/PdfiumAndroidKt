package io.legere.pdfiumandroid

/**
 * An interface for providing custom data source to Pdfium.
 */
interface PdfiumSource : AutoCloseable {
    /**
     * Data length, in bytes
     */
    val length: Long

    /**
     * Read data from the source.
     *
     * The position and size will never go out of range of the data source [length].
     * It may be possible for Pdfium to call this function multiple times for the same position.
     *
     * @param position byte offset from the beginning of the data source
     * @param buffer the buffer to read data into. Always have enough space to read [size] bytes.
     * It should be filled starting from index 0.
     * @param size the number of bytes to read. Never 0.
     * @return number of bytes that was read, or a negative value to indicate an error.
     */
    fun read(
        position: Long,
        buffer: ByteArray,
        size: Int,
    ): Int
}
