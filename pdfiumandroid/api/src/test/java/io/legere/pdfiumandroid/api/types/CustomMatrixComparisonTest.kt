package io.legere.pdfiumandroid.api.types

import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
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
    private lateinit var mutableMatrix: MutablePdfMatrix
    private lateinit var pdfMatrix: PdfMatrix
    private lateinit var platformMatrix: AndroidPlatformMatrix

    @Before
    fun setup() {
        pdfMatrix = PdfMatrix()
        mutableMatrix = MutablePdfMatrix()
        platformMatrix = AndroidPlatformMatrix()
    }

    @Test
    fun resetBehavesLikePlatformMatrix() {
        // Modify both matrices first to ensure reset has an effect
        mutableMatrix.postTranslate(10f, 20f)
        platformMatrix.postTranslate(10f, 20f)

        mutableMatrix.reset()
        platformMatrix.reset()

        val result = pdfMatrix.postTranslate(10f, 20f).reset()

        assertThat(mutableMatrix.isIdentity()).isTrue()
        assertThat(result.isIdentity()).isTrue()
        assertThat(platformMatrix.isIdentity).isTrue()
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun setTranslateBehavesLikePlatformMatrix() {
        val dx = 100f
        val dy = 200f

        mutableMatrix.setTranslate(dx, dy)
        platformMatrix.setTranslate(dx, dy)
        val result = pdfMatrix.setTranslate(dx, dy)

        assertThat(mutableMatrix.isIdentity()).isFalse()
        assertThat(result.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun setScaleBehavesLikePlatformMatrix() {
        val sx = 2f
        val sy = 0.5f

        mutableMatrix.setScale(sx, sy)
        platformMatrix.setScale(sx, sy)
        val result = pdfMatrix.setScale(sx, sy)

        assertThat(mutableMatrix.isIdentity()).isFalse()
        assertThat(result.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun setRotateBehavesLikePlatformMatrix() {
        val degrees = 45f

        mutableMatrix.setRotate(degrees)
        platformMatrix.setRotate(degrees)
        val result = pdfMatrix.setRotate(degrees)

        assertThat(mutableMatrix.isIdentity()).isFalse()
        assertThat(result.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun setSkewBehavesLikePlatformMatrix() {
        val kx = 2f
        val ky = 3f

        mutableMatrix.setSkew(kx, ky)
        platformMatrix.setSkew(kx, ky)
        val result = pdfMatrix.setSkew(kx, ky)

        assertThat(mutableMatrix.isIdentity()).isFalse()
        assertThat(result.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun postTranslateBehavesLikePlatformMatrix() {
        val dx = 10f
        val dy = 20f

        mutableMatrix.postTranslate(dx, dy)
        platformMatrix.postTranslate(dx, dy)
        var result = pdfMatrix.postTranslate(dx, dy)

        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)

        // Chained operations
        mutableMatrix.postTranslate(5f, 5f)
        platformMatrix.postTranslate(5f, 5f)
        result = result.postTranslate(5f, 5f)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun postScaleBehavesLikePlatformMatrix() {
        val sx = 2f
        val sy = 3f

        mutableMatrix.postScale(sx, sy)
        platformMatrix.postScale(sx, sy)
        var result = pdfMatrix.postScale(sx, sy)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)

        // Chained operations
        mutableMatrix.postScale(0.5f, 0.5f)
        platformMatrix.postScale(0.5f, 0.5f)
        result = result.postScale(0.5f, 0.5f)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun postRotateBehavesLikePlatformMatrix() {
        val degrees = 30f

        mutableMatrix.postRotate(degrees)
        platformMatrix.postRotate(degrees)
        var result = pdfMatrix.postRotate(degrees)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)

        // Chained operations
        mutableMatrix.postRotate(60f)
        platformMatrix.postRotate(60f)
        result = result.postRotate(60f)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun postSkewBehavesLikePlatformMatrix() {
        val kx = 2f
        val ky = 3f

        mutableMatrix.postSkew(kx, ky)
        platformMatrix.postSkew(kx, ky)
        var result = pdfMatrix.postSkew(kx, ky)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)

        // Chained operations
        mutableMatrix.postSkew(0.5f, 0.5f)
        platformMatrix.postSkew(0.5f, 0.5f)
        result = result.postSkew(0.5f, 0.5f)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun preTranslateBehavesLikePlatformMatrix() {
        val dx = 10f
        val dy = 20f

        mutableMatrix.preTranslate(dx, dy)
        platformMatrix.preTranslate(dx, dy)

        var result = pdfMatrix.preTranslate(dx, dy)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)

        // Chained operations
        mutableMatrix.preTranslate(5f, 5f)
        platformMatrix.preTranslate(5f, 5f)
        result = result.preTranslate(5f, 5f)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun preScaleBehavesLikePlatformMatrix() {
        val sx = 2f
        val sy = 3f

        mutableMatrix.preScale(sx, sy)
        platformMatrix.preScale(sx, sy)
        var result = pdfMatrix.preScale(sx, sy)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)

        // Chained operations
        mutableMatrix.preScale(0.5f, 0.5f)
        platformMatrix.preScale(0.5f, 0.5f)
        result = result.preScale(0.5f, 0.5f)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun preRotateBehavesLikePlatformMatrix() {
        val degrees = 30f

        mutableMatrix.preRotate(degrees)
        platformMatrix.preRotate(degrees)
        var result = pdfMatrix.preRotate(degrees)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)

        // Chained operations
        mutableMatrix.preRotate(60f)
        platformMatrix.preRotate(60f)
        result = result.preRotate(60f)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun preSkewBehavesLikePlatformMatrix() {
        val kx = 2f
        val ky = 3f

        mutableMatrix.preSkew(kx, ky)
        platformMatrix.preSkew(kx, ky)
        var result = pdfMatrix.preSkew(kx, ky)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)

        // Chained operations
        mutableMatrix.preSkew(0.5f, 0.5f)
        platformMatrix.preSkew(0.5f, 0.5f)
        result = result.preSkew(0.5f, 0.5f)
        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun postConcatBehavesLikePlatformMatrix() {
        val otherCustom = MutablePdfMatrix()
        val otherPlatform = AndroidPlatformMatrix()

        otherCustom.setTranslate(10f, 20f)
        otherPlatform.setTranslate(10f, 20f)

        mutableMatrix.setScale(2f, 2f)
        platformMatrix.setScale(2f, 2f)
        var result = pdfMatrix.setScale(2f, 2f)

        mutableMatrix.postConcat(otherCustom)
        platformMatrix.postConcat(otherPlatform)
        result = result.postConcat(otherCustom)

        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun preConcatBehavesLikePlatformMatrix() {
        val otherCustom = MutablePdfMatrix()
        val otherPlatform = AndroidPlatformMatrix()

        otherCustom.setTranslate(10f, 20f)
        otherPlatform.setTranslate(10f, 20f)

        mutableMatrix.setScale(2f, 2f)
        platformMatrix.setScale(2f, 2f)
        var result = pdfMatrix.setScale(2f, 2f)

        mutableMatrix.preConcat(otherCustom)
        platformMatrix.preConcat(otherPlatform)
        result = result.concat(otherCustom)

        assertMatrixValuesEqual(mutableMatrix, platformMatrix)
        assertMatrixValuesEqual(result, platformMatrix)
    }

    @Test
    fun invertBehavesLikePlatformMatrix() {
        mutableMatrix.setScale(2f, 2f)
        mutableMatrix.postTranslate(10f, 20f)

        platformMatrix.setScale(2f, 2f)
        platformMatrix.postTranslate(10f, 20f)

        val result = pdfMatrix.setScale(2f, 2f).postTranslate(10f, 20f)

        val customInverse = MutablePdfMatrix()
        val platformInverse = AndroidPlatformMatrix()

        val customResult = mutableMatrix.invert(customInverse)
        val platformResult = platformMatrix.invert(platformInverse)
        val result2 = result.invert()

        println("customInverse: ${customInverse.values.contentToString()}")
        println("result2: ${result2?.values.contentToString()}")

        assertThat(customResult).isEqualTo(platformResult)
        assertMatrixValuesEqual(customInverse, platformInverse)
        assertMatrixValuesEqual(result2!!, platformInverse)
    }

    @Test
    fun invertBehavesLikePlatformMatrix2() {
        val expected =
            floatArrayOf(1.0194546f, 0.0f, -20.997845f, 0.0f, 1.4150456f, -4.237648f, 0.0f, 0.0f, 1.0f)
        mutableMatrix.set(
            floatArrayOf(
                1.0194546f,
                0.0f,
                10.449388f,
                0.0f,
                1.4150456f,
                -23.396986f,
                0.0f,
                0.0f,
                1.0f,
            ),
        )
        mutableMatrix.preTranslate(-30.847115f, 13.539732f)

        assertThat(mutableMatrix.values).isEqualTo(expected)
    }

    @Test
    fun isIdentityBehavesLikePlatformMatrix() {
        assertThat(mutableMatrix.isIdentity()).isTrue()
        assertThat(mutableMatrix.isIdentity()).isTrue()
        assertThat(platformMatrix.isIdentity).isTrue()

        mutableMatrix.postTranslate(1f, 0f)
        platformMatrix.postTranslate(1f, 0f)
        val result = pdfMatrix.postTranslate(1f, 0f)

        assertThat(mutableMatrix.isIdentity()).isFalse()
        assertThat(result.isIdentity()).isFalse()
        assertThat(platformMatrix.isIdentity).isFalse()
    }

    @Test
    fun isAffineBehavesLikePlatformMatrix() {
        assertThat(mutableMatrix.isAffine()).isTrue() // Identity is affine
        assertThat(platformMatrix.isAffine).isTrue()

        mutableMatrix.postScale(2f, 2f)
        platformMatrix.postScale(2f, 2f)
        val result = pdfMatrix.postScale(2f, 2f)
        assertThat(mutableMatrix.isAffine()).isTrue()
        assertThat(result.isAffine()).isTrue()
        assertThat(platformMatrix.isAffine).isTrue()

        mutableMatrix.set(
            PdfMatrix(
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
        assertThat(mutableMatrix.isAffine()).isFalse()
        assertThat(platformMatrix.isAffine).isFalse()
    }

    @Test
    fun mapPointsSingleArgBehavesLikePlatformMatrix() {
        val points = floatArrayOf(0f, 0f, 10f, 10f, 5f, 5f)
        val customPoints = points.copyOf()
        val customPoints2 = points.copyOf()
        val platformPoints = points.copyOf()

        mutableMatrix.postTranslate(10f, 20f)
        platformMatrix.postTranslate(10f, 20f)
        val result = pdfMatrix.postTranslate(10f, 20f)

        mutableMatrix.mapPoints(customPoints)
        platformMatrix.mapPoints(platformPoints)
        result.mapPoints(customPoints2)

        assertFloatArraysEqual(platformPoints, customPoints, 0.001f)
        assertFloatArraysEqual(platformPoints, customPoints2, 0.001f)
    }

    @Test
    fun mapPointsTwoArgsBehavesLikePlatformMatrix() {
        val srcPoints = floatArrayOf(0f, 0f, 10f, 10f, 5f, 5f)
        val customDstPoints = FloatArray(srcPoints.size)
        val customDstPoints2 = FloatArray(srcPoints.size)
        val platformDstPoints = FloatArray(srcPoints.size)

        mutableMatrix.postScale(2f, 3f)
        platformMatrix.postScale(2f, 3f)
        val result = pdfMatrix.postScale(2f, 3f)

        mutableMatrix.mapPoints(customDstPoints, srcPoints)
        platformMatrix.mapPoints(platformDstPoints, srcPoints)
        result.mapPoints(customDstPoints2, srcPoints)

        assertFloatArraysEqual(platformDstPoints, customDstPoints, 0.001f)
        assertFloatArraysEqual(platformDstPoints, customDstPoints2, 0.001f)
    }

    @Test
    fun mapPointsMultiArgBehavesLikePlatformMatrix() {
        val srcPoints = floatArrayOf(0f, 0f, 1f, 1f, 2f, 2f, 3f, 3f)
        val customDstPoints = FloatArray(srcPoints.size)
        val customDstPoints2 = FloatArray(srcPoints.size)
        val platformDstPoints = FloatArray(srcPoints.size)

        mutableMatrix.postTranslate(10f, 10f)
        platformMatrix.postTranslate(10f, 10f)
        val result = pdfMatrix.postTranslate(10f, 10f)

        val srcIndex = 2
        val dstIndex = 4
        val pointCount = 2

        mutableMatrix.mapPoints(customDstPoints, dstIndex, srcPoints, srcIndex, pointCount)
        result.mapPoints(customDstPoints2, dstIndex, srcPoints, srcIndex, pointCount)
        platformMatrix.mapPoints(platformDstPoints, dstIndex, srcPoints, srcIndex, pointCount)

        assertFloatArraysEqual(platformDstPoints, customDstPoints, 0.001f)
        assertFloatArraysEqual(platformDstPoints, customDstPoints2, 0.001f)
    }

    @Test
    fun mapRadiusBehavesLikePlatformMatrix() {
        val radius = 10f

        mutableMatrix.postScale(2f, 3f)
        platformMatrix.postScale(2f, 3f)
        var result = pdfMatrix.postScale(2f, 3f)

        val customMappedRadius = mutableMatrix.mapRadius(radius)
        val customMappedRadius2 = result.mapRadius(radius)
        val platformMappedRadius = platformMatrix.mapRadius(radius)

        assertThat(customMappedRadius).isWithin(0.001f).of(platformMappedRadius)
        assertThat(customMappedRadius2).isWithin(0.001f).of(platformMappedRadius)

        // With rotation
        mutableMatrix.reset()
        platformMatrix.reset()
        result = result.reset()
        mutableMatrix.postRotate(90f)
        result = result.postRotate(90f)
        platformMatrix.postRotate(90f)
        mutableMatrix.postScale(2f, 3f)
        platformMatrix.postScale(2f, 3f)
        result = result.postScale(2f, 3f)

        val customMappedRadiusRotated = mutableMatrix.mapRadius(radius)
        val customMappedRadiusRotated2 = result.mapRadius(radius)
        val platformMappedRadiusRotated = platformMatrix.mapRadius(radius)

        assertThat(customMappedRadiusRotated).isWithin(0.001f).of(platformMappedRadiusRotated)
        assertThat(customMappedRadiusRotated2).isWithin(0.001f).of(platformMappedRadiusRotated)
    }

    @Test
    fun mapRectSingleArgBehavesLikePlatformMatrix() {
        val rect = RectF(0f, 0f, 100f, 200f)
        val customRect = PdfRectF(0f, 0f, 100f, 200f)
        val customRect2 = PdfRectF(0f, 0f, 100f, 200f)
        val platformRect = RectF(rect)

        mutableMatrix.postTranslate(10f, 20f)
        platformMatrix.postTranslate(10f, 20f)
        mutableMatrix.postScale(0.5f, 2f)
        platformMatrix.postScale(0.5f, 2f)
        val result = pdfMatrix.postTranslate(10f, 20f).postScale(0.5f, 2f)

        val customResult = mutableMatrix.mapRect(customRect)
        val customResult2 = result.mapRect(customRect2)
        platformMatrix.mapRect(platformRect)

        assertThat(customResult.left).isWithin(0.001f).of(platformRect.left)
        assertThat(customResult.top).isWithin(0.001f).of(platformRect.top)
        assertThat(customResult.right).isWithin(0.001f).of(platformRect.right)
        assertThat(customResult.bottom).isWithin(0.001f).of(platformRect.bottom)
        assertThat(customResult2.left).isWithin(0.001f).of(platformRect.left)
        assertThat(customResult2.top).isWithin(0.001f).of(platformRect.top)
        assertThat(customResult2.right).isWithin(0.001f).of(platformRect.right)
        assertThat(customResult2.bottom).isWithin(0.001f).of(platformRect.bottom)
    }

    @Test
    fun mapRectTwoArgsBehavesLikePlatformMatrix() {
        val srcRect = RectF(0f, 0f, 100f, 200f)
        val customDstRect = MutablePdfRectF()
        val customDstRect2 = MutablePdfRectF()
        val platformDstRect = RectF()

        mutableMatrix.postTranslate(10f, 20f)
        platformMatrix.postTranslate(10f, 20f)
        mutableMatrix.postRotate(90f)
        platformMatrix.postRotate(90f)
        val result = pdfMatrix.postTranslate(10f, 20f).postRotate(90f)

        mutableMatrix.mapRect(customDstRect, srcRect.toPdfRectF())
        result.mapRect(customDstRect2, srcRect.toPdfRectF())
        platformMatrix.mapRect(platformDstRect, srcRect)

//        assertThat(customResult).isEqualTo(platformResult)
        assertThat(customDstRect.left).isWithin(0.001f).of(platformDstRect.left)
        assertThat(customDstRect.top).isWithin(0.001f).of(platformDstRect.top)
        assertThat(customDstRect.right).isWithin(0.001f).of(platformDstRect.right)
        assertThat(customDstRect.bottom).isWithin(0.001f).of(platformDstRect.bottom)
        assertThat(customDstRect2.left).isWithin(0.001f).of(platformDstRect.left)
        assertThat(customDstRect2.top).isWithin(0.001f).of(platformDstRect.top)
        assertThat(customDstRect2.right).isWithin(0.001f).of(platformDstRect.right)
        assertThat(customDstRect2.bottom).isWithin(0.001f).of(platformDstRect.bottom)
    }

    @Test
    fun mapVectorsSingleArgBehavesLikePlatformMatrix() {
        val vectors = floatArrayOf(1f, 1f, 0f, 10f, -5f, 0f)
        val customVectors = vectors.copyOf()
        val customVectors2 = vectors.copyOf()
        val platformVectors = vectors.copyOf()

        mutableMatrix.postTranslate(100f, 200f) // Translation should be ignored by mapVectors
        platformMatrix.postTranslate(100f, 200f)
        mutableMatrix.postScale(2f, 3f)
        platformMatrix.postScale(2f, 3f)
        val result = pdfMatrix.postTranslate(100f, 200f).postScale(2f, 3f)

        mutableMatrix.mapVectors(customVectors)
        result.mapVectors(customVectors2)
        platformMatrix.mapVectors(platformVectors)

        assertFloatArraysEqual(platformVectors, customVectors, 0.001f)
        assertFloatArraysEqual(platformVectors, customVectors2, 0.001f)
    }

    @Test
    fun mapVectorsTwoArgsBehavesLikePlatformMatrix() {
        val srcVectors = floatArrayOf(1f, 1f, 0f, 10f, -5f, 0f)
        val customDstVectors = FloatArray(srcVectors.size)
        val customDstVectors2 = FloatArray(srcVectors.size)
        val platformDstVectors = FloatArray(srcVectors.size)

        mutableMatrix.postTranslate(100f, 200f)
        platformMatrix.postTranslate(100f, 200f)
        mutableMatrix.postRotate(45f)
        platformMatrix.postRotate(45f)
        val result = pdfMatrix.postTranslate(100f, 200f).postRotate(45f)

        mutableMatrix.mapVectors(customDstVectors, srcVectors)
        result.mapVectors(customDstVectors2, srcVectors)
        platformMatrix.mapVectors(platformDstVectors, srcVectors)

        assertFloatArraysEqual(platformDstVectors, customDstVectors, 0.001f)
        assertFloatArraysEqual(platformDstVectors, customDstVectors2, 0.001f)
    }

    @Test
    fun mapVectorsMultiArgBehavesLikePlatformMatrix() {
        val srcVectors = floatArrayOf(1f, 1f, 2f, 2f, 3f, 3f, 4f, 4f)
        val customDstVectors = FloatArray(srcVectors.size)
        val customDstVectors2 = FloatArray(srcVectors.size)
        val platformDstVectors = FloatArray(srcVectors.size)

        mutableMatrix.postTranslate(10f, 10f)
        platformMatrix.postTranslate(10f, 10f)
        mutableMatrix.postScale(2f, 2f)
        platformMatrix.postScale(2f, 2f)
        val result = pdfMatrix.postTranslate(10f, 10f).postScale(2f, 2f)

        val srcIndex = 2
        val dstIndex = 4
        val vectorCount = 2

        mutableMatrix.mapVectors(customDstVectors, dstIndex, srcVectors, srcIndex, vectorCount)
        result.mapVectors(customDstVectors2, dstIndex, srcVectors, srcIndex, vectorCount)
        platformMatrix.mapVectors(platformDstVectors, dstIndex, srcVectors, srcIndex, vectorCount)

        assertFloatArraysEqual(platformDstVectors, customDstVectors, 0.001f)
        assertFloatArraysEqual(platformDstVectors, customDstVectors2, 0.001f)
    }

    @Test
    fun conversions() {
        val result = pdfMatrix.postTranslate(10f, 10f).postScale(2f, 2f)
        val matrix = result.toMatrix()
        val pdfMatrixConverted = matrix.toPdfMatrix()
        val mutablePdfMatrix = matrix.toMutablePdfMatrix()

        val platformValues = FloatArray(9)

        matrix.getValues(platformValues)

        assertMatrixValuesEqual(result, matrix)
        assertMatrixValuesEqual(pdfMatrixConverted, matrix)
        assertMatrixValuesEqual(mutablePdfMatrix, matrix)
    }

    @Test
    fun pointFConversions() { // Note testing these function here because are using robolectric
        val result = PdfPointF(10f, 10f)
        val point = result.toPointF()
        val pdfPointF = point.toPdfPointF()
        val mutablePdfPointF = point.toMutablePdfPointF()

        assertThat(result).isEqualTo(pdfPointF)
        assertThat(result.toMutable()).isEqualTo(mutablePdfPointF)
    }

    @Test
    fun pointConversions() { // Note testing these function here because are using robolectric
        val result = PdfPoint(10, 10)
        val point = result.toPoint()
        val pdfPoint = point.toPdfPoint()
        val mutablePdfPoint = point.toMutablePdfPoint()

        assertThat(result).isEqualTo(pdfPoint)
        assertThat(result.toMutable()).isEqualTo(mutablePdfPoint)
    }

    @Test
    fun rectConversions() { // Note testing these function here because are using robolectric
        val result = PdfRect(10, 10, 100, 100)
        val rect = result.toRect()
        val pdfRect = rect.toPdfRect()
        val mutablepdfRectt = rect.toMutablePdfRect()

        assertThat(result).isEqualTo(pdfRect)
        assertThat(result.toMutable()).isEqualTo(mutablepdfRectt)

        val rectF = result.toPdfRectF()
        assertThat(rectF.left).isEqualTo(10f)
        assertThat(rectF.top).isEqualTo(10f)
        assertThat(rectF.right).isEqualTo(100f)
        assertThat(rectF.bottom).isEqualTo(100f)
    }

    @Test
    fun rectFConversions() { // Note testing these function here because are using robolectric
        val result = PdfRectF(10f, 10f, 100f, 100f)
        val rect = result.toRectF()
        val pdfRect = rect.toPdfRectF()
        val mutablepdfRectt = rect.toMutablePdfRectF()

        assertThat(result).isEqualTo(pdfRect)
        assertThat(result.toMutable()).isEqualTo(mutablepdfRectt)

        val rectOut1 = result.toPdfRect()
        assertThat(rectOut1.left).isEqualTo(10)
        assertThat(rectOut1.top).isEqualTo(10)
        assertThat(rectOut1.right).isEqualTo(100)
        assertThat(rectOut1.bottom).isEqualTo(100)

        val rectOut2 = rect.toPdfRect()
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

        assertFloatArraysEqual(platformValues, customValues, delta)
    }

    private fun assertFloatArraysEqual(
        expected: FloatArray,
        actual: FloatArray,
        delta: Float,
    ) {
        assertThat(actual).hasLength(expected.size)
        for (i in expected.indices) {
            assertWithMessage("Value at index $i, expected: ${expected.contentToString()}, actual: ${actual.contentToString()}")
                .that(actual[i])
                .isWithin(delta)
                .of(expected[i])
        }
    }
}
