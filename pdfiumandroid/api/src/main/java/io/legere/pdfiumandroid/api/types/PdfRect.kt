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

import androidx.annotation.Keep
import kotlin.math.max
import kotlin.math.min

@Keep
@Suppress("MemberVisibilityCanBePrivate")
data class PdfRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    constructor(values: IntArray) : this(values[0], values[1], values[2], values[3])

    fun toIntArray(): IntArray = intArrayOf(left, top, right, bottom)

    fun width(): Int = right - left

    fun height(): Int = bottom - top

    fun isEmpty(): Boolean = left >= right || top >= bottom

    fun centerX(): Int = (left + right) / 2

    fun centerY(): Int = (top + bottom) / 2

    fun union(other: PdfRect): PdfRect {
        if (other.isEmpty()) return this
        if (this.isEmpty()) return other
        return PdfRect(
            left = min(left, other.left),
            top = min(top, other.top),
            right = max(right, other.right),
            bottom = max(bottom, other.bottom),
        )
    }

    fun intersects(other: PdfRect): Boolean {
        if (this.isEmpty() || other.isEmpty()) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    companion object {
        val EMPTY = PdfRect(0, 0, 0, 0)
    }
}
