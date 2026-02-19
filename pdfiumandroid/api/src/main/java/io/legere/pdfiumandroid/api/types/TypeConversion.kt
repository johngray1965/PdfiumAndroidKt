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

package io.legere.pdfiumandroid.api.types

import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.toRect
import kotlin.math.roundToInt

fun PdfPoint.toPoint(): Point = Point(x, y)

fun Point.toPdfPoint(): PdfPoint = PdfPoint(x, y)

fun PdfPointF.toPointF(): PointF = PointF(x, y)

fun PointF.toPdfPointF(): PdfPointF = PdfPointF(x, y)

fun PdfRectF.toRectF(): RectF = RectF(left, top, right, bottom)

fun RectF.toPdfRectF(): PdfRectF = PdfRectF(left, top, right, bottom)

fun Rect.toPdfRect(): PdfRect = PdfRect(left, top, right, bottom)

fun PdfRect.toRect(): Rect = Rect(left, top, right, bottom)

fun RectF.toPdfRect(): PdfRect {
    val rect = this.toRect()
    return PdfRect(rect.left, rect.top, rect.right, rect.bottom)
}

fun PdfRectF.toPdfRect(): PdfRect = PdfRect(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())

fun PdfRect.toPdfRectF(): PdfRectF = PdfRectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

fun PdfMatrix.toMatrix(): Matrix =
    Matrix().apply {
        setValues(values)
    }

fun Matrix.toPdfMatrix(): PdfMatrix {
    val values = FloatArray(9)
    getValues(values)
    return PdfMatrix(values)
}
