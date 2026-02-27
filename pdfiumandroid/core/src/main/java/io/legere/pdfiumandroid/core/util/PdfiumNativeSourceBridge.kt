/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.legere.pdfiumandroid.core.util

import io.legere.pdfiumandroid.api.Logger
import io.legere.pdfiumandroid.api.PdfiumSource

/**
 * A bridge class used internally to provide a custom [io.legere.pdfiumandroid.api.PdfiumSource] to
 * the native PDFium library. This class facilitates reading data from a Kotlin/Java
 * [io.legere.pdfiumandroid.api.PdfiumSource] when the native code requires a custom callback
 * mechanism for data access.
 *
 * It is primarily used by [PdfiumCoreU] for `openCustomDocument`.
 *
 * @property source The [io.legere.pdfiumandroid.api.PdfiumSource] implementation that provides the actual PDF data.
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
