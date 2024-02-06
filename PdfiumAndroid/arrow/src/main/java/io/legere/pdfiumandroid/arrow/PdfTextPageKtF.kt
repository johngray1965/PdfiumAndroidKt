@file:Suppress("unused")

package io.legere.pdfiumandroid.arrow

import android.graphics.RectF
import arrow.core.Either
import io.legere.pdfiumandroid.PdfTextPage
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

/**
 * PdfTextPageKtF represents a single text page of a PDF file.
 * @property page the [PdfTextPage] to wrap
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 */
@Suppress("TooManyFunctions")
class PdfTextPageKtF(private val page: PdfTextPage, private val dispatcher: CoroutineDispatcher) : Closeable {

    /**
     * suspend version of [PdfTextPage.textPageCountChars]
     */
    suspend fun textPageCountChars(): Either<PdfiumKtFErrors, Int> {
        return wrapEither(dispatcher) {
            page.textPageCountChars()
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetText]
     */
    suspend fun textPageGetText(
        startIndex: Int,
        length: Int
    ): Either<PdfiumKtFErrors, String?> {
        return wrapEither(dispatcher) {
            page.textPageGetText(startIndex, length)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetUnicode]
     */
    suspend fun textPageGetUnicode(index: Int): Either<PdfiumKtFErrors, Char> {
        return wrapEither(dispatcher) {
            page.textPageGetUnicode(index)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetCharBox]
     */
    suspend fun textPageGetCharBox(index: Int): Either<PdfiumKtFErrors, RectF?> {
        return wrapEither(dispatcher) {
            page.textPageGetCharBox(index)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetCharIndexAtPos]
     */
    suspend fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double
    ): Either<PdfiumKtFErrors, Int> {
        return wrapEither(dispatcher) {
            page.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageCountRects]
     */
    suspend fun textPageCountRects(
        startIndex: Int,
        count: Int
    ): Either<PdfiumKtFErrors, Int> {
        return wrapEither(dispatcher) {
            page.textPageCountRects(startIndex, count)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetRect]
     */
    suspend fun textPageGetRect(rectIndex: Int): Either<PdfiumKtFErrors, RectF?> {
        return wrapEither(dispatcher) {
            page.textPageGetRect(rectIndex)
        }
    }

    /**
     * suspend version of [PdfTextPage.textPageGetBoundedText]
     */
    suspend fun textPageGetBoundedText(
        rect: RectF,
        length: Int
    ): Either<PdfiumKtFErrors, String?> {
        return wrapEither(dispatcher) {
            page.textPageGetBoundedText(rect, length)
        }
    }

    /**
     * suspend version of [PdfTextPage.getFontSize]
     */
    suspend fun getFontSize(charIndex: Int): Either<PdfiumKtFErrors, Double> {
        return wrapEither(dispatcher) {
            page.getFontSize(charIndex)
        }
    }

    /**
     * Close the page and free all resources.
     */
    override fun close() {
        page.close()
    }

    fun safeClose(): Either<PdfiumKtFErrors, Boolean> {
        return Either.catch {
            page.close()
            true
        }.mapLeft { exceptionToPdfiumKtFError(it) }
    }
}
