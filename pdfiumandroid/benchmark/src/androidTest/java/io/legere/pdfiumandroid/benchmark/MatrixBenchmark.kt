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
import com.google.common.truth.Truth.assertWithMessage
import io.legere.pdfiumandroid.api.types.MutablePdfMatrix
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.toFloatArray
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs
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
    enum class OpType {
        PRE_TRANSLATE,
        PRE_SCALE,
        PRE_ROTATE,
        PRE_SKEW,
        POST_TRANSLATE,
        POST_SCALE,
        POST_ROTATE,
        POST_SKEW,
    }

    data class Op(
        val type: OpType,
        val p1: Float,
        val p2: Float,
    )

    data class DoubleOp(
        val type: OpType,
        val p1: Double,
        val p2: Double,
    )

    private val operations: List<Op> by lazy {
        val rand = Random(randomSeed)
        List(operationCount) {
            val type = OpType.entries[rand.nextInt(OpType.entries.size)]
            val p1: Float
            val p2: Float

            when (type) {
                OpType.PRE_TRANSLATE, OpType.POST_TRANSLATE -> { // Translate
                    p1 = (rand.nextFloat() - 0.5f) * 10f
                    p2 = (rand.nextFloat() - 0.5f) * 10f
                }

                OpType.PRE_SCALE, OpType.POST_SCALE -> { // Scale - use a range like 0.9 to 1.1
                    p1 = 1.0f + (rand.nextFloat() - 0.5f) * 0.2f
                    p2 = 1.0f + (rand.nextFloat() - 0.5f) * 0.2f
                }

                OpType.PRE_ROTATE, OpType.POST_ROTATE -> { // Rotate
                    if (rand.nextBoolean()) {
                        // 50% chance for cardinal angles
                        p1 = listOf(0f, 90f, 180f, 270f).random(rand)
                    } else {
                        // 50% chance for random angles
                        p1 = (rand.nextFloat() - 0.5f) * 90f
                    }
                    p2 = 0f // Degrees only
                }

                else -> { // Skew
                    p1 = (rand.nextFloat() - 0.5f) * 0.1f
                    p2 = (rand.nextFloat() - 0.5f) * 0.1f
                }
            }
            Op(type, p1, p2)
        }
    }
    private val doubleOperations: List<DoubleOp> by lazy {
        val rand = Random(randomSeed)
        List(operationCount) {
            val type = OpType.entries[rand.nextInt(OpType.entries.size)]
            val p1: Double
            val p2: Double

            when (type) {
                OpType.PRE_TRANSLATE, OpType.POST_TRANSLATE -> { // Translate
                    p1 = (rand.nextDouble() - 0.5) * 10
                    p2 = (rand.nextDouble() - 0.5) * 10
                }

                OpType.PRE_SCALE, OpType.POST_SCALE -> { // Scale - use a range like 0.9 to 1.1
                    p1 = 1.0f + (rand.nextDouble() - 0.5) * 0.2
                    p2 = 1.0f + (rand.nextDouble() - 0.5) * 0.2
                }

                OpType.PRE_ROTATE, OpType.POST_ROTATE -> { // Rotate
                    if (rand.nextBoolean()) {
                        // 50% chance for cardinal angles
                        p1 = listOf(0.0, 90.0, 180.0, 270.0).random(rand)
                    } else {
                        // 50% chance for random angles
                        p1 = (rand.nextDouble() - 0.5) * 90
                    }
                    p2 = 0.0 // Degrees only
                }

                else -> { // Skew
                    p1 = (rand.nextDouble() - 0.5) * 0.1
                    p2 = (rand.nextDouble() - 0.5) * 0.1
                }
            }
            DoubleOp(type, p1, p2)
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
    fun benchmarkMutablePdfMatrixWithDoubles() {
        benchmarkRule.measureRepeated {
            val matrix = MutablePdfMatrix()
            for (op in doubleOperations) {
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
                        OpType.PRE_TRANSLATE -> matrix.preTranslate(op.p1, op.p2)
                        OpType.PRE_SCALE -> matrix.preScale(op.p1, op.p2)
                        OpType.PRE_ROTATE -> matrix.preRotate(op.p1)
                        OpType.PRE_SKEW -> matrix.preSkew(op.p1, op.p2)
                        OpType.POST_TRANSLATE -> matrix.postTranslate(op.p1, op.p2)
                        OpType.POST_SCALE -> matrix.postScale(op.p1, op.p2)
                        OpType.POST_ROTATE -> matrix.postRotate(op.p1)
                        OpType.POST_SKEW -> matrix.postSkew(op.p1, op.p2)
                    }
            }
        }
    }

    @Test
    fun benchmarkMutablePdfMatrixCreation() {
        // Benchmark creating new instances (functional style)
        benchmarkRule.measureRepeated {
            MutablePdfMatrix()
        }
    }

    @Test
    fun benchmarkAndroidMatrixCreation() {
        // Benchmark creating new instances (functional style)
        benchmarkRule.measureRepeated {
            AndroidMatrix()
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

            val pdfValues = pdfMatrix.values.toFloatArray()
            val androidValues = FloatArray(9)
            androidMatrix.getValues(androidValues)

            println("pdfValues:     ${pdfValues.contentToString()}")
            println("androidValues: ${androidValues.contentToString()}")
//            assertThat(pdfValues).usingTolerance(0.01).containsExactly(androidValues)
//            assertMatrixClose(androidValues, pdfValues)
// )
//            assertWithMessage("expected: ${pdfValues.contentToString()}, actual: ${androidValues.contentToString()}")
//                .that(pdfValues)
//                .usingTolerance(0.1)
//                .containsExactly(androidValues)
        }
//
        val pdfValues = pdfMatrix.values.toFloatArray()
        val androidValues = FloatArray(9)
        androidMatrix.getValues(androidValues)

        assertWithMessage("expected: ${pdfValues.contentToString()}, actual: ${androidValues.contentToString()}")
            .that(pdfValues)
            .usingTolerance(0.1)
            .containsExactly(androidValues)

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
            val diff = abs(exp - act)

            // 3. Scale-aware epsilon:
            // For 1.0, 1e-6 is fine. For 1000.0, we need 1e-3.
            val maxVal = abs(exp).coerceAtLeast(abs(act))
            val epsilon = (maxVal * 1e-6f).coerceAtLeast(1e-9f)

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
            OpType.PRE_TRANSLATE -> matrix.preTranslate(op.p1, op.p2)
            OpType.PRE_SCALE -> matrix.preScale(op.p1, op.p2)
            OpType.PRE_ROTATE -> matrix.preRotate(op.p1)
            OpType.PRE_SKEW -> matrix.preSkew(op.p1, op.p2)
            OpType.POST_TRANSLATE -> matrix.postTranslate(op.p1, op.p2)
            OpType.POST_SCALE -> matrix.postScale(op.p1, op.p2)
            OpType.POST_ROTATE -> matrix.postRotate(op.p1)
            OpType.POST_SKEW -> matrix.postSkew(op.p1, op.p2)
        }
    }

    private fun runWithMutablePdfMatrix(
        matrix: MutablePdfMatrix,
        op: DoubleOp,
    ) {
        when (op.type) {
            OpType.PRE_TRANSLATE -> matrix.preTranslate(op.p1, op.p2)
            OpType.PRE_SCALE -> matrix.preScale(op.p1, op.p2)
            OpType.PRE_ROTATE -> matrix.preRotate(op.p1)
            OpType.PRE_SKEW -> matrix.preSkew(op.p1, op.p2)
            OpType.POST_TRANSLATE -> matrix.postTranslate(op.p1, op.p2)
            OpType.POST_SCALE -> matrix.postScale(op.p1, op.p2)
            OpType.POST_ROTATE -> matrix.postRotate(op.p1)
            OpType.POST_SKEW -> matrix.postSkew(op.p1, op.p2)
        }
    }

    private fun runWithAndroidMatrix(
        matrix: AndroidMatrix,
        op: Op,
    ) {
        when (op.type) {
            OpType.PRE_TRANSLATE -> matrix.preTranslate(op.p1, op.p2)
            OpType.PRE_SCALE -> matrix.preScale(op.p1, op.p2)
            OpType.PRE_ROTATE -> matrix.preRotate(op.p1)
            OpType.PRE_SKEW -> matrix.preSkew(op.p1, op.p2)
            OpType.POST_TRANSLATE -> matrix.postTranslate(op.p1, op.p2)
            OpType.POST_SCALE -> matrix.postScale(op.p1, op.p2)
            OpType.POST_ROTATE -> matrix.postRotate(op.p1)
            OpType.POST_SKEW -> matrix.postSkew(op.p1, op.p2)
        }
    }
}
