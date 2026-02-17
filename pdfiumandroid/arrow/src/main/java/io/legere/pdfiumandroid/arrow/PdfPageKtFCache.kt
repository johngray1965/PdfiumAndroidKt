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

package io.legere.pdfiumandroid.arrow

import arrow.core.Either
import io.legere.pdfiumandroid.suspend.PdfPageSuspendCacheBase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal const val CACHE_SIZE = 64L

/**
 * A cache for pages of a PDF document, designed to be used with Arrow and coroutines.
 *
 * This class is thread-safe.
 *
 * To use this class, you need to provide a `pdfDocument` and a `pageHolderFactory`.
 * The `pageHolderFactory` is a lambda that takes a `PdfPageKtF` and a `PdfTextPageKtF` and returns an instance of `H`.
 *
 * The generic type `H` is a holder for the page and text page. It can be a simple [PageHolderKtF] or a custom class.
 *
 * Example usage with [PageHolderKtF]:
 * ```
 * val pageCache = PdfPageKtFCache(pdfDocument) { page, textPage ->
 *     PageHolderKtF(page, textPage)
 * }
 * ```
 *
 * Example usage with a custom holder:
 * ```
 * data class CustomPageHolder(
 *     val page: PdfPageKtF,
 *     val textPage: PdfTextPageKtF,
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
 * val pageCache = PdfPageKtFCache(pdfDocument) { page, textPage ->
 *     CustomPageHolder(page, textPage, "some other data")
 * }
 * ```
 *
 * @param H The type of the holder for the page and text page. Must be [AutoCloseable].
 * @property pdfDocument The [PdfDocumentKtF] to cache pages from.
 * @param dispatcher The [CoroutineDispatcher] to use for opening pages. Defaults to [Dispatchers.IO].
 * @property pageHolderFactory A factory for creating a holder for a page and text page.
 */
class PdfPageKtFCache<H : AutoCloseable>(
    private val pdfDocument: PdfDocumentKtF,
    maxSize: Long = CACHE_SIZE,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val pageHolderFactory: suspend (PdfPageKtF, PdfTextPageKtF) -> H,
) : AutoCloseable {
    private val suspendCache =
        object : PdfPageSuspendCacheBase<H>(dispatcher, maxSize) {
            @Suppress("ReturnCount")
            override suspend fun openPageAndText(pageIndex: Int): H? {
                val page = pdfDocument.openPage(pageIndex).getOrNull() ?: return null
                val textPage = page.openTextPage().getOrNull() ?: return null
                return pageHolderFactory(
                    page,
                    textPage,
                )
            }
        }

    /**
     * Get a page from the cache.
     *
     * @param pageIndex The index of the page to get.
     * @return An [Either] with the page holder or an error.
     */
    suspend fun getF(pageIndex: Int): Either<Throwable, H> =
        Either.catch {
            suspendCache.get(pageIndex)
        }

    override fun close() {
        suspendCache.close()
    }
}

/**
 * A holder for a [PdfPageKtF] and a [PdfTextPageKtF].
 *
 * @param TPage The type of the page.
 * @param TTextPage The type of the text page.
 * @property page The page.
 * @property textPage The text page.
 */
data class PageHolderKtF<TPage : AutoCloseable, TTextPage : AutoCloseable>(
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
