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

class PdfPointTest {
    @Test
    fun `Basic functionality with positive integers`() {
        // Verify that the method correctly converts a PdfPoint with positive x and y coordinates into an IntArray of size 2,
        // where the first element is x and the second is y.
        val point = PdfPoint(10, 20)
        val expectedArray = intArrayOf(10, 20)
        assertThat(point.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Functionality with negative integers`() {
        // Verify that the method correctly handles negative integer values for both x and y coordinates,
        // preserving their signs in the resulting IntArray.
        val point = PdfPoint(-10, -20)
        val expectedArray = intArrayOf(-10, -20)
        assertThat(point.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Functionality with zero values`() {
        // Test the method using the predefined ZERO companion object (PdfPoint(0, 0)) to ensure it produces an array containing two zeros.
        val point = PdfPoint.ZERO
        val expectedArray = intArrayOf(0, 0)
        assertThat(point.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Functionality with mixed positive and negative integers`() {
        // Ensure the method works correctly when one coordinate is positive and the other is negative, and vice-versa.
        val point = PdfPoint(-10, 20)
        val expectedArray = intArrayOf(-10, 20)
        assertThat(point.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Testing with Int MAX VALUE`() {
        // Test the method with a PdfPoint where both x and y are Int.MAX_VALUE to ensure it handles
        // the maximum integer limit without overflow or issues.
        val point = PdfPoint(Int.MAX_VALUE, Int.MAX_VALUE)
        val expectedArray = intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE)
        assertThat(point.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Testing with Int MIN VALUE`() {
        // Test the method with a PdfPoint where both x and y are Int.MIN_VALUE to ensure it handles the minimum integer limit correctly.
        val point = PdfPoint(Int.MIN_VALUE, Int.MIN_VALUE)
        val expectedArray = intArrayOf(Int.MIN_VALUE, Int.MIN_VALUE)
        assertThat(point.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `Testing with mixed Int MAX VALUE and Int MIN VALUE`() {
        // Verify correct behavior when x is Int.MAX_VALUE and y is Int.MIN_VALUE, and the reverse case.
        val point1 = PdfPoint(Int.MAX_VALUE, Int.MIN_VALUE)
        val point2 = PdfPoint(Int.MIN_VALUE, Int.MAX_VALUE)
        val expectedArray1 = intArrayOf(Int.MAX_VALUE, Int.MIN_VALUE)
        val expectedArray2 = intArrayOf(Int.MIN_VALUE, Int.MAX_VALUE)
        assertThat(point1.toIntArray()).isEqualTo(expectedArray1)
        assertThat(point2.toIntArray()).isEqualTo(expectedArray2)
    }

    @Test
    fun `Return array type and size validation`() {
        // Confirm that the returned object is specifically an IntArray and that its size is always
        // 2, regardless of the input coordinate values.
        val point = PdfPoint(10, 20)
        val result = point.toIntArray()
        assertThat(result).isInstanceOf(IntArray::class.java)
        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun `Immutability of the original object`() {
        // Verify that invoking toIntArray() does not mutate the state of the original PdfPoint object.
        val originalPoint = PdfPoint(10, 20)
        originalPoint.toIntArray()
        assertThat(originalPoint.x).isEqualTo(10)
        assertThat(originalPoint.y).isEqualTo(20)
    }

    @Test
    fun `Array instance uniqueness`() {
        // Ensure that multiple calls to toIntArray() on the same PdfPoint instance return different
        // IntArray instances, proving a new array is created each time.
        val point = PdfPoint(10, 20)
        val result1 = point.toIntArray()
        val result2 = point.toIntArray()
        assertThat(result1).isNotSameInstanceAs(result2)
    }
}
