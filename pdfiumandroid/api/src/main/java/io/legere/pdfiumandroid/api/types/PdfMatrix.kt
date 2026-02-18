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
import kotlin.math.cos
import kotlin.math.sin

const val THREE_BY_THREE = 9
const val MPERSP_0 = 6
const val MPERSP_1 = 7
const val MPERSP_2 = 8
const val MSCALE_X = 0
const val MSCALE_Y = 4
const val MSKEW_X = 1
const val MSKEW_Y = 3
const val MTRANS_X = 2
const val MTRANS_Y = 5

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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PdfMatrix

        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()

    fun reset() {
        for (i in values.indices) values[i] = 0f
        values[MSCALE_X] = 1f
        values[MSCALE_Y] = 1f
        values[MPERSP_2] = 1f
    }

    fun set(src: PdfMatrix) {
        System.arraycopy(src.values, 0, values, 0, THREE_BY_THREE)
    }

    fun setTranslate(
        dx: Float,
        dy: Float,
    ) {
        reset()
        values[MTRANS_X] = dx
        values[MTRANS_Y] = dy
    }

    fun setScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        reset()
        values[MSCALE_X] = sx
        values[MSCALE_Y] = sy
        values[MTRANS_X] = px - sx * px
        values[MTRANS_Y] = py - sy * py
    }

    fun setRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        reset()
        val radians = Math.toRadians(degrees.toDouble())
        val sin = sin(radians).toFloat()
        val cos = cos(radians).toFloat()
        setSinCos(sin, cos, px, py)
    }

    fun setSinCos(
        sin: Float,
        cos: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        reset()
        values[MSCALE_X] = cos
        values[MSKEW_X] = -sin
        values[MSKEW_Y] = sin
        values[MSCALE_Y] = cos
        values[MTRANS_X] = px - cos * px + sin * py
        values[MTRANS_Y] = py - sin * px - cos * py
    }

    fun setSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        reset()
        values[MSCALE_X] = 1f
        values[MSKEW_X] = kx
        values[MSKEW_Y] = ky
        values[MSCALE_Y] = 1f
        values[MTRANS_X] = -kx * py
        values[MTRANS_Y] = -ky * px
        // Adjust for pivot (Android Matrix setSkew(kx, ky, px, py) implementation behavior)
        // Android docs: "Set the matrix to skew by sx and sy, with a pivot point at (px, py)"
        // M = T(px, py) * K(kx, ky) * T(-px, -py)
        // K = | 1 kx 0 |
        //     | ky 1 0 |
        //     | 0 0 1 |
        // T(-px, -py) -> x-px, y-py
        // Apply K: x' = (x-px) + kx*(y-py), y' = ky*(x-px) + (y-py)
        // Apply T(px, py): x'' = x' + px, y'' = y' + py
        // x'' = x - px + kx*y - kx*py + px = x + kx*y - kx*py
        // y'' = ky*x - ky*px + y - py + py = ky*x + y - ky*px
        // Matrix:
        // | 1  kx  -kx*py |
        // | ky 1   -ky*px |
        // | 0  0   1      |

        // My manual calc matches above.
        values[MTRANS_X] = -kx * py
        values[MTRANS_Y] = -ky * px
    }

    fun setConcat(
        a: PdfMatrix,
        b: PdfMatrix,
    ) {
        // C = A * B
        val vA = a.values
        val vB = b.values

        // Row 0
        val scaleX = vA[MSCALE_X] * vB[MSCALE_X] + vA[MSKEW_X] * vB[MSKEW_Y] + vA[MTRANS_X] * vB[MPERSP_0]
        val skewX = vA[MSCALE_X] * vB[MSKEW_X] + vA[MSKEW_X] * vB[MSCALE_Y] + vA[MTRANS_X] * vB[MPERSP_1]
        val transX = vA[MSCALE_X] * vB[MTRANS_X] + vA[MSKEW_X] * vB[MTRANS_Y] + vA[MTRANS_X] * vB[MPERSP_2]

        // Row 1
        val skewY = vA[MSKEW_Y] * vB[MSCALE_X] + vA[MSCALE_Y] * vB[MSKEW_Y] + vA[MTRANS_Y] * vB[MPERSP_0]
        val scaleY = vA[MSKEW_Y] * vB[MSKEW_X] + vA[MSCALE_Y] * vB[MSCALE_Y] + vA[MTRANS_Y] * vB[MPERSP_1]
        val transY = vA[MSKEW_Y] * vB[MTRANS_X] + vA[MSCALE_Y] * vB[MTRANS_Y] + vA[MTRANS_Y] * vB[MPERSP_2]

        // Row 2
        val persp0 = vA[MPERSP_0] * vB[MSCALE_X] + vA[MPERSP_1] * vB[MSKEW_Y] + vA[MPERSP_2] * vB[MPERSP_0]
        val persp1 = vA[MPERSP_0] * vB[MSKEW_X] + vA[MPERSP_1] * vB[MSCALE_Y] + vA[MPERSP_2] * vB[MPERSP_1]
        val persp2 = vA[MPERSP_0] * vB[MTRANS_X] + vA[MPERSP_1] * vB[MTRANS_Y] + vA[MPERSP_2] * vB[MPERSP_2]

        values[MSCALE_X] = scaleX
        values[MSKEW_X] = skewX
        values[MTRANS_X] = transX
        values[MSKEW_Y] = skewY
        values[MSCALE_Y] = scaleY
        values[MTRANS_Y] = transY
        values[MPERSP_0] = persp0
        values[MPERSP_1] = persp1
        values[MPERSP_2] = persp2
    }

    fun preTranslate(
        dx: Float,
        dy: Float,
    ) {
        // M' = M * T
        // T has 1, 0, dx; 0, 1, dy; 0, 0, 1 in columns? No rows.
        // T = | 1 0 dx |
        //     | 0 1 dy |
        //     | 0 0 1  |

        // M * T
        // | a b c |   | 1 0 dx |   | a b a*dx+b*dy+c |
        // | d e f | * | 0 1 dy | = | d e d*dx+e*dy+f |
        // | g h i |   | 0 0 1  |   | g h g*dx+h*dy+i |

        // Only last column changes.
        values[MTRANS_X] += values[MSCALE_X] * dx + values[MSKEW_X] * dy
        values[MTRANS_Y] += values[MSKEW_Y] * dx + values[MSCALE_Y] * dy
        values[MPERSP_2] += values[MPERSP_0] * dx + values[MPERSP_1] * dy
    }

    fun preScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        // M' = M * S(sx, sy, px, py)
        val temp = PdfMatrix()
        temp.setScale(sx, sy, px, py)
        preConcat(temp)
    }

    fun preRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        val temp = PdfMatrix()
        temp.setRotate(degrees, px, py)
        preConcat(temp)
    }

    fun preSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        val temp = PdfMatrix()
        temp.setSkew(kx, ky, px, py)
        preConcat(temp)
    }

    fun preConcat(other: PdfMatrix) {
        // M' = M * other
        val temp = PdfMatrix()
        temp.setConcat(this, other)
        set(temp)
    }

    fun postTranslate(
        dx: Float,
        dy: Float,
    ) {
        // M' = T * M
        // T = | 1 0 dx |
        //     | 0 1 dy |
        //     | 0 0 1  |

        // | 1 0 dx |   | a b c |   | a+dx*g b+dx*h c+dx*i |
        // | 0 1 dy | * | d e f | = | d+dy*g e+dy*h f+dy*i |
        // | 0 0 1  |   | g h i |   | g      h      i      |

        // If persp0 (g) and persp1 (h) are 0, and persp2 (i) is 1 (Affine)
        // Row 0: a, b, c+dx
        // Row 1: d, e, f+dy

        // But we should support perspective.
        val g = values[MPERSP_0]
        val h = values[MPERSP_1]
        val i = values[MPERSP_2]

        values[MSCALE_X] += dx * g
        values[MSKEW_X] += dx * h
        values[MTRANS_X] += dx * i

        values[MSKEW_Y] += dy * g
        values[MSCALE_Y] += dy * h
        values[MTRANS_Y] += dy * i
    }

    fun postScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        val temp = PdfMatrix()
        temp.setScale(sx, sy, px, py)
        postConcat(temp)
    }

    fun postRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        val temp = PdfMatrix()
        temp.setRotate(degrees, px, py)
        postConcat(temp)
    }

    fun postSkew(
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        val temp = PdfMatrix()
        temp.setSkew(kx, ky, px, py)
        postConcat(temp)
    }

    fun postConcat(other: PdfMatrix) {
        // M' = other * M
        val temp = PdfMatrix()
        temp.setConcat(other, this)
        set(temp)
    }

    fun isAffine(): Boolean = values[MPERSP_0] == 0f && values[MPERSP_1] == 0f && values[MPERSP_2] == 1f

    fun isIdentity(): Boolean =
        values[MSCALE_X] == 1f && values[MSKEW_X] == 0f && values[MTRANS_X] == 0f &&
            values[MSKEW_Y] == 0f && values[MSCALE_Y] == 1f && values[MTRANS_Y] == 0f &&
            values[MPERSP_0] == 0f && values[MPERSP_1] == 0f && values[MPERSP_2] == 1f

    companion object {
        val IDENTITY = PdfMatrix()
    }
}
