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

@file:JvmName("PdfiumAndroidMappers")

package io.legere.pdfiumandroid.api.types

import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.toRect
import io.legere.geokt.FloatPointValues
import io.legere.geokt.FloatRectValues
import io.legere.geokt.IntPointValues
import io.legere.geokt.IntRectValues
import io.legere.geokt.KtImmutableMatrix
import io.legere.geokt.KtImmutablePoint
import io.legere.geokt.KtImmutablePointF
import io.legere.geokt.KtImmutableRect
import io.legere.geokt.KtImmutableRectF
import io.legere.geokt.KtMatrix
import io.legere.geokt.KtPoint
import io.legere.geokt.KtPointF
import io.legere.geokt.KtRect
import io.legere.geokt.MatrixValues
import io.legere.geokt.THREE_BY_THREE
import kotlin.math.roundToInt

// --- Point Conversions ---

fun IntPointValues.toPoint(): Point = Point(x, y)

fun Point.toKtPoint(): KtImmutablePoint = KtImmutablePoint(x, y)

fun Point.toMutableKtPoint(): KtPoint = KtPoint(x, y)

fun FloatPointValues.toPointF(): PointF = PointF(x, y)

fun PointF.toKtImmutablePointF(): KtImmutablePointF = KtImmutablePointF(x, y)

fun PointF.toKtPointF(): KtPointF = KtPointF(x, y)

// --- Rect Conversions ---

fun FloatRectValues.toRectF(): RectF = RectF(left, top, right, bottom)

fun RectF.toKtRectF(): KtImmutableRectF = KtImmutableRectF(left, top, right, bottom)

fun RectF.toMutableKtRectF(): KtImmutableRectF = KtImmutableRectF(left, top, right, bottom)

fun IntRectValues.toRect(): Rect = Rect(left, top, right, bottom)

fun Rect.toKtRect(): KtImmutableRect = KtImmutableRect(left, top, right, bottom)

fun Rect.toMutableKtRect(): KtRect = KtRect(left, top, right, bottom)

fun RectF.toKtRect(): KtImmutableRect {
    val rect = this.toRect()
    return KtImmutableRect(rect.left, rect.top, rect.right, rect.bottom)
}

fun FloatRectValues.toKtRect(): KtImmutableRect =
    KtImmutableRect(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())

fun IntRectValues.toKtRectF(): KtImmutableRectF = KtImmutableRectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

// --- Matrix Conversions ---

fun MatrixValues.toMatrix(): Matrix =
    Matrix().apply {
        setValues(values.toFloatArray())
    }

fun Matrix.toKtMatrix(): KtImmutableMatrix {
    val values = FloatArray(THREE_BY_THREE)
    getValues(values)
    return KtImmutableMatrix(values.toDoubleArray())
}

fun Matrix.toMutableKtMatrix(): KtMatrix {
    val values = FloatArray(THREE_BY_THREE)
    getValues(values)
    return KtMatrix(values.toDoubleArray())
}

fun DoubleArray.toFloatArray(): FloatArray {
    val floatArray = FloatArray(size)
    for (i in indices) {
        floatArray[i] = this[i].toFloat()
    }
    return floatArray
}

fun FloatArray.toDoubleArray(): DoubleArray {
    val doubleArray = DoubleArray(size)
    for (i in indices) {
        doubleArray[i] = this[i].toDouble()
    }
    return doubleArray
}
