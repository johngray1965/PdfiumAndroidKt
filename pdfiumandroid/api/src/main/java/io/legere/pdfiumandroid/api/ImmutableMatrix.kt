@file:Suppress("unused")

package io.legere.pdfiumandroid.api

import android.graphics.Matrix
import android.graphics.RectF

@Suppress("TooManyFunctions")
data class ImmutableMatrix(
    private val internalMatrix: Matrix = Matrix(),
) {
    /**
     * Returns a copy of the underlying mutable Matrix.
     */
    fun toMutableMatrix(): Matrix = Matrix(internalMatrix)

    fun getValues(values: FloatArray?) {
        internalMatrix.getValues(values)
    }

    fun isAffine(): Boolean = internalMatrix.isAffine

    fun isIdentity(): Boolean = internalMatrix.isIdentity

    fun mapPoints(pts: FloatArray?) {
        internalMatrix.mapPoints(pts)
    }

    fun mapPoints(
        dst: FloatArray?,
        src: FloatArray?,
    ) {
        internalMatrix.mapPoints(dst, src)
    }

    fun mapPoints(
        dst: FloatArray?,
        dstIndex: Int,
        src: FloatArray?,
        srcIndex: Int,
        pointCount: Int,
    ) {
        internalMatrix.mapPoints(dst, dstIndex, src, srcIndex, pointCount)
    }

    fun mapRadius(radius: Float): Float = internalMatrix.mapRadius(radius)

    fun mapRect(rect: RectF?): Boolean = internalMatrix.mapRect(rect)

    fun mapRect(
        dst: RectF?,
        src: RectF?,
    ): Boolean = internalMatrix.mapRect(dst, src)

    fun mapVectors(vecs: FloatArray?) {
        internalMatrix.mapVectors(vecs)
    }

    fun mapVectors(
        dst: FloatArray?,
        src: FloatArray?,
    ) {
        internalMatrix.mapVectors(dst, src)
    }

    fun mapVectors(
        dst: FloatArray?,
        dstIndex: Int,
        src: FloatArray?,
        srcIndex: Int,
        vectorCount: Int,
    ) {
        internalMatrix.mapVectors(dst, dstIndex, src, srcIndex, vectorCount)
    }
}
