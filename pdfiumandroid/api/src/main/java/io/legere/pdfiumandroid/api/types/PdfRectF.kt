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
data class PdfRectF(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    fun toFloatArray(): FloatArray = floatArrayOf(left, top, right, bottom)

    fun width(): Float = right - left

    fun height(): Float = bottom - top

    fun isEmpty(): Boolean = left >= right || top >= bottom

    fun centerX(): Float = (left + right) / 2f

    fun centerY(): Float = (top + bottom) / 2f

    fun union(other: PdfRectF): PdfRectF {
        if (other.isEmpty()) return this
        if (this.isEmpty()) return other
        return PdfRectF(
            left = min(left, other.left),
            top = min(top, other.top),
            right = max(right, other.right),
            bottom = max(bottom, other.bottom),
        )
    }

    companion object {
        val EMPTY = PdfRectF(0f, 0f, 0f, 0f)
    }
}
