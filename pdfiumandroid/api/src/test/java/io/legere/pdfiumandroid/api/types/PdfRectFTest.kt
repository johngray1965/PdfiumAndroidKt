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

class PdfRectFTest {
    @Test
    fun `toFloatArray with positive values`() {
        // Verify that toFloatArray correctly returns an array with positive float values.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        val expectedArray = floatArrayOf(10.5f, 20.5f, 30.5f, 40.5f)
        assertThat(rect.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toFloatArray with negative values`() {
        // Verify that toFloatArray correctly returns an array with negative float values.
        val rect = PdfRectF(-10.5f, -20.5f, -30.5f, -40.5f)
        val expectedArray = floatArrayOf(-10.5f, -20.5f, -30.5f, -40.5f)
        assertThat(rect.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toFloatArray with zero values`() {
        // Test toFloatArray with all coordinates set to zero, including the EMPTY companion object.
        val rect = PdfRectF.EMPTY
        val expectedArray = floatArrayOf(0f, 0f, 0f, 0f)
        assertThat(rect.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toFloatArray with mixed sign values`() {
        // Ensure toFloatArray functions correctly when coordinates have a mix of positive and negative values.
        val rect = PdfRectF(-10.5f, 20.5f, 30.5f, -40.5f)
        val expectedArray = floatArrayOf(-10.5f, 20.5f, 30.5f, -40.5f)
        assertThat(rect.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toFloatArray with large magnitude values`() {
        // Test toFloatArray with very large positive and negative float values to check for precision issues.
        val rect = PdfRectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
        val expectedArray = floatArrayOf(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
        assertThat(rect.toFloatArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `width with positive coordinates`() {
        // Calculate width for a standard rectangle where right is greater than left.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        assertThat(rect.width()).isEqualTo(20.0f)
    }

    @Test
    fun `width with a zero width rectangle`() {
        // Test width calculation when right and left coordinates are equal, expecting a result of 0.
        val rect = PdfRectF(10.5f, 20.5f, 10.5f, 40.5f)
        assertThat(rect.width()).isEqualTo(0.0f)
    }

    @Test
    fun `width resulting in a negative value`() {
        // Check the width calculation for a rectangle where the left coordinate is greater than the right coordinate.
        val rect = PdfRectF(30.5f, 20.5f, 10.5f, 40.5f)
        assertThat(rect.width()).isEqualTo(-20.0f)
    }

    @Test
    fun `width with negative coordinates`() {
        // Verify width calculation when both left and right coordinates are negative.
        val rect = PdfRectF(-10.5f, 20.5f, -30.5f, 40.5f)
        assertThat(rect.width()).isEqualTo(-20.0f)
    }

    @Test
    fun `width spanning the zero axis`() {
        // Calculate width for a rectangle that crosses the y-axis (e.g., left is negative, right is positive).
        val rect = PdfRectF(-10.5f, 20.5f, 10.5f, 40.5f)
        assertThat(rect.width()).isEqualTo(21.0f)
    }

    @Test
    fun `width with floating point precision`() {
        // Test width calculation with floating-point numbers that require high precision to ensure accuracy.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        assertThat(rect.width()).isEqualTo(20.0f)
    }

    @Test
    fun `height with positive coordinates`() {
        // Calculate height for a standard rectangle where bottom is greater than top.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        assertThat(rect.height()).isEqualTo(20.0f)
    }

    @Test
    fun `height with a zero height rectangle`() {
        // Test height calculation when bottom and top coordinates are equal, expecting a result of 0.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 20.5f)
        assertThat(rect.height()).isEqualTo(0.0f)
    }

    @Test
    fun `height resulting in a negative value`() {
        // Check the height calculation for a rectangle where the top coordinate is greater than the bottom coordinate (inverted rectangle).
        val rect = PdfRectF(10.5f, 40.5f, 30.5f, 20.5f)
        assertThat(rect.height()).isEqualTo(-20.0f)
    }

    @Test
    fun `height with negative coordinates`() {
        // Verify height calculation when both top and bottom coordinates are negative.
        val rect = PdfRectF(10.5f, -20.5f, 30.5f, -40.5f)
        assertThat(rect.height()).isEqualTo(-20.0f)
    }

    @Test
    fun `height spanning the zero axis`() {
        // Calculate height for a rectangle that crosses the x-axis (e.g., top is negative, bottom is positive).
        val rect = PdfRectF(10.5f, -20.5f, 30.5f, 40.5f)
        assertThat(rect.height()).isEqualTo(61.0f)
    }

    @Test
    fun `height with floating point precision`() {
        // Test height calculation with floating-point numbers that require high precision to ensure accuracy.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        assertThat(rect.height()).isEqualTo(20.0f)
    }
}
