@file:Suppress("unused")

package io.legere.pdfiumandroid.suspend

import android.graphics.RectF
import io.legere.pdfiumandroid.PdfTextPage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.Closeable

class PdfTextPageKt(val page: PdfTextPage, private val dispatcher: CoroutineDispatcher) : Closeable  {

    suspend fun textPageCountChars(): Int {
        return withContext(dispatcher) {
            page.textPageCountChars()
        }
    }

    suspend fun textPageGetText(
        startIndex: Int,
        length: Int
    ): String? {
        return withContext(dispatcher) {
            page.textPageGetText(startIndex, length)
        }
    }

    suspend fun textPageGetUnicode(index: Int): Char {
        return withContext(dispatcher) {
            page.textPageGetUnicode(index)
        }
    }

    suspend fun textPageGetCharBox(index: Int): RectF? {
        return withContext(dispatcher) {
            page.textPageGetCharBox(index)
        }
    }

    suspend fun textPageGetCharIndexAtPos(
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double
    ): Int {
        return withContext(dispatcher) {
            page.textPageGetCharIndexAtPos(x, y, xTolerance, yTolerance)
        }
    }

    suspend fun textPageCountRects(
        startIndex: Int,
        count: Int
    ): Int {
        return withContext(dispatcher) {
            page.textPageCountRects(startIndex, count)
        }
    }

    suspend fun textPageGetRect(rectIndex: Int): RectF? {
        return withContext(dispatcher) {
            page.textPageGetRect(rectIndex)
        }
    }

    suspend fun textPageGetBoundedText(
        rect: RectF,
        length: Int
    ): String? {
        return withContext(dispatcher) {
            page.textPageGetBoundedText(rect, length)
        }
    }

    override fun close() {
        page.close()
    }

}
