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

package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.core.unlocked.PdfPageLinkU
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

/**
 * Arrow-based suspending version of [io.legere.pdfiumandroid.PdfPageLink] that provides asynchronous
 * access to web link information on a PDF page with functional error handling.
 *
 * This class wraps the native [io.legere.pdfiumandroid.core.unlocked.PdfPageLinkU] object and dispatches its operations
 * to a [CoroutineDispatcher] using the [wrapEither] function. This ensures non-blocking
 * execution and provides robust error handling by returning results as [Either<PdfiumKtFErrors, T>].
 * It allows for querying the number of web links, their URLs, and the bounding rectangles
 * and text ranges associated with them in a suspendable and functional manner.
 *
 * @property pageLink The underlying unlocked native page link object.
 * @property dispatcher The [CoroutineDispatcher] to use for suspending calls.
 */
class PdfPageLinkKtF internal constructor(
    internal val pageLink: PdfPageLinkU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.PdfPageLink.countWebLinks].
     * Counts the number of web links found on the page asynchronously.
     *
     * @return An [Either] containing `PdfiumKtFErrors` on the left or the total number of web links
     * on the page on the right.
     */
    suspend fun countWebLinks(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            pageLink.countWebLinks()
        }

    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.PdfPageLink.getURL].
     * Retrieves the URL for a specific web link asynchronously.
     *
     * @param index The 0-based index of the web link.
     * @param length The maximum length of the URL to retrieve.
     * @return An [Either] containing `PdfiumKtFErrors` on the left or the URL as a [String] (or `null`) on the right.
     */
    suspend fun getURL(
        index: Int,
        length: Int,
    ): Either<PdfiumKtFErrors, String?> =
        wrapEither(dispatcher) {
            pageLink.getURL(index, length)
        }

    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.PdfPageLink.countRects].
     * Counts the number of rectangles associated with a specific web link asynchronously.
     * A single web link can span multiple rectangular areas on the page.
     *
     * @param index The 0-based index of the web link.
     * @return An [Either] containing `PdfiumKtFErrors` on the left or the number of rectangles on the right.
     */
    suspend fun countRects(index: Int): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            pageLink.countRects(index)
        }

    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.PdfPageLink.getRect].
     * Retrieves a specific bounding rectangle for a given web link asynchronously.
     *
     * @param linkIndex The 0-based index of the web link.
     * @param rectIndex The 0-based index of the rectangle within that web link.
     * @return An [Either] containing `PdfiumKtFErrors` on the left or a [PdfRectF] representing
     * the bounding box on the right.
     */
    suspend fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): Either<PdfiumKtFErrors, PdfRectF> =
        wrapEither(dispatcher) {
            pageLink.getRect(linkIndex, rectIndex)
        }

    /**
     * Suspending and Arrow-based version of [io.legere.pdfiumandroid.PdfPageLink.getTextRange].
     * Retrieves the text range (start index and count) associated with a web link asynchronously.
     *
     * @param index The 0-based index of the web link.
     * @return An [Either] containing `PdfiumKtFErrors` on the left or a [Pair] (start index, count) on the right.
     */
    suspend fun getTextRange(index: Int): Either<PdfiumKtFErrors, Pair<Int, Int>> =
        wrapEither(dispatcher) {
            pageLink.getTextRange(index)
        }

    /**
     * Closes the [PdfPageLinkKtF] object and releases associated native resources.
     * This makes the object unusable after this call.
     */
    override fun close() {
        pageLink.close()
    }
}
