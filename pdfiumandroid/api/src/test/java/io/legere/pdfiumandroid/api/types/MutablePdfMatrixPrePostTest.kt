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

class MutablePdfMatrixPrePostTest {
    @Test
    fun preRotate0() = preRotate(0.0)

    @Test
    fun preRotate0WithOffset() = preRotateWIthOffset(0.0)

    @Test
    fun postRotate0() = postRotate(0.0)

    @Test
    fun postRotate0WithOffset() = postRotateWIthOffset(0.0)

    @Test
    fun preRotate45() = preRotate(45.0)

    @Test
    fun preRotate45WithOffset() = preRotateWIthOffset(45.0)

    @Test
    fun postRotate45() = postRotate(45.0)

    @Test
    fun postRotate45WithOffset() = postRotateWIthOffset(45.0)

    @Test
    fun preRotate90() = preRotate(90.0)

    @Test
    fun preRotate90WithOffset() = preRotateWIthOffset(90.0)

    @Test
    fun postRotate90() = postRotate(90.0)

    @Test
    fun postRotate90WithOffset() = postRotateWIthOffset(90.0)

    @Test
    fun preRotate180() = preRotate(180.0)

    @Test
    fun preRotate180WithOffset() = preRotateWIthOffset(180.0)

    @Test
    fun postRotate180() = postRotate(180.0)

    @Test
    fun postRotate180WithOffset() = postRotateWIthOffset(180.0)

    @Test
    fun preRotate270() = preRotate(270.0)

    @Test
    fun preRotate270WithOffset() = preRotateWIthOffset(270.0)

    @Test
    fun postRotate270() = postRotate(270.0)

    @Test
    fun postRotate270WithOffset() = postRotateWIthOffset(270.0)

    private fun preRotateWIthOffset(angle: Double) {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setScale(0.5, 0.5, 25.0, 25.0)

        val justRotated = start.setRotate(angle, 25.0, 25.0)

        val rotated = start.preRotate(angle, 25.0, 25.0)

        val alt = start.preConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    private fun preRotate(angle: Double) {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setScale(0.5, 0.5)

        val justRotated = start.setRotate(angle)

        val rotated = start.preRotate(angle)

        val alt = start.preConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    private fun postRotateWIthOffset(angle: Double) {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setScale(0.5, 0.5, 25.0, 25.0)

        val justRotated = start.setRotate(angle, 25.0, 25.0)

        val rotated = start.postRotate(angle, 25.0, 25.0)

        val alt = start.preConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    private fun postRotate(angle: Double) {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setScale(0.5, 0.5)

        val justRotated = start.setRotate(angle)

        val rotated = start.postRotate(angle)

        val alt = start.postConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun preScale() {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setRotate(90.0)

        val justRotated = start.setScale(0.5, 0.5)

        val rotated = start.preScale(0.5, 0.5)

        val alt = start.postConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun preScaleWithOffset() {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setRotate(90.0, 25.0, 25.0)

        val justRotated = start.setScale(0.5, 0.5, 25.0, 25.0)

        val rotated = start.preScale(0.5, 0.5, 25.0, 25.0)

        val alt = start.preConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun postScale() {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setRotate(90.0)

        val justRotated = start.setScale(0.5, 0.5)

        val rotated = start.postScale(0.5, 0.5)

        val alt = start.postConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun postScaleWithOffset() {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setRotate(90.0, 25.0, 25.0)

        val justRotated = start.setScale(0.5, 0.5, 25.0, 25.0)

        val rotated = start.postScale(0.5, 0.5, 25.0, 25.0)

        val alt = start.postConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun preSkew() {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setRotate(90.0)

        val justRotated = start.setSkew(0.5, 0.5)

        val rotated = start.preSkew(0.5, 0.5)

        val alt = start.preConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun preSkewWithOffset() {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setRotate(90.0, 25.0, 25.0)

        val justRotated = start.setSkew(0.5, 0.5, 25.0, 25.0)

        val rotated = start.preSkew(0.5, 0.5, 25.0, 25.0)

        val alt = start.preConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun postSkew() {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setRotate(90.0)

        val justRotated = start.setSkew(0.5, 0.5)

        val rotated = start.postSkew(0.5, 0.5)

        val alt = start.postConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun postSkewWithOffset() {
        val start = MutablePdfMatrix().setTranslate(50.0, 50.0).setRotate(90.0, 25.0, 25.0)

        val justRotated = start.setSkew(0.5, 0.5, 25.0, 25.0)

        val rotated = start.postSkew(0.5, 0.5, 25.0, 25.0)

        val alt = start.postConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun preTranslate() {
        val start = MutablePdfMatrix().setRotate(90.0).setScale(0.5, 0.5)

        val justRotated = start.setTranslate(50.0, 50.0)

        val rotated = start.preTranslate(50.0, 50.0)

        val alt = start.preConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }

    @Test
    fun postTranslate() {
        val start = MutablePdfMatrix().setRotate(90.0).setScale(0.5, 0.5)

        val justRotated = start.setTranslate(50.0, 50.0)

        val rotated = start.postTranslate(50.0, 50.0)

        val alt = start.postConcat(justRotated)

        assertThat(rotated.values).isEqualTo(alt.values)
    }
}
