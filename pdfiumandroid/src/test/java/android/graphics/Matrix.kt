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

package android.graphics

class Matrix {
    private val values =
        FloatArray(9).apply {
            this[0] = 1f
            this[4] = 1f
            this[8] = 1f
        }

    fun setValues(v: FloatArray) {
        v.copyInto(values)
    }

    fun getValues(v: FloatArray) {
        values.copyInto(v)
    }

    fun reset() {
        for (i in 0..8) values[i] = 0f
        values[0] = 1f
        values[4] = 1f
        values[8] = 1f
    }

    fun postTranslate(
        dx: Float,
        dy: Float,
    ): Boolean {
        values[2] += dx
        values[5] += dy
        return true
    }

    fun postScale(
        sx: Float,
        sy: Float,
    ): Boolean {
        values[0] *= sx
        values[1] *= sx
        values[2] *= sx
        values[3] *= sy
        values[4] *= sy
        values[5] *= sy
        return true
    }

    fun setScale(
        sx: Float,
        sy: Float,
    ) {
        reset()
        values[0] = sx
        values[4] = sy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()
}
