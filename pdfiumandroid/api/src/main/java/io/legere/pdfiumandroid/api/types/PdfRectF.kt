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
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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

abstract class BaseRectF :
    FloatRectValues,
    RectInterface<Float, PdfRectF, FloatRectValues> {
    override fun height(): Float = height

    override fun width(): Float = width

    override fun centerX(): Float = centerX

    override fun centerY(): Float = centerY

    override fun intersects(other: FloatRectValues): Boolean {
        if (this.isEmpty || other.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    override fun intersects(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Boolean {
        if (this.isEmpty) return false
        return !(this.right < left || this.left > right || this.bottom < top || this.top > bottom)
    }

    override fun contains(
        x: Float,
        y: Float,
    ): Boolean {
        if (this.isEmpty) return false
        return (x in left..<right && y >= top && y < bottom)
    }

    override fun contains(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Boolean {
        if (this.isEmpty) return false
        return this.left <= left &&
            this.top <= top &&
            this.right >= right &&
            this.bottom >= bottom
    }

    override fun contains(rect: FloatRectValues): Boolean {
        if (this.isEmpty) return false
        return !rect.isEmpty &&
            left <= rect.left &&
            top <= rect.top &&
            right >= rect.right &&
            bottom >= rect.bottom
    }

    fun contains(other: IntRectValues): Boolean {
        if (this.isEmpty) return false
        return !other.isEmpty &&
            left <= other.left &&
            top <= other.top &&
            right >= other.right &&
            bottom >= other.bottom
    }

    fun round(dst: MutablePdfRect) {
        dst.left = left.roundToInt()
        dst.top = top.roundToInt()
        dst.right = right.roundToInt()
        dst.bottom = bottom.roundToInt()
    }

    fun roundOut(dst: MutablePdfRect) {
        dst.left = floor(this.left).toInt()
        dst.top = floor(this.top).toInt()
        dst.right = ceil(this.right).toInt()
        dst.bottom = ceil(this.bottom).toInt()
    }

    override fun isEmpty(): Boolean = isEmpty
}

@Keep
data class PdfRectF(
    override val left: Float,
    override val top: Float,
    override val right: Float,
    override val bottom: Float,
) : BaseRectF(),
    ImmutableRectInterface<Float, PdfRectF, FloatRectValues> {
    constructor() : this(0f, 0f, 0f, 0f)
    constructor(rectF: FloatRectValues) : this(rectF.left, rectF.top, rectF.right, rectF.bottom)
    constructor(rectF: IntRectValues) : this(rectF.left.toFloat(), rectF.top.toFloat(), rectF.right.toFloat(), rectF.bottom.toFloat())

    override fun inset(
        dx: Float,
        dy: Float,
    ): PdfRectF {
        if (dx == 0f && dy == 0f) return this
        return PdfRectF(
            left = left + dx,
            top = top + dy,
            right = right - dx,
            bottom = bottom - dy,
        )
    }

    override fun inset(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): PdfRectF =
        PdfRectF(
            left = this.left + left,
            top = this.top + top,
            right = this.right - right,
            bottom = this.bottom - right,
        )

    override fun intersect(other: FloatRectValues): PdfRectF {
        if (this.isEmpty || other.isEmpty) return this
        return PdfRectF(
            left = max(left, other.left),
            top = max(top, other.top),
            right = min(right, other.right),
            bottom = min(bottom, other.bottom),
        )
    }

    override fun intersect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): PdfRectF {
        if (this.isEmpty) return this
        return PdfRectF(
            left = max(left, left),
            top = max(top, top),
            right = min(right, right),
            bottom = min(bottom, bottom),
        )
    }

    fun setEmpty(): PdfRectF =
        PdfRectF(
            left = 0f,
            top = 0f,
            right = 0f,
            bottom = 0f,
        )

    override fun union(other: FloatRectValues): PdfRectF =
        when {
            other.isEmpty -> {
                this
            }

            this.isEmpty -> {
                other as? PdfRectF ?: PdfRectF(other.left, other.top, other.right, other.bottom)
            }

            else -> {
                PdfRectF(
                    left = min(left, other.left),
                    top = min(top, other.top),
                    right = max(right, other.right),
                    bottom = max(bottom, other.bottom),
                )
            }
        }

    override fun union(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): PdfRectF =
        when {
            this.isEmpty -> {
                PdfRectF(left, top, right, bottom)
            }

            else -> {
                PdfRectF(
                    left = min(left, this.left),
                    top = min(top, this.top),
                    right = max(right, this.right),
                    bottom = max(bottom, this.bottom),
                )
            }
        }

    override fun union(
        x: Float,
        y: Float,
    ): PdfRectF =
        when {
            this.isEmpty -> {
                PdfRectF()
            }

            else -> {
                PdfRectF(
                    left = min(x, this.left),
                    top = min(y, this.top),
                    right = max(x, this.right),
                    bottom = max(y, this.bottom),
                )
            }
        }

    fun roundOut(): PdfRect =
        PdfRect(
            left = floor(this.left).toInt(),
            top = floor(this.top).toInt(),
            right = ceil(this.right).toInt(),
            bottom = ceil(this.bottom).toInt(),
        )

    override fun offset(
        dx: Float,
        dy: Float,
    ): PdfRectF =
        PdfRectF(
            left + dx,
            top + dy,
            right + dx,
            bottom + dy,
        )

    override fun offsetTo(
        newLeft: Float,
        newTop: Float,
    ): PdfRectF {
        val width = right - left
        val height = bottom - top
        return PdfRectF(
            newLeft,
            newTop,
            newLeft + width,
            newTop + height,
        )
    }

    override fun sort(): PdfRectF =
        PdfRectF(
            left = min(left, right),
            top = min(top, bottom),
            right = max(left, right),
            bottom = max(top, bottom),
        )

    fun toMutable() = MutablePdfRectF(left, top, right, bottom)

    fun toFloatArray(): FloatArray = floatArrayOf(left, top, right, bottom)

    companion object {
        val EMPTY = PdfRectF(0f, 0f, 0f, 0f)
    }
}

@Keep
data class MutablePdfRectF(
    override var left: Float = 0f,
    override var top: Float = 0f,
    override var right: Float = 0f,
    override var bottom: Float = 0f,
) : BaseRectF(),
    MutableRectInterface<Float, PdfRectF, FloatRectValues> {
    constructor() : this(0f, 0f, 0f, 0f)
    constructor(rectF: FloatRectValues) : this(rectF.left, rectF.top, rectF.right, rectF.bottom)
    constructor(rectF: IntRectValues) : this(
        rectF.left.toFloat(),
        rectF.top.toFloat(),
        rectF.right.toFloat(),
        rectF.bottom.toFloat(),
    )

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

    fun set(src: IntRectValues) {
        set(src.left.toFloat(), src.top.toFloat(), src.right.toFloat(), src.bottom.toFloat())
    }

    fun setEmpty() {
        left = 0f
        top = 0f
        right = 0f
        bottom = 0f
    }

    override fun inset(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        this.left += left
        this.top += top
        this.right -= right
        this.bottom -= right
    }

    override fun inset(
        dx: Float,
        dy: Float,
    ) {
        if (dx == 0f && dy == 0f) return
        left += dx
        top += dy
        right -= dx
        bottom -= dy
    }

    override fun intersect(other: FloatRectValues) {
        if (this.isEmpty || other.isEmpty) return
        left = max(left, other.left)
        top = max(top, other.top)
        right = min(right, other.right)
        bottom = min(bottom, other.bottom)
    }

    override fun intersect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        if (this.isEmpty) return
        this.left = max(this.left, left)
        this.top = max(this.top, top)
        this.right = min(this.right, right)
        this.bottom = min(this.bottom, bottom)
    }

    override fun union(other: FloatRectValues) {
        when
            {
                other.isEmpty -> {
                }

                this.isEmpty -> {
                    left = other.left
                    top = other.top
                    right = other.right
                    bottom = other.bottom
                }

                else -> {
                    left = min(left, other.left)
                    top = min(top, other.top)
                    right = max(right, other.right)
                    bottom = max(bottom, other.bottom)
                }
            }
    }

    override fun union(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
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
        x: Float,
        y: Float,
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

    fun roundOut(): MutablePdfRect =
        MutablePdfRect(
            left = floor(this.left).toInt(),
            top = floor(this.top).toInt(),
            right = ceil(this.right).toInt(),
            bottom = ceil(this.bottom).toInt(),
        )

    override fun intersects(other: FloatRectValues): Boolean {
        if (this.isEmpty || other.isEmpty) return false
        return !(right < other.left || left > other.right || bottom < other.top || top > other.bottom)
    }

    override fun offset(
        dx: Float,
        dy: Float,
    ) {
        left += dx
        top += dy
        right += dx
        bottom += dy
    }

    override fun offsetTo(
        newLeft: Float,
        newTop: Float,
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

    fun toImmutable() = PdfRectF(left, top, right, bottom)

    fun toFloatArray(): FloatArray = floatArrayOf(left, top, right, bottom)

    companion object {
        val EMPTY = PdfRectF(0f, 0f, 0f, 0f)
    }
}
