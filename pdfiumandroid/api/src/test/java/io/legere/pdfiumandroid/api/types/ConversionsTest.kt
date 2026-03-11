package io.legere.pdfiumandroid.api.types

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import io.legere.geokt.KtImmutableMatrix
import io.legere.geokt.KtImmutablePoint
import io.legere.geokt.KtImmutablePointF
import io.legere.geokt.KtImmutableRect
import io.legere.geokt.KtImmutableRectF
import io.legere.geokt.MatrixValues
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.graphics.Matrix as AndroidPlatformMatrix

/**
 * Instrumented test, which will execute on an Android device.
 *
 * Compares the behavior of the custom Matrix implementation against the Android platform Matrix.
 */
@RunWith(AndroidJUnit4::class)
@Suppress("LargeClass")
class ConversionsTest {
    private lateinit var pdfMatrix: KtImmutableMatrix

    @Before
    fun setup() {
        pdfMatrix = KtImmutableMatrix()
    }

    @Test
    fun conversions() {
        val result = pdfMatrix.postTranslate(10f, 10f).postScale(2f, 2f)
        val matrix = result.toMatrix()
        val pdfMatrixConverted = matrix.toKtMatrix()
        val mutableKtMatrix = matrix.toMutableKtMatrix()

        val platformValues = FloatArray(9)

        matrix.getValues(platformValues)

        assertMatrixValuesEqual(result, matrix)
        assertMatrixValuesEqual(pdfMatrixConverted, matrix)
        assertMatrixValuesEqual(mutableKtMatrix, matrix)
    }

    @Test
    fun pointFConversions() { // Note testing these function here because are using robolectric
        val result = KtImmutablePointF(10f, 10f)
        val point = result.toPointF()
        val pdfPointF = point.toKtPointF()
        val immutableKtPointF = point.toKtImmutablePointF()

        assertThat(result).isEqualTo(immutableKtPointF)
        assertThat(result.toMutable()).isEqualTo(pdfPointF)
    }

    @Test
    fun pointConversions() { // Note testing these function here because are using robolectric
        val result = KtImmutablePoint(10, 10)
        val point = result.toPoint()
        val pdfPoint = point.toKtPoint()
        val mutableKtPoint = point.toMutableKtPoint()

        assertThat(result).isEqualTo(pdfPoint)
        assertThat(result.toMutable()).isEqualTo(mutableKtPoint)
    }

    @Test
    fun rectConversions() { // Note testing these function here because are using robolectric
        val result = KtImmutableRect(10, 10, 100, 100)
        val rect = result.toRect()
        val pdfRect = rect.toKtRect()
        val mutablepdfRectt = rect.toMutableKtRect()

        assertThat(result).isEqualTo(pdfRect)
        assertThat(result.toMutable()).isEqualTo(mutablepdfRectt)

        val rectF = result.toKtRectF()
        assertThat(rectF.left).isEqualTo(10f)
        assertThat(rectF.top).isEqualTo(10f)
        assertThat(rectF.right).isEqualTo(100f)
        assertThat(rectF.bottom).isEqualTo(100f)
    }

    @Test
    fun rectFConversions() { // Note testing these function here because are using robolectric
        val result = KtImmutableRectF(10f, 10f, 100f, 100f)
        val rect = result.toRectF()
        val pdfRect = rect.toKtRectF()
        val mutablepdfRectt = rect.toMutableKtRectF()

        assertThat(result).isEqualTo(pdfRect)
        assertThat(result).isEqualTo(mutablepdfRectt)

        val rectOut1 = result.toKtRect()
        assertThat(rectOut1.left).isEqualTo(10)
        assertThat(rectOut1.top).isEqualTo(10)
        assertThat(rectOut1.right).isEqualTo(100)
        assertThat(rectOut1.bottom).isEqualTo(100)

        val rectOut2 = rect.toKtRect()
        assertThat(rectOut2.left).isEqualTo(10)
        assertThat(rectOut2.top).isEqualTo(10)
        assertThat(rectOut2.right).isEqualTo(100)
        assertThat(rectOut2.bottom).isEqualTo(100)
    }

    private fun assertMatrixValuesEqual(
        custom: MatrixValues,
        platform: AndroidPlatformMatrix,
        delta: Float = 0.001f,
    ) {
        val customValues = custom.values
        val platformValues = FloatArray(9)

        platform.getValues(platformValues)

        assertFloatArraysEqual(platformValues, customValues.toFloatArray(), delta)
    }

    private fun assertFloatArraysEqual(
        expected: FloatArray,
        actual: FloatArray,
        delta: Float,
    ) {
        assertThat(actual).hasLength(expected.size)
        for (i in expected.indices) {
            assertWithMessage(
                "Value at index $i, \n" +
                    "expected: ${expected.contentToString()}, \n" +
                    "actual:   ${actual.contentToString()}",
            ).that(actual[i])
                .isWithin(delta)
                .of(expected[i])
        }
    }
}
