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
