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

class MutablePdfPointFTest {
    @Test
    fun `Positive float values conversion`() {
        // Verify that a PdfPointF with positive x and y float values is correctly converted to a FloatArray.
        val point = MutablePdfPointF(10.5f, 20.5f)
        val expectedArray = floatArrayOf(10.5f, 20.5f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Negative float values conversion`() {
        // Verify that a PdfPointF with negative x and y float values is correctly converted to a FloatArray.
        val point = MutablePdfPointF(-10.5f, -20.5f)
        val expectedArray = floatArrayOf(-10.5f, -20.5f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Zero float values conversion`() {
        // Verify that a MutablePdfPointF with zero x and y values is correctly converted to a FloatArray.
        val point = MutablePdfPointF(0f, 0f)
        val expectedArray = floatArrayOf(0f, 0f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Mixed positive and negative values`() {
        // Verify correct conversion when MutablePdfPointF has one positive and one negative value.
        val point = MutablePdfPointF(10.5f, -20.5f)
        val expectedArray = floatArrayOf(10.5f, -20.5f)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Maximum float values`() {
        // Test the conversion with Float.MAX_VALUE for both x and y to check for any overflow or precision issues.
        val point = MutablePdfPointF(Float.MAX_VALUE, Float.MAX_VALUE)
        val expectedArray = floatArrayOf(Float.MAX_VALUE, Float.MAX_VALUE)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Minimum float values`() {
        // Test the conversion with Float.MIN_VALUE for both x and y to check for any underflow or precision issues.
        val point = MutablePdfPointF(Float.MIN_VALUE, Float.MIN_VALUE)
        val expectedArray = floatArrayOf(Float.MIN_VALUE, Float.MIN_VALUE)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Positive infinity values`() {
        // Test the function's behavior when x and y are Float.POSITIVE_INFINITY.
        val point = MutablePdfPointF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        val expectedArray = floatArrayOf(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Negative infinity values`() {
        // Test the function's behavior when x and y are Float.NEGATIVE_INFINITY.
        val point = MutablePdfPointF(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        val expectedArray = floatArrayOf(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `NaN values`() {
        // Test the function's behavior when x and y are Float.NaN (Not a Number).
        val point = MutablePdfPointF(Float.NaN, Float.NaN)
        val expectedArray = floatArrayOf(Float.NaN, Float.NaN)
        assertThat(point.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun offset() {
        val point = MutablePdfPointF(10.5f, 20.5f)
        val expected = MutablePdfPointF(15.5f, 30.5f)
        assertThat(point.offset(5.0f, 10.0f)).isEqualTo(expected)
    }

    @Test
    fun negate() {
        val point = MutablePdfPointF(10.5f, 20.5f)
        val expected = MutablePdfPointF(-10.5f, -20.5f)
        assertThat(point.negate()).isEqualTo(expected)
    }

    @Test
    fun length() {
        val point = MutablePdfPointF(3f, 4f)
        assertThat(point.length()).isEqualTo(5f)
    }

    @Test
    fun toImmutable() {
        val point = MutablePdfPointF(3f, 4f).toImmutable()
        assertThat(point).isInstanceOf(PdfPointF::class.java)
        assertThat(point.x).isEqualTo(3f)
        assertThat(point.y).isEqualTo(4f)
    }

    @Test
    fun `set with FloatPointValues`() {
        val point = MutablePdfPointF()
        val src = PdfPointF(10.5f, 20.5f)
        point.set(src)
        assertThat(point.x).isEqualTo(10.5f)
        assertThat(point.y).isEqualTo(20.5f)
    }

    @Test
    fun `set with Float values`() {
        val point = MutablePdfPointF()
        point.set(10.5f, 20.5f)
        assertThat(point.x).isEqualTo(10.5f)
        assertThat(point.y).isEqualTo(20.5f)
    }
}
