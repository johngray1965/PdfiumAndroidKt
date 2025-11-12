package io.legere.pdfiumandroid.arrow

import android.os.Build
import androidx.annotation.RequiresApi
import arrow.core.Either
import io.legere.pdfiumandroid.suspend.PdfPageSuspendCacheBase
import io.legere.pdfiumandroid.util.PdfPageCacheBase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Concrete cache implementation for the Arrow-based [PdfPageKtF] and [PdfTextPageKtF].
 */
class PdfPageKtFCache(
    private val pdfDocument: PdfDocumentKtF,
    private val dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : AutoCloseable {
    private val suspendCache =
        object : PdfPageSuspendCacheBase<PdfPageKtF, PdfTextPageKtF>(scope) {
            override suspend fun openPageAndText(pageIndex: Int): PdfPageCacheBase.PageHolder<PdfPageKtF, PdfTextPageKtF> {
                val page = pdfDocument.document.openPage(pageIndex)
                val textPage = page.openTextPage()
                return PdfPageCacheBase.PageHolder(PdfPageKtF(page, dispatcher), PdfTextPageKtF(textPage, dispatcher))
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getF(pageIndex: Int): Either<Throwable, PdfPageCacheBase.PageHolder<PdfPageKtF, PdfTextPageKtF>> =
        Either.catch {
            suspendCache.get(pageIndex)
        }

    override fun close() {
        suspendCache.close()
    }
}
