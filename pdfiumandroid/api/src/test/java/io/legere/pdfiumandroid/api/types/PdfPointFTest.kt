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

package io.legere.pdfiumandroid.api.types

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PdfPointFTest {
    @Test
    fun `Positive float values conversion`() {
        // Verify that a PdfPointF with positive x and y float values is correctly converted to a FloatArray.
        val point = PdfPointF(10.5f, 20.5f)
        val expectedArray = floatArrayOf(10.5f, 20.5f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Negative float values conversion`() {
        // Verify that a PdfPointF with negative x and y float values is correctly converted to a FloatArray.
        val point = PdfPointF(-10.5f, -20.5f)
        val expectedArray = floatArrayOf(-10.5f, -20.5f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Zero float values conversion`() {
        // Verify that a PdfPointF with zero x and y values is correctly converted to a FloatArray.
        val point = PdfPointF(0f, 0f)
        val expectedArray = floatArrayOf(0f, 0f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Mixed positive and negative values`() {
        // Verify correct conversion when PdfPointF has one positive and one negative value.
        val point = PdfPointF(10.5f, -20.5f)
        val expectedArray = floatArrayOf(10.5f, -20.5f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Maximum float values`() {
        // Test the conversion with Float.MAX_VALUE for both x and y to check for any overflow or precision issues.
        val point = PdfPointF(Float.MAX_VALUE, Float.MAX_VALUE)
        val expectedArray = floatArrayOf(Float.MAX_VALUE, Float.MAX_VALUE)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Minimum float values`() {
        // Test the conversion with Float.MIN_VALUE for both x and y to check for any underflow or precision issues.
        val point = PdfPointF(Float.MIN_VALUE, Float.MIN_VALUE)
        val expectedArray = floatArrayOf(Float.MIN_VALUE, Float.MIN_VALUE)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Positive infinity values`() {
        // Test the function's behavior when x and y are Float.POSITIVE_INFINITY.
        val point = PdfPointF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        val expectedArray = floatArrayOf(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Negative infinity values`() {
        // Test the function's behavior when x and y are Float.NEGATIVE_INFINITY.
        val point = PdfPointF(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        val expectedArray = floatArrayOf(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `NaN values`() {
        // Test the function's behavior when x and y are Float.NaN (Not a Number).
        val point = PdfPointF(Float.NaN, Float.NaN)
        val expectedArray = floatArrayOf(Float.NaN, Float.NaN)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Companion object ZERO conversion`() {
        // Verify that the predefined PdfPointF.ZERO object correctly converts to a float array of [0f, 0f].
        val point = PdfPointF.ZERO
        val expectedArray = floatArrayOf(0f, 0f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }
}
