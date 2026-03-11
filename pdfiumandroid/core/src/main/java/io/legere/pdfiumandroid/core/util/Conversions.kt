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

package io.legere.pdfiumandroid.core.util

import android.graphics.Matrix
import android.graphics.RectF
import io.legere.geokt.FloatRectValues
import io.legere.geokt.KtImmutableMatrix
import io.legere.geokt.KtImmutableRectF
import io.legere.geokt.MatrixValues
import io.legere.geokt.PERSP_0
import io.legere.geokt.PERSP_1
import io.legere.geokt.PERSP_2
import io.legere.geokt.SCALE_X
import io.legere.geokt.SCALE_Y
import io.legere.geokt.SKEW_X
import io.legere.geokt.SKEW_Y
import io.legere.geokt.THREE_BY_THREE
import io.legere.geokt.TRANS_X
import io.legere.geokt.TRANS_Y
import io.legere.pdfiumandroid.api.types.toDoubleArray
import io.legere.pdfiumandroid.api.types.toFloatArray

/**
 * Returns the first 6 values of the matrix in the order:
 * [MSCALE_X, MSKEW_X, MTRANS_X, MSKEW_Y, MSCALE_Y, MTRANS_Y]
 */
fun matrixToFloatArray(matrix: Matrix): FloatArray {
    val values = FloatArray(THREE_BY_THREE)
    matrix.getValues(values)
    return floatArrayOf(
        values[SCALE_X],
        values[SKEW_Y],
        values[SKEW_X],
        values[SCALE_Y],
        values[TRANS_X],
        values[TRANS_Y],
    )
}

/**
 * Returns the first 6 values of the KtImmutableMatrix in the order:
 * [MSCALE_X, MSKEW_X, MTRANS_X, MSKEW_Y, MSCALE_Y, MTRANS_Y]
 */
fun matrixToFloatArray(matrix: MatrixValues): FloatArray {
    val values = matrix.values
    return floatArrayOf(
        values[SCALE_X].toFloat(),
        values[SKEW_Y].toFloat(),
        values[SKEW_X].toFloat(),
        values[SCALE_Y].toFloat(),
        values[TRANS_X].toFloat(),
        values[TRANS_Y].toFloat(),
    )
}

fun floatArrayToMatrix(matrixValues: FloatArray): Matrix {
    val values = FloatArray(THREE_BY_THREE)
    // input is [MSCALE_X, MSKEW_X, MTRANS_X, MSKEW_Y, MSCALE_Y, MTRANS_Y]
    var i = 0
    values[SCALE_X] = matrixValues[i++]
    values[SKEW_Y] = matrixValues[i++]
    values[SKEW_X] = matrixValues[i++]
    values[SCALE_Y] = matrixValues[i++]
    values[TRANS_X] = matrixValues[i++]
    values[TRANS_Y] = matrixValues[i]
    values[PERSP_0] = 0f
    values[PERSP_1] = 0f
    values[PERSP_2] = 1f

    val matrix = Matrix()
    matrix.setValues(values)
    return matrix
}

fun floatArrayToPdfMatrix(matrixValues: FloatArray): KtImmutableMatrix {
    val values = DoubleArray(THREE_BY_THREE)
    // input is [MSCALE_X, MSKEW_X, MTRANS_X, MSKEW_Y, MSCALE_Y, MTRANS_Y]
    var i = 0
    values[SCALE_X] = matrixValues[i++].toDouble()
    values[SKEW_Y] = matrixValues[i++].toDouble()
    values[SKEW_X] = matrixValues[i++].toDouble()
    values[SCALE_Y] = matrixValues[i++].toDouble()
    values[TRANS_X] = matrixValues[i++].toDouble()
    values[TRANS_Y] = matrixValues[i].toDouble()
    values[PERSP_0] = 0.0
    values[PERSP_1] = 0.0
    values[PERSP_2] = 1.0
    return KtImmutableMatrix(values)
}

fun matricesToFloatArray(matrices: Collection<Matrix>): FloatArray =
    matrices.flatMap { matrix -> matrixToFloatArray(matrix).asIterable() }.toFloatArray()

@JvmName("pdfMatricesToFloatArray")
fun matricesToFloatArray(matrices: Collection<MatrixValues>): FloatArray =
    matrices.flatMap { matrix -> matrixToFloatArray(matrix).asIterable() }.toFloatArray()

fun rectToFloatArray(rect: RectF): FloatArray =
    floatArrayOf(
        rect.left,
        rect.top,
        rect.right,
        rect.bottom,
    )

fun rectToFloatArray(rect: FloatRectValues): FloatArray =
    floatArrayOf(
        rect.left,
        rect.top,
        rect.right,
        rect.bottom,
    )

fun floatArrayToRect(rectValues: FloatArray): RectF {
    var i = 0
    return RectF(
        rectValues[i++],
        rectValues[i++],
        rectValues[i++],
        rectValues[i],
    )
}

fun floatArrayToPdfRect(rectValues: FloatArray): KtImmutableRectF {
    var i = 0
    return KtImmutableRectF(
        rectValues[i++],
        rectValues[i++],
        rectValues[i++],
        rectValues[i],
    )
}

fun rectsToFloatArray(rects: Collection<RectF>): FloatArray =
    rects
        .flatMap { rect ->
            rectToFloatArray(rect).asIterable()
        }.toFloatArray()

@JvmName("pdfRectsToFloatArray")
fun rectsToFloatArray(rects: Collection<FloatRectValues>): FloatArray =
    rects
        .flatMap { rect ->
            rectToFloatArray(rect).asIterable()
        }.toFloatArray()

fun Matrix.toPdfMatrix(): KtImmutableMatrix {
    val values = FloatArray(THREE_BY_THREE)
    this.getValues(values)
    return KtImmutableMatrix(values.toDoubleArray())
}

fun KtImmutableMatrix.toMatrix(): Matrix {
    val matrix = Matrix()
    matrix.setValues(this.values.toFloatArray())
    return matrix
}

fun RectF.toPdfRectF(): KtImmutableRectF = KtImmutableRectF(left, top, right, bottom)

fun KtImmutableRectF.toRectF(): RectF = RectF(left, top, right, bottom)
