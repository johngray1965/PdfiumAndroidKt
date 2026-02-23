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
const val ZERO_TOLERANCE = 1.0 / (1 shl 16)

const val DEGREES_TO_RADIANS = (PI / 180.0)

interface MatrixValues {
    val values: DoubleArray
}

/**
 * Immutable transformation matrix.
 * Methods return new instances or mapped values instead of modifying the current ones.
 */
@Keep
@Suppress("TooManyFunctions")
data class PdfMatrix(
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
    ) = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setTranslate(dx, dy) })

    fun setScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setScale(sx, sy, px, py) })

    fun setRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setRotate(degrees, px, py) })

    fun setSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) = PdfMatrix(DoubleArray(THREE_BY_THREE).apply { setSkew(kx, ky, px, py) })

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

internal fun DoubleArray.reset() {
    fill(0.0)
    this[MSCALE_X] = 1.0
    this[MSCALE_Y] = 1.0
    this[MPERSP_2] = 1.0
}

internal fun DoubleArray.isIdentity(): Boolean =
    this[MSCALE_X] == 1.0 && this[MSKEW_X] == 0.0 && this[MTRANS_X] == 0.0 &&
        this[MSKEW_Y] == 0.0 && this[MSCALE_Y] == 1.0 && this[MTRANS_Y] == 0.0 &&
        this[MPERSP_0] == 0.0 && this[MPERSP_1] == 0.0 && this[MPERSP_2] == 1.0

internal fun DoubleArray.isAffine(): Boolean = this[MPERSP_0] == 0.0 && this[MPERSP_1] == 0.0 && this[MPERSP_2] == 1.0

internal fun DoubleArray.setTranslate(
    dx: Float,
    dy: Float,
) {
    reset()
    this[MTRANS_X] = dx.toDouble()
    this[MTRANS_Y] = dy.toDouble()
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

    this[MSCALE_X] = sxd
    this[MSCALE_Y] = syd
    this[MTRANS_X] = (pxd - sxd * pxd)
    this[MTRANS_Y] = (pyd - syd * pyd)
    this[MSKEW_X] = 0.0
    this[MSKEW_Y] = 0.0
    this[MPERSP_0] = 0.0
    this[MPERSP_1] = 0.0
    this[MPERSP_2] = 1.0
}

@Suppress("MagicNumber")
internal fun DoubleArray.setRotate(
    degrees: Float,
    px: Float,
    py: Float,
) {
    val pxd = px.toDouble()
    val pyd = py.toDouble()

    val radians = degrees * DEGREES_TO_RADIANS
    val sina = sin(radians)
    val cosa = cos(radians)
    val s = if (shouldTruncate(sina)) 0.0 else sina
    val c = if (shouldTruncate(cosa)) 0.0 else cosa

    this[MSCALE_X] = c
    this[MSKEW_X] = -s
    this[MSKEW_Y] = s
    this[MSCALE_Y] = c
    this[MTRANS_X] = (pxd - c * pxd + s * pyd)
    this[MTRANS_Y] = (pyd - s * pxd - c * pyd)
    this[MPERSP_0] = 0.0
    this[MPERSP_1] = 0.0
    this[MPERSP_2] = 1.0
}

internal fun DoubleArray.setSkew(
    kx: Float,
    ky: Float,
    px: Float,
    py: Float,
) {
    val kxd = kx.toDouble()
    val kyd = ky.toDouble()
    val pxd = px.toDouble()
    val pyd = py.toDouble()

    this[MSCALE_X] = 1.0
    this[MSKEW_X] = kxd
    this[MSKEW_Y] = kyd
    this[MSCALE_Y] = 1.0
    this[MTRANS_X] = -(kxd * pyd)
    this[MTRANS_Y] = -(kyd * pxd)
    this[MPERSP_0] = 0.0
    this[MPERSP_1] = 0.0
    this[MPERSP_2] = 1.0
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
    val dxd = dx.toDouble()
    val dyd = dy.toDouble()
    val a = this[MSCALE_X]
    val b = this[MSKEW_X]
    val c = this[MTRANS_X]
    val d = this[MSKEW_Y]
    val e = this[MSCALE_Y]
    val f = this[MTRANS_Y]
    val g = this[MPERSP_0]
    val h = this[MPERSP_1]
    val i = this[MPERSP_2]

    this[MTRANS_X] = a * dxd + b * dyd + c
    this[MTRANS_Y] = d * dxd + e * dyd + f
    this[MPERSP_2] = g * dxd + h * dyd + i
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
    val sxd = sx.toDouble()
    val syd = sy.toDouble()
    val pxd = px.toDouble()
    val pyd = py.toDouble()

    val dx = pxd - sxd * pxd
    val dy = pyd - syd * pyd

    val a = this[MSCALE_X]
    val b = this[MSKEW_X]
    val c = this[MTRANS_X]
    val d = this[MSKEW_Y]
    val e = this[MSCALE_Y]
    val f = this[MTRANS_Y]
    val g = this[MPERSP_0]
    val h = this[MPERSP_1]
    val i = this[MPERSP_2]

    this[MSCALE_X] = a * sxd
    this[MSKEW_X] = b * syd
    this[MTRANS_X] = a * dx + b * dy + c
    this[MSKEW_Y] = d * sx
    this[MSCALE_Y] = e * sy
    this[MTRANS_Y] = d * dx + e * dy + f
    this[MPERSP_0] = g * sxd
    this[MPERSP_1] = h * syd
    this[MPERSP_2] = g * dx + h * dy + i
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
    val pxd = px.toDouble()
    val pyd = py.toDouble()
    val radians = degrees * DEGREES_TO_RADIANS
    val sina = sin(radians)
    val cosa = cos(radians)
    val sin = if (shouldTruncate(sina)) 0.0 else sina
    val cos = if (shouldTruncate(cosa)) 0.0 else cosa
    val dx = sin * pyd + (1 - cos) * pxd
    val dy = -sin * pxd + (1 - cos) * pyd

    val a = this[MSCALE_X]
    val b = this[MSKEW_X]
    val c = this[MTRANS_X]
    val d = this[MSKEW_Y]
    val e = this[MSCALE_Y]
    val f = this[MTRANS_Y]
    val g = this[MPERSP_0]
    val h = this[MPERSP_1]
    val i = this[MPERSP_2]

    this[MSCALE_X] = a * cos + b * sin
    this[MSKEW_X] = -a * sin + b * cos
    this[MTRANS_X] = a * dx + b * dy + c
    this[MSKEW_Y] = (d * cos + e * sin)
    this[MSCALE_Y] = (d * -sin + e * cos)
    this[MTRANS_Y] = d * dx + e * dy + f
    this[MPERSP_0] = g * cos + h * sin
    this[MPERSP_1] = -g * sin + h * cos
    this[MPERSP_2] = g * dx + h * dy + i
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
    val kxd = kx.toDouble()
    val kyd = ky.toDouble()
    val pxd = px.toDouble()
    val pyd = py.toDouble()

    val dx = -kxd * pyd
    val dy = -kyd * pxd

    val a = this[MSCALE_X]
    val b = this[MSKEW_X]
    val c = this[MTRANS_X]
    val d = this[MSKEW_Y]
    val e = this[MSCALE_Y]
    val f = this[MTRANS_Y]
    val g = this[MPERSP_0]
    val h = this[MPERSP_1]
    val i = this[MPERSP_2]

//    println("kxd: $kxd")
//    println("d: $d")
//    println("e: $e")

    this[MSCALE_X] = a + b * kyd
    this[MSKEW_X] = a * kxd + b
    this[MTRANS_X] = a * dx + b * dy + c
    this[MSKEW_Y] = d + e * kyd
    this[MSCALE_Y] = d * kxd + e
    this[MTRANS_Y] = d * dx + e * dy + f
    this[MPERSP_0] = g + h * kyd
    this[MPERSP_1] = g * kxd + h
    this[MPERSP_2] = g * dx + h * dy + i
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
    val dxd = dx.toDouble()
    val dyd = dy.toDouble()

    val j = this[MSCALE_X]
    val k = this[MSKEW_X]
    val l = this[MTRANS_X]
    val m = this[MSKEW_Y]
    val n = this[MSCALE_Y]
    val o = this[MTRANS_Y]
    val p = this[MPERSP_0]
    val q = this[MPERSP_1]
    val r = this[MPERSP_2]

//    println("j: $j")
//    println("k: $k")
//    println("l: $l")
//    println("m: $m")
//    println("n: $n")
//    println("o: $o")
//    println("p: $p")
//    println("q: $q")
//    println("r: $r")
//
//    println("dxd: $dxd")
//    println("dyd: $dyd")

    this[MSCALE_X] = j + dxd * p
    this[MSKEW_X] = k + dxd * q
    this[MTRANS_X] = l + dxd * r
    this[MSKEW_Y] = m + dyd * p
    this[MSCALE_Y] = n + dyd * q
    this[MTRANS_Y] = o + dyd * r
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
    val sxd = sx.toDouble()
    val syd = sy.toDouble()
    val pxd = px.toDouble()
    val pyd = py.toDouble()

    val dx = pxd - sxd * pxd
    val dy = pyd - syd * pyd

//    println("sxd: $sxd")
//    println("syd: $syd")
//    println("pxd: $pxd")
//    println("pyd: $pyd")
//    println("dx: $dx")
//    println("dy: $dy")

    val j = this[MSCALE_X]
    val k = this[MSKEW_X]
    val l = this[MTRANS_X]
    val m = this[MSKEW_Y]
    val n = this[MSCALE_Y]
    val o = this[MTRANS_Y]
    val p = this[MPERSP_0]
    val q = this[MPERSP_1]
    val r = this[MPERSP_2]

    this[MSCALE_X] = sxd * j + dx * p
    this[MSKEW_X] = sxd * k + dx * q
    this[MTRANS_X] = sxd * l + dx * r
    this[MSKEW_Y] = syd * m + dy * p
    this[MSCALE_Y] = syd * n + dy * q
    this[MTRANS_Y] = syd * o + dy * r
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
    val pxd = px.toDouble()
    val pyd = py.toDouble()

    val radians = degrees * DEGREES_TO_RADIANS
    val sina = sin(radians)
    val cosa = cos(radians)
    val sin = if (shouldTruncate(sina)) 0.0 else sina
    val cos = if (shouldTruncate(cosa)) 0.0 else cosa

    val dx = sin * pyd + (1 - cos) * pxd
    val dy = -sin * pxd + (1 - cos) * pyd

    val j = this[MSCALE_X]
    val k = this[MSKEW_X]
    val l = this[MTRANS_X]
    val m = this[MSKEW_Y]
    val n = this[MSCALE_Y]
    val o = this[MTRANS_Y]
    val p = this[MPERSP_0]
    val q = this[MPERSP_1]
    val r = this[MPERSP_2]

//    println("j: $j")
//    println("k: $k")
//    println("l: $l")
//    println("m: $m")
//    println("n: $n")
//    println("o: $o")
//    println("p: $p")
//    println("q: $q")
//    println("r: $r")
//
//    println("dx: $dx")
//    println("dy: $dy")
//
//    println("cos: $cos")
//    println("sin: $sin")

    this[MSCALE_X] = cos * j - sin * m + dx * p
    this[MSKEW_X] = cos * k - sin * n + dx * q
    this[MTRANS_X] = cos * l - sin * o + dx * r
    this[MSKEW_Y] = sin * j + cos * m + dy * p
    this[MSCALE_Y] = sin * k + cos * n + dy * q
    this[MTRANS_Y] = sin * l + cos * o + dy * r
}

private fun shouldTruncate(value: Double): Boolean = abs(value) < ZERO_TOLERANCE

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
    val kxd = kx.toDouble()
    val kyd = ky.toDouble()
    val pxd = px.toDouble()
    val pyd = py.toDouble()

    val dx = -kxd * pyd
    val dy = -kyd * pxd

    val j = this[MSCALE_X]
    val k = this[MSKEW_X]
    val l = this[MTRANS_X]
    val m = this[MSKEW_Y]
    val n = this[MSCALE_Y]
    val o = this[MTRANS_Y]
    val p = this[MPERSP_0]
    val q = this[MPERSP_1]
    val r = this[MPERSP_2]

    this[MSCALE_X] = j + kxd * m + dx * p
    this[MSKEW_X] = k + kxd * n + dx * q
    this[MTRANS_X] = l + kxd * o + dx * r
    this[MSKEW_Y] = kyd * j + m + dy * p
    this[MSCALE_Y] = kyd * k + n + dy * q
    this[MTRANS_Y] = kyd * l + o + dy * r
}

@Suppress("MagicNumber", "UnnecessaryVariable")
internal fun DoubleArray.preConcat(other: DoubleArray) {
    val a = this
    val b = other
    // note that a = this, so the temp variables are important.
    // without them we would overwrite the original matrix
    // before the values are calculated
    val a0 = a[0]
    val a1 = a[1]
    val a2 = a[2]
    val a3 = a[3]
    val a4 = a[4]
    val a5 = a[5]
    val a6 = a[6]
    val a7 = a[7]
    val a8 = a[8]

    val b0 = b[0]
    val b1 = b[1]
    val b2 = b[2]
    val b3 = b[3]
    val b4 = b[4]
    val b5 = b[5]
    val b6 = b[6]
    val b7 = b[7]
    val b8 = b[8]

    val v0 = a0 * b0 + a1 * b3 + a2 * b6
    val v1 = a0 * b1 + a1 * b4 + a2 * b7
    val v2 = a0 * b2 + a1 * b5 + a2 * b8
    val v3 = a3 * b0 + a4 * b3 + a5 * b6
    val v4 = a3 * b1 + a4 * b4 + a5 * b7
    val v5 = a3 * b2 + a4 * b5 + a5 * b8
    val v6 = a6 * b0 + a7 * b3 + a8 * b6
    val v7 = a6 * b1 + a7 * b4 + a8 * b7
    val v8 = a6 * b2 + a7 * b5 + a8 * b8
    this[0] = v0
    this[1] = v1
    this[2] = v2
    this[3] = v3
    this[4] = v4
    this[5] = v5
    this[6] = v6
    this[7] = v7
    this[8] = v8
}

@Suppress("MagicNumber")
internal fun DoubleArray.postConcat(other: DoubleArray) {
    val a = other
    val b = this
    // note that b = this, so the temp variables are important.
    // without them we would overwrite the original matrix
    // before the values are calculated
    val a0 = a[0]
    val a1 = a[1]
    val a2 = a[2]
    val a3 = a[3]
    val a4 = a[4]
    val a5 = a[5]
    val a6 = a[6]
    val a7 = a[7]
    val a8 = a[8]

    val b0 = b[0]
    val b1 = b[1]
    val b2 = b[2]
    val b3 = b[3]
    val b4 = b[4]
    val b5 = b[5]
    val b6 = b[6]
    val b7 = b[7]
    val b8 = b[8]

    val v0 = a0 * b0 + a1 * b3 + a2 * b6
    val v1 = a0 * b1 + a1 * b4 + a2 * b7
    val v2 = a0 * b2 + a1 * b5 + a2 * b8
    val v3 = a3 * b0 + a4 * b3 + a5 * b6
    val v4 = a3 * b1 + a4 * b4 + a5 * b7
    val v5 = a3 * b2 + a4 * b5 + a5 * b8
    val v6 = a6 * b0 + a7 * b3 + a8 * b6
    val v7 = a6 * b1 + a7 * b4 + a8 * b7
    val v8 = a6 * b2 + a7 * b5 + a8 * b8
    this[0] = v0
    this[1] = v1
    this[2] = v2
    this[3] = v3
    this[4] = v4
    this[5] = v5
    this[6] = v6
    this[7] = v7
    this[8] = v8
}

@Suppress("MagicNumber")
internal fun DoubleArray.invert(): DoubleArray? {
    val v = this
    val v0 = v[0]
    val v1 = v[1]
    val v2 = v[2]
    val v3 = v[3]
    val v4 = v[4]
    val v5 = v[5]
    val v6 = v[6]
    val v7 = v[7]
    val v8 = v[8]

    val det =
        v0 * (v4 * v8 - v5 * v7) -
            v1 * (v3 * v8 - v5 * v6) +
            v2 * (v3 * v7 - v4 * v6)
    if (abs(det) < 1e-10) return null
    val invDet = 1.0 / det
    val res = DoubleArray(THREE_BY_THREE)
    res[0] = ((v4 * v8 - v5 * v7) * invDet)
    res[1] = ((v2 * v7 - v1 * v8) * invDet)
    res[2] = ((v1 * v5 - v2 * v4) * invDet)
    res[3] = ((v5 * v6 - v3 * v8) * invDet)
    res[4] = ((v0 * v8 - v2 * v6) * invDet)
    res[5] = ((v2 * v3 - v0 * v5) * invDet)
    res[6] = ((v3 * v7 - v4 * v6) * invDet)
    res[7] = ((v1 * v6 - v0 * v7) * invDet)
    res[8] = ((v0 * v4 - v1 * v3) * invDet)
    return res
}

internal fun DoubleArray.mapX(
    x: Float,
    y: Float,
): Float {
    val w = this[MPERSP_0] * x + this[MPERSP_1] * y + this[MPERSP_2]
    return ((this[MSCALE_X] * x + this[MSKEW_X] * y + this[MTRANS_X]) / w).toFloat()
}

internal fun DoubleArray.mapY(
    x: Float,
    y: Float,
): Float {
    val w = this[MPERSP_0] * x + this[MPERSP_1] * y + this[MPERSP_2]
    return ((this[MSKEW_Y] * x + this[MSCALE_Y] * y + this[MTRANS_Y]) / w).toFloat()
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
        val w = this[MPERSP_0] * x + this[MPERSP_1] * y + this[MPERSP_2]
        dst[di] = ((this[MSCALE_X] * x + this[MSKEW_X] * y + this[MTRANS_X]) / w).toFloat()
        dst[di + 1] = ((this[MSKEW_Y] * x + this[MSCALE_Y] * y + this[MTRANS_Y]) / w).toFloat()
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
        dst[di] = (this[MSCALE_X] * x + this[MSKEW_X] * y).toFloat()
        dst[di + 1] = (this[MSKEW_Y] * x + this[MSCALE_Y] * y).toFloat()
    }
}

fun getDeterminant3x3(m: DoubleArray): Double {
    // Standard Column-Major indices:
    // [0 3 6]
    // [1 4 7]
    // [2 5 8]
    return m[0] * (m[4] * m[8] - m[5] * m[7]) -
        m[3] * (m[1] * m[8] - m[2] * m[7]) +
        m[6] * (m[1] * m[5] - m[2] * m[4])
}

fun normalize3x3Rotation(m: DoubleArray) {
    // 1. Get the length of the first column (X-axis)
    val len1 = sqrt((m[0] * m[0] + m[1] * m[1]))
    if (len1 > 0) {
        m[0] /= len1
        m[1] /= len1
    }

    // 2. Make the second column (Y-axis) perpendicular to the first
    // In 2D, if X is (a, b), the perpendicular Y is (-b, a)
    m[3] = -m[1]
    m[4] = m[0]

    // 3. Clean the bottom row (Standard for affine transforms)
    m[2] = 0.0
    m[5] = 0.0
    m[8] = 1.0
}

private val threadLocalFloatArray = ThreadLocal.withInitial { DoubleArray(THREE_BY_THREE) }

private inline fun <T> tempFloatArray(block: (DoubleArray) -> T): T {
    val tmp = threadLocalFloatArray.get() ?: DoubleArray(THREE_BY_THREE)
    return block(tmp)
}
