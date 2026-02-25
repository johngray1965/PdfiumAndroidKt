@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package io.legere.pdfiumandroidkt.ui

import android.graphics.Matrix
import android.graphics.Rect
import android.view.Surface
import androidx.compose.foundation.AndroidEmbeddedExternalSurface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.suspend.PdfPageKtCache
import io.legere.pdfiumandroidkt.PdfHolder
import timber.log.Timber
import kotlin.math.min

@Composable
fun PdfViewer(
    pageCache: PdfPageKtCache<PdfHolder>,
    pageNum: Int,
) {
    var surface: Surface? by remember { mutableStateOf(null) }
    var viewWidth by remember { mutableIntStateOf(0) }
    var viewHeight by remember { mutableIntStateOf(0) }

    AndroidEmbeddedExternalSurface(
        modifier = Modifier.fillMaxSize(),
    ) {
        onSurface { s, w, h ->

            s.lockCanvas(Rect(0, 0, w, h)).apply {
                drawColor(Color.White.toArgb())
                s.unlockCanvasAndPost(this)
            }
            surface = s
            viewWidth = w
            viewHeight = h
        }
    }

    LaunchedEffect(surface, pageNum, viewWidth, viewHeight) {
        val s = surface
        if (s != null && viewWidth > 0 && viewHeight > 0) {
            drawPdf(
                s,
                viewWidth,
                viewHeight,
                pageCache,
                pageNum,
            )
        }
    }
}

@Suppress("LongParameterList", "TooGenericExceptionCaught")
suspend fun drawPdf(
    surface: Surface,
    surfaceWidth: Int,
    surfaceHeight: Int,
    pageCache: PdfPageKtCache<PdfHolder>,
    currentPage: Int,
) {
    if (!surface.isValid || surfaceWidth == 0 || surfaceHeight == 0) {
        // Skip rendering if the Surface is not valid or has zero dimensions
        return
    }

    try {
        val pageHolder = pageCache.get(currentPage)
        val pageWidth = pageHolder.pageAttributes.pageWidth
        val pageHeight = pageHolder.pageAttributes.pageHeight
        // Calculate scaling factor to fit page within Surface bounds
        val scaleFactor =
            min(
                surfaceWidth.toFloat() / pageWidth,
                surfaceHeight.toFloat() / pageHeight,
            )

        val height = (pageHeight * scaleFactor).toInt()
        val width = (pageWidth * scaleFactor).toInt()

        val startX = (surfaceWidth - width) / 2
        val startY = (surfaceHeight - height) / 2

        val matrix = Matrix()
        matrix.postScale(scaleFactor, scaleFactor)
        matrix.postTranslate(startX.toFloat(), startY.toFloat())
        val clipRect =
            PdfRectF(0f, 0f, surfaceWidth.toFloat(), surfaceHeight.toFloat())
        pageHolder.page.renderPage(
            surface = surface,
            matrix.toPdfMatrix(),
            clipRect,
        )
    } catch (e: Exception) {
        Timber.d(e)
    }
}

@Suppress("MagicNumber")
private fun Matrix.toPdfMatrix(): PdfMatrix {
    val values = FloatArray(9)
    this.getValues(values)
    return PdfMatrix(values.map { it.toDouble() }.toDoubleArray())
}
