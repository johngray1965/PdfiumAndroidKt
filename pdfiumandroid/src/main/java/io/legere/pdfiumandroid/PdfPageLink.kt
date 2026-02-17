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

package io.legere.pdfiumandroid

import android.graphics.RectF
import io.legere.pdfiumandroid.core.unlocked.PdfPageLinkU
import io.legere.pdfiumandroid.core.util.wrapLock
import java.io.Closeable

/**
 * Represents a collection of web links on a PDF page.
 *
 * This class provides access to web link information on a PDF page.
 * It allows for querying the number of web links, their URLs,
 * and the bounding rectangles and text ranges associated with them.
 *
 * @property pageLink The underlying native page link object.
 */
@Suppress("TooManyFunctions")
class PdfPageLink internal constructor(
    internal val pageLink: PdfPageLinkU,
) : Closeable {
    /**
     * Counts the number of web links found on the page.
     *
     * @return The total number of web links on the page.
     */
    fun countWebLinks(): Int =
        wrapLock {
            pageLink.countWebLinks()
        }

    /**
     * Retrieves the URL for a specific web link.
     *
     * @param index The 0-based index of the web link.
     * @param length The maximum length of the URL to retrieve.
     * @return The URL as a [String], or `null` if not found or an error occurs.
     */
    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    fun getURL(
        index: Int,
        length: Int,
    ): String? =
        wrapLock {
            pageLink.getURL(index, length)
        }

    /**
     * Counts the number of rectangles associated with a specific web link.
     * A single web link can span multiple rectangular areas on the page.
     *
     * @param index The 0-based index of the web link.
     * @return The number of rectangles associated with the given web link.
     */
    fun countRects(index: Int): Int =
        wrapLock {
            pageLink.countRects(index)
        }

    /**
     * Retrieves a specific bounding rectangle for a given web link.
     *
     * @param linkIndex The 0-based index of the web link.
     * @param rectIndex The 0-based index of the rectangle within that web link.
     * @return A [RectF] representing the bounding box of the specified rectangle.
     */
    @Suppress("MagicNumber")
    fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): RectF =
        wrapLock {
            pageLink.getRect(linkIndex, rectIndex)
        }

    /**
     * Retrieves the text range (start index and count) associated with a web link.
     *
     * @param index The 0-based index of the web link.
     * @return A [Pair] where `first` is the starting character index and `second` is the character count.
     */
    fun getTextRange(index: Int): Pair<Int, Int> =
        wrapLock {
            pageLink.getTextRange(index)
        }

    /**
     * Closes the [PdfPageLink] object and releases associated native resources.
     * This makes the object unusable after this call.
     */
    override fun close() {
        wrapLock {
            pageLink.close()
        }
    }
}
