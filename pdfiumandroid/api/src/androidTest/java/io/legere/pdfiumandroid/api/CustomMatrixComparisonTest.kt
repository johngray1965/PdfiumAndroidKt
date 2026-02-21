package io.legere.pdfiumandroid.api

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.legere.pdfiumandroid.api.types.MutablePdfMatrix
import io.legere.pdfiumandroid.api.types.MutablePdfRectF
import io.legere.pdfiumandroid.api.types.PdfRectF
import io.legere.pdfiumandroid.api.types.toPdfRectF
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
class CustomMatrixComparisonTest {
    private lateinit var customMatrix: MutablePdfMatrix
    private lateinit var platformMatrix: AndroidPlatformMatrix

    @Before
    fun setup() {
        customMatrix = MutablePdfMatrix()
        platformMatrix = AndroidPlatformMatrix()
    }

    @Test
    fun resetBehavesLikePlatformMatrix() {
        // Modify both matrices first to ensure reset has an effect
        customMatrix.postTranslate(10f, 20f)
        platformMatrix.postTranslate(10f, 20f)

        customMatrix.reset()
        platformMatrix.reset()

        assertThat(customMatrix.isIdentity()).isTrue()
        assertThat(platformMatrix.isIdentity).isTrue()
        assertMatrixValuesEqual(customMatrix, platformMatrix)
    }

    @Test
    fun setTranslateBehavesLikePlatformMatrix() {
        val dx = 100f
        val dy = 200f

        customMatrix.setTranslate(dx, dy)
        platformMatrix.setTranslate(dx, dy)

        assertThat(customMatrix.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
        assertMatrixValuesEqual(customMatrix, platformMatrix)
    }

    @Test
    fun setScaleBehavesLikePlatformMatrix() {
        val sx = 2f
        val sy = 0.5f

        customMatrix.setScale(sx, sy)
        platformMatrix.setScale(sx, sy)

        assertThat(customMatrix.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
        assertMatrixValuesEqual(customMatrix, platformMatrix)
    }

    @Test
    fun setRotateBehavesLikePlatformMatrix() {
        val degrees = 45f

        customMatrix.setRotate(degrees)
        platformMatrix.setRotate(degrees)

        assertThat(customMatrix.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
        assertMatrixValuesEqual(customMatrix, platformMatrix)
    }

    @Test
    fun postTranslateBehavesLikePlatformMatrix() {
        val dx = 10f
        val dy = 20f

        customMatrix.postTranslate(dx, dy)
        platformMatrix.postTranslate(dx, dy)

        assertMatrixValuesEqual(customMatrix, platformMatrix)

        // Chained operations
        customMatrix.postTranslate(5f, 5f)
        platformMatrix.postTranslate(5f, 5f)
        assertMatrixValuesEqual(customMatrix, platformMatrix)
    }

    @Test
    fun postScaleBehavesLikePlatformMatrix() {
        val sx = 2f
        val sy = 3f

        customMatrix.postScale(sx, sy)
        platformMatrix.postScale(sx, sy)
        assertMatrixValuesEqual(customMatrix, platformMatrix)

        // Chained operations
        customMatrix.postScale(0.5f, 0.5f)
        platformMatrix.postScale(0.5f, 0.5f)
        assertMatrixValuesEqual(customMatrix, platformMatrix)
    }

    @Test
    fun postRotateBehavesLikePlatformMatrix() {
        val degrees = 30f

        customMatrix.postRotate(degrees)
        platformMatrix.postRotate(degrees)
        assertMatrixValuesEqual(customMatrix, platformMatrix)

        // Chained operations
        customMatrix.postRotate(60f)
        platformMatrix.postRotate(60f)
        assertMatrixValuesEqual(customMatrix, platformMatrix)
    }

    @Test
    fun isIdentityBehavesLikePlatformMatrix() {
        assertThat(customMatrix.isIdentity()).isTrue()
        assertThat(platformMatrix.isIdentity).isTrue()

        customMatrix.postTranslate(1f, 0f)
        platformMatrix.postTranslate(1f, 0f)
        assertThat(customMatrix.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
    }

    @Test
    fun isAffineBehavesLikePlatformMatrix() {
        assertThat(customMatrix.isAffine()).isTrue() // Identity is affine
        assertThat(platformMatrix.isAffine).isTrue()

        customMatrix.postScale(2f, 2f)
        platformMatrix.postScale(2f, 2f)
        assertThat(customMatrix.isAffine()).isTrue()
        assertThat(platformMatrix.isAffine).isTrue()

        customMatrix.set(
            floatArrayOf(
                1f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0.1f,
                0.2f,
                1f, // Perspective component
            ),
        )
        platformMatrix.setValues(
            floatArrayOf(
                1f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0.1f,
                0.2f,
                1f,
            ),
        )
        assertThat(customMatrix.isAffine()).isFalse()
        assertThat(platformMatrix.isAffine).isFalse()
    }

    @Test
    fun mapPointsSingleArgBehavesLikePlatformMatrix() {
        val points = floatArrayOf(0f, 0f, 10f, 10f, 5f, 5f)
        val customPoints = points.copyOf()
        val platformPoints = points.copyOf()

        customMatrix.postTranslate(10f, 20f)
        platformMatrix.postTranslate(10f, 20f)

        customMatrix.mapPoints(customPoints)
        platformMatrix.mapPoints(platformPoints)

        assertFloatArraysEqual(platformPoints, customPoints, 0.001f)
    }

    @Test
    fun mapPointsTwoArgsBehavesLikePlatformMatrix() {
        val srcPoints = floatArrayOf(0f, 0f, 10f, 10f, 5f, 5f)
        val customDstPoints = FloatArray(srcPoints.size)
        val platformDstPoints = FloatArray(srcPoints.size)

        customMatrix.postScale(2f, 3f)
        platformMatrix.postScale(2f, 3f)

        customMatrix.mapPoints(customDstPoints, srcPoints)
        platformMatrix.mapPoints(platformDstPoints, srcPoints)

        assertFloatArraysEqual(platformDstPoints, customDstPoints, 0.001f)
    }

    @Test
    fun mapPointsMultiArgBehavesLikePlatformMatrix() {
        val srcPoints = floatArrayOf(0f, 0f, 1f, 1f, 2f, 2f, 3f, 3f)
        val customDstPoints = FloatArray(srcPoints.size)
        val platformDstPoints = FloatArray(srcPoints.size)

        customMatrix.postTranslate(10f, 10f)
        platformMatrix.postTranslate(10f, 10f)

        val srcIndex = 2
        val dstIndex = 4
        val pointCount = 2

        customMatrix.mapPoints(customDstPoints, dstIndex, srcPoints, srcIndex, pointCount)
        platformMatrix.mapPoints(platformDstPoints, dstIndex, srcPoints, srcIndex, pointCount)

        assertFloatArraysEqual(platformDstPoints, customDstPoints, 0.001f)
    }

    @Test
    fun mapRadiusBehavesLikePlatformMatrix() {
        val radius = 10f

        customMatrix.postScale(2f, 3f)
        platformMatrix.postScale(2f, 3f)

        val customMappedRadius = customMatrix.mapRadius(radius)
        val platformMappedRadius = platformMatrix.mapRadius(radius)

        assertThat(customMappedRadius).isWithin(0.1f).of(platformMappedRadius)

        // With rotation
        customMatrix.reset()
        platformMatrix.reset()
        customMatrix.postRotate(90f)
        platformMatrix.postRotate(90f)
        customMatrix.postScale(2f, 3f)
        platformMatrix.postScale(2f, 3f)

        val customMappedRadiusRotated = customMatrix.mapRadius(radius)
        val platformMappedRadiusRotated = platformMatrix.mapRadius(radius)

        assertThat(customMappedRadiusRotated).isWithin(0.01f).of(platformMappedRadiusRotated)
    }

    @Test
    fun mapRectSingleArgBehavesLikePlatformMatrix() {
        val rect = RectF(0f, 0f, 100f, 200f)
        val customRect = PdfRectF(0f, 0f, 100f, 200f)
        val platformRect = RectF(rect)

        customMatrix.postTranslate(10f, 20f)
        platformMatrix.postTranslate(10f, 20f)
        customMatrix.postScale(0.5f, 2f)
        platformMatrix.postScale(0.5f, 2f)

        val customResult = customMatrix.mapRect(customRect)
        val platformResult = platformMatrix.mapRect(platformRect)

        assertThat(customResult.left).isWithin(0.001f).of(platformRect.left)
        assertThat(customResult.top).isWithin(0.001f).of(platformRect.top)
        assertThat(customResult.right).isWithin(0.001f).of(platformRect.right)
        assertThat(customResult.bottom).isWithin(0.001f).of(platformRect.bottom)
    }

    @Test
    fun mapRectTwoArgsBehavesLikePlatformMatrix() {
        val srcRect = RectF(0f, 0f, 100f, 200f)
        val customDstRect = MutablePdfRectF()
        val platformDstRect = RectF()

        customMatrix.postTranslate(10f, 20f)
        platformMatrix.postTranslate(10f, 20f)
        customMatrix.postRotate(90f)
        platformMatrix.postRotate(90f)

        val customResult = customMatrix.mapRect(customDstRect, srcRect.toPdfRectF())
        val platformResult = platformMatrix.mapRect(platformDstRect, srcRect)

//        assertThat(customResult).isEqualTo(platformResult)
        assertThat(customDstRect.left).isWithin(0.001f).of(platformDstRect.left)
        assertThat(customDstRect.top).isWithin(0.001f).of(platformDstRect.top)
        assertThat(customDstRect.right).isWithin(0.001f).of(platformDstRect.right)
        assertThat(customDstRect.bottom).isWithin(0.001f).of(platformDstRect.bottom)
    }

    @Test
    fun mapVectorsSingleArgBehavesLikePlatformMatrix() {
        val vectors = floatArrayOf(1f, 1f, 0f, 10f, -5f, 0f)
        val customVectors = vectors.copyOf()
        val platformVectors = vectors.copyOf()

        customMatrix.postTranslate(100f, 200f) // Translation should be ignored by mapVectors
        platformMatrix.postTranslate(100f, 200f)
        customMatrix.postScale(2f, 3f)
        platformMatrix.postScale(2f, 3f)

        customMatrix.mapVectors(customVectors)
        platformMatrix.mapVectors(platformVectors)

        assertFloatArraysEqual(platformVectors, customVectors, 0.001f)
    }

    @Test
    fun mapVectorsTwoArgsBehavesLikePlatformMatrix() {
        val srcVectors = floatArrayOf(1f, 1f, 0f, 10f, -5f, 0f)
        val customDstVectors = FloatArray(srcVectors.size)
        val platformDstVectors = FloatArray(srcVectors.size)

        customMatrix.postTranslate(100f, 200f)
        platformMatrix.postTranslate(100f, 200f)
        customMatrix.postRotate(45f)
        platformMatrix.postRotate(45f)

        customMatrix.mapVectors(customDstVectors, srcVectors)
        platformMatrix.mapVectors(platformDstVectors, srcVectors)

        assertFloatArraysEqual(platformDstVectors, customDstVectors, 0.001f)
    }

    @Test
    fun mapVectorsMultiArgBehavesLikePlatformMatrix() {
        val srcVectors = floatArrayOf(1f, 1f, 2f, 2f, 3f, 3f, 4f, 4f)
        val customDstVectors = FloatArray(srcVectors.size)
        val platformDstVectors = FloatArray(srcVectors.size)

        customMatrix.postTranslate(10f, 10f)
        platformMatrix.postTranslate(10f, 10f)
        customMatrix.postScale(2f, 2f)
        platformMatrix.postScale(2f, 2f)

        val srcIndex = 2
        val dstIndex = 4
        val vectorCount = 2

        customMatrix.mapVectors(customDstVectors, dstIndex, srcVectors, srcIndex, vectorCount)
        platformMatrix.mapVectors(platformDstVectors, dstIndex, srcVectors, srcIndex, vectorCount)

        assertFloatArraysEqual(platformDstVectors, customDstVectors, 0.001f)
    }

    private fun assertMatrixValuesEqual(
        custom: MutablePdfMatrix,
        platform: AndroidPlatformMatrix,
        delta: Float = 0.001f,
    ) {
        val customValues = custom.values
        val platformValues = FloatArray(9)

        platform.getValues(platformValues)

        assertFloatArraysEqual(platformValues, customValues, delta)
    }

    private fun assertFloatArraysEqual(
        expected: FloatArray,
        actual: FloatArray,
        delta: Float,
    ) {
        assertThat(actual).hasLength(expected.size)
        for (i in expected.indices) {
            assertThat(actual[i])
//                .withMessage("Value at index $i")
                .isWithin(delta)
                .of(expected[i])
        }
    }
}
