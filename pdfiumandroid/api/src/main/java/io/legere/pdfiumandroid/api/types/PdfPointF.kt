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

package io.legere.pdfiumandroid.api.types

import androidx.annotation.Keep

interface FloatPointValues {
    val x: Float
    val y: Float
}

@Keep
data class PdfPointF(
    override val x: Float,
    override val y: Float,
) : FloatPointValues {
    fun toFloatArray(): FloatArray = floatArrayOf(x, y)

    fun toMutable() = MutablePdfPointF(x, y)

    fun offset(
        dx: Float,
        dy: Float,
    ) = PdfPointF(x + dx, y + dy)

    fun negate() = PdfPointF(-x, -y)

    fun length() = kotlin.math.sqrt(x * x + y * y)

    companion object {
        val ZERO = PdfPointF(0f, 0f)
    }
}

@Keep
class MutablePdfPointF(
    override var x: Float = 0f,
    override var y: Float = 0f,
) : FloatPointValues {
    fun set(
        x: Float,
        y: Float,
    ) {
        this.x = x
        this.y = y
    }

    fun set(src: FloatPointValues) {
        this.x = src.x
        this.y = src.y
    }

    fun toImmutable() = PdfPointF(x, y)

    fun offset(
        dx: Float,
        dy: Float,
    ) = MutablePdfPointF(x + dx, y + dy)

    fun negate() = MutablePdfPointF(-x, -y)

    fun length() = kotlin.math.sqrt(x * x + y * y)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatPointValues) return false
        return x == other.x && y == other.y
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}
