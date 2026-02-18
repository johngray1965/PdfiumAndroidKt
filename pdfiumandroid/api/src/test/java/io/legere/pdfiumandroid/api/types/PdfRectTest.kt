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

class PdfRectTest {
    @Test
    fun `toIntArray with positive integers`() {
        // Check if the function correctly converts a PdfRect with all positive integers to an IntArray.
        val rect = PdfRect(10, 20, 30, 40)
        val expectedArray = intArrayOf(10, 20, 30, 40)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with negative integers`() {
        // Check if the function correctly converts a PdfRect with all negative integers to an IntArray.
        val rect = PdfRect(-10, -20, -30, -40)
        val expectedArray = intArrayOf(-10, -20, -30, -40)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with mixed integers`() {
        // Check if the function correctly converts a PdfRect with a mix of positive and negative integers to an IntArray.
        val rect = PdfRect(-10, 20, 30, -40)
        val expectedArray = intArrayOf(-10, 20, 30, -40)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with zero values`() {
        // Check if the function correctly converts a PdfRect with all zero values (PdfRect.EMPTY) to an IntArray.
        val rect = PdfRect(0, 0, 0, 0)
        val expectedArray = intArrayOf(0, 0, 0, 0)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with Int MAX VALUE`() {
        // Check if the function correctly converts a PdfRect with Int.MAX_VALUE for all properties to an IntArray.
        val rect = PdfRect(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        val expectedArray = intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with Int MIN VALUE`() {
        // Check if the function correctly converts a PdfRect with Int.MIN_VALUE for all properties to an IntArray.
        val rect = PdfRect(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)
        val expectedArray = intArrayOf(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `width with positive integers`() {
        // Check if the width is calculated correctly for a standard case where right > left.
        val rect = PdfRect(10, 20, 30, 40)
        assertThat(rect.width()).isEqualTo(20)
    }

    @Test
    fun `width resulting in zero`() {
        // Check if the width is calculated as 0 when right equals left.
        val rect = PdfRect(10, 20, 10, 40)
        assertThat(rect.width()).isEqualTo(0)
    }

    @Test
    fun `width with negative coordinates`() {
        // Check if the width is calculated correctly when both right and left are negative.
        val rect = PdfRect(-10, 20, -30, 40)
        assertThat(rect.width()).isEqualTo(-20)
    }

    @Test
    fun `width with inverted coordinates`() {
        // Check if the width is calculated as a negative value when left > right.
        val rect = PdfRect(30, 20, 10, 40)
        assertThat(rect.width()).isEqualTo(-20)
    }

    @Test
    fun `width integer overflow check`() {
        // Test for integer overflow when calculating width, e.g., right = Int.MAX_VALUE and left = Int.MIN_VALUE. [16]
        val rect = PdfRect(Int.MIN_VALUE, 20, Int.MAX_VALUE, 40)
        assertThat(rect.width()).isEqualTo(-1)
    }

    @Test
    fun `width integer underflow check`() {
        // Test for integer underflow when calculating width, e.g., right = Int.MIN_VALUE and left = Int.MAX_VALUE.
        val rect = PdfRect(Int.MAX_VALUE, 20, Int.MIN_VALUE, 40)
        assertThat(rect.width()).isEqualTo(1)
    }

    @Test
    fun `height with positive integers`() {
        // Check if the height is calculated correctly for a standard case where bottom > top.
        val rect = PdfRect(10, 20, 30, 40)
        assertThat(rect.height()).isEqualTo(20)
    }

    @Test
    fun `height resulting in zero`() {
        // Check if the height is calculated as 0 when bottom equals top.
        val rect = PdfRect(10, 20, 30, 20)
        assertThat(rect.height()).isEqualTo(0)
    }

    @Test
    fun `height with negative coordinates`() {
        // Check if the height is calculated correctly when both bottom and top are negative.
        val rect = PdfRect(10, -20, 30, -40)
        assertThat(rect.height()).isEqualTo(-20)
    }

    @Test
    fun `height with inverted coordinates`() {
        // Check if the height is calculated as a negative value when top > bottom.
        val rect = PdfRect(10, 40, 30, 20)
        assertThat(rect.height()).isEqualTo(-20)
    }

    @Test
    fun `height integer overflow check`() {
        // Test for integer overflow when calculating height, e.g., bottom = Int.MAX_VALUE and top = Int.MIN_VALUE. [16]
        val rect = PdfRect(10, Int.MIN_VALUE, 30, Int.MAX_VALUE)
        assertThat(rect.height()).isEqualTo(-1)
    }

    @Test
    fun `height integer underflow check`() {
        // Test for integer underflow when calculating height, e.g., bottom = Int.MIN_VALUE and top = Int.MAX_VALUE.
        val rect = PdfRect(10, Int.MAX_VALUE, 30, Int.MIN_VALUE)
        assertThat(rect.height()).isEqualTo(1)
    }

    @Test
    fun `EMPTY object width calculation`() {
        // Verify that the width of the PdfRect.EMPTY companion object is 0.
        val rect = PdfRect.EMPTY
        assertThat(rect.width()).isEqualTo(0)
    }

    @Test
    fun `EMPTY object height calculation`() {
        // Verify that the height of the PdfRect.EMPTY companion object is 0.
        val rect = PdfRect.EMPTY
        assertThat(rect.height()).isEqualTo(0)
    }
}
