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
import io.legere.pdfiumandroid.api.types.FloatRectValues
import io.legere.pdfiumandroid.api.types.MPERSP_0
import io.legere.pdfiumandroid.api.types.MPERSP_1
import io.legere.pdfiumandroid.api.types.MPERSP_2
import io.legere.pdfiumandroid.api.types.MSCALE_X
import io.legere.pdfiumandroid.api.types.MSCALE_Y
import io.legere.pdfiumandroid.api.types.MSKEW_X
import io.legere.pdfiumandroid.api.types.MSKEW_Y
import io.legere.pdfiumandroid.api.types.MTRANS_X
import io.legere.pdfiumandroid.api.types.MTRANS_Y
import io.legere.pdfiumandroid.api.types.MatrixValues
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.api.types.THREE_BY_THREE
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
        values[MSCALE_X],
        values[MSKEW_Y],
        values[MSKEW_X],
        values[MSCALE_Y],
        values[MTRANS_X],
        values[MTRANS_Y],
    )
}

/**
 * Returns the first 6 values of the PdfMatrix in the order:
 * [MSCALE_X, MSKEW_X, MTRANS_X, MSKEW_Y, MSCALE_Y, MTRANS_Y]
 */
fun matrixToFloatArray(matrix: MatrixValues): FloatArray {
    val values = matrix.values
    return floatArrayOf(
        values[MSCALE_X].toFloat(),
        values[MSKEW_Y].toFloat(),
        values[MSKEW_X].toFloat(),
        values[MSCALE_Y].toFloat(),
        values[MTRANS_X].toFloat(),
        values[MTRANS_Y].toFloat(),
    )
}

fun floatArrayToMatrix(matrixValues: FloatArray): Matrix {
    val values = FloatArray(THREE_BY_THREE)
    // input is [MSCALE_X, MSKEW_X, MTRANS_X, MSKEW_Y, MSCALE_Y, MTRANS_Y]
    var i = 0
    values[MSCALE_X] = matrixValues[i++]
    values[MSKEW_Y] = matrixValues[i++]
    values[MSKEW_X] = matrixValues[i++]
    values[MSCALE_Y] = matrixValues[i++]
    values[MTRANS_X] = matrixValues[i++]
    values[MTRANS_Y] = matrixValues[i]
    values[MPERSP_0] = 0f
    values[MPERSP_1] = 0f
    values[MPERSP_2] = 1f

    val matrix = Matrix()
    matrix.setValues(values)
    return matrix
}

fun floatArrayToPdfMatrix(matrixValues: FloatArray): PdfMatrix {
    val values = DoubleArray(THREE_BY_THREE)
    // input is [MSCALE_X, MSKEW_X, MTRANS_X, MSKEW_Y, MSCALE_Y, MTRANS_Y]
    var i = 0
    values[MSCALE_X] = matrixValues[i++].toDouble()
    values[MSKEW_Y] = matrixValues[i++].toDouble()
    values[MSKEW_X] = matrixValues[i++].toDouble()
    values[MSCALE_Y] = matrixValues[i++].toDouble()
    values[MTRANS_X] = matrixValues[i++].toDouble()
    values[MTRANS_Y] = matrixValues[i].toDouble()
    values[MPERSP_0] = 0.0
    values[MPERSP_1] = 0.0
    values[MPERSP_2] = 1.0
    return PdfMatrix(values)
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

fun floatArrayToPdfRect(rectValues: FloatArray): PdfRectF {
    var i = 0
    return PdfRectF(
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

fun Matrix.toPdfMatrix(): PdfMatrix {
    val values = FloatArray(THREE_BY_THREE)
    this.getValues(values)
    return PdfMatrix(values.toDoubleArray())
}

fun PdfMatrix.toMatrix(): Matrix {
    val matrix = Matrix()
    matrix.setValues(this.values.toFloatArray())
    return matrix
}

fun RectF.toPdfRectF(): PdfRectF = PdfRectF(left, top, right, bottom)

fun PdfRectF.toRectF(): RectF = RectF(left, top, right, bottom)
