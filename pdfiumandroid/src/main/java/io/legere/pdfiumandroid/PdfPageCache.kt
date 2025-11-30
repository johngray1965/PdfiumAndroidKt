@file:Suppress("unused")

package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.util.CACHE_SIZE
import io.legere.pdfiumandroid.util.PdfPageCacheBase

/**
 * A cache for pages of a PDF document.
 *
 * This class is thread-safe.
 *
 * To use this class, you need to provide a `pdfDocument` and a `pageHolderFactory`.
 * The `pageHolderFactory` is a lambda that takes a `PdfPage` and a `PdfTextPage` and returns an instance of `H`.
 *
 * The generic type `H` is a holder for the page and text page. It can be a simple `PageHolder` or a custom class.
 *
 * Example usage with `PageHolder`:
 * ```
 * val pageCache = PdfPageCache(pdfDocument) { page, textPage ->
 *     PageHolder(page, textPage)
 * }
 * ```
 *
 * Example usage with a custom holder:
 * ```
 * data class CustomPageHolder(
 *     val page: PdfPage,
 *     val textPage: PdfTextPage,
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
 * val pageCache = PdfPageCache(pdfDocument) { page, textPage ->
 *     CustomPageHolder(page, textPage, "some other data")
 * }
 * ```
 *
 * @param H The type of the holder for the page and text page. Must be [AutoCloseable].
 * @property pdfDocument The [PdfDocument] to cache pages from.
 * @property pageHolderFactory A factory for creating a holder for a page and text page.
 */
class PdfPageCache<H : AutoCloseable>(
    private val pdfDocument: PdfDocument,
    maxSize: Long = CACHE_SIZE,
    private val pageHolderFactory: (PdfPage, PdfTextPage) -> H,
) : PdfPageCacheBase<H>(
        maxSize = maxSize,
    ) {
    override fun openPageAndText(pageIndex: Int): H? {
        val page = pdfDocument.openPage(pageIndex) ?: return null
        val textPage = page.openTextPage()
        return pageHolderFactory(page, textPage)
    }
}

data class PageHolder<TPage : AutoCloseable, TTextPage : AutoCloseable>(
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
