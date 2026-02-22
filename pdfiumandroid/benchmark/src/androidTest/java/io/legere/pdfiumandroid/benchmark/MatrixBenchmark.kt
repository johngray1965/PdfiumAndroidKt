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
import io.legere.pdfiumandroid.api.types.MutablePdfMatrix
import io.legere.pdfiumandroid.api.types.PdfMatrix
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.pow
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
            val type = rand.nextInt(8)
            val p1: Float
            val p2: Float

            when (type) {
                0, 4 -> { // Translate
                    p1 = (rand.nextFloat() - 0.5f) * 100f
                    p2 = (rand.nextFloat() - 0.5f) * 100f
                }

                1, 5 -> { // Scale - use a range like 0.5 to 2.0
                    p1 = 2.0.pow((rand.nextDouble() - 0.5) * 2.0).toFloat()
                    p2 = 2.0.pow((rand.nextDouble() - 0.5) * 2.0).toFloat()
                }

                2, 6 -> { // Rotate
                    p1 = (rand.nextFloat() - 0.5f) * 720f
                    p2 = 0f // Degrees only
                }

                else -> { // Skew
                    p1 = (rand.nextFloat() - 0.5f) * 0.2f
                    p2 = (rand.nextFloat() - 0.5f) * 0.2f
                }
            }
            Op(type, p1, p2)
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

            println("pdfValues: ${pdfValues.contentToString()}, androidValues: ${androidValues.contentToString()}")
            assertMatrixClose(androidValues, pdfValues)
// )
//            assertWithMessage("expected: ${pdfValues.contentToString()}, actual: ${androidValues.contentToString()}")
//                .that(pdfValues)
//                .usingTolerance(0.00001)
//                .containsExactly(androidValues)
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

    fun assertMatrixClose(
        expected: FloatArray,
        actual: FloatArray,
    ) {
        for (i in expected.indices) {
            val exp = expected[i]
            val act = actual[i]

            // 1. If they are bit-for-bit identical, we are good.
            if (exp.toRawBits() == act.toRawBits()) continue

            // 2. Calculate the difference
            val diff = Math.abs(exp - act)

            // 3. Scale-aware epsilon:
            // For 1.0, 1e-6 is fine. For 1000.0, we need 1e-3.
            val maxVal = Math.max(Math.abs(exp), Math.abs(act))
            val epsilon = Math.max(maxVal * 1e-6f, 1e-9f)

            if (diff > epsilon) {
                throw AssertionError(
                    "Index $i failed at iteration! " +
                        "Expected: $exp, Actual: $act, Diff: $diff, Limit: $epsilon",
                )
            }
        }
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
