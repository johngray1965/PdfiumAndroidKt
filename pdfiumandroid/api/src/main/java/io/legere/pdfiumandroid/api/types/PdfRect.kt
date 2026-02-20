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

@file:Suppress("unused", "TooManyFunctions")

package io.legere.pdfiumandroid.api.types

import androidx.annotation.Keep
import kotlin.math.max
import kotlin.math.min

/**
 * Interface defining common operations for integer rectangles.
 * Shared between Immutable and Mutable versions to avoid code duplication.
 */
interface IntRectValues {
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
}

val IntRectValues.width get() = right - left
val IntRectValues.height get() = bottom - top
val IntRectValues.centerX get() = (left + right) / 2
val IntRectValues.centerY get() = (top + bottom) / 2
val IntRectValues.isEmpty get() = left >= right || top >= bottom

@Keep
data class PdfRect(
    override val left: Int,
    override val top: Int,
    override val right: Int,
    override val bottom: Int,
) : IntRectValues {
    fun union(other: IntRectValues): PdfRect =
        when {
            other.isEmpty -> {
                this
            }

            this.isEmpty -> {
                other as? PdfRect ?: PdfRect(other.left, other.top, other.right, other.bottom)
            }

            else -> {
                PdfRect(
                    left = min(left, other.left),
                    top = min(top, other.top),
                    right = max(right, other.right),
                    bottom = max(bottom, other.bottom),
                )
            }
        }

    fun intersects(other: IntRectValues): Boolean {
        if (this.isEmpty || other.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    fun contains(other: FloatRectValues): Boolean {
        if (this.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    fun toMutable() = MutablePdfRect(left, top, right, bottom)

    fun toIntArray(): IntArray = intArrayOf(left, top, right, bottom)

    companion object {
        val EMPTY = PdfRect(0, 0, 0, 0)
    }
}

@Keep
class MutablePdfRect(
    override var left: Int = 0,
    override var top: Int = 0,
    override var right: Int = 0,
    override var bottom: Int = 0,
) : IntRectValues {
    fun set(
        l: Int,
        t: Int,
        r: Int,
        b: Int,
    ) {
        left = l
        top = t
        right = r
        bottom = b
    }

    fun reset() {
        set(0, 0, 0, 0)
    }

    fun set(src: IntRectValues) {
        set(src.left, src.top, src.right, src.bottom)
    }

    fun union(other: IntRectValues) {
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

    fun intersects(other: IntRectValues): Boolean {
        if (this.isEmpty || other.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    fun contains(other: IntRectValues): Boolean {
        if (this.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    fun contains(other: FloatRectValues): Boolean {
        if (this.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    fun offset(
        dx: Int,
        dy: Int,
    ) {
        left += dx
        top += dy
        right += dx
        bottom += dy
    }

    fun toImmutable() = PdfRect(left, top, right, bottom)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntRectValues) return false
        return left == other.left && top == other.top && right == other.right && bottom == other.bottom
    }

    override fun hashCode(): Int {
        var result = left
        result = 31 * result + top
        result = 31 * result + right
        result = 31 * result + bottom
        return result
    }
}
