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

@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal const val CACHE_SIZE = 64L

/**
 * A cache for pages of a PDF document, designed to be used with coroutines.
 *
 * This class is thread-safe.
 *
 * To use this class, you need to provide a `pdfDocument`, a `dispatcher` and a `pageHolderFactory`.
 * The `pageHolderFactory` is a lambda that takes a `PdfPageKt` and a `PdfTextPageKt` and returns an instance of `H`.
 *
 * The generic type `H` is a holder for the page and text page. It can be a simple [PageHolderKt] or a custom class.
 *
 * Example usage with [PageHolderKt]:
 * ```
 * val pageCache = PdfPageKtCache(pdfDocument) { page, textPage ->
 *     PageHolderKt(page, textPage)
 * }
 * ```
 *
 * Example usage with a custom holder:
 * ```
 * data class CustomPageHolder(
 *     val page: PdfPageKt,
 *     val textPage: PdfTextPageKt,
 *     val someOtherData: String
 * ) : AutoCloseable {
 *     override fun close() {
 *         try {
 *             textPage.close()
 *         } finally {
 *             page.close()
 *         }
 *     }
 * }
 *
 * val pageCache = PdfPageKtCache(pdfDocument) { page, textPage ->
 *     CustomPageHolder(page, textPage, "some other data")
 * }
 * ```
 *
 * @param H The type of the holder for the page and text page. Must be [AutoCloseable].
 * @property pdfDocument The [PdfDocumentKt] to cache pages from.
 * @param dispatcher The [CoroutineDispatcher] to use for opening pages. Defaults to [Dispatchers.IO].
 * @property pageHolderFactory A factory for creating a holder for a page and text page.
 */
class PdfPageKtCache<H : AutoCloseable>(
    private val pdfDocument: PdfDocumentKt,
    maxSize: Long = CACHE_SIZE,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val pageHolderFactory: suspend (PdfPageKt, PdfTextPageKt) -> H,
) : PdfPageSuspendCacheBase<H>(dispatcher, maxSize) {
    override suspend fun openPageAndText(pageIndex: Int): H? {
        val page = pdfDocument.openPage(pageIndex) ?: return null
        val textPage = page.openTextPage()
        return pageHolderFactory(page, textPage)
    }
}

/**
 * A holder for a [PdfPageKt] and a [PdfTextPageKt].
 *
 * @param TPage The type of the page.
 * @param TTextPage The type of the text page.
 * @property page The page.
 * @property textPage The text page.
 */
data class PageHolderKt<TPage : AutoCloseable, TTextPage : AutoCloseable>(
    val page: TPage,
    val textPage: TTextPage,
) : AutoCloseable {
    override fun close() {
        try {
            textPage.close()
        } finally {
            page.close()
        }
    }
}
