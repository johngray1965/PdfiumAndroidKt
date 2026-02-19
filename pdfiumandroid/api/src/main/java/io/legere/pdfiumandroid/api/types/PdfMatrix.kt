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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

const val THREE_BY_THREE = 9
const val MSCALE_X = 0
const val MSKEW_X = 1
const val MTRANS_X = 2
const val MSKEW_Y = 3
const val MSCALE_Y = 4
const val MTRANS_Y = 5
const val MPERSP_0 = 6
const val MPERSP_1 = 7
const val MPERSP_2 = 8

/**
 * Immutable transformation matrix.
 * Methods return new instances or mapped values instead of modifying the current ones.
 */
@Keep
@Suppress("TooManyFunctions")
data class PdfMatrix(
    val values: FloatArray =
        floatArrayOf(
            1f,
            0f,
            0f,
            0f,
            1f,
            0f,
            0f,
            0f,
            1f,
        ),
) {
    constructor(other: PdfMatrix) : this(other.values.copyOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PdfMatrix
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()

    fun isIdentity(): Boolean = values.isIdentity()

    fun isAffine(): Boolean = values.isAffine()

    // --- Mapping functions ---

    fun mapPoint(point: PdfPointF): PdfPointF = mapPoint(point.x, point.y)

    fun mapPoint(
        x: Float,
        y: Float,
    ): PdfPointF {
        val w = values[MPERSP_0] * x + values[MPERSP_1] * y + values[MPERSP_2]
        return PdfPointF(
            x = (values[MSCALE_X] * x + values[MSKEW_X] * y + values[MTRANS_X]) / w,
            y = (values[MSKEW_Y] * x + values[MSCALE_Y] * y + values[MTRANS_Y]) / w,
        )
    }

    fun mapRect(rect: PdfRectF): PdfRectF {
        val p1 = mapPoint(rect.left, rect.top)
        val p2 = mapPoint(rect.right, rect.top)
        val p3 = mapPoint(rect.right, rect.bottom)
        val p4 = mapPoint(rect.left, rect.bottom)
        return PdfRectF(
            left = min(p1.x, min(p2.x, min(p3.x, p4.x))),
            top = min(p1.y, min(p2.y, min(p3.y, p4.y))),
            right = max(p1.x, max(p2.x, max(p3.x, p4.x))),
            bottom = max(p1.y, max(p2.y, max(p3.y, p4.y))),
        )
    }

    fun mapRadius(radius: Float): Float {
        val a = values[MSCALE_X]
        val b = values[MSKEW_Y]
        val c = values[MSKEW_X]
        val d = values[MSCALE_Y]
        val d1 = sqrt((a * a + b * b).toDouble()).toFloat()
        val d2 = sqrt((c * c + d * d).toDouble()).toFloat()
        return radius * (d1 + d2) / 2f
    }

    fun mapVector(vector: PdfPointF): PdfPointF = mapVector(vector.x, vector.y)

    fun mapVector(
        x: Float,
        y: Float,
    ): PdfPointF =
        PdfPointF(
            x = values[MSCALE_X] * x + values[MSKEW_X] * y,
            y = values[MSKEW_Y] * x + values[MSCALE_Y] * y,
        )

    fun mapPoints(src: FloatArray): FloatArray {
        val dst = FloatArray(src.size)
        values.mapPoints(dst, 0, src, 0, src.size / 2)
        return dst
    }

    fun mapPoints(
        dst: FloatArray,
        src: FloatArray,
    ) {
        values.mapPoints(dst, 0, src, 0, min(dst.size, src.size) / 2)
    }

    fun mapVectors(src: FloatArray): FloatArray {
        val dst = FloatArray(src.size)
        values.mapVectors(dst, 0, src, 0, src.size / 2)
        return dst
    }

    fun mapVectors(
        dst: FloatArray,
        src: FloatArray,
    ) {
        values.mapVectors(dst, 0, src, 0, min(dst.size, src.size) / 2)
    }

    /**
     * Returns the inverse of this matrix, or null if it's not invertible.
     */
    fun invert(): PdfMatrix? = values.invert()?.let { PdfMatrix(it) }

    // --- Immutable modifiers (return new instance) ---

    fun translate(
        dx: Float,
        dy: Float,
    ) = PdfMatrix(values.copyOf().apply { preTranslate(dx, dy) })

    fun scale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(values.copyOf().apply { preScale(sx, sy, px, py) })

    fun rotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(values.copyOf().apply { preRotate(degrees, px, py) })

    fun skew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(values.copyOf().apply { preSkew(kx, ky, px, py) })

    fun concat(other: PdfMatrix) = PdfMatrix(values.copyOf().apply { preConcat(other.values) })

    fun toMutable() = MutablePdfMatrix(values.copyOf())

    companion object {
        val IDENTITY = PdfMatrix()
    }
}

/**
 * Mutable transformation matrix for performance-critical operations and pooling.
 */
@Keep
@Suppress("TooManyFunctions")
class MutablePdfMatrix(
    val values: FloatArray = FloatArray(THREE_BY_THREE),
) {
    init {
        values.reset()
    }

    fun reset(): MutablePdfMatrix {
        values.reset()
        return this
    }

    fun set(src: PdfMatrix): MutablePdfMatrix {
        System.arraycopy(src.values, 0, values, 0, THREE_BY_THREE)
        return this
    }

    fun setTranslate(
        dx: Float,
        dy: Float,
    ): MutablePdfMatrix {
        values.setTranslate(dx, dy)
        return this
    }

    fun setScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.setScale(sx, sy, px, py)
        return this
    }

    fun setRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.setRotate(degrees, px, py)
        return this
    }

    fun setSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.setSkew(kx, ky, px, py)
        return this
    }

    fun postConcat(other: PdfMatrix): MutablePdfMatrix {
        values.postConcat(other.values)
        return this
    }

    fun preConcat(other: PdfMatrix): MutablePdfMatrix {
        values.preConcat(other.values)
        return this
    }

    fun postRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.postRotate(degrees, px, py)
        return this
    }

    fun preRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.preRotate(degrees, px, py)
        return this
    }

    fun postSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.postSkew(kx, ky, px, py)
        return this
    }

    fun preSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.preSkew(kx, ky, px, py)
        return this
    }

    fun postTranslate(
        dx: Float,
        dy: Float,
    ): MutablePdfMatrix {
        values.postTranslate(dx, dy)
        return this
    }

    fun preTranslate(
        dx: Float,
        dy: Float,
    ): MutablePdfMatrix {
        values.preTranslate(dx, dy)
        return this
    }

    fun postScale(
        dx: Float,
        dy: Float,
    ): MutablePdfMatrix {
        values.postScale(dx, dy)
        return this
    }

    fun preScale(
        dx: Float,
        dy: Float,
    ): MutablePdfMatrix {
        values.preScale(dx, dy)
        return this
    }

    fun invert(target: MutablePdfMatrix): Boolean {
        val inv = values.invert()
        return if (inv != null) {
            System.arraycopy(inv, 0, target.values, 0, THREE_BY_THREE)
            true
        } else {
            false
        }
    }

    fun toImmutable() = PdfMatrix(values.copyOf())

    fun isIdentity(): Boolean = values.isIdentity()

    fun isAffine(): Boolean = values.isAffine()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MutablePdfMatrix) return false
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()
}

// --- Internal Shared Math Logic ---

internal fun FloatArray.reset() {
    fill(0f)
    this[MSCALE_X] = 1f
    this[MSCALE_Y] = 1f
    this[MPERSP_2] = 1f
}

internal fun FloatArray.normalize() {
    for (i in 0 until THREE_BY_THREE) if (this[i] == -0.0f) this[i] = 0.0f
}

internal fun FloatArray.isIdentity(): Boolean =
    this[MSCALE_X] == 1f && this[MSKEW_X] == 0f && this[MTRANS_X] == 0f &&
        this[MSKEW_Y] == 0f && this[MSCALE_Y] == 1f && this[MTRANS_Y] == 0f &&
        this[MPERSP_0] == 0f && this[MPERSP_1] == 0f && this[MPERSP_2] == 1f

internal fun FloatArray.isAffine(): Boolean = this[MPERSP_0] == 0f && this[MPERSP_1] == 0f && this[MPERSP_2] == 1f

internal fun FloatArray.setTranslate(
    dx: Float,
    dy: Float,
) {
    reset()
    this[MTRANS_X] = dx
    this[MTRANS_Y] = dy
}

internal fun FloatArray.setScale(
    sx: Float,
    sy: Float,
    px: Float,
    py: Float,
) {
    reset()
    this[MSCALE_X] = sx
    this[MSCALE_Y] = sy
    this[MTRANS_X] = px - sx * px
    this[MTRANS_Y] = py - sy * py
    normalize()
}

internal fun FloatArray.setRotate(
    degrees: Float,
    px: Float,
    py: Float,
) {
    reset()
    val radians = degrees.toDouble() * PI / 180.0
    val s = sin(radians).toFloat()
    val c = cos(radians).toFloat()
    this[MSCALE_X] = c
    this[MSKEW_X] = -s
    this[MSKEW_Y] = s
    this[MSCALE_Y] = c
    this[MTRANS_X] = px - c * px + s * py
    this[MTRANS_Y] = py - s * px - c * py
    normalize()
}

internal fun FloatArray.setSkew(
    kx: Float,
    ky: Float,
    px: Float,
    py: Float,
) {
    reset()
    this[MSCALE_X] = 1f
    this[MSKEW_X] = kx
    this[MSKEW_Y] = ky
    this[MSCALE_Y] = 1f
    this[MTRANS_X] = -kx * py
    this[MTRANS_Y] = -ky * px
    normalize()
}

internal fun FloatArray.preTranslate(
    dx: Float,
    dy: Float,
) {
    this[MTRANS_X] += this[MSCALE_X] * dx + this[MSKEW_X] * dy
    this[MTRANS_Y] += this[MSKEW_Y] * dx + this[MSCALE_Y] * dy
    this[MPERSP_2] += this[MPERSP_0] * dx + this[MPERSP_1] * dy
    normalize()
}

internal fun FloatArray.preScale(
    sx: Float,
    sy: Float,
    px: Float = 0f,
    py: Float = 0f,
) {
    val tmp = FloatArray(THREE_BY_THREE)
    tmp.setScale(sx, sy, px, py)
    preConcat(tmp)
}

internal fun FloatArray.preRotate(
    degrees: Float,
    px: Float,
    py: Float,
) {
    val tmp = FloatArray(THREE_BY_THREE)
    tmp.setRotate(degrees, px, py)
    preConcat(tmp)
}

internal fun FloatArray.preSkew(
    kx: Float,
    ky: Float,
    px: Float,
    py: Float,
) {
    val tmp = FloatArray(THREE_BY_THREE)
    tmp.setSkew(kx, ky, px, py)
    preConcat(tmp)
}

internal fun FloatArray.postTranslate(
    dx: Float,
    dy: Float,
) {
    val g = this[MPERSP_0]
    val h = this[MPERSP_1]
    val i = this[MPERSP_2]
    this[MSCALE_X] += dx * g
    this[MSKEW_X] += dx * h
    this[MTRANS_X] += dx * i
    this[MSKEW_Y] += dy * g
    this[MSCALE_Y] += dy * h
    this[MTRANS_Y] += dy * i
    normalize()
}

internal fun FloatArray.postScale(
    sx: Float,
    sy: Float,
    px: Float = 0f,
    py: Float = 0f,
) {
    val tmp = FloatArray(THREE_BY_THREE)
    tmp.setScale(sx, sy, px, py)
    postConcat(tmp)
}

internal fun FloatArray.postRotate(
    degrees: Float,
    px: Float,
    py: Float,
) {
    val tmp = FloatArray(THREE_BY_THREE)
    tmp.setRotate(degrees, px, py)
    postConcat(tmp)
}

internal fun FloatArray.postSkew(
    kx: Float,
    ky: Float,
    px: Float,
    py: Float,
) {
    val tmp = FloatArray(THREE_BY_THREE)
    tmp.setSkew(kx, ky, px, py)
    postConcat(tmp)
}

internal fun FloatArray.preConcat(other: FloatArray) {
    val a = this
    val b = other
    val v0 = a[0] * b[0] + a[1] * b[3] + a[2] * b[6]
    val v1 = a[0] * b[1] + a[1] * b[4] + a[2] * b[7]
    val v2 = a[0] * b[2] + a[1] * b[5] + a[2] * b[8]
    val v3 = a[3] * b[0] + a[4] * b[3] + a[5] * b[6]
    val v4 = a[3] * b[1] + a[4] * b[4] + a[5] * b[7]
    val v5 = a[3] * b[2] + a[4] * b[5] + a[5] * b[8]
    val v6 = a[6] * b[0] + a[7] * b[3] + a[8] * b[6]
    val v7 = a[6] * b[1] + a[7] * b[4] + a[8] * b[7]
    val v8 = a[6] * b[2] + a[7] * b[5] + a[8] * b[8]
    this[0] = v0
    this[1] = v1
    this[2] = v2
    this[3] = v3
    this[4] = v4
    this[5] = v5
    this[6] = v6
    this[7] = v7
    this[8] = v8
    normalize()
}

internal fun FloatArray.postConcat(other: FloatArray) {
    val a = other
    val b = this
    val v0 = a[0] * b[0] + a[1] * b[3] + a[2] * b[6]
    val v1 = a[0] * b[1] + a[1] * b[4] + a[2] * b[7]
    val v2 = a[0] * b[2] + a[1] * b[5] + a[2] * b[8]
    val v3 = a[3] * b[0] + a[4] * b[3] + a[5] * b[6]
    val v4 = a[3] * b[1] + a[4] * b[4] + a[5] * b[7]
    val v5 = a[3] * b[2] + a[4] * b[5] + a[5] * b[8]
    val v6 = a[6] * b[0] + a[7] * b[3] + a[8] * b[6]
    val v7 = a[6] * b[1] + a[7] * b[4] + a[8] * b[7]
    val v8 = a[6] * b[2] + a[7] * b[5] + a[8] * b[8]
    this[0] = v0
    this[1] = v1
    this[2] = v2
    this[3] = v3
    this[4] = v4
    this[5] = v5
    this[6] = v6
    this[7] = v7
    this[8] = v8
    normalize()
}

internal fun FloatArray.invert(): FloatArray? {
    val v = this
    val det =
        v[0] * (v[4] * v[8] - v[5] * v[7]) -
            v[1] * (v[3] * v[8] - v[5] * v[6]) +
            v[2] * (v[3] * v[7] - v[4] * v[6])
    if (abs(det) < 1e-10) return null
    val invDet = 1.0f / det
    val res = FloatArray(THREE_BY_THREE)
    res[0] = (v[4] * v[8] - v[5] * v[7]) * invDet
    res[1] = (v[2] * v[7] - v[1] * v[8]) * invDet
    res[2] = (v[1] * v[5] - v[2] * v[4]) * invDet
    res[3] = (v[5] * v[6] - v[3] * v[8]) * invDet
    res[4] = (v[0] * v[8] - v[2] * v[6]) * invDet
    res[5] = (v[2] * v[3] - v[0] * v[5]) * invDet
    res[6] = (v[3] * v[7] - v[4] * v[6]) * invDet
    res[7] = (v[1] * v[6] - v[0] * v[7]) * invDet
    res[8] = (v[0] * v[4] - v[1] * v[3]) * invDet
    res.normalize()
    return res
}

internal fun FloatArray.mapPoints(
    dst: FloatArray,
    dstIndex: Int,
    src: FloatArray,
    srcIndex: Int,
    pointCount: Int,
) {
    for (i in 0 until pointCount) {
        val si = srcIndex + i * 2
        val di = dstIndex + i * 2
        val x = src[si]
        val y = src[si + 1]
        val w = this[MPERSP_0] * x + this[MPERSP_1] * y + this[MPERSP_2]
        dst[di] = (this[MSCALE_X] * x + this[MSKEW_X] * y + this[MTRANS_X]) / w
        dst[di + 1] = (this[MSKEW_Y] * x + this[MSCALE_Y] * y + this[MTRANS_Y]) / w
    }
}

internal fun FloatArray.mapVectors(
    dst: FloatArray,
    dstIndex: Int,
    src: FloatArray,
    srcIndex: Int,
    vectorCount: Int,
) {
    for (i in 0 until vectorCount) {
        val si = srcIndex + i * 2
        val di = dstIndex + i * 2
        val x = src[si]
        val y = src[si + 1]
        dst[di] = this[MSCALE_X] * x + this[MSKEW_X] * y
        dst[di + 1] = this[MSKEW_Y] * x + this[MSCALE_Y] * y
    }
}
