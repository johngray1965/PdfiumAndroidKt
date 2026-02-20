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

interface IntPointValues {
    val x: Int
    val y: Int
}

@Keep
data class PdfPoint(
    override val x: Int,
    override val y: Int,
) : IntPointValues {
    fun toIntArray(): IntArray = intArrayOf(x, y)

    fun toMutable() = MutablePdfPoint(x, y)

    fun offset(
        dx: Int,
        dy: Int,
    ) = PdfPoint(x + dx, y + dy)

    fun negate() = PdfPoint(-x, -y)

    companion object {
        val ZERO = PdfPoint(0, 0)
    }
}

@Keep
class MutablePdfPoint(
    override var x: Int = 0,
    override var y: Int = 0,
) : IntPointValues {
    fun set(
        x: Int,
        y: Int,
    ) {
        this.x = x
        this.y = y
    }

    fun set(src: IntPointValues) {
        this.x = src.x
        this.y = src.y
    }

    fun toImmutable() = PdfPoint(x, y)

    fun offset(
        dx: Int,
        dy: Int,
    ) = PdfPoint(x + dx, y + dy)

    fun negate() = PdfPoint(-x, -y)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntPointValues) return false
        return x == other.x && y == other.y
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}
