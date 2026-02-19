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

/**
 * Interface defining common operations for float rectangles.
 * Shared between Immutable and Mutable versions to avoid code duplication.
 */
interface FloatRectValues {
    val left: Float
    val top: Float
    val right: Float
    val bottom: Float
}

val FloatRectValues.width get() = right - left
val FloatRectValues.height get() = bottom - top
val FloatRectValues.centerX get() = (left + right) / 2f
val FloatRectValues.centerY get() = (top + bottom) / 2f
val FloatRectValues.isEmpty get() = left >= right || top >= bottom

@Keep
data class PdfRectF(
    override val left: Float,
    override val top: Float,
    override val right: Float,
    override val bottom: Float,
) : FloatRectValues {
    fun union(other: FloatRectValues): PdfRectF {
        if (other.isEmpty) return this
        if (this.isEmpty) return if (other is PdfRectF) other else PdfRectF(other.left, other.top, other.right, other.bottom)
        return PdfRectF(
            left = min(left, other.left),
            top = min(top, other.top),
            right = max(right, other.right),
            bottom = max(bottom, other.bottom),
        )
    }

    fun intersects(other: PdfRectF): Boolean {
        if (this.isEmpty || other.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    fun toMutable() = MutablePdfRectF(left, top, right, bottom)

    fun toFloatArray(): FloatArray = floatArrayOf(left, top, right, bottom)

    companion object {
        val EMPTY = PdfRectF(0f, 0f, 0f, 0f)
    }
}

@Keep
class MutablePdfRectF(
    override var left: Float = 0f,
    override var top: Float = 0f,
    override var right: Float = 0f,
    override var bottom: Float = 0f,
) : FloatRectValues {
    fun set(
        l: Float,
        t: Float,
        r: Float,
        b: Float,
    ) {
        left = l
        top = t
        right = r
        bottom = b
    }

    fun set(src: FloatRectValues) {
        set(src.left, src.top, src.right, src.bottom)
    }

    fun union(other: FloatRectValues) {
        if (other.isEmpty) return
        if (this.isEmpty) {
            set(other)
            return
        }
        left = min(left, other.left)
        top = min(top, other.top)
        right = max(right, other.right)
        bottom = max(bottom, other.bottom)
    }

    fun intersects(other: PdfRectF): Boolean {
        if (this.isEmpty || other.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    fun toImmutable() = PdfRectF(left, top, right, bottom)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatRectValues) return false
        return left == other.left && top == other.top && right == other.right && bottom == other.bottom
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + top.hashCode()
        result = 31 * result + right.hashCode()
        result = 31 * result + bottom.hashCode()
        return result
    }
}
