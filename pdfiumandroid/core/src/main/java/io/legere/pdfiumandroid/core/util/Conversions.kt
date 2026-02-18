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
import io.legere.pdfiumandroid.api.types.PdfMatrix
import io.legere.pdfiumandroid.api.types.PdfRectF

private const val THREE_BY_THREE = 9

fun matrixToFloatArray(matrix: Matrix): FloatArray {
    val matrixValues = FloatArray(THREE_BY_THREE)
    matrix.getValues(matrixValues)
    return floatArrayOf(
        matrixValues[Matrix.MSCALE_X],
        matrixValues[Matrix.MSKEW_X],
        matrixValues[Matrix.MSKEW_Y],
        matrixValues[Matrix.MSCALE_Y],
        matrixValues[Matrix.MTRANS_X],
        matrixValues[Matrix.MTRANS_Y],
    )
}

fun matrixToFloatArray(matrix: PdfMatrix): FloatArray {
    val matrixValues = matrix.values
    return floatArrayOf(
        matrixValues[Matrix.MSCALE_X],
        matrixValues[Matrix.MSKEW_X],
        matrixValues[Matrix.MSKEW_Y],
        matrixValues[Matrix.MSCALE_Y],
        matrixValues[Matrix.MTRANS_X],
        matrixValues[Matrix.MTRANS_Y],
    )
}

fun floatArrayToMatrix(matrixValues: FloatArray): Matrix {
    val values = FloatArray(THREE_BY_THREE)

    var i = 0
    values[Matrix.MSCALE_X] = matrixValues[i++]
    values[Matrix.MSKEW_X] = matrixValues[i++]
    values[Matrix.MSKEW_Y] = matrixValues[i++]
    values[Matrix.MSCALE_Y] = matrixValues[i++]

    values[Matrix.MTRANS_X] = matrixValues[i++]
    values[Matrix.MTRANS_Y] = matrixValues[i]

    values[Matrix.MPERSP_0] = 0f
    values[Matrix.MPERSP_1] = 0f
    values[Matrix.MPERSP_2] = 1f

    val matrix = Matrix()
    matrix.setValues(values)
    return matrix
}

fun floatArrayToPdfMatrix(matrixValues: FloatArray): PdfMatrix {
    val values = FloatArray(THREE_BY_THREE)

    // Using the same index mapping as floatArrayToMatrix
    var i = 0
    val scaleX = matrixValues[i++]
    val skewX = matrixValues[i++]
    val skewY = matrixValues[i++]
    val scaleY = matrixValues[i++]
    val transX = matrixValues[i++]
    val transY = matrixValues[i]
    i = 0
    values[i++] = scaleX
    values[i++] = skewX
    values[i++] = transX

    values[i++] = skewY
    values[i++] = scaleY
    values[i++] = transY

    values[i++] = 0f
    values[i++] = 0f
    values[i] = 1f

    return PdfMatrix(values)
}

fun matricesToFloatArray(matrices: Collection<Matrix>): FloatArray =
    matrices.flatMap { matrix -> matrixToFloatArray(matrix).asIterable() }.toFloatArray()

@JvmName("pdfMatricesToFloatArray")
fun matricesToFloatArray(matrices: Collection<PdfMatrix>): FloatArray =
    matrices.flatMap { matrix -> matrixToFloatArray(matrix).asIterable() }.toFloatArray()

fun rectToFloatArray(rect: RectF): FloatArray =
    floatArrayOf(
        rect.left,
        rect.top,
        rect.right,
        rect.bottom,
    )

fun rectToFloatArray(rect: PdfRectF): FloatArray =
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
fun rectsToFloatArray(rects: Collection<PdfRectF>): FloatArray =
    rects
        .flatMap { rect ->
            rectToFloatArray(rect).asIterable()
        }.toFloatArray()

fun Matrix.toPdfMatrix(): PdfMatrix {
    val values = FloatArray(THREE_BY_THREE)
    this.getValues(values)
    return PdfMatrix(values)
}

fun PdfMatrix.toMatrix(): Matrix {
    val matrix = Matrix()
    matrix.setValues(this.values)
    return matrix
}

fun RectF.toPdfRectF(): PdfRectF = PdfRectF(left, top, right, bottom)

fun PdfRectF.toRectF(): RectF = RectF(left, top, right, bottom)
