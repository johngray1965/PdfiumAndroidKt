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

interface MatrixValues {
    val values: FloatArray
}

/**
 * Immutable transformation matrix.
 * Methods return new instances or mapped values instead of modifying the current ones.
 */
@Keep
@Suppress("TooManyFunctions")
data class PdfMatrix(
    override val values: FloatArray =
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
) : MatrixValues {
    constructor(other: MatrixValues) : this(other.values.copyOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PdfMatrix
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()

    fun reset(): PdfMatrix = PdfMatrix()

    fun isIdentity(): Boolean = values.isIdentity()

    fun isAffine(): Boolean = values.isAffine()

    // --- Mapping functions ---

    fun mapRect(rect: FloatRectValues): PdfRectF = values.mapRect(rect)

    fun mapRect(
        dst: MutablePdfRectF,
        src: FloatRectValues,
    ) = values.mapRect(dst, src)

    fun mapRadius(radius: Float): Float = values.mapRadius(radius)

    fun mapPoints(pts: FloatArray) = values.mapPoints(pts)

    fun mapPoints(
        dst: FloatArray,
        src: FloatArray,
    ) = values.mapPoints(dst, src)

    fun mapPoints(
        dst: FloatArray,
        dstIndex: Int,
        src: FloatArray,
        srcIndex: Int,
        pointCount: Int,
    ) = values.mapPoints(dst, dstIndex, src, srcIndex, pointCount)

    fun mapVectors(vecs: FloatArray) = values.mapVectors(vecs)

    fun mapVectors(
        dest: FloatArray,
        src: FloatArray,
    ) = values.mapVectors(dest, src)

    fun mapVectors(
        dst: FloatArray,
        dstIndex: Int,
        src: FloatArray,
        srcIndex: Int,
        vectorCount: Int,
    ) = values.mapVectors(dst, dstIndex, src, srcIndex, vectorCount)

    /**
     * Returns the inverse of this matrix, or null if it's not invertible.
     */
    fun invert(): PdfMatrix? = values.invert()?.let { PdfMatrix(it) }

    // --- Immutable modifiers (return new instance) ---

    fun setTranslate(
        dx: Float,
        dy: Float,
    ) = PdfMatrix(FloatArray(THREE_BY_THREE).apply { setTranslate(dx, dy) })

    fun setScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(FloatArray(THREE_BY_THREE).apply { setScale(sx, sy, px, py) })

    fun setRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(FloatArray(THREE_BY_THREE).apply { setRotate(degrees, px, py) })

    fun setSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(FloatArray(THREE_BY_THREE).apply { setSkew(kx, ky, px, py) })

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

    fun concat(other: MatrixValues) = PdfMatrix(values.copyOf().apply { preConcat(other.values) })

    fun preTranslate(
        dx: Float,
        dy: Float,
    ) = translate(dx, dy)

    fun preScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = scale(sx, sy, px, py)

    fun preRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = rotate(degrees, px, py)

    fun preSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = skew(kx, ky, px, py)

    fun postTranslate(
        dx: Float,
        dy: Float,
    ) = PdfMatrix(values.copyOf().apply { postTranslate(dx, dy) })

    fun postScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(values.copyOf().apply { postScale(sx, sy, px, py) })

    fun postRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(values.copyOf().apply { postRotate(degrees, px, py) })

    fun postSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(values.copyOf().apply { postSkew(kx, ky, px, py) })

    fun postConcat(other: MatrixValues) = PdfMatrix(values.copyOf().apply { postConcat(other.values) })

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
    override val values: FloatArray =
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
) : MatrixValues {
    constructor(other: MatrixValues) : this(other.values.copyOf())

    fun reset(): MutablePdfMatrix {
        values.reset()
        return this
    }

    fun set(src: MatrixValues): MutablePdfMatrix {
        System.arraycopy(src.values, 0, values, 0, THREE_BY_THREE)
        return this
    }

    fun set(src: FloatArray): MutablePdfMatrix {
        System.arraycopy(src, 0, values, 0, THREE_BY_THREE)
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

    fun preTranslate(
        dx: Float,
        dy: Float,
    ): MutablePdfMatrix {
        values.preTranslate(dx, dy)
        return this
    }

    fun preScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.preScale(sx, sy, px, py)
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

    fun postScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.postScale(sx, sy, px, py)
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

    fun postSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ): MutablePdfMatrix {
        values.postSkew(kx, ky, px, py)
        return this
    }

    fun postConcat(other: MatrixValues): MutablePdfMatrix {
        values.postConcat(other.values)
        return this
    }

    fun preConcat(other: MatrixValues): MutablePdfMatrix {
        values.preConcat(other.values)
        return this
    }

    fun invert(target: MatrixValues): Boolean {
        val inv = values.invert()
        return if (inv != null) {
            System.arraycopy(inv, 0, target.values, 0, THREE_BY_THREE)
            true
        } else {
            false
        }
    }
    // --- Mapping functions (Mutable matrix can still return new values) ---

    fun mapRect(rect: FloatRectValues): PdfRectF = values.mapRect(rect)

    fun mapRect(
        dst: MutablePdfRectF,
        src: FloatRectValues,
    ) = values.mapRect(dst, src)

    fun mapRadius(radius: Float): Float = values.mapRadius(radius)

    fun mapPoints(pts: FloatArray) = values.mapPoints(pts)

    fun mapPoints(
        dst: FloatArray,
        src: FloatArray,
    ) = values.mapPoints(dst, src)

    fun mapPoints(
        dst: FloatArray,
        dstIndex: Int,
        src: FloatArray,
        srcIndex: Int,
        pointCount: Int,
    ) = values.mapPoints(dst, dstIndex, src, srcIndex, pointCount)

    fun mapVectors(vecs: FloatArray) = values.mapVectors(vecs)

    fun mapVectors(
        dest: FloatArray,
        src: FloatArray,
    ) = values.mapVectors(dest, src)

    fun mapVectors(
        dst: FloatArray,
        dstIndex: Int,
        src: FloatArray,
        srcIndex: Int,
        vectorCount: Int,
    ) = values.mapVectors(dst, dstIndex, src, srcIndex, vectorCount)

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

@Suppress("MagicNumber")
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
    this[MSCALE_X] = sx
    this[MSCALE_Y] = sy
    this[MTRANS_X] = px - sx * px
    this[MTRANS_Y] = py - sy * py
    this[MSKEW_X] = 0f
    this[MSKEW_Y] = 0f
    this[MPERSP_0] = 0f
    this[MPERSP_1] = 0f
    this[MPERSP_2] = 1f
    normalize()
}

@Suppress("MagicNumber")
internal fun FloatArray.setRotate(
    degrees: Float,
    px: Float,
    py: Float,
) {
    val radians = degrees.toDouble() * PI / 180.0
    val s = sin(radians).toFloat()
    val c = cos(radians).toFloat()
    this[MSCALE_X] = c
    this[MSKEW_X] = -s
    this[MSKEW_Y] = s
    this[MSCALE_Y] = c
    this[MTRANS_X] = px - c * px + s * py
    this[MTRANS_Y] = py - s * px - c * py
    this[MPERSP_0] = 0f
    this[MPERSP_1] = 0f
    this[MPERSP_2] = 1f
    normalize()
}

internal fun FloatArray.setSkew(
    kx: Float,
    ky: Float,
    px: Float,
    py: Float,
) {
    this[MSCALE_X] = 1f
    this[MSKEW_X] = kx
    this[MSKEW_Y] = ky
    this[MSCALE_Y] = 1f
    this[MTRANS_X] = -kx * py
    this[MTRANS_Y] = -ky * px
    this[MPERSP_0] = 0f
    this[MPERSP_1] = 0f
    this[MPERSP_2] = 1f
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
    px: Float,
    py: Float,
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
    px: Float,
    py: Float,
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

@Suppress("MagicNumber", "UnnecessaryVariable")
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

@Suppress("MagicNumber")
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

@Suppress("MagicNumber")
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

internal fun FloatArray.mapX(
    x: Float,
    y: Float,
): Float {
    val w = this[MPERSP_0] * x + this[MPERSP_1] * y + this[MPERSP_2]
    return (this[MSCALE_X] * x + this[MSKEW_X] * y + this[MTRANS_X]) / w
}

internal fun FloatArray.mapY(
    x: Float,
    y: Float,
): Float {
    val w = this[MPERSP_0] * x + this[MPERSP_1] * y + this[MPERSP_2]
    return (this[MSKEW_Y] * x + this[MSCALE_Y] * y + this[MTRANS_Y]) / w
}

internal fun FloatArray.mapRect(rect: FloatRectValues): PdfRectF {
    val x1 = mapX(rect.left, rect.top)
    val y1 = mapY(rect.left, rect.top)
    val x2 = mapX(rect.right, rect.top)
    val y2 = mapY(rect.right, rect.top)
    val x3 = mapX(rect.right, rect.bottom)
    val y3 = mapY(rect.right, rect.bottom)
    val x4 = mapX(rect.left, rect.bottom)
    val y4 = mapY(rect.left, rect.bottom)
    return PdfRectF(
        left = min(x1, min(x2, min(x3, x4))),
        top = min(y1, min(y2, min(y3, y4))),
        right = max(x1, max(x2, max(x3, x4))),
        bottom = max(y1, max(y2, max(y3, y4))),
    )
}

internal fun FloatArray.mapRect(
    dst: MutablePdfRectF,
    src: FloatRectValues,
) {
    val x1 = mapX(src.left, src.top)
    val y1 = mapY(src.left, src.top)
    val x2 = mapX(src.right, src.top)
    val y2 = mapY(src.right, src.top)
    val x3 = mapX(src.right, src.bottom)
    val y3 = mapY(src.right, src.bottom)
    val x4 = mapX(src.left, src.bottom)
    val y4 = mapY(src.left, src.bottom)
    dst.set(
        l = min(x1, min(x2, min(x3, x4))),
        t = min(y1, min(y2, min(y3, y4))),
        r = max(x1, max(x2, max(x3, x4))),
        b = max(y1, max(y2, max(y3, y4))),
    )
}

internal fun FloatArray.mapRadius(radius: Float): Float {
    val tmp = FloatArray(4)
    tmp[0] = radius
    tmp[1] = 0f
    tmp[2] = 0f
    tmp[3] = radius
    mapVectors(tmp)

    val d1 = distance(0f, 0f, tmp[0], tmp[1])
    val d2 = distance(0f, 0f, tmp[2], tmp[3])
    return sqrt(d1 * d2)
}

internal fun distance(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return sqrt(dx * dx + dy * dy)
}

internal fun FloatArray.mapPoints(pts: FloatArray) {
    mapPoints(pts, 0, pts, 0, pts.size / 2)
}

internal fun FloatArray.mapPoints(
    dst: FloatArray,
    src: FloatArray,
) {
    mapPoints(dst, 0, src, 0, src.size / 2)
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

internal fun FloatArray.mapVectors(vecs: FloatArray) {
    mapVectors(vecs, 0, vecs, 0, vecs.size / 2)
}

internal fun FloatArray.mapVectors(
    dest: FloatArray,
    src: FloatArray,
) {
    mapVectors(dest, 0, src, 0, src.size / 2)
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
