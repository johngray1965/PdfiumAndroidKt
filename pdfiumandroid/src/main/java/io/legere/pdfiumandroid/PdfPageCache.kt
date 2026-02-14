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
 * * }
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

/**
 * A generic data class that holds a [PdfPage] and its corresponding [PdfTextPage].
 * This class implements [AutoCloseable] to ensure that both the page and text page
 * resources are properly released when this holder is closed.
 *
 * @param TPage The type of the PDF page, which must implement [AutoCloseable].
 * @param TTextPage The type of the PDF text page, which must implement [AutoCloseable].
 * @property page The PDF page held by this instance.
 * @property textPage The PDF text page corresponding to the held PDF page.
 */
data class PageHolder<TPage : AutoCloseable, TTextPage : AutoCloseable>(
    val page: TPage,
    val textPage: TTextPage,
) : AutoCloseable {
    /**
     * Closes both the [textPage] and the [page], ensuring that native resources are released.
     * The text page is closed before the page.
     */
    override fun close() {
        try {
            textPage.close()
        } finally {
            page.close()
        }
    }
}
