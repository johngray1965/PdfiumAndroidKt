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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

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
    constructor(other: PdfMatrix) : this(other.values)

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
        normalize()
    }

    fun setRotate(
        degrees: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        reset()
        val radians = degrees.toDouble() * PI / 180.0
        val sinVal = sin(radians).toFloat()
        val cosVal = cos(radians).toFloat()
        setSinCos(sinVal, cosVal, px, py)
    }

    fun setSinCos(
        sinVal: Float,
        cosVal: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
        reset()
        values[MSCALE_X] = cosVal
        values[MSKEW_X] = -sinVal
        values[MSKEW_Y] = sinVal
        values[MSCALE_Y] = cosVal
        values[MTRANS_X] = px - cosVal * px + sinVal * py
        values[MTRANS_Y] = py - sinVal * px - cosVal * py
        normalize()
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
        normalize()
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
        normalize()
    }

    fun setRectToRect(
        src: PdfRectF,
        dst: PdfRectF,
    ): Boolean {
        if (src.isEmpty()) {
            reset()
            return false
        }
        val sx = dst.width() / src.width()
        val sy = dst.height() / src.height()
        val tx = dst.left - sx * src.left
        val ty = dst.top - sy * src.top

        reset()
        values[MSCALE_X] = sx
        values[MSCALE_Y] = sy
        values[MTRANS_X] = tx
        values[MTRANS_Y] = ty
        normalize()
        return true
    }

    fun preTranslate(
        dx: Float,
        dy: Float,
    ) {
        val temp = PdfMatrix()
        temp.setTranslate(dx, dy)
        preConcat(temp)
    }

    fun preScale(
        sx: Float,
        sy: Float,
        px: Float = 0f,
        py: Float = 0f,
    ) {
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
        val temp = PdfMatrix()
        temp.setConcat(this, other)
        set(temp)
    }

    fun postTranslate(
        dx: Float,
        dy: Float,
    ) {
        val temp = PdfMatrix()
        temp.setTranslate(dx, dy)
        postConcat(temp)
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
        val temp = PdfMatrix()
        temp.setConcat(other, this)
        set(temp)
    }

    fun isAffine(): Boolean = values[MPERSP_0] == 0f && values[MPERSP_1] == 0f && values[MPERSP_2] == 1f

    fun isIdentity(): Boolean =
        values[MSCALE_X] == 1f && values[MSKEW_X] == 0f && values[MTRANS_X] == 0f &&
            values[MSKEW_Y] == 0f && values[MSCALE_Y] == 1f && values[MTRANS_Y] == 0f &&
            values[MPERSP_0] == 0f && values[MPERSP_1] == 0f && values[MPERSP_2] == 1f

    private fun normalize() {
        for (idx in values.indices) {
            if (values[idx] == -0.0f) {
                values[idx] = 0.0f
            }
        }
    }

    /**
     * Map the specified point by this matrix, and return the new point.
     */
    fun mapPoint(point: PdfPointF): PdfPointF {
        val x = point.x
        val y = point.y
        val w = values[MPERSP_0] * x + values[MPERSP_1] * y + values[MPERSP_2]
        val px = (values[MSCALE_X] * x + values[MSKEW_X] * y + values[MTRANS_X]) / w
        val py = (values[MSKEW_Y] * x + values[MSCALE_Y] * y + values[MTRANS_Y]) / w
        return PdfPointF(px, py)
    }

    /**
     * Map the specified rectangle by this matrix, and return a new rectangle
     * that bounds the transformed points.
     */
    fun mapRect(rect: PdfRectF): PdfRectF {
        val p1 = mapPoint(PdfPointF(rect.left, rect.top))
        val p2 = mapPoint(PdfPointF(rect.right, rect.top))
        val p3 = mapPoint(PdfPointF(rect.right, rect.bottom))
        val p4 = mapPoint(PdfPointF(rect.left, rect.bottom))

        val l = min(p1.x, min(p2.x, min(p3.x, p4.x)))
        val t = min(p1.y, min(p2.y, min(p3.y, p4.y)))
        val r = max(p1.x, max(p2.x, max(p3.x, p4.x)))
        val b = max(p1.y, max(p2.y, max(p3.y, p4.y)))

        return PdfRectF(l, t, r, b)
    }

    /**
     * Return the mean radius of a circle after it has been mapped by this matrix.
     */
    fun mapRadius(radius: Float): Float {
        val a = values[MSCALE_X]
        val b = values[MSKEW_Y]
        val c = values[MSKEW_X]
        val d = values[MSCALE_Y]
        val d1 = sqrt((a * a + b * b).toDouble()).toFloat()
        val d2 = sqrt((c * c + d * d).toDouble()).toFloat()
        return radius * (d1 + d2) / 2f
    }

    /**
     * Map the specified vector by this matrix, and return the new vector.
     * Translation is ignored.
     */
    fun mapVector(vector: PdfPointF): PdfPointF {
        val x = vector.x
        val y = vector.y
        val px = values[MSCALE_X] * x + values[MSKEW_X] * y
        val py = values[MSKEW_Y] * x + values[MSCALE_Y] * y
        return PdfPointF(px, py)
    }

    companion object {
        val IDENTITY = PdfMatrix()
    }
}
