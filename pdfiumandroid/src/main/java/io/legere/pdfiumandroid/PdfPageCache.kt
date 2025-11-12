package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.util.PdfPageCacheBase

class PdfPageCache(
    private val pdfDocument: PdfDocument,
) : PdfPageCacheBase<PdfPage, PdfTextPage>() {
    override fun openPageAndText(pageIndex: Int): PageHolder<PdfPage, PdfTextPage> {
        val page = pdfDocument.openPage(pageIndex)
        val textPage = page.openTextPage()
        return PageHolder(page, textPage)
    }
}
