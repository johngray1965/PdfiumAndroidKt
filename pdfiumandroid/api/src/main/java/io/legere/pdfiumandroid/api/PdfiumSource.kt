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

package io.legere.pdfiumandroid.api

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
