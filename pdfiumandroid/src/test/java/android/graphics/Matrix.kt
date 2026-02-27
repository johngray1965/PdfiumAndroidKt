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
@file:Suppress("unused")

package android.graphics

@Suppress("TooGenericExceptionThrown")
class Matrix {
    constructor()

    constructor(src: Matrix?) {
        src?.getValues(values)
    }

    enum class ScaleToFit {
        FILL,
        START,
        CENTER,
        END,
    }

    private val values =
        FloatArray(9).apply {
            this[0] = 1f
            this[4] = 1f
            this[8] = 1f
        }

    fun setValues(v: FloatArray) {
        v.copyInto(values)
    }

    fun getValues(v: FloatArray) {
        values.copyInto(v)
    }

    fun reset() {
        for (i in 0..8) values[i] = 0f
        values[0] = 1f
        values[4] = 1f
        values[8] = 1f
    }

    fun postTranslate(
        dx: Float,
        dy: Float,
    ): Boolean {
        values[2] += dx
        values[5] += dy
        return true
    }

    fun postScale(
        sx: Float,
        sy: Float,
    ): Boolean {
        values[0] *= sx
        values[1] *= sx
        values[2] *= sx
        values[3] *= sy
        values[4] *= sy
        values[5] *= sy
        return true
    }

    fun setScale(
        sx: Float,
        sy: Float,
    ) {
        reset()
        values[0] = sx
        values[4] = sy
    }

    fun isAffine(): Boolean = throw RuntimeException("Stub!")

    fun isIdentity(): Boolean = throw RuntimeException("Stub!")

    fun mapPoints(pts: FloatArray?): Unit = throw java.lang.RuntimeException("Stub!")

    fun mapPoints(
        dst: FloatArray?,
        src: FloatArray?,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun mapPoints(
        dst: FloatArray?,
        dstIndex: Int,
        src: FloatArray?,
        srcIndex: Int,
        pointCount: Int,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun mapRadius(radius: Float): Float = throw java.lang.RuntimeException("Stub!")

    fun mapRect(rect: RectF?): Boolean = throw java.lang.RuntimeException("Stub!")

    fun mapRect(
        dst: RectF?,
        src: RectF?,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun mapVectors(vecs: FloatArray?): Unit = throw java.lang.RuntimeException("Stub!")

    fun mapVectors(
        dst: FloatArray?,
        src: FloatArray?,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun mapVectors(
        dst: FloatArray?,
        dstIndex: Int,
        src: FloatArray?,
        srcIndex: Int,
        vectorCount: Int,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun postConcat(other: Matrix?): Boolean = throw java.lang.RuntimeException("Stub!")

    fun postRotate(degrees: Float): Boolean = throw java.lang.RuntimeException("Stub!")

    fun postRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun postSkew(
        kx: Float,
        ky: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun postSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun preConcat(other: Matrix?): Boolean = throw java.lang.RuntimeException("Stub!")

    fun preRotate(degrees: Float): Boolean = throw java.lang.RuntimeException("Stub!")

    fun preRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun preScale(
        sx: Float,
        sy: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun preScale(
        sx: Float,
        sy: Float,
        px: Float,
        py: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun preSkew(
        kx: Float,
        ky: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun preSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun preTranslate(
        dx: Float,
        dy: Float,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun rectStaysRect(): Boolean = throw java.lang.RuntimeException("Stub!")

    fun set(src: Matrix?): Unit = throw java.lang.RuntimeException("Stub!")

    fun setConcat(
        a: Matrix?,
        b: Matrix?,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun setPolyToPoly(
        src: FloatArray?,
        srcIndex: Int,
        dst: FloatArray?,
        dstIndex: Int,
        pointCount: Int,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun setRectToRect(
        src: RectF?,
        dst: RectF?,
        stf: ScaleToFit?,
    ): Boolean = throw java.lang.RuntimeException("Stub!")

    fun setRotate(degrees: Float): Unit = throw java.lang.RuntimeException("Stub!")

    fun setRotate(
        degrees: Float,
        px: Float,
        py: Float,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun setSinCos(
        sinValue: Float,
        cosValue: Float,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun setSinCos(
        sinValue: Float,
        cosValue: Float,
        px: Float,
        py: Float,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun setSkew(
        kx: Float,
        ky: Float,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun setSkew(
        kx: Float,
        ky: Float,
        px: Float,
        py: Float,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    fun setTranslate(
        dx: Float,
        dy: Float,
    ): Unit = throw java.lang.RuntimeException("Stub!")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()
}
