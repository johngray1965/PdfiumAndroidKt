package io.legere.pdfiumandroid.arrow

import android.graphics.RectF
import arrow.core.Either
import io.legere.pdfiumandroid.PdfPageLink
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

class PdfPageLinkKtF(
    val pageLink: PdfPageLink,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    suspend fun countWebLinks(): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            pageLink.countWebLinks()
        }

    suspend fun getURL(
        index: Int,
        length: Int,
    ): Either<PdfiumKtFErrors, String?> =
        wrapEither(dispatcher) {
            pageLink.getURL(index, length)
        }

    suspend fun countRects(index: Int): Either<PdfiumKtFErrors, Int> =
        wrapEither(dispatcher) {
            pageLink.countRects(index)
        }

    suspend fun getRect(
        linkIndex: Int,
        rectIndex: Int,
    ): Either<PdfiumKtFErrors, RectF> =
        wrapEither(dispatcher) {
            pageLink.getRect(linkIndex, rectIndex)
        }

    suspend fun getTextRange(index: Int): Either<PdfiumKtFErrors, Pair<Int, Int>> =
        wrapEither(dispatcher) {
            pageLink.getTextRange(index)
        }

    override fun close() {
        pageLink.close()
    }
}
