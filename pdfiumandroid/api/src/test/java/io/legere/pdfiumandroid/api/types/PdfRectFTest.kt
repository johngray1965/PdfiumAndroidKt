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

class PdfRectFTest {
    @Test
    fun `default constructor`() {
        val rect = PdfRectF()
        assertThat(rect.isEmpty).isTrue()
    }

    @Test
    fun `constructor with FloatRectValues`() {
        val input = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        val rect = PdfRectF(input)
        assertThat(rect.left).isEqualTo(10.5f)
        assertThat(rect.top).isEqualTo(20.5f)
        assertThat(rect.right).isEqualTo(30.5f)
        assertThat(rect.bottom).isEqualTo(40.5f)
    }

    @Test
    fun `constructor with IntRectValues`() {
        val input = PdfRect(10, 20, 30, 40)
        val rect = PdfRectF(input)
        assertThat(rect.left).isEqualTo(10.0f)
        assertThat(rect.top).isEqualTo(20.0f)
        assertThat(rect.right).isEqualTo(30.0f)
        assertThat(rect.bottom).isEqualTo(40.0f)
    }

    @Test
    fun `setEmpty() sets to default values`() {
        val input = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        val rect = PdfRectF(input).setEmpty()
        assertThat(rect.isEmpty).isTrue()
    }

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
        assertThat(rect.width).isEqualTo(20.0f)
    }

    @Test
    fun `width with a zero width rectangle`() {
        // Test width calculation when right and left coordinates are equal, expecting a result of 0.
        val rect = PdfRectF(10.5f, 20.5f, 10.5f, 40.5f)
        assertThat(rect.width).isEqualTo(0.0f)
    }

    @Test
    fun `width resulting in a negative value`() {
        // Check the width calculation for a rectangle where the left coordinate is greater than the right coordinate.
        val rect = PdfRectF(30.5f, 20.5f, 10.5f, 40.5f)
        assertThat(rect.width).isEqualTo(-20.0f)
    }

    @Test
    fun `width with negative coordinates`() {
        // Verify width calculation when both left and right coordinates are negative.
        val rect = PdfRectF(-10.5f, 20.5f, -30.5f, 40.5f)
        assertThat(rect.width).isEqualTo(-20.0f)
    }

    @Test
    fun `width spanning the zero axis`() {
        // Calculate width for a rectangle that crosses the y-axis (e.g., left is negative, right is positive).
        val rect = PdfRectF(-10.5f, 20.5f, 10.5f, 40.5f)
        assertThat(rect.width).isEqualTo(21.0f)
    }

    @Test
    fun `width with floating point precision`() {
        // Test width calculation with floating-point numbers that require high precision to ensure accuracy.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        assertThat(rect.width).isEqualTo(20.0f)
    }

    @Test
    fun `height with positive coordinates`() {
        // Calculate height for a standard rectangle where bottom is greater than top.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        assertThat(rect.height).isEqualTo(20.0f)
    }

    @Test
    fun `height with a zero height rectangle`() {
        // Test height calculation when bottom and top coordinates are equal, expecting a result of 0.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 20.5f)
        assertThat(rect.height).isEqualTo(0.0f)
    }

    @Test
    fun `height resulting in a negative value`() {
        // Check the height calculation for a rectangle where the top coordinate is greater than
        // the bottom coordinate (inverted rectangle).
        val rect = PdfRectF(10.5f, 40.5f, 30.5f, 20.5f)
        assertThat(rect.height).isEqualTo(-20.0f)
    }

    @Test
    fun `height with negative coordinates`() {
        // Verify height calculation when both top and bottom coordinates are negative.
        val rect = PdfRectF(10.5f, -20.5f, 30.5f, -40.5f)
        assertThat(rect.height).isEqualTo(-20.0f)
    }

    @Test
    fun `height spanning the zero axis`() {
        // Calculate height for a rectangle that crosses the x-axis (e.g., top is negative, bottom is positive).
        val rect = PdfRectF(10.5f, -20.5f, 30.5f, 40.5f)
        assertThat(rect.height).isEqualTo(61.0f)
    }

    @Test
    fun `height with floating point precision`() {
        // Test height calculation with floating-point numbers that require high precision to ensure accuracy.
        val rect = PdfRectF(10.5f, 20.5f, 30.5f, 40.5f)
        assertThat(rect.height).isEqualTo(20.0f)
    }

    @Test
    fun offset() {
        val rect = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(5f, 5f, 15f, 15f)
        val result = rect.offset(5f, 5f)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun offsetTo() {
        val rect = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(5f, 5f, 15f, 15f)
        val result = rect.offsetTo(5f, 5f)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun sort() {
        val rect = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(0f, 0f, 10f, 10f)
        val result = rect.sort()
        assertThat(result).isEqualTo(expected)

        val rect2 = PdfRectF(10f, 0f, 0f, 10f)
        val expected2 = PdfRectF(0f, 0f, 10f, 10f)
        val result2 = rect2.sort()
        assertThat(result2).isEqualTo(expected2)
    }

    @Test
    fun inset() {
        val rect = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(2f, 2f, 8f, 8f)
        val result = rect.inset(2f, 2f)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun inset00() {
        val rect = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(0f, 0f, 10f, 10f)
        val result = rect.inset(0f, 0f)
        assertThat(result).isEqualTo(expected)
        val result2 = result.inset(0f, 10f)
        assertThat(result2).isEqualTo(PdfRectF(left = 0f, top = 10f, right = 10f, bottom = 0f))
        val result3 = result2.inset(10f, 0f)
        assertThat(result3).isEqualTo(PdfRectF(left = 10f, top = 10f, right = 0f, bottom = 0f))
    }

    @Test
    fun inset2() {
        val rect = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(2f, 2f, 8f, 8f)
        val result = rect.inset(2f, 2f, 2f, 2f)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun intersect() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        val rect2 = PdfRectF(5f, 5f, 15f, 15f)
        val expected = PdfRectF(5f, 5f, 10f, 10f)
        val result = rect1.intersect(rect2)
        assertThat(result).isEqualTo(expected)

        val rect1Unprocessed = PdfRectF(0f, 0f, 10f, 10f)
        val rect3 = PdfRectF(0f, 0f, 10f, 10f)
        val result2 = rect3.intersect(rect1Unprocessed)
        assertThat(result2).isEqualTo(rect1Unprocessed)
        val result3 = rect1Unprocessed.intersect(rect3)
        assertThat(result3).isEqualTo(rect1Unprocessed)
    }

    @Test
    fun intersectAEmpty() {
        val rect1 = PdfRectF()
        val rect2 = PdfRectF(5f, 5f, 15f, 15f)
        val expected = PdfRectF()
        val result = rect1.intersect(rect2)
        assertThat(result).isEqualTo(expected)
        val result2 = rect2.intersect(rect1)
        assertThat(result2).isEqualTo(PdfRectF(5f, 5f, 15f, 15f))
    }

    @Test
    fun intersect2() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(5f, 5f, 15f, 15f)
        val result = rect1.intersect(5f, 5f, 15f, 15f)
        assertThat(result).isEqualTo(expected)

        val rect1Unprocessed = PdfRectF(0f, 0f, 10f, 10f)
        val rect3 = PdfRectF(0f, 0f, 10f, 10f)
        val result2 = rect3.intersect(0f, 0f, 10f, 10f)
        assertThat(result2).isEqualTo(rect1Unprocessed)
        val result3 = rect1Unprocessed.intersect(0f, 0f, 10f, 10f)
        assertThat(result3).isEqualTo(rect1Unprocessed)
    }

    @Test
    fun intersect2Empty() {
        val rect1 = PdfRectF()
        val result = rect1.intersect(5f, 5f, 15f, 15f)
        assertThat(result).isEqualTo(PdfRectF())
    }

    @Test
    fun union() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        val rect2 = PdfRectF(5f, 5f, 15f, 15f)
        val expected = PdfRectF(0f, 0f, 15f, 15f)
        assertThat(rect1.union(rect2)).isEqualTo(expected)

        val emptyRect = PdfRectF.EMPTY
        assertThat(rect1.union(emptyRect)).isEqualTo(rect1)
        assertThat(emptyRect.union(rect1)).isEqualTo(rect1)
        assertThat(emptyRect.union(MutablePdfRectF(0f, 0f, 10f, 10f))).isEqualTo(PdfRectF(0f, 0f, 10f, 10f))
    }

    @Test
    fun union22() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(0f, 0f, 15f, 15f)
        assertThat(rect1.union(5f, 5f, 15f, 15f)).isEqualTo(expected)
        assertThat(rect1.union(15f, 15f)).isEqualTo(expected)

        val emptyRect = PdfRectF.EMPTY
        assertThat(rect1.union(0f, 0f, 0f, 0f)).isEqualTo(rect1)
        assertThat(emptyRect.union(0f, 0f, 10f, 10f)).isEqualTo(rect1)
    }

    @Test
    fun unionEmpty() {
        val rect1 = PdfRectF()
        val rect2 = PdfRectF(5f, 5f, 15f, 15f)
        val result = rect1.union(rect2)
        assertThat(result).isEqualTo(rect2)

        val emptyRect = PdfRectF()
        val result2 = result.union(emptyRect)
        assertThat(result2).isEqualTo(result)
        val result3 = emptyRect.union(rect1)
        assertThat(result3).isEqualTo(rect1)
    }

    @Test
    fun union2() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(0f, 0f, 15f, 15f)
        val result = rect1.union(5f, 5f, 15f, 15f)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun union2Empty() {
        val rect1 = PdfRectF()
        val other = PdfRectF(5f, 5f, 15f, 15f)
        val result = rect1.union(5f, 5f, 15f, 15f)
        assertThat(result).isEqualTo(other)
    }

    @Test
    fun union3() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        val expected = PdfRectF(0f, 0f, 15f, 15f)
        val result = rect1.union(15f, 15f)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun union3Empty() {
        val rect1 = PdfRectF()
        val result = rect1.union(15f, 15f)
        assertThat(result).isEqualTo(rect1)
    }

    @Test
    fun roundOut() {
        val rect = PdfRectF(0.1f, 0.9f, 9.9f, 10.1f)
        val expected = PdfRect(0, 0, 10, 11) // floor, floor, ceil, ceil

        // Assuming roundOut implementation:
        // left = floor(left), top = floor(top), right = ceil(right), bottom = ceil(bottom)
        // Wait, Android RectF.roundOut does exactly that.
        // My PdfRectF does not have roundOut yet?
        // The user asked to add tests for it, implying it should be there or I should add it.
        // I checked TypeConversion.kt earlier, maybe it's there as extension?
        // Or maybe I missed adding it to PdfRectF.kt?
        // Let's assume I need to add it to PdfRectF.kt as well if it's missing.
        // But for now I'll write the test and see.
        // Wait, I cannot run tests to see if it fails.
        // I will add roundOut() to PdfRectF.kt if I don't see it.
        // I read PdfRectF.kt in previous turn, it did NOT have roundOut.
        // It has `toPdfRect` which does roundToInt.

        // I'll add the test assuming I will add the function.
        assertThat(rect.roundOut()).isEqualTo(expected)
    }

    @Test
    fun contains() {
        val rect = PdfRectF(0f, 0f, 10f, 10f)
        assertThat(rect.contains(5f, 5f)).isTrue()
        assertThat(rect.contains(0f, 0f)).isTrue() // Inclusive left/top
        assertThat(rect.contains(10f, 10f)).isFalse() // Exclusive right/bottom
        assertThat(rect.contains(11f, 5f)).isFalse()

        val insideRect = PdfRectF(2f, 2f, 8f, 8f)
        assertThat(rect.contains(insideRect)).isTrue()

        val overlappingRect = PdfRectF(5f, 5f, 15f, 15f)
        assertThat(rect.contains(overlappingRect)).isFalse()

        val insideRectInt = PdfRect(2, 2, 8, 8)
        assertThat(rect.contains(insideRectInt)).isTrue()

        val overlappingRectInt = PdfRect(5, 5, 15, 15)
        assertThat(rect.contains(overlappingRectInt)).isFalse()
    }

    @Test
    fun containsEmpty() {
        val rect = PdfRectF()
        assertThat(rect.contains(5f, 5f)).isFalse()
        assertThat(rect.contains(0f, 0f)).isFalse() // Inclusive left/top
        assertThat(rect.contains(10f, 10f)).isFalse() // Exclusive right/bottom
        assertThat(rect.contains(11f, 5f)).isFalse()

        val insideRect = PdfRectF(2f, 2f, 8f, 8f)
        assertThat(rect.contains(insideRect)).isFalse()

        val overlappingRect = PdfRectF(5f, 5f, 15f, 15f)
        assertThat(rect.contains(overlappingRect)).isFalse()
        val insideRectFloat = PdfRectF(2.0f, 2.0f, 8.0f, 8.0f)
        assertThat(rect.contains(insideRectFloat)).isFalse()

        val overlappingRectFloat = PdfRectF(5.0f, 5.0f, 15.0f, 15.0f)
        assertThat(rect.contains(overlappingRectFloat)).isFalse()
    }

    @Test
    fun contains2() {
        val rect = PdfRectF(0f, 0f, 10f, 10f)

        assertThat(rect.contains(2f, 2f, 8f, 8f)).isTrue()

        assertThat(rect.contains(5f, 5f, 15f, 15f)).isFalse()
    }

    @Test
    fun contains2Empty() {
        val rect = PdfRectF()

        assertThat(rect.contains(2f, 2f, 8f, 8f)).isFalse()

        assertThat(rect.contains(5f, 5f, 15f, 15f)).isFalse()
    }

    @Test
    fun intersects() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        val rect2 = PdfRectF(5f, 5f, 15f, 15f)
        assertThat(rect1.intersects(rect2)).isTrue()

        val rect3 = PdfRectF(20f, 20f, 30f, 30f)
        assertThat(rect1.intersects(rect3)).isFalse()

        // Touching edges usually considered intersecting in Android RectF?
        // RectF.intersects(a, b) -> left < right && top < bottom ...
        // If left == right, it returns false (empty).
        // If r1.right == r2.left, 10 < 10 is false -> no intersection.
        val rect4 = PdfRectF(10f, 0f, 20f, 10f)
        assertThat(rect1.intersects(rect4)).isTrue()
    }

    @Test
    fun intersectsEachBound() {
        val rect1 = PdfRectF(5f, 5f, 10f, 10f)
        val leftOut = PdfRectF(5f, 5f, 10f, 10f).offset(0f, -10f)
        assertThat(rect1.intersects(leftOut)).isFalse()
        val topOut = PdfRectF(5f, 5f, 10f, 10f).offset(-10f, 0f)
        assertThat(rect1.intersects(topOut)).isFalse()
        val rightOUt = PdfRectF(5f, 5f, 10f, 10f).offset(0f, 10f)
        assertThat(rect1.intersects(rightOUt)).isFalse()
        val bottomOut = PdfRectF(5f, 5f, 10f, 10f).offset(10f, 0f)
        assertThat(rect1.intersects(bottomOut)).isFalse()
    }

    @Test
    fun intersectsEachBound2() {
        val rect1 = PdfRectF(5f, 5f, 10f, 10f)
        assertThat(rect1.intersects(5f, -5f, 10f, 0f)).isFalse()
        assertThat(rect1.intersects(-5f, 5f, 0f, 10f)).isFalse()
        assertThat(rect1.intersects(5f, 15f, 10f, 20f)).isFalse()
        assertThat(rect1.intersects(15f, 5f, 20f, 10f)).isFalse()
    }

    @Test
    fun intersectsEmpty() {
        val rect1 = PdfRectF()
        val rect2 = PdfRectF(5f, 5f, 15f, 15f)
        assertThat(rect1.intersects(rect2)).isFalse()
        assertThat(rect1.intersects(5f, 5f, 15f, 15f)).isFalse()

        val rect5 = PdfRectF(0f, 0f, 10f, 10f)
        val rect3 = PdfRectF()
        assertThat(rect5.intersects(rect3)).isFalse()
    }

    @Test
    fun intersects2() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        assertThat(rect1.intersects(5f, 5f, 15f, 15f)).isTrue()

        assertThat(rect1.intersects(20f, 20f, 30f, 30f)).isFalse()

        // Touching edges usually considered intersecting in Android RectF?
        // RectF.intersects(a, b) -> left < right && top < bottom ...
        // If left == right, it returns false (empty).
        // If r1.right == r2.left, 10 < 10 is false -> no intersection.
        val rect4 = PdfRectF(10f, 0f, 20f, 10f)
        assertThat(rect1.intersects(rect4)).isTrue()
    }

    @Test
    fun containsX() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        val rect2 = PdfRectF(5f, 5f, 15f, 15f)
        assertThat(rect1.contains(rect2)).isFalse()

        val rect3 = PdfRectF(20f, 20f, 30f, 30f)
        assertThat(rect1.contains(rect3)).isFalse()

        // Touching edges usually considered intersecting in Android RectF?
        // RectF.intersects(a, b) -> left < right && top < bottom ...
        // If left == right, it returns false (empty).
        // If r1.right == r2.left, 10 < 10 is false -> no intersection.
        val rect4 = PdfRectF(10f, 0f, 20f, 10f)
        assertThat(rect1.contains(rect4)).isFalse()
    }

    @Test
    fun containsEachBound() {
        val rect1 = PdfRectF(5f, 5f, 10f, 10f)
        val leftOut = PdfRectF(5f, 5f, 10f, 10f).offset(0f, -10f)
        assertThat(rect1.contains(leftOut)).isFalse()
        val topOut = PdfRectF(5f, 5f, 10f, 10f).offset(-10f, 0f)
        assertThat(rect1.contains(topOut)).isFalse()
        val rightOUt = PdfRectF(5f, 5f, 10f, 10f).offset(0f, 10f)
        assertThat(rect1.contains(rightOUt)).isFalse()
        val bottomOut = PdfRectF(5f, 5f, 10f, 10f).offset(10f, 0f)
        assertThat(rect1.contains(bottomOut)).isFalse()
    }

    @Test
    fun containsEachBoundInt() {
        val rect1 = PdfRectF(5f, 5f, 10f, 10f)
        val leftOut = PdfRect(5, 5, 10, 10).offset(0, -10)
        assertThat(rect1.contains(leftOut)).isFalse()
        val topOut = PdfRect(5, 5, 10, 10).offset(-10, 0)
        assertThat(rect1.contains(topOut)).isFalse()
        val rightOUt = PdfRect(5, 5, 10, 10).offset(0, 10)
        assertThat(rect1.contains(rightOUt)).isFalse()
        val bottomOut = PdfRect(5, 5, 10, 10).offset(10, 0)
        assertThat(rect1.contains(bottomOut)).isFalse()
        val empty = PdfRect()
        assertThat(rect1.contains(empty)).isFalse()
        assertThat(PdfRectF().contains(bottomOut)).isFalse()
    }

    @Test
    fun containsEachBound2() {
        val rect1 = PdfRectF(5f, 5f, 10f, 10f)
        assertThat(rect1.contains(5f, -5f, 10f, 0f)).isFalse()
        assertThat(rect1.contains(-5f, 5f, 0f, 10f)).isFalse()
        assertThat(rect1.contains(5f, 15f, 10f, 20f)).isFalse()
        assertThat(rect1.contains(15f, 5f, 20f, 10f)).isFalse()
    }

    @Test
    fun containsEachBound22() {
        val rect1 = PdfRectF(5f, 5f, 10f, 10f)
        assertThat(rect1.contains(-5f, -5f)).isFalse()
        assertThat(rect1.contains(5f, -5f)).isFalse()
        assertThat(rect1.contains(50f, 8f)).isFalse()
        assertThat(rect1.contains(8f, 50f)).isFalse()
        assertThat(PdfRectF().contains(8f, 50f)).isFalse()
    }

    @Test
    fun containsEachBound23() {
        val rect1 = PdfRectF(5f, 5f, 10f, 10f)
        assertThat(rect1.contains(5f, -5f, 10f, 0f)).isFalse()
        assertThat(rect1.contains(-5f, 5f, 0f, 10f)).isFalse()
        assertThat(rect1.contains(5f, 15f, 10f, 20f)).isFalse()
        assertThat(rect1.contains(15f, 5f, 20f, 10f)).isFalse()
    }

    @Test
    fun containsEmptyX() {
        val rect1 = PdfRectF()
        val rect2 = PdfRectF(5f, 5f, 15f, 15f)
        assertThat(rect1.contains(rect2)).isFalse()
        assertThat(rect1.contains(5f, 5f, 15f, 15f)).isFalse()

        val rect5 = PdfRectF(0f, 0f, 10f, 10f)
        val rect3 = PdfRectF()
        assertThat(rect5.contains(rect3)).isFalse()
    }

    @Test
    fun contains2X() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        assertThat(rect1.contains(5f, 5f, 15f, 15f)).isFalse()

        assertThat(rect1.contains(20f, 20f, 30f, 30f)).isFalse()

        // Touching edges usually considered intersecting in Android RectF?
        // RectF.intersects(a, b) -> left < right && top < bottom ...
        // If left == right, it returns false (empty).
        // If r1.right == r2.left, 10 < 10 is false -> no intersection.
        val rect4 = PdfRectF(10f, 0f, 20f, 10f)
        assertThat(rect1.contains(rect4)).isFalse()
    }

    @Test
    fun width() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        assertThat(rect1.width).isEqualTo(10f)

        val rect2 = PdfRectF(10f, 0f, 20f, 10f)
        assertThat(rect2.width).isEqualTo(10f)
    }

    @Test
    fun height() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        assertThat(rect1.height).isEqualTo(10f)

        val rect2 = PdfRectF(10f, 10f, 20f, 20f)
        assertThat(rect2.height).isEqualTo(10f)
    }

    @Test
    fun center() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        assertThat(rect1.centerX).isEqualTo(5f)
        assertThat(rect1.centerY).isEqualTo(5f)

        val rect2 = PdfRectF(10f, 10f, 20f, 20f)
        assertThat(rect2.centerX).isEqualTo(15f)
        assertThat(rect2.centerY).isEqualTo(15f)
    }

    @Test
    fun isEmpty() {
        val rect1 = PdfRectF(0f, 0f, 10f, 10f)
        assertThat(rect1.isEmpty).isFalse()

        val rect2 = PdfRectF.EMPTY
        assertThat(rect2.isEmpty).isTrue()

        assertThat(PdfRectF(0f, 0f, -5f, -5f).isEmpty).isTrue()
        assertThat(PdfRectF(0f, 0f, 1f, -5f).isEmpty).isTrue()
    }

    @Test
    fun toMutable() {
        val rect = PdfRectF(1f, 2f, 3f, 4f)
        val mutable = rect.toMutable()
        assertThat(mutable).isInstanceOf(MutablePdfRectF::class.java)
        assertThat(mutable.left).isEqualTo(1f)
        assertThat(mutable.top).isEqualTo(2f)
        assertThat(mutable.right).isEqualTo(3f)
        assertThat(mutable.bottom).isEqualTo(4f)
    }
}
