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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

const val THREE_BY_THREE = 9
const val SCALE_X = 0
const val SKEW_X = 1
const val TRANS_X = 2
const val SKEW_Y = 3
const val SCALE_Y = 4
const val TRANS_Y = 5
const val PERSP_0 = 6
const val PERSP_1 = 7
const val PERSP_2 = 8
const val ZERO_TOLERANCE = 1.0 / (1 shl 16)

const val DEGREES_TO_RADIANS = (PI / 180.0)

// Custom abs function for Double to avoid potential overhead of kotlin.math.abs
private fun fastAbs(x: Double) = if (x < 0) -x else x

interface MatrixValues {
    val values: DoubleArray
}

/**
 * Immutable transformation matrix.
 * Methods return new instances or mapped values instead of modifying the current ones.
 */
@Keep
@Suppress("TooManyFunctions")
class PdfMatrix(
    override val values: DoubleArray =
        doubleArrayOf(
            1.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            1.0,
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
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setTranslate(dx, dy) })

    fun setTranslate(
        dx: Double,
        dy: Double,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setTranslate(dx, dy) })

    fun setScale(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setScale(sx, sy, px, py) })

    fun setScale(
        sx: Double,
        sy: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setScale(sx, sy, px, py) })

    fun setScale(
        sx: Float,
        sy: Float,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setScale(sx, sy) })

    fun setScale(
        sx: Double,
        sy: Double,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setScale(sx, sy) })

    fun setRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ) = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setRotate(degrees, px, py) })

    fun setRotate(
        degrees: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setRotate(degrees, px, py) })

    fun setRotate(degrees: Float): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setRotate(degrees) })

    fun setRotate(degrees: Double): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setRotate(degrees) })

    fun setSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setSkew(kx, ky, px, py) })

    fun setSkew(
        kx: Double,
        ky: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setSkew(kx, ky, px, py) })

    fun setSkew(
        kx: Float,
        ky: Float,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setSkew(kx, ky) })

    fun setSkew(
        kx: Double,
        ky: Double,
    ): PdfMatrix = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setSkew(kx, ky) })

    fun preConcat(other: MatrixValues): PdfMatrix = PdfMatrix(values.copyOf().apply { preConcat(other.values) })

    fun preTranslate(
        dx: Float,
        dy: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preTranslate(dx, dy) })

    fun preTranslate(
        dx: Double,
        dy: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preTranslate(dx, dy) })

    fun preScale(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preScale(sx, sy, px, py) })

    fun preScale(
        sx: Double,
        sy: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preScale(sx, sy, px, py) })

    fun preScale(
        sx: Float,
        sy: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preScale(sx, sy) })

    fun preScale(
        sx: Double,
        sy: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preScale(sx, sy) })

    fun preRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preRotate(degrees, px, py) })

    fun preRotate(
        degrees: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preRotate(degrees, px, py) })

    fun preRotate(degrees: Float): PdfMatrix = PdfMatrix(values.copyOf().apply { preRotate(degrees) })

    fun preRotate(degrees: Double): PdfMatrix = PdfMatrix(values.copyOf().apply { preRotate(degrees) })

    fun preSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preSkew(kx, ky, px, py) })

    fun preSkew(
        kx: Double,
        ky: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preSkew(kx, ky, px, py) })

    fun preSkew(
        kx: Float,
        ky: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preSkew(kx, ky) })

    fun preSkew(
        kx: Double,
        ky: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { preSkew(kx, ky) })

    fun postTranslate(
        dx: Float,
        dy: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postTranslate(dx, dy) })

    fun postTranslate(
        dx: Double,
        dy: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postTranslate(dx, dy) })

    fun postScale(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postScale(sx, sy, px, py) })

    fun postScale(
        sx: Double,
        sy: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postScale(sx, sy, px, py) })

    fun postScale(
        sx: Float,
        sy: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postScale(sx, sy) })

    fun postScale(
        sx: Double,
        sy: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postScale(sx, sy) })

    fun postRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postRotate(degrees, px, py) })

    fun postRotate(
        degrees: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postRotate(degrees, px, py) })

    fun postRotate(degrees: Float): PdfMatrix = PdfMatrix(values.copyOf().apply { postRotate(degrees) })

    fun postRotate(degrees: Double): PdfMatrix = PdfMatrix(values.copyOf().apply { postRotate(degrees) })

    fun postSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postSkew(kx, ky, px, py) })

    fun postSkew(
        kx: Double,
        ky: Double,
        px: Double,
        py: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postSkew(kx, ky, px, py) })

    fun postSkew(
        kx: Float,
        ky: Float,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postSkew(kx, ky) })

    fun postSkew(
        kx: Double,
        ky: Double,
    ): PdfMatrix = PdfMatrix(values.copyOf().apply { postSkew(kx, ky) })

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
    override val values: DoubleArray =
        doubleArrayOf(
            1.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            1.0,
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

    fun set(src: DoubleArray): MutablePdfMatrix {
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

    fun setTranslate(
        dx: Double,
        dy: Double,
    ): MutablePdfMatrix {
        values.setTranslate(dx, dy)
        return this
    }

    fun setScale(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.setScale(sx, sy, px, py)
        return this
    }

    fun setScale(
        sx: Double,
        sy: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.setScale(sx, sy, px, py)
        return this
    }

    fun setScale(
        sx: Float,
        sy: Float,
    ): MutablePdfMatrix {
        values.setScale(sx, sy)
        return this
    }

    fun setScale(
        sx: Double,
        sy: Double,
    ): MutablePdfMatrix {
        values.setScale(sx, sy)
        return this
    }

    fun setRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.setRotate(degrees, px, py)
        return this
    }

    fun setRotate(
        degrees: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.setRotate(degrees, px, py)
        return this
    }

    fun setRotate(degrees: Float): MutablePdfMatrix {
        values.setRotate(degrees)
        return this
    }

    fun setRotate(degrees: Double): MutablePdfMatrix {
        values.setRotate(degrees)
        return this
    }

    fun setSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.setSkew(kx, ky, px, py)
        return this
    }

    fun setSkew(
        kx: Double,
        ky: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.setSkew(kx, ky, px, py)
        return this
    }

    fun setSkew(
        kx: Float,
        ky: Float,
    ): MutablePdfMatrix {
        values.setSkew(kx, ky)
        return this
    }

    fun setSkew(
        kx: Double,
        ky: Double,
    ): MutablePdfMatrix {
        values.setSkew(kx, ky)
        return this
    }

    fun preTranslate(
        dx: Float,
        dy: Float,
    ): MutablePdfMatrix {
        values.preTranslate(dx, dy)
        return this
    }

    fun preTranslate(
        dx: Double,
        dy: Double,
    ): MutablePdfMatrix {
        values.preTranslate(dx, dy)
        return this
    }

    fun preScale(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.preScale(sx, sy, px, py)
        return this
    }

    fun preScale(
        sx: Double,
        sy: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.preScale(sx, sy, px, py)
        return this
    }

    fun preScale(
        sx: Float,
        sy: Float,
    ): MutablePdfMatrix {
        values.preScale(sx, sy)
        return this
    }

    fun preScale(
        sx: Double,
        sy: Double,
    ): MutablePdfMatrix {
        values.preScale(sx, sy)
        return this
    }

    fun preRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.preRotate(degrees, px, py)
        return this
    }

    fun preRotate(
        degrees: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.preRotate(degrees, px, py)
        return this
    }

    fun preRotate(degrees: Float): MutablePdfMatrix {
        values.preRotate(degrees)
        return this
    }

    fun preRotate(degrees: Double): MutablePdfMatrix {
        values.preRotate(degrees)
        return this
    }

    fun preSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.preSkew(kx, ky, px, py)
        return this
    }

    fun preSkew(
        kx: Double,
        ky: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.preSkew(kx, ky, px, py)
        return this
    }

    fun preSkew(
        kx: Float,
        ky: Float,
    ): MutablePdfMatrix {
        values.preSkew(kx, ky)
        return this
    }

    fun preSkew(
        kx: Double,
        ky: Double,
    ): MutablePdfMatrix {
        values.preSkew(kx, ky)
        return this
    }

    fun postTranslate(
        dx: Float,
        dy: Float,
    ): MutablePdfMatrix {
        values.postTranslate(dx, dy)
        return this
    }

    fun postTranslate(
        dx: Double,
        dy: Double,
    ): MutablePdfMatrix {
        values.postTranslate(dx, dy)
        return this
    }

    fun postScale(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.postScale(sx, sy, px, py)
        return this
    }

    fun postScale(
        sx: Double,
        sy: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.postScale(sx, sy, px, py)
        return this
    }

    fun postScale(
        sx: Float,
        sy: Float,
    ): MutablePdfMatrix {
        values.postScale(sx, sy)
        return this
    }

    fun postScale(
        sx: Double,
        sy: Double,
    ): MutablePdfMatrix {
        values.postScale(sx, sy)
        return this
    }

    fun postRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.postRotate(degrees, px, py)
        return this
    }

    fun postRotate(
        degrees: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.postRotate(degrees, px, py)
        return this
    }

    fun postRotate(degrees: Float): MutablePdfMatrix {
        values.postRotate(degrees)
        return this
    }

    fun postRotate(degrees: Double): MutablePdfMatrix {
        values.postRotate(degrees)
        return this
    }

    fun postSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): MutablePdfMatrix {
        values.postSkew(kx, ky, px, py)
        return this
    }

    fun postSkew(
        kx: Double,
        ky: Double,
        px: Double,
        py: Double,
    ): MutablePdfMatrix {
        values.postSkew(kx, ky, px, py)
        return this
    }

    fun postSkew(
        kx: Float,
        ky: Float,
    ): MutablePdfMatrix {
        values.postSkew(kx, ky)
        return this
    }

    fun postSkew(
        kx: Double,
        ky: Double,
    ): MutablePdfMatrix {
        values.postSkew(kx, ky)
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

internal fun DoubleArray.reset() {
    fill(0.0)
    this[SCALE_X] = 1.0
    this[SCALE_Y] = 1.0
    this[PERSP_2] = 1.0
}

internal fun DoubleArray.isIdentity(): Boolean =
    this[SCALE_X] == 1.0 && this[SKEW_X] == 0.0 && this[TRANS_X] == 0.0 &&
        this[SKEW_Y] == 0.0 && this[SCALE_Y] == 1.0 && this[TRANS_Y] == 0.0 &&
        this[PERSP_0] == 0.0 && this[PERSP_1] == 0.0 && this[PERSP_2] == 1.0

internal fun DoubleArray.isAffine(): Boolean = this[PERSP_0] == 0.0 && this[PERSP_1] == 0.0 && this[PERSP_2] == 1.0

internal fun DoubleArray.setTranslate(
    dx: Float,
    dy: Float,
) {
    reset()
    this[TRANS_X] = dx.toDouble()
    this[TRANS_Y] = dy.toDouble()
}

internal fun DoubleArray.setTranslate(
    dx: Double,
    dy: Double,
) {
    reset()
    this[TRANS_X] = dx
    this[TRANS_Y] = dy
}

internal fun DoubleArray.setScale(
    sx: Float,
    sy: Float,
    px: Float,
    py: Float,
) {
    val sxd = sx.toDouble()
    val syd = sy.toDouble()
    val pxd = px.toDouble()
    val pyd = py.toDouble()
    setScale(sxd, syd, pxd, pyd)
}

internal fun DoubleArray.setScale(
    sx: Double,
    sy: Double,
    px: Double,
    py: Double,
) {
    this[SCALE_X] = sx
    this[SCALE_Y] = sy
    this[TRANS_X] = (px - sx * px)
    this[TRANS_Y] = (py - sy * py)
    this[SKEW_X] = 0.0
    this[SKEW_Y] = 0.0
    this[PERSP_0] = 0.0
    this[PERSP_1] = 0.0
    this[PERSP_2] = 1.0
}

internal fun DoubleArray.setScale(
    sx: Float,
    sy: Float,
) {
    val sxd = sx.toDouble()
    val syd = sy.toDouble()
    setScale(sxd, syd)
}

internal fun DoubleArray.setScale(
    sx: Double,
    sy: Double,
) {
    this[SCALE_X] = sx
    this[SCALE_Y] = sy
    this[TRANS_X] = 0.0
    this[TRANS_Y] = 0.0
    this[SKEW_X] = 0.0
    this[SKEW_Y] = 0.0
    this[PERSP_0] = 0.0
    this[PERSP_1] = 0.0
    this[PERSP_2] = 1.0
}

internal fun DoubleArray.setRotate(
    degrees: Float,
    px: Float,
    py: Float,
) {
    setRotate(degrees.toDouble(), px.toDouble(), py.toDouble())
}

@Suppress("MagicNumber")
internal fun DoubleArray.setRotate(
    degrees: Double,
    px: Double,
    py: Double,
) {
    // Note we do the same chunk of math for the cos/sin in 6 places.
    // It would be nice to have one common function, right?
    // DO NOT DO IT.  This is fast, very fast.  You can't make
    // this cleaner without making it slower (I've spent a lot of time trying).
    val normalizedDegrees = (degrees % 360.0 + 360.0) % 360.0

    val sin: Double
    val cos: Double
    when (degrees) {
        0.0 -> { // Exact 0 degrees
            sin = 0.0
            cos = 1.0
        }

        90.0 -> { // Exact 90 degrees
            sin = 1.0
            cos = 0.0
        }

        180.0 -> {
            sin = 0.0
            cos = -1.0
        }

        270.0 -> {
            sin = -1.0
            cos = 0.0
        }

        else -> {
            val radians = normalizedDegrees * DEGREES_TO_RADIANS
            val sina = sin(radians)
            val cosa = cos(radians)
            sin = if (fastAbs(sina) < ZERO_TOLERANCE) 0.0 else sina
            cos = if (fastAbs(cosa) < ZERO_TOLERANCE) 0.0 else cosa
        }
    }

    this[SCALE_X] = cos
    this[SKEW_X] = -sin
    this[SKEW_Y] = sin
    this[SCALE_Y] = cos
    this[TRANS_X] = (px - cos * px + sin * py)
    this[TRANS_Y] = (py - sin * px - cos * py)
    this[PERSP_0] = 0.0
    this[PERSP_1] = 0.0
    this[PERSP_2] = 1.0
}

@Suppress("MagicNumber")
internal fun DoubleArray.setRotate(degrees: Float) {
    setRotate(degrees.toDouble())
}

@Suppress("MagicNumber")
internal fun DoubleArray.setRotate(degrees: Double) {
    // Note we do the same chunk of math for the cos/sin in 6 places.
    // It would be nice to have one common function, right?
    // DO NOT DO IT.  This is fast, very fast.  You can't make
    // this cleaner without making it slower (I've spent a lot of time trying).
    val normalizedDegrees = (degrees % 360.0 + 360.0) % 360.0

    val sin: Double
    val cos: Double
    when (degrees) {
        0.0 -> { // Exact 0 degrees
            sin = 0.0
            cos = 1.0
        }

        90.0 -> { // Exact 90 degrees
            sin = 1.0
            cos = 0.0
        }

        180.0 -> {
            sin = 0.0
            cos = -1.0
        }

        270.0 -> {
            sin = -1.0
            cos = 0.0
        }

        else -> {
            val radians = normalizedDegrees * DEGREES_TO_RADIANS
            val sina = sin(radians)
            val cosa = cos(radians)
            sin = if (fastAbs(sina) < ZERO_TOLERANCE) 0.0 else sina
            cos = if (fastAbs(cosa) < ZERO_TOLERANCE) 0.0 else cosa
        }
    }

    this[SCALE_X] = cos
    this[SKEW_X] = -sin
    this[SKEW_Y] = sin
    this[SCALE_Y] = cos
    this[TRANS_X] = 0.0
    this[TRANS_Y] = 0.0
    this[PERSP_0] = 0.0
    this[PERSP_1] = 0.0
    this[PERSP_2] = 1.0
}

internal fun DoubleArray.setSkew(
    kx: Float,
    ky: Float,
    px: Float,
    py: Float,
) {
    setSkew(kx.toDouble(), ky.toDouble(), px.toDouble(), py.toDouble())
}

internal fun DoubleArray.setSkew(
    kx: Double,
    ky: Double,
    px: Double,
    py: Double,
) {
    this[SCALE_X] = 1.0
    this[SKEW_X] = kx
    this[SKEW_Y] = ky
    this[SCALE_Y] = 1.0
    this[TRANS_X] = -(kx * py)
    this[TRANS_Y] = -(ky * px)
    this[PERSP_0] = 0.0
    this[PERSP_1] = 0.0
    this[PERSP_2] = 1.0
}

internal fun DoubleArray.setSkew(
    kx: Float,
    ky: Float,
) {
    setSkew(kx.toDouble(), ky.toDouble())
}

internal fun DoubleArray.setSkew(
    kx: Double,
    ky: Double,
) {
    this[SCALE_X] = 1.0
    this[SKEW_X] = kx
    this[SKEW_Y] = ky
    this[SCALE_Y] = 1.0
    this[TRANS_X] = 0.0
    this[TRANS_Y] = 0.0
    this[PERSP_0] = 0.0
    this[PERSP_1] = 0.0
    this[PERSP_2] = 1.0
}

/**
 *        Given:
 *
 *                      | A B C |               | 1 0 dx |
 *             Matrix = | D E F |,  T(dx, dy) = | 0 1 dy |
 *                      | G H I |               | 0 0  1 |
 *
 *         sets SkMatrix to:
 *
 *                                  | A B C | | 1 0 dx |   | A B A*dx+B*dy+C |
 *             Matrix * T(dx, dy) = | D E F | | 0 1 dy | = | D E D*dx+E*dy+F |
 *                                  | G H I | | 0 0  1 |   | G H G*dx+H*dy+I |
 *
 */
internal fun DoubleArray.preTranslate(
    dx: Float,
    dy: Float,
) {
    preTranslate(dx.toDouble(), dy.toDouble())
}

internal fun DoubleArray.preTranslate(
    dx: Double,
    dy: Double,
) {
    val a = this[SCALE_X]
    val b = this[SKEW_X]
    val c = this[TRANS_X]
    val d = this[SKEW_Y]
    val e = this[SCALE_Y]
    val f = this[TRANS_Y]
    val g = this[PERSP_0]
    val h = this[PERSP_1]
    val i = this[PERSP_2]

    this[TRANS_X] = a * dx + b * dy + c
    this[TRANS_Y] = d * dx + e * dy + f
    this[PERSP_2] = g * dx + h * dy + i
}

/**
 *         Given:
 *
 *                      | A B C |                       | sx  0 dx |
 *             Matrix = | D E F |,  S(sx, sy, px, py) = |  0 sy dy |
 *                      | G H I |                       |  0  0  1 |
 *
 *         where
 *
 *             dx = px - sx * px
 *             dy = py - sy * py
 *
 *         sets SkMatrix to:
 *
 *                                          | A B C | | sx  0 dx |   | A*sx B*sy A*dx+B*dy+C |
 *             Matrix * S(sx, sy, px, py) = | D E F | |  0 sy dy | = | D*sx E*sy D*dx+E*dy+F |
 *                                          | G H I | |  0  0  1 |   | G*sx H*sy G*dx+H*dy+I |
 *
 *
 */
internal fun DoubleArray.preScale(
    sx: Float,
    sy: Float,
    px: Float,
    py: Float,
) {
    preScale(sx.toDouble(), sy.toDouble(), px.toDouble(), py.toDouble())
}

internal fun DoubleArray.preScale(
    sx: Double,
    sy: Double,
    px: Double,
    py: Double,
) {
    val dx = px - sx * px
    val dy = py - sy * py

    val a = this[SCALE_X]
    val b = this[SKEW_X]
    val c = this[TRANS_X]
    val d = this[SKEW_Y]
    val e = this[SCALE_Y]
    val f = this[TRANS_Y]
    val g = this[PERSP_0]
    val h = this[PERSP_1]
    val i = this[PERSP_2]

    this[SCALE_X] = a * sx
    this[SKEW_X] = b * sy
    this[TRANS_X] = a * dx + b * dy + c
    this[SKEW_Y] = d * sx
    this[SCALE_Y] = e * sy
    this[TRANS_Y] = d * dx + e * dy + f
    this[PERSP_0] = g * sx
    this[PERSP_1] = h * sy
    this[PERSP_2] = g * dx + h * dy + i
}

internal fun DoubleArray.preScale(
    sx: Float,
    sy: Float,
) {
    preScale(sx.toDouble(), sy.toDouble())
}

internal fun DoubleArray.preScale(
    sx: Double,
    sy: Double,
) {
    val a = this[SCALE_X]
    val b = this[SKEW_X]
    val c = this[TRANS_X]
    val d = this[SKEW_Y]
    val e = this[SCALE_Y]
    val f = this[TRANS_Y]
    val g = this[PERSP_0]
    val h = this[PERSP_1]
    val i = this[PERSP_2]

    this[SCALE_X] = a * sx
    this[SKEW_X] = b * sy
    this[TRANS_X] = c
    this[SKEW_Y] = d * sx
    this[SCALE_Y] = e * sy
    this[TRANS_Y] = f
    this[PERSP_0] = g * sx
    this[PERSP_1] = h * sy
    this[PERSP_2] = i
}

/**
 *         Given:
 *
 *                      | A B C |                        | c -s dx |
 *             Matrix = | D E F |,  R(degrees, px, py) = | s  c dy |
 *                      | G H I |                        | 0  0  1 |
 *
 *         where
 *
 *             c  = cos(degrees)
 *             s  = sin(degrees)
 *             dx =  s * py + (1 - c) * px
 *             dy = -s * px + (1 - c) * py
 *
 *         sets SkMatrix to:
 *
 *                                           | A B C | | c -s dx |   | Ac+Bs -As+Bc A*dx+B*dy+C |
 *             Matrix * R(degrees, px, py) = | D E F | | s  c dy | = | Dc+Es -Ds+Ec D*dx+E*dy+F |
 *                                           | G H I | | 0  0  1 |   | Gc+Hs -Gs+Hc G*dx+H*dy+I |
 *
 *
 */
internal fun DoubleArray.preRotate(
    degrees: Float,
    px: Float,
    py: Float,
) {
    preRotate(degrees.toDouble(), px.toDouble(), py.toDouble())
}

@Suppress("MagicNumber")
internal fun DoubleArray.preRotate(
    degrees: Double,
    px: Double,
    py: Double,
) {
    // Note we do the same chunk of math for the cos/sin in 6 places.
    // It would be nice to have one common function, right?
    // DO NOT DO IT.  This is fast, very fast.  You can't make
    // this cleaner without making it slower (I've spent a lot of time trying).
    val normalizedDegrees = (degrees % 360.0 + 360.0) % 360.0

    val sin: Double
    val cos: Double
    when (degrees) {
        0.0 -> { // Exact 0 degrees
            sin = 0.0
            cos = 1.0
        }

        90.0 -> { // Exact 90 degrees
            sin = 1.0
            cos = 0.0
        }

        180.0 -> {
            sin = 0.0
            cos = -1.0
        }

        270.0 -> {
            sin = -1.0
            cos = 0.0
        }

        else -> {
            val radians = normalizedDegrees * DEGREES_TO_RADIANS
            val sina = sin(radians)
            val cosa = cos(radians)
            sin = if (fastAbs(sina) < ZERO_TOLERANCE) 0.0 else sina
            cos = if (fastAbs(cosa) < ZERO_TOLERANCE) 0.0 else cosa
        }
    }

    val dx = sin * py + (1 - cos) * px
    val dy = -sin * px + (1 - cos) * py

    val a = this[SCALE_X]
    val b = this[SKEW_X]
    val c = this[TRANS_X]
    val d = this[SKEW_Y]
    val e = this[SCALE_Y]
    val f = this[TRANS_Y]
    val g = this[PERSP_0]
    val h = this[PERSP_1]
    val i = this[PERSP_2]

    this[SCALE_X] = a * cos + b * sin
    this[SKEW_X] = -a * sin + b * cos
    this[TRANS_X] = a * dx + b * dy + c
    this[SKEW_Y] = (d * cos + e * sin)
    this[SCALE_Y] = (d * -sin + e * cos)
    this[TRANS_Y] = d * dx + e * dy + f
    this[PERSP_0] = g * cos + h * sin
    this[PERSP_1] = -g * sin + h * cos
    this[PERSP_2] = g * dx + h * dy + i
}

internal fun DoubleArray.preRotate(degrees: Float) {
    preRotate(degrees.toDouble())
}

@Suppress("MagicNumber")
internal fun DoubleArray.preRotate(degrees: Double) {
    // Note we do the same chunk of math for the cos/sin in 6 places.
    // It would be nice to have one common function, right?
    // DO NOT DO IT.  This is fast, very fast.  You can't make
    // this cleaner without making it slower (I've spent a lot of time trying).
    val normalizedDegrees = (degrees % 360.0 + 360.0) % 360.0

    val sin: Double
    val cos: Double
    when (degrees) {
        0.0 -> { // Exact 0 degrees
            sin = 0.0
            cos = 1.0
        }

        90.0 -> { // Exact 90 degrees
            sin = 1.0
            cos = 0.0
        }

        180.0 -> {
            sin = 0.0
            cos = -1.0
        }

        270.0 -> {
            sin = -1.0
            cos = 0.0
        }

        else -> {
            val radians = normalizedDegrees * DEGREES_TO_RADIANS
            val sina = sin(radians)
            val cosa = cos(radians)
            sin = if (fastAbs(sina) < ZERO_TOLERANCE) 0.0 else sina
            cos = if (fastAbs(cosa) < ZERO_TOLERANCE) 0.0 else cosa
        }
    }

    val a = this[SCALE_X]
    val b = this[SKEW_X]
    val c = this[TRANS_X]
    val d = this[SKEW_Y]
    val e = this[SCALE_Y]
    val f = this[TRANS_Y]
    val g = this[PERSP_0]
    val h = this[PERSP_1]
    val i = this[PERSP_2]

    this[SCALE_X] = a * cos + b * sin
    this[SKEW_X] = -a * sin + b * cos
    this[TRANS_X] = c
    this[SKEW_Y] = (d * cos + e * sin)
    this[SCALE_Y] = (d * -sin + e * cos)
    this[TRANS_Y] = f
    this[PERSP_0] = g * cos + h * sin
    this[PERSP_1] = -g * sin + h * cos
    this[PERSP_2] = i
}

/**
 *         Given:
 *
 *                      | A B C |                       |  1 kx dx |
 *             Matrix = | D E F |,  K(kx, ky, px, py) = | ky  1 dy |
 *                      | G H I |                       |  0  0  1 |
 *
 *         where
 *
 *             dx = -kx * py
 *             dy = -ky * px
 *
 *         sets SkMatrix to:
 *
 *                                          | A B C | |  1 kx dx |   | A+B*ky A*kx+B A*dx+B*dy+C |
 *             Matrix * K(kx, ky, px, py) = | D E F | | ky  1 dy | = | D+E*ky D*kx+E D*dx+E*dy+F |
 *                                          | G H I | |  0  0  1 |   | G+H*ky G*kx+H G*dx+H*dy+I |
 */
internal fun DoubleArray.preSkew(
    kx: Float,
    ky: Float,
    px: Float,
    py: Float,
) {
    preSkew(kx.toDouble(), ky.toDouble(), px.toDouble(), py.toDouble())
}

internal fun DoubleArray.preSkew(
    kx: Double,
    ky: Double,
    px: Double,
    py: Double,
) {
    val dx = -kx * py
    val dy = -ky * px

    val a = this[SCALE_X]
    val b = this[SKEW_X]
    val c = this[TRANS_X]
    val d = this[SKEW_Y]
    val e = this[SCALE_Y]
    val f = this[TRANS_Y]
    val g = this[PERSP_0]
    val h = this[PERSP_1]
    val i = this[PERSP_2]

    this[SCALE_X] = a + b * ky
    this[SKEW_X] = a * kx + b
    this[TRANS_X] = a * dx + b * dy + c
    this[SKEW_Y] = d + e * ky
    this[SCALE_Y] = d * kx + e
    this[TRANS_Y] = d * dx + e * dy + f
    this[PERSP_0] = g + h * ky
    this[PERSP_1] = g * kx + h
    this[PERSP_2] = g * dx + h * dy + i
}

internal fun DoubleArray.preSkew(
    kx: Float,
    ky: Float,
) {
    preSkew(kx.toDouble(), ky.toDouble())
}

internal fun DoubleArray.preSkew(
    kx: Double,
    ky: Double,
) {
    val a = this[SCALE_X]
    val b = this[SKEW_X]
    val c = this[TRANS_X]
    val d = this[SKEW_Y]
    val e = this[SCALE_Y]
    val f = this[TRANS_Y]
    val g = this[PERSP_0]
    val h = this[PERSP_1]
    val i = this[PERSP_2]

    this[SCALE_X] = a + b * ky
    this[SKEW_X] = a * kx + b
    this[TRANS_X] = c
    this[SKEW_Y] = d + e * ky
    this[SCALE_Y] = d * kx + e
    this[TRANS_Y] = f
    this[PERSP_0] = g + h * ky
    this[PERSP_1] = g * kx + h
    this[PERSP_2] = i
}

/**
 *         Given:
 *
 *                      | J K L |               | 1 0 dx |
 *             Matrix = | M N O |,  T(dx, dy) = | 0 1 dy |
 *                      | P Q R |               | 0 0  1 |
 *
 *         sets SkMatrix to:
 *
 *                                  | 1 0 dx | | J K L |   | J+dx*P K+dx*Q L+dx*R |
 *             T(dx, dy) * Matrix = | 0 1 dy | | M N O | = | M+dy*P N+dy*Q O+dy*R |
 *                                  | 0 0  1 | | P Q R |   |      P      Q      R |
 *
 */
internal fun DoubleArray.postTranslate(
    dx: Float,
    dy: Float,
) {
    postTranslate(dx.toDouble(), dy.toDouble())
}

internal fun DoubleArray.postTranslate(
    dx: Double,
    dy: Double,
) {
    val j = this[SCALE_X]
    val k = this[SKEW_X]
    val l = this[TRANS_X]
    val m = this[SKEW_Y]
    val n = this[SCALE_Y]
    val o = this[TRANS_Y]
    val p = this[PERSP_0]
    val q = this[PERSP_1]
    val r = this[PERSP_2]

    this[SCALE_X] = j + dx * p
    this[SKEW_X] = k + dx * q
    this[TRANS_X] = l + dx * r
    this[SKEW_Y] = m + dy * p
    this[SCALE_Y] = n + dy * q
    this[TRANS_Y] = o + dy * r
}

/*
        Given:

                     | J K L |                       | sx  0 dx |
            Matrix = | M N O |,  S(sx, sy, px, py) = |  0 sy dy |
                     | P Q R |                       |  0  0  1 |

        where

            dx = px - sx * px
            dy = py - sy * py

        sets SkMatrix to:

                                         | sx  0 dx | | J K L |   | sx*J+dx*P sx*K+dx*Q sx*L+dx+R |
            S(sx, sy, px, py) * Matrix = |  0 sy dy | | M N O | = | sy*M+dy*P sy*N+dy*Q sy*O+dy*R |
                                         |  0  0  1 | | P Q R |   |         P         Q         R |

 */
internal fun DoubleArray.postScale(
    sx: Float,
    sy: Float,
    px: Float,
    py: Float,
) {
    postScale(sx.toDouble(), sy.toDouble(), px.toDouble(), py.toDouble())
}

internal fun DoubleArray.postScale(
    sx: Double,
    sy: Double,
    px: Double,
    py: Double,
) {
    val dx = px - sx * px
    val dy = py - sy * py

    val j = this[SCALE_X]
    val k = this[SKEW_X]
    val l = this[TRANS_X]
    val m = this[SKEW_Y]
    val n = this[SCALE_Y]
    val o = this[TRANS_Y]
    val p = this[PERSP_0]
    val q = this[PERSP_1]
    val r = this[PERSP_2]

    this[SCALE_X] = sx * j + dx * p
    this[SKEW_X] = sx * k + dx * q
    this[TRANS_X] = sx * l + dx * r
    this[SKEW_Y] = sy * m + dy * p
    this[SCALE_Y] = sy * n + dy * q
    this[TRANS_Y] = sy * o + dy * r
}

internal fun DoubleArray.postScale(
    sx: Float,
    sy: Float,
) {
    postScale(sx.toDouble(), sy.toDouble())
}

internal fun DoubleArray.postScale(
    sx: Double,
    sy: Double,
) {
    val j = this[SCALE_X]
    val k = this[SKEW_X]
    val l = this[TRANS_X]
    val m = this[SKEW_Y]
    val n = this[SCALE_Y]
    val o = this[TRANS_Y]

    this[SCALE_X] = sx * j
    this[SKEW_X] = sx * k
    this[TRANS_X] = sx * l
    this[SKEW_Y] = sy * m
    this[SCALE_Y] = sy * n
    this[TRANS_Y] = sy * o
}

/**
 *         Given:
 *
 *                      | J K L |                        | c -s dx |
 *             Matrix = | M N O |,  R(degrees, px, py) = | s  c dy |
 *                      | P Q R |                        | 0  0  1 |
 *
 *         where
 *
 *             c  = cos(degrees)
 *             s  = sin(degrees)
 *             dx =  s * py + (1 - c) * px
 *             dy = -s * px + (1 - c) * py
 *
 *         sets SkMatrix to:
 *
 *                                           |c -s dx| |J K L|   |cJ-sM+dx*P cK-sN+dx*Q cL-sO+dx+R|
 *             R(degrees, px, py) * Matrix = |s  c dy| |M N O| = |sJ+cM+dy*P sK+cN+dy*Q sL+cO+dy*R|
 *                                           |0  0  1| |P Q R|   |         P          Q          R|
 *
 */
internal fun DoubleArray.postRotate(
    degrees: Float,
    px: Float,
    py: Float,
) {
    postRotate(degrees.toDouble(), px.toDouble(), py.toDouble())
}

@Suppress("MagicNumber")
internal fun DoubleArray.postRotate(
    degrees: Double,
    px: Double,
    py: Double,
) {
    // Note we do the same chunk of math for the cos/sin in 6 places.
    // It would be nice to have one common function, right?
    // DO NOT DO IT.  This is fast, very fast.  You can't make
    // this cleaner without making it slower (I've spent a lot of time trying).
    val normalizedDegrees = (degrees % 360.0 + 360.0) % 360.0

    val sin: Double
    val cos: Double
    when (degrees) {
        0.0 -> { // Exact 0 degrees
            sin = 0.0
            cos = 1.0
        }

        90.0 -> { // Exact 90 degrees
            sin = 1.0
            cos = 0.0
        }

        180.0 -> {
            sin = 0.0
            cos = -1.0
        }

        270.0 -> {
            sin = -1.0
            cos = 0.0
        }

        else -> {
            val radians = normalizedDegrees * DEGREES_TO_RADIANS
            val sina = sin(radians)
            val cosa = cos(radians)
            sin = if (fastAbs(sina) < ZERO_TOLERANCE) 0.0 else sina
            cos = if (fastAbs(cosa) < ZERO_TOLERANCE) 0.0 else cosa
        }
    }

    val dx = sin * py + (1 - cos) * px
    val dy = -sin * px + (1 - cos) * py

    val j = this[SCALE_X]
    val k = this[SKEW_X]
    val l = this[TRANS_X]
    val m = this[SKEW_Y]
    val n = this[SCALE_Y]
    val o = this[TRANS_Y]
    val p = this[PERSP_0]
    val q = this[PERSP_1]
    val r = this[PERSP_2]

    this[SCALE_X] = cos * j - sin * m + dx * p
    this[SKEW_X] = cos * k - sin * n + dx * q
    this[TRANS_X] = cos * l - sin * o + dx * r
    this[SKEW_Y] = sin * j + cos * m + dy * p
    this[SCALE_Y] = sin * k + cos * n + dy * q
    this[TRANS_Y] = sin * l + cos * o + dy * r
}

internal fun DoubleArray.postRotate(degrees: Float) {
    postRotate(degrees.toDouble())
}

@Suppress("MagicNumber")
internal fun DoubleArray.postRotate(degrees: Double) {
    // Note we do the same chunk of math for the cos/sin in 6 places.
    // It would be nice to have one common function, right?
    // DO NOT DO IT.  This is fast, very fast.  You can't make
    // this cleaner without making it slower (I've spent a lot of time trying).
    val normalizedDegrees = (degrees % 360.0 + 360.0) % 360.0

    val sin: Double
    val cos: Double
    when (degrees) {
        0.0 -> { // Exact 0 degrees
            sin = 0.0
            cos = 1.0
        }

        90.0 -> { // Exact 90 degrees
            sin = 1.0
            cos = 0.0
        }

        180.0 -> {
            sin = 0.0
            cos = -1.0
        }

        270.0 -> {
            sin = -1.0
            cos = 0.0
        }

        else -> {
            val radians = normalizedDegrees * DEGREES_TO_RADIANS
            val sina = sin(radians)
            val cosa = cos(radians)
            sin = if (fastAbs(sina) < ZERO_TOLERANCE) 0.0 else sina
            cos = if (fastAbs(cosa) < ZERO_TOLERANCE) 0.0 else cosa
        }
    }

    val j = this[SCALE_X]
    val k = this[SKEW_X]
    val l = this[TRANS_X]
    val m = this[SKEW_Y]
    val n = this[SCALE_Y]
    val o = this[TRANS_Y]

    this[SCALE_X] = cos * j - sin * m
    this[SKEW_X] = cos * k - sin * n
    this[TRANS_X] = cos * l - sin * o
    this[SKEW_Y] = sin * j + cos * m
    this[SCALE_Y] = sin * k + cos * n
    this[TRANS_Y] = sin * l + cos * o
}

// private fun shouldTruncate(value: Double): Boolean =

/**
 *
 *         Given:
 *
 *                      | J K L |                       |  1 kx dx |
 *             Matrix = | M N O |,  K(kx, ky, px, py) = | ky  1 dy |
 *                      | P Q R |                       |  0  0  1 |
 *
 *         where
 *
 *             dx = -kx * py
 *             dy = -ky * px
 *
 *         sets SkMatrix to:
 *
 *                                          | 1 kx dx| |J K L|   |J+kx*M+dx*P K+kx*N+dx*Q L+kx*O+dx+R|
 *             K(kx, ky, px, py) * Matrix = |ky  1 dy| |M N O| = |ky*J+M+dy*P ky*K+N+dy*Q ky*L+O+dy*R|
 *                                          | 0  0  1| |P Q R|   |          P           Q           R|
 *
 *
 */
internal fun DoubleArray.postSkew(
    kx: Float,
    ky: Float,
    px: Float,
    py: Float,
) {
    postSkew(kx.toDouble(), ky.toDouble(), px.toDouble(), py.toDouble())
}

internal fun DoubleArray.postSkew(
    kx: Double,
    ky: Double,
    px: Double,
    py: Double,
) {
    val dx = -kx * py
    val dy = -ky * px

    val j = this[SCALE_X]
    val k = this[SKEW_X]
    val l = this[TRANS_X]
    val m = this[SKEW_Y]
    val n = this[SCALE_Y]
    val o = this[TRANS_Y]
    val p = this[PERSP_0]
    val q = this[PERSP_1]
    val r = this[PERSP_2]

    this[SCALE_X] = j + kx * m + dx * p
    this[SKEW_X] = k + kx * n + dx * q
    this[TRANS_X] = l + kx * o + dx * r
    this[SKEW_Y] = ky * j + m + dy * p
    this[SCALE_Y] = ky * k + n + dy * q
    this[TRANS_Y] = ky * l + o + dy * r
}

internal fun DoubleArray.postSkew(
    kx: Float,
    ky: Float,
) {
    postSkew(kx.toDouble(), ky.toDouble())
}

internal fun DoubleArray.postSkew(
    kx: Double,
    ky: Double,
) {
    val j = this[SCALE_X]
    val k = this[SKEW_X]
    val l = this[TRANS_X]
    val m = this[SKEW_Y]
    val n = this[SCALE_Y]
    val o = this[TRANS_Y]

    this[SCALE_X] = j + kx * m
    this[SKEW_X] = k + kx * n
    this[TRANS_X] = l + kx * o
    this[SKEW_Y] = ky * j + m
    this[SCALE_Y] = ky * k + n
    this[TRANS_Y] = ky * l + o
}

@Suppress("MagicNumber", "UnnecessaryVariable")
internal fun DoubleArray.preConcat(other: DoubleArray) {
    val a = this
    val b = other
    // note that a = this, so the temp variables are important.
    // without them, we would overwrite the original matrix
    // before the values are calculated
    val a0 = a[SCALE_X]
    val a1 = a[SKEW_X]
    val a2 = a[TRANS_X]
    val a3 = a[SKEW_Y]
    val a4 = a[SCALE_Y]
    val a5 = a[TRANS_Y]
    val a6 = a[PERSP_0]
    val a7 = a[PERSP_1]
    val a8 = a[PERSP_2]

    val b0 = b[SCALE_X]
    val b1 = b[SKEW_X]
    val b2 = b[TRANS_X]
    val b3 = b[SKEW_Y]
    val b4 = b[SCALE_Y]
    val b5 = b[TRANS_Y]
    val b6 = b[PERSP_0]
    val b7 = b[PERSP_1]
    val b8 = b[PERSP_2]

    val v0 = a0 * b0 + a1 * b3 + a2 * b6
    val v1 = a0 * b1 + a1 * b4 + a2 * b7
    val v2 = a0 * b2 + a1 * b5 + a2 * b8
    val v3 = a3 * b0 + a4 * b3 + a5 * b6
    val v4 = a3 * b1 + a4 * b4 + a5 * b7
    val v5 = a3 * b2 + a4 * b5 + a5 * b8
    val v6 = a6 * b0 + a7 * b3 + a8 * b6
    val v7 = a6 * b1 + a7 * b4 + a8 * b7
    val v8 = a6 * b2 + a7 * b5 + a8 * b8
    this[SCALE_X] = v0
    this[SKEW_X] = v1
    this[TRANS_X] = v2
    this[SKEW_Y] = v3
    this[SCALE_Y] = v4
    this[TRANS_Y] = v5
    this[PERSP_0] = v6
    this[PERSP_1] = v7
    this[PERSP_2] = v8
}

@Suppress("MagicNumber", "UnnecessaryVariable")
internal fun DoubleArray.postConcat(other: DoubleArray) {
    val a = other
    val b = this
    // note that b = this, so the temp variables are important.
    // without them we would overwrite the original matrix
    // before the values are calculated
    val a0 = a[SCALE_X]
    val a1 = a[SKEW_X]
    val a2 = a[TRANS_X]
    val a3 = a[SKEW_Y]
    val a4 = a[SCALE_Y]
    val a5 = a[TRANS_Y]
    val a6 = a[PERSP_0]
    val a7 = a[PERSP_1]
    val a8 = a[PERSP_2]

    val b0 = b[SCALE_X]
    val b1 = b[SKEW_X]
    val b2 = b[TRANS_X]
    val b3 = b[SKEW_Y]
    val b4 = b[SCALE_Y]
    val b5 = b[TRANS_Y]
    val b6 = b[PERSP_0]
    val b7 = b[PERSP_1]
    val b8 = b[PERSP_2]

    val v0 = a0 * b0 + a1 * b3 + a2 * b6
    val v1 = a0 * b1 + a1 * b4 + a2 * b7
    val v2 = a0 * b2 + a1 * b5 + a2 * b8
    val v3 = a3 * b0 + a4 * b3 + a5 * b6
    val v4 = a3 * b1 + a4 * b4 + a5 * b7
    val v5 = a3 * b2 + a4 * b5 + a5 * b8
    val v6 = a6 * b0 + a7 * b3 + a8 * b6
    val v7 = a6 * b1 + a7 * b4 + a8 * b7
    val v8 = a6 * b2 + a7 * b5 + a8 * b8
    this[SCALE_X] = v0
    this[SKEW_X] = v1
    this[TRANS_X] = v2
    this[SKEW_Y] = v3
    this[SCALE_Y] = v4
    this[TRANS_Y] = v5
    this[PERSP_0] = v6
    this[PERSP_1] = v7
    this[PERSP_2] = v8
}

@Suppress("MagicNumber")
internal fun DoubleArray.invert(): DoubleArray? {
    val v = this
    val v0 = v[SCALE_X]
    val v1 = v[SKEW_X]
    val v2 = v[TRANS_X]
    val v3 = v[SKEW_Y]
    val v4 = v[SCALE_Y]
    val v5 = v[TRANS_Y]
    val v6 = v[PERSP_0]
    val v7 = v[PERSP_1]
    val v8 = v[PERSP_2]

    val det =
        v0 * (v4 * v8 - v5 * v7) -
            v1 * (v3 * v8 - v5 * v6) +
            v2 * (v3 * v7 - v4 * v6)
    if (fastAbs(det) < 1e-10) return null
    val invDet = 1.0 / det
    val res = DoubleArray(THREE_BY_THREE)
    res[SCALE_X] = ((v4 * v8 - v5 * v7) * invDet)
    res[SKEW_X] = ((v2 * v7 - v1 * v8) * invDet)
    res[TRANS_X] = ((v1 * v5 - v2 * v4) * invDet)
    res[SKEW_Y] = ((v5 * v6 - v3 * v8) * invDet)
    res[SCALE_Y] = ((v0 * v8 - v2 * v6) * invDet)
    res[TRANS_Y] = ((v2 * v3 - v0 * v5) * invDet)
    res[PERSP_0] = ((v3 * v7 - v4 * v6) * invDet)
    res[PERSP_1] = ((v1 * v6 - v0 * v7) * invDet)
    res[PERSP_2] = ((v0 * v4 - v1 * v3) * invDet)
    return res
}

internal fun DoubleArray.mapX(
    x: Float,
    y: Float,
): Float {
    val w = this[PERSP_0] * x + this[PERSP_1] * y + this[PERSP_2]
    return ((this[SCALE_X] * x + this[SKEW_X] * y + this[TRANS_X]) / w).toFloat()
}

internal fun DoubleArray.mapY(
    x: Float,
    y: Float,
): Float {
    val w = this[PERSP_0] * x + this[PERSP_1] * y + this[PERSP_2]
    return ((this[SKEW_Y] * x + this[SCALE_Y] * y + this[TRANS_Y]) / w).toFloat()
}

internal fun DoubleArray.mapRect(rect: FloatRectValues): PdfRectF {
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

internal fun DoubleArray.mapRect(
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

@Suppress("MagicNumber")
internal fun DoubleArray.mapRadius(radius: Float): Float {
    val tmp = FloatArray(4)
    tmp[0] = radius
    tmp[1] = 0.0f
    tmp[2] = 0.0f
    tmp[3] = radius
    mapVectors(tmp)

    val d1 = distance(0.0f, 0.0f, tmp[0], tmp[1])
    val d2 = distance(0.0f, 0.0f, tmp[2], tmp[3])
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

internal fun DoubleArray.mapPoints(pts: FloatArray) {
    mapPoints(pts, 0, pts, 0, pts.size / 2)
}

internal fun DoubleArray.mapPoints(
    dst: FloatArray,
    src: FloatArray,
) {
    mapPoints(dst, 0, src, 0, src.size / 2)
}

internal fun DoubleArray.mapPoints(
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
        val w = this[PERSP_0] * x + this[PERSP_1] * y + this[PERSP_2]
        dst[di] = ((this[SCALE_X] * x + this[SKEW_X] * y + this[TRANS_X]) / w).toFloat()
        dst[di + 1] = ((this[SKEW_Y] * x + this[SCALE_Y] * y + this[TRANS_Y]) / w).toFloat()
    }
}

internal fun DoubleArray.mapVectors(vecs: FloatArray) {
    mapVectors(vecs, 0, vecs, 0, vecs.size / 2)
}

internal fun DoubleArray.mapVectors(
    dest: FloatArray,
    src: FloatArray,
) {
    mapVectors(dest, 0, src, 0, src.size / 2)
}

internal fun DoubleArray.mapVectors(
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
        dst[di] = (this[SCALE_X] * x + this[SKEW_X] * y).toFloat()
        dst[di + 1] = (this[SKEW_Y] * x + this[SCALE_Y] * y).toFloat()
    }
}
