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
val IntRectValues.exactCenterX get() = (left + right) / 2.0f
val IntRectValues.exactCenterY get() = (top + bottom) / 2.0f
val IntRectValues.isEmpty get() = left >= right || top >= bottom

abstract class BaseRect :
    IntRectValues,
    RectInterface<Int, PdfRect, IntRectValues> {
    override fun height(): Int = height

    override fun width(): Int = width

    override fun centerX(): Int = centerX

    override fun centerY(): Int = centerY

    override fun intersects(other: IntRectValues): Boolean {
        if (this.isEmpty || other.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    override fun intersects(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ): Boolean {
        if (this.isEmpty) return false
        return !(this.right < left || this.left > right || this.bottom < top || this.top > bottom)
    }

    override fun contains(
        x: Int,
        y: Int,
    ): Boolean {
        if (this.isEmpty) return false
        return (x in left..<right && y >= top && y < bottom)
    }

    override fun contains(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ): Boolean {
        if (this.isEmpty) return false
        return this.left <= left &&
            this.top <= top &&
            this.right >= right &&
            this.bottom >= bottom
    }

    fun contains(rect: FloatRectValues): Boolean {
        if (this.isEmpty) return false
        return !rect.isEmpty &&
            left <= rect.left &&
            top <= rect.top &&
            right >= rect.right &&
            bottom >= rect.bottom
    }

    override fun contains(rect: IntRectValues): Boolean {
        if (this.isEmpty) return false
        return !rect.isEmpty &&
            left <= rect.left &&
            top <= rect.top &&
            right >= rect.right &&
            bottom >= rect.bottom
    }

    override fun isEmpty(): Boolean = isEmpty
}

@Keep
data class PdfRect(
    override val left: Int,
    override val top: Int,
    override val right: Int,
    override val bottom: Int,
) : BaseRect(),
    ImmutableRectInterface<Int, PdfRect, IntRectValues> {
    constructor() : this(0, 0, 0, 0)
    constructor(rectF: FloatRectValues) : this(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
    constructor(rect: IntRectValues) : this(rect.left, rect.top, rect.right, rect.bottom)

    fun setEmpty(): PdfRect = PdfRect()

    override fun inset(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ): PdfRect =
        PdfRect(
            left = this.left + left,
            top = this.top + top,
            right = this.right - right,
            bottom = this.bottom - right,
        )

    override fun inset(
        dx: Int,
        dy: Int,
    ): PdfRect {
        if (dx == 0 && dy == 0) return this
        return PdfRect(
            left = left + dx,
            top = top + dy,
            right = right - dx,
            bottom = bottom - dy,
        )
    }

    override fun intersect(other: IntRectValues): PdfRect {
        if (this.isEmpty || other.isEmpty) return this
        return PdfRect(
            left = max(left, other.left),
            top = max(top, other.top),
            right = min(right, other.right),
            bottom = min(bottom, other.bottom),
        )
    }

    override fun intersect(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ): PdfRect {
        if (this.isEmpty) return this
        return PdfRect(
            left = max(left, left),
            top = max(top, top),
            right = min(right, right),
            bottom = min(bottom, bottom),
        )
    }

    override fun offset(
        dx: Int,
        dy: Int,
    ): PdfRect =
        PdfRect(
            left + dx,
            top + dy,
            right + dx,
            bottom + dy,
        )

    override fun offsetTo(
        newLeft: Int,
        newTop: Int,
    ): PdfRect {
        val width = right - left
        val height = bottom - top
        return PdfRect(
            newLeft,
            newTop,
            newLeft + width,
            newTop + height,
        )
    }

    override fun union(other: IntRectValues): PdfRect =
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

    override fun union(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ): PdfRect =
        when {
            this.isEmpty -> {
                PdfRect(left, top, right, bottom)
            }

            else -> {
                PdfRect(
                    left = min(left, this.left),
                    top = min(top, this.top),
                    right = max(right, this.right),
                    bottom = max(bottom, this.bottom),
                )
            }
        }

    override fun union(
        x: Int,
        y: Int,
    ): PdfRect =
        when {
            this.isEmpty -> {
                PdfRect()
            }

            else -> {
                PdfRect(
                    left = min(x, this.left),
                    top = min(y, this.top),
                    right = max(x, this.right),
                    bottom = max(y, this.bottom),
                )
            }
        }

    override fun sort(): PdfRect =
        PdfRect(
            left = min(left, right),
            top = min(top, bottom),
            right = max(left, right),
            bottom = max(top, bottom),
        )

    fun toMutable() = MutablePdfRect(left, top, right, bottom)

    fun toIntArray(): IntArray = intArrayOf(left, top, right, bottom)

    companion object {
        val EMPTY = PdfRect(0, 0, 0, 0)
    }
}

@Keep
data class MutablePdfRect(
    override var left: Int = 0,
    override var top: Int = 0,
    override var right: Int = 0,
    override var bottom: Int = 0,
) : BaseRect(),
    MutableRectInterface<Int, PdfRect, IntRectValues> {
    constructor() : this(0, 0, 0, 0)
    constructor(rectF: FloatRectValues) : this(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
    constructor(rect: IntRectValues) : this(rect.left, rect.top, rect.right, rect.bottom)

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

    fun setEmpty() {
        set(0, 0, 0, 0)
    }

    fun set(src: IntRectValues) {
        set(src.left, src.top, src.right, src.bottom)
    }

    override fun union(other: IntRectValues) {
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

    override fun union(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        when {
            this.isEmpty -> {
                return
            }

            else -> {
                this.left = min(left, this.left)
                this.top = min(top, this.top)
                this.right = max(right, this.right)
                this.bottom = max(bottom, this.bottom)
            }
        }
    }

    override fun union(
        x: Int,
        y: Int,
    ) {
        when {
            this.isEmpty -> {
                return
            }

            else -> {
                this.left = min(x, this.left)
                this.top = min(y, this.top)
                this.right = max(x, this.right)
                this.bottom = max(y, this.bottom)
            }
        }
    }

    override fun inset(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        this.left += left
        this.top += top
        this.right -= right
        this.bottom -= right
    }

    override fun inset(
        dx: Int,
        dy: Int,
    ) {
        if (dx == 0 && dy == 0) return
        left += dx
        top += dy
        right -= dx
        bottom -= dy
    }

    override fun intersect(other: IntRectValues) {
        if (this.isEmpty || other.isEmpty) return
        left = max(left, other.left)
        top = max(top, other.top)
        right = min(right, other.right)
        bottom = min(bottom, other.bottom)
    }

    override fun intersect(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        if (this.isEmpty) return
        this.left = max(this.left, left)
        this.top = max(this.top, top)
        this.right = min(this.right, right)
        this.bottom = min(this.bottom, bottom)
    }

    override fun offset(
        dx: Int,
        dy: Int,
    ) {
        left += dx
        top += dy
        right += dx
        bottom += dy
    }

    override fun offsetTo(
        newLeft: Int,
        newTop: Int,
    ) {
        val width = right - left
        val height = bottom - top
        left = newLeft
        top = newTop
        right = newLeft + width
        bottom = newTop + height
    }

    override fun sort() {
        val newLeft = min(left, right)
        val newTop = min(top, bottom)
        val newRight = max(left, right)
        val newBottom = max(top, bottom)
        left = newLeft
        top = newTop
        right = newRight
        bottom = newBottom
    }

    fun toImmutable() = PdfRect(left, top, right, bottom)

    fun toIntArray(): IntArray = intArrayOf(left, top, right, bottom)
}
