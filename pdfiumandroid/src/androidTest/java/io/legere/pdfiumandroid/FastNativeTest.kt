package io.legere.pdfiumandroid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.base.BasePDFTest
import io.legere.pdfiumandroid.util.Size
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.nanoseconds

@RunWith(AndroidJUnit4::class)
class FastNativeTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocument
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCore().newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun getPagAttributesOpenEveryPass() {
        val time =
            measureNanoTime {
                repeat(10_000) {
                    pdfDocument.openPage(0).use { page ->
                        testPageAttributes(page)
                    }
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / 10_000).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    @Test
    fun getPagAttributesSingleOpen() {
        val time =
            measureNanoTime {
                pdfDocument.openPage(0).use { page ->
                    repeat(10_000) {
                        testPageAttributes(page)
                    }
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / 10_000).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapOpenEveryPass() {
        val iterations = 1_000
        val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.RGB_565)
        val time =
            measureNanoTime {
                repeat(iterations) {
                    pdfDocument.openPage(0).use { page ->
                        page.renderPageBitmap(
                            bitmap,
                            0,
                            0,
                            612,
                            792,
                        )
                    }
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / iterations).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapSingleOpen() {
        val iterations = 1_000
        val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.RGB_565)
        val time =
            measureNanoTime {
                pdfDocument.openPage(0).use { page ->
                    repeat(iterations) {
                        page.renderPageBitmap(
                            bitmap,
                            0,
                            0,
                            612,
                            792,
                        )
                    }
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / iterations).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapViaMatrixOpenEveryPass() {
        val iterations = 1_000
        val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.RGB_565)
        val rect = RectF(0f, 0f, 612f, 792f)
        val matrix = Matrix()
        val time =
            measureNanoTime {
                repeat(iterations) {
                    pdfDocument.openPage(0).use { page ->
                        page.renderPageBitmap(
                            bitmap,
                            matrix,
                            rect,
                        )
                    }
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / iterations).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapViaMatrixSingleOpen() {
        val iterations = 1_000
        val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.RGB_565)
        val rect = RectF(0f, 0f, 612f, 792f)
        val matrix = Matrix()
        val time =
            measureNanoTime {
                pdfDocument.openPage(0).use { page ->
                    repeat(iterations) {
                        page.renderPageBitmap(
                            bitmap,
                            matrix,
                            rect,
                        )
                    }
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / iterations).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapViaMatrixSingleOpen8x() {
        val iterations = 100
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.RGB_565)
        val time =
            measureNanoTime {
                pdfDocument.openPage(0).use { page ->
                    repeat(iterations) {
                        page.renderPageBitmap(
                            bitmap,
                            matrix,
                            rect,
                        )
                    }
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / iterations).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapViaMatrixSingleOpen8xARGB_8888() {
        val iterations = 100
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.ARGB_8888)
        val time =
            measureNanoTime {
                pdfDocument.openPage(0).use { page ->
                    repeat(iterations) {
                        page.renderPageBitmap(
                            bitmap,
                            matrix,
                            rect,
                        )
                    }
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / iterations).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapViaMatrixSingleOpen8xARGB_8888ReadFromDisk() {
        val iterations = 100
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.ARGB_8888)
        pdfDocument.openPage(0).use { page ->
            repeat(iterations) {
                page.renderPageBitmap(
                    bitmap,
                    matrix,
                    rect,
                )
            }
        }
        val targetCtx: Context = InstrumentationRegistry.getInstrumentation().targetContext
        targetCtx.openFileOutput("test.png", Context.MODE_PRIVATE).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val bitmapOptios =
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inJustDecodeBounds = false
                inBitmap = bitmap
                inSampleSize = 1
            }
        val time =
            measureNanoTime {
                repeat(iterations) {
                    val bitmapFromDisk = BitmapFactory.decodeFile(targetCtx.filesDir.path + "/test.png", bitmapOptios)
                    assertThat(bitmapFromDisk).isNotNull()
                }
            }
        val totalDuration = time.nanoseconds
        val averageDuration = (time / iterations).nanoseconds
        println("Total Time: $totalDuration, Average Time: $averageDuration")
    }

    private fun commonParams8X(bitmapConfig: Bitmap.Config): Triple<Bitmap, RectF, Matrix> {
        val scaleFactor = (1080f / 612) * 8
        val width = 1080 * 3
        val height = 2280 * 3
        val bitmap =
            Bitmap.createBitmap(
                width,
                height,
                bitmapConfig,
            )
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val matrix = Matrix()
        matrix.postScale(scaleFactor, scaleFactor)
        return Triple(bitmap, rect, matrix)
    }

    private fun testPageAttributes(page: PdfPage) {
        val pageWidth = page.getPageWidth(72)
        val pageHeight = page.getPageHeight(72)
        val pageWidthPoint = page.getPageWidthPoint()
        val pageHeightPoint = page.getPageHeightPoint()
        val cropBox = page.getPageCropBox()
        val mediaBox = page.getPageMediaBox()
        val bleedBox = page.getPageBleedBox()
        val trimBox = page.getPageTrimBox()
        val artBox = page.getPageArtBox()
        val boundingBox = page.getPageBoundingBox()
        val size = page.getPageSize(72)
        val links = page.getPageLinks()
        val devicePt = page.mapRectToDevice(0, 0, 100, 100, 0, RectF(0f, 0f, 100f, 100f))

        assertThat(pageWidth).isEqualTo(612) // 8.5 inches * 72 dpi
        assertThat(pageHeight).isEqualTo(792) // 11 inches * 72 dpi
        assertThat(pageWidthPoint).isEqualTo(612) // 11 inches * 72 dpi
        assertThat(pageHeightPoint).isEqualTo(792) // 11 inches * 72 dpi
        assertThat(cropBox).isEqualTo(noResultRect)
        assertThat(mediaBox).isEqualTo(RectF(0.0f, 0.0f, 612.0f, 792.0f))
        assertThat(bleedBox).isEqualTo(noResultRect)
        assertThat(trimBox).isEqualTo(noResultRect)
        assertThat(artBox).isEqualTo(noResultRect)
        assertThat(boundingBox).isEqualTo(RectF(0f, 792f, 612f, 0f))
        assertThat(size).isEqualTo(Size(612, 792))
        assertThat(links.size).isEqualTo(0) // The test doc doesn't have links
        assertThat(devicePt).isEqualTo(
            Rect(
                // 0f in coords to 0f in device
                0,
                // 0f in corrds in at the bottom, the bottom of the device is 100f
                100,
                // 100f in coords = 100f/(8.5*72) * 100f = 16f
                16,
                // 100f in coords = 100 - 100f/(11*72) * 100f = 87f
                87,
            ),
        )
    }
}
