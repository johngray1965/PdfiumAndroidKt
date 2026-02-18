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

package io.legere.pdfiumandroid.api

import android.graphics.Matrix
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test

class ImmutableMatrixTest {
    val matrix: Matrix = mockk()

    val immutableMatrix = ImmutableMatrix(matrix)

    @Test
    fun toMutableMatrix() {
    }

    @Test
    fun getValues() {
        val floatArray = FloatArray(9)
        every { matrix.getValues(any()) } answers {
            firstArg<FloatArray>().forEachIndexed { index, _ ->
                floatArray[index] = index.toFloat()
            }
        }
        immutableMatrix.getValues(floatArray)
        assertThat(floatArray).isEqualTo(floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f))
    }

    @Test
    fun isAffine() {
        every { matrix.isAffine() } returns true
        assertThat(immutableMatrix.isAffine()).isTrue()
    }

    @Test
    fun isIdentity() {
        every { matrix.isIdentity() } returns true
        assertThat(immutableMatrix.isIdentity()).isTrue()
    }

    @Test
    fun mapPoints() {
        every { matrix.mapPoints(any()) } just runs
        immutableMatrix.mapPoints(null)
        verify { matrix.mapPoints(null) }
    }

    @Test
    fun testMapPoints() {
        every { matrix.mapPoints(any(), any()) } just runs
        val floatArray = FloatArray(2)
        val floatArray2 = FloatArray(2)
        immutableMatrix.mapPoints(floatArray, floatArray2)
        verify { matrix.mapPoints(floatArray, floatArray2) }
    }

    @Test
    fun testMapPoints1() {
        every { matrix.mapPoints(any(), any(), any(), any(), any()) } just runs
        val floatArray = FloatArray(2)
        val floatArray2 = FloatArray(2)
        immutableMatrix.mapPoints(floatArray, 1, floatArray2, 1, 1)
        verify { matrix.mapPoints(floatArray, 1, floatArray2, 1, 1) }
    }

    @Test
    fun mapRadius() {
        every { matrix.mapRadius(any()) } returns 1f
        assertThat(immutableMatrix.mapRadius(0f)).isEqualTo(1f)
    }

    @Test
    fun mapRect() {
        every { matrix.mapRect(any()) } returns true
        assertThat(immutableMatrix.mapRect(null)).isTrue()
    }

    @Test
    fun testMapRect() {
        every { matrix.mapRect(any(), any()) } returns true
        val rectF = android.graphics.RectF()
        val rectF2 = android.graphics.RectF()
        assertThat(immutableMatrix.mapRect(rectF, rectF2)).isTrue()
    }

    @Test
    fun mapVectors() {
        every { matrix.mapVectors(any()) } just runs
        immutableMatrix.mapVectors(null)
        verify { matrix.mapVectors(null) }
    }

    @Test
    fun testMapVectors() {
        every { matrix.mapVectors(any(), any()) } just runs
        val floatArray = FloatArray(2)
        val floatArray2 = FloatArray(2)
        immutableMatrix.mapVectors(floatArray, floatArray2)
        verify { matrix.mapVectors(floatArray, floatArray2) }
    }

    @Test
    fun testMapVectors1() {
        every { matrix.mapVectors(any(), any(), any(), any(), any()) } just runs
        val floatArray = FloatArray(2)
        val floatArray2 = FloatArray(2)
        immutableMatrix.mapVectors(floatArray, 1, floatArray2, 1, 1)
        verify { matrix.mapVectors(floatArray, 1, floatArray2, 1, 1) }
    }

    @Test
    fun copy() {
        every { matrix.set(any()) } just runs
        every { matrix.getValues(any()) } answers {
            firstArg<FloatArray>().forEachIndexed { index, _ ->
                index.toFloat()
            }
        }
        immutableMatrix.toMutableMatrix()
    }
}
