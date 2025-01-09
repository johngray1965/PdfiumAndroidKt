@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package io.legere.pdfiumandroidkt.ui

import android.graphics.Matrix
import android.graphics.RectF
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.legere.pdfiumandroid.suspend.PdfDocumentKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.min
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.nanoseconds

@Composable
fun PdfViewer(
    pdfDocument: PdfDocumentKt,
    pageNum: Int,
) {
    val surfaceState = remember { mutableStateOf<Surface?>(null) }
    val surfaceViewState = remember { mutableStateOf<SurfaceView?>(null) } // Store SurfaceView
    AndroidView(
        factory = { context ->
            SurfaceView(context).apply {
                surfaceViewState.value = this // Store SurfaceView in state
                holder.addCallback(
                    object : SurfaceHolder.Callback {
                        // ... surface lifecycle callbacks ...
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            surfaceState.value = holder.surface
                            CoroutineScope(Dispatchers.IO).launch {
                                drawPdf(
                                    holder.surface,
                                    holder,
                                    pdfDocument,
                                    pageNum,
                                )
                            }
                        }

                        override fun surfaceChanged(
                            p0: SurfaceHolder,
                            p1: Int,
                            p2: Int,
                            p3: Int,
                        ) {
                            // Handle surface changes if needed
                        }

                        override fun surfaceDestroyed(p0: SurfaceHolder) {
                            // Handle surface destruction if needed
                        }
                    },
                )
            }
        },
        modifier =
            Modifier
                .fillMaxSize(),
//                .pointerInput(Unit) {
//                    detectTransformGestures { centroid, pan, detectedZoom, rotation ->
//                        val pinchCenterX = centroid.x
//                        val pinchCenterY = centroid.y
//
// //                    debouncedZoom(this@PdfViewer.zoom * zoom)
// //                    debouncedPanX(this@PdfViewer.panX + pan.x)
// //                    debouncedPanY(this@PdfViewer.panY + pan.y)
// //
// //
//                        zoom *= detectedZoom
//                        panX += pan.x
//                        panY += pan.y
//
//                        transformMatrix.translate(-pinchCenterX, -pinchCenterY)
//                        transformMatrix.scale(zoom, zoom)
//                        transformMatrix.translate(panX + pinchCenterX, panY + pinchCenterY)
//                        // Update zoom and pan state
//                    }
//                },
    )
}

@Suppress("LongParameterList")
suspend fun drawPdf(
    surface: Surface,
    holder: SurfaceHolder,
    pdfDocument: PdfDocumentKt,
    currentPage: Int,
) {
    val surfaceFrame = holder.surfaceFrame
//    val canvas =
//        surface.lockCanvas(
//            Rect(
//                0,
//                0,
//                surfaceFrame.width(),
//                surfaceFrame.height(),
//            ),
//        ) ?: return
    try {
        // Clear the canvas
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        if (!surface.isValid || surfaceFrame.width() == 0 || surfaceFrame.height() == 0) {
            // Skip rendering if the Surface is not valid or has zero dimensions
            return
        }

        pdfDocument.openPage(currentPage).use { page ->
            val pageWidth = page.getPageWidthPoint()
            val pageHeight = page.getPageHeightPoint()
            // Calculate scaling factor to fit page within Surface bounds
            val scaleFactor =
                min(
                    surfaceFrame.width().toFloat() / pageWidth,
                    surfaceFrame.height().toFloat() / pageHeight,
                )

            val height = (pageHeight * scaleFactor).toInt()
            val width = (pageWidth * scaleFactor).toInt()

            val startX = (surfaceFrame.width() - width) / 2
            val startY = (surfaceFrame.height() - height) / 2

            val matrix = Matrix()
            matrix.postScale(scaleFactor, scaleFactor)
            matrix.postTranslate(startX.toFloat(), startY.toFloat())
            val clipRect =
                RectF(0f, 0f, surfaceFrame.width().toFloat(), surfaceFrame.height().toFloat())
            val time =
                measureNanoTime {
                    page.renderPage(
                        surface = surface,
                        matrix,
                        clipRect,
                    )
                }
            val duration = time.nanoseconds
            Timber.d("drawPdf: time: $duration")
        }
    } finally {
//        surface.unlockCanvasAndPost(canvas)
    }
}
