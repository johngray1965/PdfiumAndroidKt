package io.legere.pdfiumandroid.util

import android.graphics.Matrix
import android.graphics.RectF

private const val THREE_BY_THREE = 9

internal fun matrixToFloatArray(matrix: Matrix): FloatArray {
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

internal fun floatArrayToMatrix(matrixValues: FloatArray): Matrix {
    // Translation is performed with [1 0 0 1 tx ty].
    // Scaling is performed with [sx 0 0 sy 0 0].
    // Matrix for transformation, in the form [a b c d e f], equivalent to:
    // | a  b  0 |
    // | c  d  0 |
    // | e  f  1 |

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

internal fun matricesToFloatArray(matrices: Collection<Matrix>): FloatArray =
    matrices.flatMap { matrix -> matrixToFloatArray(matrix).asIterable() }.toFloatArray()

internal fun rectToFloatArray(rect: RectF): FloatArray =
    floatArrayOf(
        rect.left,
        rect.top,
        rect.right,
        rect.bottom,
    )

internal fun floatArrayToRect(rectValues: FloatArray): RectF {
    var i = 0
    return RectF(
        rectValues[i++],
        rectValues[i++],
        rectValues[i++],
        rectValues[i],
    )
}

internal fun rectsToFloatArray(rects: Collection<RectF>): FloatArray =
    rects
        .flatMap { rect ->
            rectToFloatArray(rect).asIterable()
        }.toFloatArray()
