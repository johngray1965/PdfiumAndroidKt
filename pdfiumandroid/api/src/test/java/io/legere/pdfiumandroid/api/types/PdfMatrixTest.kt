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

package io.legere.pdfiumandroid.api.types

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class PdfMatrixTest {
    @Test
    fun `default constructor creates identity matrix`() {
        val matrix = PdfMatrix()
        assertThat(matrix.isIdentity()).isTrue()
        assertThat(matrix.isAffine()).isTrue()
    }

    @Test
    fun `mutable default constructor creates identity matrix`() {
        val matrix = MutablePdfMatrix()
        assertThat(matrix.isIdentity()).isTrue()
        assertThat(matrix.isAffine()).isTrue()
    }

    @Test
    fun `reset sets matrix to identity`() {
        val matrix = MutablePdfMatrix()
        matrix.setTranslate(10f, 20f)
        assertThat(matrix.isIdentity()).isFalse()

        matrix.reset()
        assertThat(matrix.isIdentity()).isTrue()
    }

    @Test
    fun `set copies values`() {
        val src = PdfMatrix().setTranslate(10f, 20f)
        val dst = MutablePdfMatrix()
        dst.set(src)
        assertThat(dst.toImmutable()).isEqualTo(src)
        assertThat(dst.values[TRANS_X]).isEqualTo(10.0)
    }

    @Test
    fun `setTranslate sets correct values`() {
        val matrix = MutablePdfMatrix()
        matrix.setTranslate(10f, 20f)

        val values = matrix.values
        assertThat(values[TRANS_X]).isEqualTo(10.0)
        assertThat(values[TRANS_Y]).isEqualTo(20.0)
        assertThat(values[SCALE_X]).isEqualTo(1.0)
        assertThat(values[SCALE_Y]).isEqualTo(1.0)
    }

    @Test
    fun `setScale sets correct values`() {
        val matrix = MutablePdfMatrix()
        matrix.setScale(2f, 3f)

        val values = matrix.values
        assertThat(values[SCALE_X]).isEqualTo(2.0)
        assertThat(values[SCALE_Y]).isEqualTo(3.0)
        assertThat(values[TRANS_X]).isEqualTo(0.0)
        assertThat(values[TRANS_Y]).isEqualTo(0.0)
    }

    @Test
    fun `setScale with pivot sets correct values`() {
        val matrix = MutablePdfMatrix()
        matrix.setScale(2f, 3f, 10f, 10f)
        // px - sx*px = 10 - 2*10 = -10
        // py - sy*py = 10 - 3*10 = -20

        val values = matrix.values
        assertThat(values[SCALE_X]).isEqualTo(2)
        assertThat(values[SCALE_Y]).isEqualTo(3)
        assertThat(values[TRANS_X]).isEqualTo(-10)
        assertThat(values[TRANS_Y]).isEqualTo(-20)
    }

    @Test
    fun `setRotate sets correct values for 0`() {
        val matrix = MutablePdfMatrix()
        matrix.setRotate(0f)

        val values = matrix.values
        // cos(90) = 0, sin(90) = 1
        assertThat(values[SCALE_X]).isWithin(0.0001).of(1.0)
        assertThat(values[SKEW_X]).isWithin(0.0001).of(0.0)
        assertThat(values[SKEW_Y]).isWithin(0.0001).of(0.0)
        assertThat(values[SCALE_Y]).isWithin(0.0001).of(1.0)
    }

    @Test
    fun `setRotate sets correct values for 45`() {
        val matrix = MutablePdfMatrix()
        matrix.setRotate(45f)

        val values = matrix.values
        // cos(90) = 0, sin(90) = 1
        assertThat(values[SCALE_X]).isWithin(0.0001).of(0.7071067811865476)
        assertThat(values[SKEW_X]).isWithin(0.0001).of(-0.7071067811865476)
        assertThat(values[SKEW_Y]).isWithin(0.0001).of(0.7071067811865475)
        assertThat(values[SCALE_Y]).isWithin(0.0001).of(0.7071067811865475)
    }

    @Test
    fun `setRotate sets correct values for 90`() {
        val matrix = MutablePdfMatrix()
        matrix.setRotate(90f)

        val values = matrix.values
        // cos(90) = 0, sin(90) = 1
        assertThat(values[SCALE_X]).isWithin(0.0001).of(0.0)
        assertThat(values[SKEW_X]).isWithin(0.0001).of(-1.0)
        assertThat(values[SKEW_Y]).isWithin(0.0001).of(1.0)
        assertThat(values[SCALE_Y]).isWithin(0.0001).of(0.0)
    }

    @Test
    fun `setRotate sets correct values for 180`() {
        val matrix = MutablePdfMatrix()
        matrix.setRotate(180f)

        val values = matrix.values
        // cos(90) = 0, sin(90) = 1
        assertThat(values[SCALE_X]).isWithin(0.0001).of(-1.0)
        assertThat(values[SKEW_X]).isWithin(0.0001).of(0.0)
        assertThat(values[SKEW_Y]).isWithin(0.0001).of(0.0)
        assertThat(values[SCALE_Y]).isWithin(0.0001).of(-1.0)
    }

    @Test
    fun `setRotate sets correct values for 270`() {
        val matrix = MutablePdfMatrix()
        matrix.setRotate(270f)

        val values = matrix.values
        // cos(90) = 0, sin(90) = 1
        assertThat(values[SCALE_X]).isWithin(0.0001).of(0.0)
        assertThat(values[SKEW_X]).isWithin(0.0001).of(1.0)
        assertThat(values[SKEW_Y]).isWithin(0.0001).of(-1.0)
        assertThat(values[SCALE_Y]).isWithin(0.0001).of(0.0)
    }

    @Test
    fun `setRotate sets correct values`() {
        val matrix = MutablePdfMatrix()
        matrix.setRotate(90f)

        val values = matrix.values
        // cos(90) = 0, sin(90) = 1
        assertThat(values[SCALE_X]).isWithin(0.0001).of(0.0)
        assertThat(values[SKEW_X]).isWithin(0.0001).of(-1.0)
        assertThat(values[SKEW_Y]).isWithin(0.0001).of(1.0)
        assertThat(values[SCALE_Y]).isWithin(0.0001).of(0.0)
    }

    @Test
    fun `setSkew sets correct values`() {
        val matrix = MutablePdfMatrix()
        matrix.setSkew(0.5f, 0.5f)

        val values = matrix.values
        assertThat(values[SKEW_X]).isEqualTo(0.5)
        assertThat(values[SKEW_Y]).isEqualTo(0.5)
        assertThat(values[SCALE_X]).isEqualTo(1)
    }

    @Test
    fun `concat multiplies matrices`() {
        // A: translate(10, 0)
        val a = PdfMatrix().setTranslate(10f, 0f)
        // B: scale(2, 1)
        val b = PdfMatrix().setScale(2f, 1f)

        // C = A * B
        // v' = A * (B * v)
        val c = a.preConcat(b)

        assertThat(c.values[SCALE_X]).isEqualTo(2.0)
        assertThat(c.values[TRANS_X]).isEqualTo(10.0)
    }

    @Test
    fun `preTranslate modifies matrix correctly`() {
        val matrix = MutablePdfMatrix()
        matrix.setTranslate(10f, 10f)
        matrix.preTranslate(5f, 5f)

        val values = matrix.values
        assertThat(values[TRANS_X]).isEqualTo(15.0)
        assertThat(values[TRANS_Y]).isEqualTo(15.0)
    }

    @Test
    fun `postTranslate modifies matrix correctly`() {
        val matrix = MutablePdfMatrix()
        matrix.setTranslate(10f, 10f)
        matrix.postTranslate(5f, 5f)

        val values = matrix.values
        assertThat(values[TRANS_X]).isEqualTo(15.0)
        assertThat(values[TRANS_Y]).isEqualTo(15.0)
    }

    @Test
    fun `preScale modifies matrix correctly`() {
        val matrix = MutablePdfMatrix()
        matrix.setTranslate(10f, 10f)
        matrix.preScale(2f, 2f)

        val values = matrix.values
        assertThat(values[SCALE_X]).isEqualTo(2.0)
        assertThat(values[SCALE_Y]).isEqualTo(2.0)
        assertThat(values[TRANS_X]).isEqualTo(10.0)
        assertThat(values[TRANS_Y]).isEqualTo(10.0)
    }

    @Test
    fun `postScale modifies matrix correctly`() {
        val matrix = MutablePdfMatrix()
        matrix.setTranslate(10f, 10f)
        matrix.postScale(2f, 2f)

        val values = matrix.values
        assertThat(values[SCALE_X]).isEqualTo(2.0)
        assertThat(values[SCALE_Y]).isEqualTo(2.0)
        assertThat(values[TRANS_X]).isEqualTo(20.0)
        assertThat(values[TRANS_Y]).isEqualTo(20.0)
    }

    @Test
    fun `preSkew modifies matrix correctly`() {
        val matrix = MutablePdfMatrix()
        matrix.setTranslate(10f, 10f)
        matrix.preSkew(1f, 0f)

        val values = matrix.values
        assertThat(values[SCALE_X]).isEqualTo(1.0)
        assertThat(values[SKEW_X]).isEqualTo(1.0)
        assertThat(values[TRANS_X]).isEqualTo(10.0)
    }

    @Test
    fun `postSkew modifies matrix correctly`() {
        val matrix = MutablePdfMatrix()
        matrix.setTranslate(10f, 10f)
        matrix.postSkew(1f, 0f)

        val values = matrix.values
        assertThat(values[SCALE_X]).isEqualTo(1.0)
        assertThat(values[SKEW_X]).isEqualTo(1.0)
        assertThat(values[TRANS_X]).isEqualTo(20.0)
    }

    @Test
    fun `isIdentity checks all fields`() {
        val matrix = MutablePdfMatrix()
        assertThat(matrix.isIdentity()).isTrue()

        matrix.values[PERSP_0] = 0.1
        assertThat(matrix.isIdentity()).isFalse()
    }

    @Test
    fun `isAffine checks perspective fields`() {
        val matrix = MutablePdfMatrix()
        assertThat(matrix.isAffine()).isTrue()

        matrix.values[PERSP_0] = 0.1
        assertThat(matrix.isAffine()).isFalse()

        matrix.reset()
        matrix.values[PERSP_2] = 0.5
        assertThat(matrix.isAffine()).isFalse()
    }

    @Test
    fun `invert calculates correct inverse`() {
        val matrix = MutablePdfMatrix()
        matrix.setScale(2f, 2f)
        matrix.postTranslate(10f, 20f)

        val inverse = MutablePdfMatrix()
        val success = matrix.invert(inverse)
        assertThat(success).isTrue()

        val product = matrix.toImmutable().preConcat(inverse.toImmutable())
        assertThat(product.isIdentity()).isTrue()
    }

    @Test
    fun `invert returns false for non-invertible matrix`() {
        val matrix = MutablePdfMatrix()
        matrix.setScale(0f, 2f)
        val inverse = MutablePdfMatrix()
        assertThat(matrix.invert(inverse)).isFalse()
    }

    @Test
    fun `immutable invert returns null for non-invertible matrix`() {
        val matrix = PdfMatrix().setScale(0f, 2f)
        assertThat(matrix.invert()).isNull()
    }

    @Test
    fun `equals returns true for equal matrices`() {
        val matrixA = MutablePdfMatrix().setScale(0f, 2f)
        val matrixB = MutablePdfMatrix().setScale(0f, 2f)
        assertThat(matrixA == matrixB).isTrue()
    }

    @Test
    fun `equals returns true for the same matrices`() {
        val matrixA = MutablePdfMatrix().setScale(0f, 2f)
        val matrixB = matrixA
        assertThat(matrixA == matrixB).isTrue()
    }

    @Test
    fun `equals returns false for non-equal matrices`() {
        val matrixA = MutablePdfMatrix().setScale(0f, 2f)
        val matrixB = MutablePdfMatrix().setScale(2f, 0f)
        assertThat(matrixA == matrixB).isFalse()
    }

    @Test
    fun `equals returns false not for different types`() {
        val matrixA = MutablePdfMatrix().setScale(0f, 2f)
        val matrixB = PdfMatrix().setScale(0f, 2f)
        assertThat(matrixA == matrixB).isFalse()
    }

    @Test
    fun `hashCode returns true for equal matrices`() {
        val matrixA = MutablePdfMatrix().setScale(0f, 2f)
        val matrixB = MutablePdfMatrix().setScale(0f, 2f)
        assertThat(matrixA.hashCode() == matrixB.hashCode()).isTrue()
    }

    @Test
    fun `hashCode returns true for the same matrices`() {
        val matrixA = MutablePdfMatrix().setScale(0f, 2f)
        val matrixB = matrixA
        assertThat(matrixA.hashCode() == matrixB.hashCode()).isTrue()
    }

//    @Test
//    fun `hashCode returns false for non-equal matrices`() {
//        val matrixA = MutablePdfMatrix().setScale(0f, 2f)
//        val matrixB = MutablePdfMatrix().setScale(2f, 0f)
//        assertThat(matrixA.hashCode() == matrixB.hashCode()).isFalse()
//    }
//
//    @Test
//    fun `hashCode returns false not for different types`() {
//        val matrixA = MutablePdfMatrix().setScale(0f, 2f)
//        val matrixB = PdfMatrix().setScale(0f, 2f)
//        assertThat(matrixA.hashCode() == matrixB.hashCode()).isFalse()
//    }
}
