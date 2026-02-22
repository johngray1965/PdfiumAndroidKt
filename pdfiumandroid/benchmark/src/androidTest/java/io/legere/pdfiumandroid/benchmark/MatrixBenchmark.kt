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

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.types.MutablePdfMatrix
import io.legere.pdfiumandroid.api.types.PdfMatrix
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random
import android.graphics.Matrix as AndroidMatrix

@RunWith(AndroidJUnit4::class)
class MatrixBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    // 0: translate
    // 1: scale
    // 2: rotate
    // 3: skew
    // 4: postTranslate
    // 5: postScale
    // 6: postRotate
    // 7: postSkew
    private val operationCount = 100_000
    private val randomSeed = 42L

    // Pre-generate operations and parameters to avoid benchmarking Random
    data class Op(
        val type: Int,
        val p1: Float,
        val p2: Float,
    )

    private val operations: List<Op> by lazy {
        val rand = Random(randomSeed)
        List(operationCount) {
            Op(
                type = rand.nextInt(8),
                p1 = rand.nextFloat(),
                p2 = rand.nextFloat(),
            )
        }
    }

    @Test
    fun benchmarkMutablePdfMatrix() {
        benchmarkRule.measureRepeated {
            val matrix = MutablePdfMatrix()
            for (op in operations) {
                runWithMutablePdfMatrix(matrix, op)
            }
        }
    }

    @Test
    fun benchmarkAndroidMatrix() {
        benchmarkRule.measureRepeated {
            val matrix = AndroidMatrix()
            for (op in operations) {
                runWithAndroidMatrix(matrix, op)
            }
        }
    }

    @Test
    fun benchmarkImmutablePdfMatrix() {
        // Benchmark creating new instances (functional style)
        benchmarkRule.measureRepeated {
            var matrix = PdfMatrix()
            for (op in operations) {
                matrix =
                    when (op.type) {
                        0 -> matrix.preTranslate(op.p1, op.p2)
                        1 -> matrix.preScale(op.p1, op.p2)
                        2 -> matrix.preRotate(op.p1)
                        3 -> matrix.preSkew(op.p1, op.p2)
                        4 -> matrix.postTranslate(op.p1, op.p2)
                        5 -> matrix.postScale(op.p1, op.p2)
                        6 -> matrix.postRotate(op.p1)
                        7 -> matrix.postSkew(op.p1, op.p2)
                        else -> matrix
                    }
            }
        }
    }

    @Test
    fun verifyCorrectness() {
        val pdfMatrix = MutablePdfMatrix()
        val androidMatrix = AndroidMatrix()

        for ((i, op) in operations.withIndex()) {
            println("op: $i $op")
            runWithMutablePdfMatrix(pdfMatrix, op)
            runWithAndroidMatrix(androidMatrix, op)

            val pdfValues = pdfMatrix.values
            val androidValues = FloatArray(9)
            androidMatrix.getValues(androidValues)

            assertThat(pdfValues).usingTolerance(0.01).containsExactly(androidValues)
        }
//
//        val pdfValues = pdfMatrix.values
//        val androidValues = FloatArray(9)
//        androidMatrix.getValues(androidValues)
//
//        // Using a small delta for floating point differences
//        for (i in 0 until 9) {
//            assertEquals("Value at index $i mismatch", androidValues[i], pdfValues[i], 0.01f)
//        }
    }

    private fun runWithMutablePdfMatrix(
        matrix: MutablePdfMatrix,
        op: Op,
    ) {
        when (op.type) {
            0 -> matrix.preTranslate(op.p1, op.p2)
            1 -> matrix.preScale(op.p1, op.p2)
            2 -> matrix.preRotate(op.p1)
            3 -> matrix.preSkew(op.p1, op.p2)
            4 -> matrix.postTranslate(op.p1, op.p2)
            5 -> matrix.postScale(op.p1, op.p2)
            6 -> matrix.postRotate(op.p1)
            7 -> matrix.postSkew(op.p1, op.p2)
        }
    }

    private fun runWithAndroidMatrix(
        matrix: AndroidMatrix,
        op: Op,
    ) {
        when (op.type) {
            0 -> matrix.preTranslate(op.p1, op.p2)
            1 -> matrix.preScale(op.p1, op.p2)
            2 -> matrix.preRotate(op.p1)
            3 -> matrix.preSkew(op.p1, op.p2)
            4 -> matrix.postTranslate(op.p1, op.p2)
            5 -> matrix.postScale(op.p1, op.p2)
            6 -> matrix.postRotate(op.p1)
            7 -> matrix.postSkew(op.p1, op.p2)
        }
    }
}
