package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.util.PdfPageCacheBase
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Concrete cache implementation for the coroutines-based [PdfPageKt] and [PdfTextPageKt].
 */
class PdfPageKtCache(
    private val pdfDocument: PdfDocumentKt,
    dispatcher: CoroutineDispatcher,
) : PdfPageSuspendCacheBase<PdfPageKt, PdfTextPageKt>(dispatcher) {
    override suspend fun openPageAndText(pageIndex: Int): PdfPageCacheBase.PageHolder<PdfPageKt, PdfTextPageKt> {
        val page = pdfDocument.openPage(pageIndex)
        val textPage = page.openTextPage()
        return PdfPageCacheBase.PageHolder(page, textPage)
    }
}
