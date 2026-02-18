/*
 * Original work Copyright 2015 Bekket McClane
 * Modified work Copyright 2016 Bartosz Schiller
 * Modified work Copyright 2023-2026 John Gray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.legere.pdfiumandroid.benchmark

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfPage
import io.legere.pdfiumandroid.PdfTextPage
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfRectF
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class PDFBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    fun getPdfBytes(filename: String): ByteArray? {
        val appContext = InstrumentationRegistry.getInstrumentation().context
        val assetManager = appContext.assets
        try {
            val input = assetManager.open(filename)
            return input.readBytes()
        } catch (e: Exception) {
            Log.e(PDFBenchmark::class.simpleName, "Ugh", e)
        }
        assetManager.close()
        return null
    }

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
    fun getPageAttributes() {
        pdfDocument.openPage(0)?.use { page ->
            benchmarkRule.measureRepeated {
                testPageAttributes(page)
            }
        }
    }

    @Test
    fun getTextPageAttributes() {
        pdfDocument.openPage(0)?.use { page ->
            page.openTextPage().use { textPage ->
                benchmarkRule.measureRepeated {
                    testTextPageAttributes(textPage)
                }
            }
        }
    }

    @Test
    fun getTextPageText() {
        pdfDocument.openPage(0)?.use { page ->
            page.openTextPage().use { textPage ->
                benchmarkRule.measureRepeated {
                    val textPageCountChars = textPage.textPageCountChars()
                    textPage.textPageGetText(0, textPageCountChars)
                }
            }
        }
    }

    @Test
    fun getPageBitmap() {
        val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.RGB_565)
        pdfDocument.openPage(0)?.use { page ->
            benchmarkRule.measureRepeated {
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

    @Test
    fun getPagBitmapViaMatrix() {
        val bitmap = Bitmap.createBitmap(612, 792, Bitmap.Config.RGB_565)
        val rect = PdfRectF(0f, 0f, 612f, 792f)
        val matrix = PdfMatrix()
        pdfDocument.openPage(0)?.use { page ->
            benchmarkRule.measureRepeated {
                page.renderPageBitmap(
                    bitmap,
                    matrix,
                    rect,
                )
            }
        }
    }

    @Test
    fun openPage() {
        benchmarkRule.measureRepeated {
            pdfDocument.openPage(0)?.use { _ ->
            }
        }
    }

    @Test
    fun openTextPage() {
        pdfDocument.openPage(0)?.use { page ->
            benchmarkRule.measureRepeated {
                page.openTextPage().use { _ ->
                }
            }
        }
    }

    @Test
    fun getPagBitmapViaMatrix8x() {
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.RGB_565)
        pdfDocument.openPage(0)?.use { page ->
            benchmarkRule.measureRepeated {
                page.renderPageBitmap(
                    bitmap,
                    matrix,
                    rect,
                )
            }
        }
    }

    @Test
    fun getPagBitmapViaMatrix8xARGB_8888() {
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.ARGB_8888)
        pdfDocument.openPage(0)?.use { page ->
            benchmarkRule.measureRepeated {
                page.renderPageBitmap(
                    bitmap,
                    matrix,
                    rect,
                )
            }
        }
    }

    fun findWordRanges(text: String): List<Pair<Int, Int>> {
        val boundaries = Regex("\\b").findAll(text).map { it.range.first }.toMutableList()
        boundaries.add(text.length)
        return boundaries
            .zipWithNext { start, end -> Pair(start, end - start) }
            .filter { it.first < text.length }
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
                    benchmarkRule.measureRepeated {
                        val result = textPage.textPageGetRectsForRanges(wordRangesArray)
                        assertThat(result).isNotNull()
                        assertThat(result?.size).isEqualTo(1238)
                    }
                }
            }
        }
    }

    @Test
    fun getTextPageGetRects() {
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
                    val wordBoundaries =
                        findWordRanges(pageText)
                            .flatMap { (first, second) ->
                                listOf(
                                    first,
                                    second,
                                )
                            }.toIntArray()
                    benchmarkRule.measureRepeated {
                        val list = textPage.textPageGetRectsForRanges(wordBoundaries)
                        assertThat(list).isNotNull()
                        assertThat(list?.size).isEqualTo(1238)
                    }
                }
            }
        }
    }

    @Test
    fun getRenderPageBitmap() {
        val (bitmap, rect, matrix) = commonParams8X(Bitmap.Config.ARGB_8888)
        pdfDocument.openPage(0)?.use { page ->
            benchmarkRule.measureRepeated {
                page.renderPageBitmap(
                    bitmap,
                    matrix,
                    rect,
                )
            }
        }
    }

    @Test
    fun getPagBitmapViaMatrix8xARGB_8888ReadFromDisk() {
        val (bitmap, _, _) = commonParams8X(Bitmap.Config.ARGB_8888)
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
        benchmarkRule.measureRepeated {
            val bitmapFromDisk =
                BitmapFactory.decodeFile(targetCtx.filesDir.path + "/test.png", bitmapOptios)
            assertThat(bitmapFromDisk).isNotNull()
        }
    }

    private fun commonParams8X(bitmapConfig: Bitmap.Config): Triple<Bitmap, PdfRectF, PdfMatrix> {
        val scaleFactor = (1080f / 612) * 8
        val width = 1080 * 3
        val height = 2280 * 3
        val bitmap =
            Bitmap.createBitmap(
                width,
                height,
                bitmapConfig,
            )
        val rect = PdfRectF(0f, 0f, width.toFloat(), height.toFloat())
        val matrix = PdfMatrix()
        matrix.postScale(scaleFactor, scaleFactor)
        return Triple(bitmap, rect, matrix)
    }

    private fun testTextPageAttributes(page: PdfTextPage) {
        val textPageCountChars = page.textPageCountChars()
        val textPageCountRects = page.textPageCountRects(0, textPageCountChars)
//        assertThat(textPageCountChars).isEqualTo(3468)
        page.textPageGetText(0, textPageCountChars)
//        assertThat(textPageGetText).startsWith("The 50 Best Videos For Kids")
        page.textPageGetUnicode(0)
//        assertThat(textPageGetUnicode).isEqualTo('T')
        page.textPageGetCharBox(0)
//        assertThat(textPageGetCharBox).isEqualTo(
//            RectF(
//                90.314415f,
//                715.3187f,
//                103.44171f,
//                699.1206f,
//            ),
//        )

        repeat(textPageCountRects) {
            page.textPageGetRect(it)
        }
    }

    private fun testPageAttributes(page: PdfPage) {
        page.getPageAttributes()
//        val pageWidth = page.getPageWidth(72)
//        val pageHeight = page.getPageHeight(72)
//        val pageWidthPoint = page.getPageWidthPoint()
//        val pageHeightPoint = page.getPageHeightPoint()
//        val cropBox = page.getPageCropBox()
//        val mediaBox = page.getPageMediaBox()
//        val bleedBox = page.getPageBleedBox()
//        val trimBox = page.getPageTrimBox()
//        val artBox = page.getPageArtBox()
//        val boundingBox = page.getPageBoundingBox()
//        val size = page.getPageSize(72)
//        val links = page.getPageLinks()
//        val devicePt = page.mapRectToDevice(0, 0, 100, 100, 0, RectF(0f, 0f, 100f, 100f))

//        assertThat(pageAttributes.pageWidth).isEqualTo(612) // 8.5 inches * 72 dpi
//        assertThat(pageAttributes.pageHeight).isEqualTo(792) // 11 inches * 72 dpi
// //        assertThat(pageWidthPoint).isEqualTo(612) // 11 inches * 72 dpi
// //        assertThat(pageHeightPoint).isEqualTo(792) // 11 inches * 72 dpi
//        assertThat(pageAttributes.cropBox).isEqualTo(noResultRect)
//        assertThat(pageAttributes.mediaBox).isEqualTo(RectF(0.0f, 0.0f, 612.0f, 792.0f))
//        assertThat(pageAttributes.bleedBox).isEqualTo(noResultRect)
//        assertThat(pageAttributes.trimBox).isEqualTo(noResultRect)
//        assertThat(pageAttributes.artBox).isEqualTo(noResultRect)
//        assertThat(pageAttributes.boundingBox).isEqualTo(RectF(0f, 792f, 612f, 0f))
// //        assertThat(size).isEqualTo(Size(612, 792))
//        assertThat(pageAttributes.links.size).isEqualTo(0) // The test doc doesn't have links
//        assertThat(pageAttributes.devicePt).isEqualTo(
//            Rect(
//                // 0f in coords to 0f in device
//                0,
//                // 0f in corrds in at the bottom, the bottom of the device is 100f
//                100,
//                // 100f in coords = 100f/(8.5*72) * 100f = 16f
//                16,
//                // 100f in coords = 100 - 100f/(11*72) * 100f = 87f
//                87,
//            ),
//        )
    }
}
