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
import io.legere.pdfiumandroid.unlocked.PdfDocumentU
import io.legere.pdfiumandroid.unlocked.PdfPageU
import io.legere.pdfiumandroid.unlocked.PdfTextPageU
import io.legere.pdfiumandroid.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.util.Size
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.measureTime

@RunWith(AndroidJUnit4::class)
class FastNativeTest : BasePDFTest() {
    private lateinit var pdfDocument: PdfDocumentU
    private var pdfBytes: ByteArray? = null

    @Before
    fun setUp() {
        pdfBytes = getPdfBytes("f01.pdf")

        assertThat(pdfBytes).isNotNull()

        pdfDocument = PdfiumCoreU().newDocument(pdfBytes)
    }

    @After
    fun tearDown() {
        pdfDocument.close()
    }

    @Test
    fun getPagAttributesOpenEveryPass() {
        val time =
            measureTime {
                repeat(10_000) {
                    pdfDocument.openPage(0)?.use { page ->
                        testPageAttributes(page)
                    }
                }
            }
        val averageDuration = (time / 10_000)
        println("Total Time: $time, Average Time: $averageDuration")
    }

    @Test
    fun getPagAttributesSingleOpen() {
        val time =
            measureTime {
                pdfDocument.openPage(0)?.use { page ->
                    repeat(10_000) {
                        testPageAttributes(page)
                    }
                }
            }
        val averageDuration = (time / 10_000)
        println("Total Time: $time, Average Time: $averageDuration")
    }

    @Test
    fun getPagAttributesTimeAttributesOnly() {
        pdfDocument.openPage(0)?.use { page ->
            val time =
                measureTime {
                    repeat(10_000) {
                        testPageAttributes(page)
                    }
                }
            val averageDuration = (time / 10_000)
            println("Total Time: $time, Average Time: $averageDuration")
        }
    }

    @Test
    fun getTextPagAttributesOpenEveryPass() {
        val time =
            measureTime {
                repeat(10_000) {
                    pdfDocument.openPage(0)?.use { page ->
                        page.openTextPage().use { textPage ->
                            testTextPageAttributes(textPage)
                        }
                    }
                }
            }
        val averageDuration = (time / 10_000)
        println("Total Time: $time, Average Time: $averageDuration")
    }

    @Test
    fun getPTextPagAttributesSingleOpen() {
        val time =
            measureTime {
                pdfDocument.openPage(0)?.use { page ->
                    page.openTextPage().use { textPage ->
                        repeat(10_000) {
                            testTextPageAttributes(textPage)
                        }
                    }
                }
            }
        val averageDuration = (time / 10_000)
        println("Total Time: $time, Average Time: $averageDuration")
    }

    @Test
    fun getTextPagAttributesTimeAttributesOnly() {
        pdfDocument.openPage(0)?.use { page ->
            page.openTextPage().use { textPage ->
                val time =
                    measureTime {
                        repeat(10_000) {
                            testTextPageAttributes(textPage)
                        }
                    }
                val averageDuration = (time / 10_000)
                println("Total Time: $time, Average Time: $averageDuration")
            }
        }
    }

    @Test
    fun getPagBitmapOpenEveryPass() {
        val iterations = 1_000
        val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.RGB_565)
        val time =
            measureTime {
                repeat(iterations) {
                    pdfDocument.openPage(0)?.use { page ->
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
        val averageDuration = time / iterations
        println("Total Time: $time, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapSingleOpen() {
        val iterations = 1_000
        val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.RGB_565)
        val time =
            measureTime {
                pdfDocument.openPage(0)?.use { page ->
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
        val averageDuration = (time / iterations)
        println("Total Time: $time, Average Time: $averageDuration")
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
                    pdfDocument.openPage(0)?.use { page ->
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
            measureTime {
                pdfDocument.openPage(0)?.use { page ->
                    repeat(iterations) {
                        page.renderPageBitmap(
                            bitmap,
                            matrix,
                            rect,
                        )
                    }
                }
            }
        val averageDuration = (time / iterations)
        println("Total Time: $time, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapViaMatrixSingleOpen8x() {
        val iterations = 100
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.RGB_565)
        val time =
            measureTime {
                pdfDocument.openPage(0)?.use { page ->
                    repeat(iterations) {
                        page.renderPageBitmap(
                            bitmap,
                            matrix,
                            rect,
                        )
                    }
                }
            }
        val averageDuration = (time / iterations)
        println("Total Time: $time, Average Time: $averageDuration")
    }

    @Test
    fun getPagBitmapViaMatrixSingleOpen8xARGB_8888() {
        val iterations = 100
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.ARGB_8888)
        val time =
            measureTime {
                pdfDocument.openPage(0)?.use { page ->
                    repeat(iterations) {
                        page.renderPageBitmap(
                            bitmap,
                            matrix,
                            rect,
                        )
                    }
                }
            }
        val averageDuration = (time / iterations)
        println("Total Time: $time, Average Time: $averageDuration")
    }

    fun findWordRanges(text: String): List<Pair<Int, Int>> {
        val boundaries = Regex("\\b").findAll(text).map { it.range.first }.toMutableList()
        boundaries.add(text.length)
        return boundaries.zipWithNext { start, end -> Pair(start, end - start) }.filter { it.first < text.length }
    }

    @Test
    fun gettextPageGetRectsForRanges() {
        pdfDocument.openPage(0)?.use { page ->
            page.openTextPage().use { textPage ->
                val textCharCount =
                    textPage.textPageCountChars()
                if (textCharCount > 0) {
                    val pageText =
                        textPage.textPageGetText(
                            0,
                            textCharCount,
                        )
                            ?: ""
                    val wordBoundaries = findWordRanges(pageText)
                    val wordRangesArray =
                        wordBoundaries
                            .flatMap { listOf(it.first, it.second) }
                            .toIntArray()
                    val iterations = 100
                    val time =
                        measureTime {
                            repeat(iterations) {
                                val result = textPage.textPageGetRectsForRanges(wordRangesArray)
                                assertThat(result).isNotNull()
                                assertThat(result?.size).isEqualTo(1238)
                            }
                        }
                    val averageDuration = (time / iterations)
                    println("Total Time: $time, Average Time: $averageDuration")
                }
            }
        }
    }

    @Test
    fun gettextPageGetRects() {
        val iterations = 100
        pdfDocument.openPage(0)?.use { page ->
            page.openTextPage().use { textPage ->
                val textCharCount =
                    textPage.textPageCountChars()
                if (textCharCount > 0) {
                    val pageText =
                        textPage.textPageGetText(
                            0,
                            textCharCount,
                        )
                            ?: ""
                    val wordBoundaries = findWordRanges(pageText)
                    val time =
                        measureTime {
                            repeat(iterations) {
                                val list = mutableListOf<RectF>()
                                wordBoundaries.forEach {
                                    val count = textPage.textPageCountRects(it.first, it.second)
                                    repeat(count) {
                                        textPage.textPageGetRect(it)?.let { rect ->
                                            list.add(rect)
                                        }
                                    }
//                                    println("list: ${it.first} ${it.second} $list")
                                }
                                assertThat(list).isNotNull()
                                assertThat(list.size).isEqualTo(1238)
                            }
                        }
                    val averageDuration = (time / iterations)
                    println("Total Time: $time, Average Time: $averageDuration")
                }
            }
        }
    }

    @Test
    fun getPagBitmapViaMatrixSingleOpen8xARGB_8888ReadFromDisk() {
        val iterations = 100
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.ARGB_8888)
        pdfDocument.openPage(0)?.use { page ->
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
            measureTime {
                repeat(iterations) {
                    val bitmapFromDisk = BitmapFactory.decodeFile(targetCtx.filesDir.path + "/test.png", bitmapOptios)
                    assertThat(bitmapFromDisk).isNotNull()
                }
            }
        val averageDuration = (time / iterations)
        println("Total Time: $time, Average Time: $averageDuration")
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

    private fun testTextPageAttributes(page: PdfTextPageU) {
        val textPageCountChars = page.textPageCountChars()
        val textPageCountRects = page.textPageCountRects(0, textPageCountChars)
        assertThat(textPageCountChars).isEqualTo(3468)
        val textPageGetText = page.textPageGetText(0, textPageCountChars)
        assertThat(textPageGetText).startsWith("The 50 Best Videos For Kids")
        val textPageGetUnicode = page.textPageGetUnicode(0)
        assertThat(textPageGetUnicode).isEqualTo('T')
        val textPageGetCharBox = page.textPageGetCharBox(0)
        assertThat(textPageGetCharBox).isEqualTo(RectF(90.314415f, 715.3187f, 103.44171f, 699.1206f))

        repeat(textPageCountRects) {
            page.textPageGetRect(it)
        }
    }

    private fun testPageAttributes(page: PdfPageU) {
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
