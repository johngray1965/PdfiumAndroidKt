@file:Suppress("unused")

package io.legere.pdfiumandroid.arrow

import android.os.Build
import androidx.annotation.RequiresApi
import arrow.core.Either
import io.legere.pdfiumandroid.suspend.PdfPageSuspendCacheBase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

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
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val pageHolderFactory: (PdfPageKtF, PdfTextPageKtF) -> H,
) : AutoCloseable {
    private val suspendCache =
        object : PdfPageSuspendCacheBase<H>(dispatcher) {
            @Suppress("MaxLineLength")
            override suspend fun openPageAndText(pageIndex: Int): H {
                val page = pdfDocument.document.openPage(pageIndex)
                val textPage = page.openTextPage()
                return pageHolderFactory(
                    PdfPageKtF(page, dispatcher),
                    PdfTextPageKtF(textPage, dispatcher),
                )
            }
        }

    /**
     * Get a page from the cache.
     *
     * @param pageIndex The index of the page to get.
     * @return An [Either] with the page holder or an error.
     */
    @RequiresApi(Build.VERSION_CODES.N)
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
