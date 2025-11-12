package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.util.PdfPageCacheBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Concrete cache implementation for the coroutines-based [PdfPageKt] and [PdfTextPageKt].
 */
class PdfPageKtCache(
    private val pdfDocument: PdfDocumentKt,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : PdfPageSuspendCacheBase<PdfPageKt, PdfTextPageKt>(scope) {
    override suspend fun openPageAndText(pageIndex: Int): PdfPageCacheBase.PageHolder<PdfPageKt, PdfTextPageKt> {
        val page = pdfDocument.openPage(pageIndex)
        val textPage = page.openTextPage()
        return PdfPageCacheBase.PageHolder(page, textPage)
    }
}
