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
import org.junit.jupiter.api.Test

class MutablePdfRectTest {
    @Test
    fun `default constructor`() {
        val rect = MutablePdfRect()
        assertThat(rect.isEmpty).isTrue()
    }

    @Test
    fun `constructor with FloatRectValues`() {
        val input = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        val rect = MutablePdfRect(input)
        assertThat(rect.left).isEqualTo(10)
        assertThat(rect.top).isEqualTo(20)
        assertThat(rect.right).isEqualTo(30)
        assertThat(rect.bottom).isEqualTo(40)
    }

    @Test
    fun `constructor with IntRectValues`() {
        val input = PdfRect(10, 20, 30, 40)
        val rect = MutablePdfRect(input)
        assertThat(rect.left).isEqualTo(10)
        assertThat(rect.top).isEqualTo(20)
        assertThat(rect.right).isEqualTo(30)
        assertThat(rect.bottom).isEqualTo(40)
    }

    @Test
    fun `setEmpty() sets to default values`() {
        val input = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        val rect = MutablePdfRect(input)
        rect.setEmpty()
        assertThat(rect.isEmpty).isTrue()
    }

    @Test
    fun `toIntArray with positive integers`() {
        // Check if the function correctly converts a MutablePdfRect with all positive integers to an IntArray.
        val rect = MutablePdfRect(10, 20, 30, 40)
        val expectedArray = intArrayOf(10, 20, 30, 40)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with negative integers`() {
        // Check if the function correctly converts a MutablePdfRect with all negative integers to an IntArray.
        val rect = MutablePdfRect(-10, -20, -30, -40)
        val expectedArray = intArrayOf(-10, -20, -30, -40)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with mixed integers`() {
        // Check if the function correctly converts a MutablePdfRect with a mix of positive and negative integers to an IntArray.
        val rect = MutablePdfRect(-10, 20, 30, -40)
        val expectedArray = intArrayOf(-10, 20, 30, -40)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with zero values`() {
        // Check if the function correctly converts a MutablePdfRect with all zero values (MutablePdfRect.EMPTY) to an IntArray.
        val rect = MutablePdfRect(0, 0, 0, 0)
        val expectedArray = intArrayOf(0, 0, 0, 0)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with Int MAX VALUE`() {
        // Check if the function correctly converts a MutablePdfRect with Int.MAX_VALUE for all properties to an IntArray.
        val rect = MutablePdfRect(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        val expectedArray = intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `toIntArray with Int MIN VALUE`() {
        // Check if the function correctly converts a MutablePdfRect with Int.MIN_VALUE for all properties to an IntArray.
        val rect = MutablePdfRect(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)
        val expectedArray = intArrayOf(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)
        assertThat(rect.toIntArray()).isEqualTo(expectedArray)
    }

    @Test
    fun `width with positive integers`() {
        // Check if the width is calculated correctly for a standard case where right > left.
        val rect = MutablePdfRect(10, 20, 30, 40)
        assertThat(rect.width).isEqualTo(20)
    }

    @Test
    fun `width resulting in zero`() {
        // Check if the width is calculated as 0 when right equals left.
        val rect = MutablePdfRect(10, 20, 10, 40)
        assertThat(rect.width).isEqualTo(0)
    }

    @Test
    fun `width with negative coordinates`() {
        // Check if the width is calculated correctly when both right and left are negative.
        val rect = MutablePdfRect(-10, 20, -30, 40)
        assertThat(rect.width).isEqualTo(-20)
    }

    @Test
    fun `width with inverted coordinates`() {
        // Check if the width is calculated as a negative value when left > right.
        val rect = MutablePdfRect(30, 20, 10, 40)
        assertThat(rect.width).isEqualTo(-20)
    }

    @Test
    fun `width integer overflow check`() {
        // Test for integer overflow when calculating width, e.g., right = Int.MAX_VALUE and left = Int.MIN_VALUE. [16]
        val rect = MutablePdfRect(Int.MIN_VALUE, 20, Int.MAX_VALUE, 40)
        assertThat(rect.width).isEqualTo(-1)
    }

    @Test
    fun `width integer underflow check`() {
        // Test for integer underflow when calculating width, e.g., right = Int.MIN_VALUE and left = Int.MAX_VALUE.
        val rect = MutablePdfRect(Int.MAX_VALUE, 20, Int.MIN_VALUE, 40)
        assertThat(rect.width).isEqualTo(1)
    }

    @Test
    fun `height with positive integers`() {
        // Check if the height is calculated correctly for a standard case where bottom > top.
        val rect = MutablePdfRect(10, 20, 30, 40)
        assertThat(rect.height).isEqualTo(20)
    }

    @Test
    fun `height resulting in zero`() {
        // Check if the height is calculated as 0 when bottom equals top.
        val rect = MutablePdfRect(10, 20, 30, 20)
        assertThat(rect.height).isEqualTo(0)
    }

    @Test
    fun `height with negative coordinates`() {
        // Check if the height is calculated correctly when both bottom and top are negative.
        val rect = MutablePdfRect(10, -20, 30, -40)
        assertThat(rect.height).isEqualTo(-20)
    }

    @Test
    fun `height with inverted coordinates`() {
        // Check if the height is calculated as a negative value when top > bottom.
        val rect = MutablePdfRect(10, 40, 30, 20)
        assertThat(rect.height).isEqualTo(-20)
    }

    @Test
    fun `height integer overflow check`() {
        // Test for integer overflow when calculating height, e.g., bottom = Int.MAX_VALUE and top = Int.MIN_VALUE. [16]
        val rect = MutablePdfRect(10, Int.MIN_VALUE, 30, Int.MAX_VALUE)
        assertThat(rect.height).isEqualTo(-1)
    }

    @Test
    fun `height integer underflow check`() {
        // Test for integer underflow when calculating height, e.g., bottom = Int.MIN_VALUE and top = Int.MAX_VALUE.
        val rect = MutablePdfRect(10, Int.MAX_VALUE, 30, Int.MIN_VALUE)
        assertThat(rect.height).isEqualTo(1)
    }

    @Test
    fun `EMPTY object width calculation`() {
        // Verify that the width of the MutablePdfRect.EMPTY companion object is 0.
        val rect = PdfRect.EMPTY.toMutable()
        assertThat(rect.width).isEqualTo(0)
    }

    @Test
    fun `EMPTY object height calculation`() {
        // Verify that the height of the MutablePdfRect.EMPTY companion object is 0.
        val rect = PdfRect.EMPTY.toMutable()
        assertThat(rect.height).isEqualTo(0)
    }

    @Test
    fun union() {
        val rect1 = MutablePdfRect(0, 0, 10, 10)
        val rect2 = MutablePdfRect(5, 5, 15, 15)
        val expected = MutablePdfRect(0, 0, 15, 15)
        rect1.union(rect2)
        assertThat(rect1).isEqualTo(expected)

        val emptyRect = PdfRect.EMPTY.toMutable()
        rect1.union(emptyRect)
        assertThat(rect1).isEqualTo(rect1)
        emptyRect.union(rect1)
        assertThat(emptyRect).isEqualTo(rect1)
    }

    @Test
    fun contains() {
        val rect = MutablePdfRect(0, 0, 10, 10)
        assertThat(rect.contains(5, 5)).isTrue()
        assertThat(rect.contains(0, 0)).isTrue() // Inclusive left/top
        assertThat(rect.contains(10, 10)).isFalse() // Exclusive right/bottom
        assertThat(rect.contains(11, 5)).isFalse()

        val insideRect = MutablePdfRect(2, 2, 8, 8)
        assertThat(rect.contains(insideRect)).isTrue()

        val overlappingRect = MutablePdfRect(5, 5, 15, 15)
        assertThat(rect.contains(overlappingRect)).isFalse()
    }

    @Test
    fun intersects() {
        val rect1 = MutablePdfRect(0, 0, 10, 10)
        val rect2 = MutablePdfRect(5, 5, 15, 15)
        assertThat(rect1.intersects(rect2)).isTrue()

        val rect3 = MutablePdfRect(20, 20, 30, 30)
        assertThat(rect1.intersects(rect3)).isFalse()

        // Touching edges usually considered intersecting in Android RectF?
        // RectF.intersects(a, b) -> left < right && top < bottom ...
        // If left == right, it returns false (empty).
        // If r1.right == r2.left, 10 < 10 is false -> no intersection.
        val rect4 = MutablePdfRect(10, 0, 20, 10)
        assertThat(rect1.intersects(rect4)).isTrue()
    }

    @Test
    fun width() {
        val rect1 = MutablePdfRect(0, 0, 10, 10)
        assertThat(rect1.width).isEqualTo(10)

        val rect2 = MutablePdfRect(10, 0, 20, 10)
        assertThat(rect2.width).isEqualTo(10)
    }

    @Test
    fun height() {
        val rect1 = MutablePdfRect(0, 0, 10, 10)
        assertThat(rect1.height).isEqualTo(10)

        val rect2 = MutablePdfRect(10, 0, 20, 20)
        assertThat(rect2.height).isEqualTo(20)
    }

    @Test
    fun center() {
        val rect1 = MutablePdfRect(0, 0, 10, 10)
        assertThat(rect1.centerX).isEqualTo(5)
        assertThat(rect1.centerY).isEqualTo(5)

        val rect2 = MutablePdfRect(10, 0, 20, 20)
        assertThat(rect2.centerX).isEqualTo(15)
        assertThat(rect2.centerY).isEqualTo(10)
    }

    @Test
    fun isEmpty() {
        val rect1 = MutablePdfRect(0, 0, 10, 10)
        assertThat(rect1.isEmpty).isFalse()

        val rect2 = PdfRect.EMPTY.toMutable()
        assertThat(rect2.isEmpty).isTrue()
    }

    @Test
    fun offset() {
        val rect = MutablePdfRect(0, 0, 10, 10)
        val expected = MutablePdfRect(5, 5, 15, 15)
        rect.offset(5, 5)
        assertThat(rect).isEqualTo(expected)
    }

    @Test
    fun setEmpty() {
        val rect = MutablePdfRect(0, 0, 10, 10)
        rect.setEmpty()
        assertThat(rect).isEqualTo(MutablePdfRect().apply { set(0, 0, 0, 0) })
    }

    @Test
    fun toImmutable() {
        val rect = MutablePdfRect(1, 2, 3, 4)
        val mutable = rect.toImmutable()
        assertThat(mutable).isInstanceOf(PdfRect::class.java)
        assertThat(mutable.left).isEqualTo(1)
        assertThat(mutable.top).isEqualTo(2)
        assertThat(mutable.right).isEqualTo(3)
        assertThat(mutable.bottom).isEqualTo(4)
    }
}
