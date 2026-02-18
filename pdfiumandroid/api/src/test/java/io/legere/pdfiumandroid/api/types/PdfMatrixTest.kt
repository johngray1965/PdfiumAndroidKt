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
    fun `reset sets matrix to identity`() {
        val matrix = PdfMatrix()
        matrix.setTranslate(10f, 20f)
        assertThat(matrix.isIdentity()).isFalse()
        
        matrix.reset()
        assertThat(matrix.isIdentity()).isTrue()
    }

    @Test
    fun `set copies values`() {
        val src = PdfMatrix()
        src.setTranslate(10f, 20f)
        val dst = PdfMatrix()
        dst.set(src)
        assertThat(dst).isEqualTo(src)
        assertThat(dst.values[MTRANS_X]).isEqualTo(10f)
    }

    @Test
    fun `setTranslate sets correct values`() {
        val matrix = PdfMatrix()
        matrix.setTranslate(10f, 20f)
        
        val values = matrix.values
        assertThat(values[MTRANS_X]).isEqualTo(10f)
        assertThat(values[MTRANS_Y]).isEqualTo(20f)
        assertThat(values[MSCALE_X]).isEqualTo(1f) // Scale should be 1
        assertThat(values[MSCALE_Y]).isEqualTo(1f)
    }

    @Test
    fun `setScale sets correct values`() {
        val matrix = PdfMatrix()
        matrix.setScale(2f, 3f)
        
        val values = matrix.values
        assertThat(values[MSCALE_X]).isEqualTo(2f)
        assertThat(values[MSCALE_Y]).isEqualTo(3f)
        assertThat(values[MTRANS_X]).isEqualTo(0f)
        assertThat(values[MTRANS_Y]).isEqualTo(0f)
    }

    @Test
    fun `setScale with pivot sets correct values`() {
        val matrix = PdfMatrix()
        matrix.setScale(2f, 3f, 10f, 10f)
        // px - sx*px = 10 - 2*10 = -10
        // py - sy*py = 10 - 3*10 = -20
        
        val values = matrix.values
        assertThat(values[MSCALE_X]).isEqualTo(2f)
        assertThat(values[MSCALE_Y]).isEqualTo(3f)
        assertThat(values[MTRANS_X]).isEqualTo(-10f)
        assertThat(values[MTRANS_Y]).isEqualTo(-20f)
    }

    @Test
    fun `setRotate sets correct values`() {
        val matrix = PdfMatrix()
        matrix.setRotate(90f)
        
        val values = matrix.values
        // cos(90) = 0, sin(90) = 1
        assertThat(values[MSCALE_X]).isWithin(0.0001f).of(0f)
        assertThat(values[MSKEW_X]).isWithin(0.0001f).of(-1f)
        assertThat(values[MSKEW_Y]).isWithin(0.0001f).of(1f)
        assertThat(values[MSCALE_Y]).isWithin(0.0001f).of(0f)
    }

    @Test
    fun `setSkew sets correct values`() {
        val matrix = PdfMatrix()
        matrix.setSkew(0.5f, 0.5f)
        
        val values = matrix.values
        assertThat(values[MSKEW_X]).isEqualTo(0.5f)
        assertThat(values[MSKEW_Y]).isEqualTo(0.5f)
        assertThat(values[MSCALE_X]).isEqualTo(1f)
    }

    @Test
    fun `setConcat multiplies matrices`() {
        // A: translate(10, 0)
        val a = PdfMatrix()
        a.setTranslate(10f, 0f)
        // B: scale(2, 1)
        val b = PdfMatrix()
        b.setScale(2f, 1f)
        
        // C = A * B (Translate then Scale? No, M * N means N applied then M if thinking transformations? 
        // Or if row vectors v' = v * M, then v * A * B means A then B.
        // Android Matrix is typically v' = M * v (column vectors).
        // M = A * B means apply B then A.
        // Let's check logic in setConcat.
        // setConcat(A, B) -> C = A * B
        
        // A = Translate(10, 0)
        // | 1 0 10 |
        // | 0 1 0  |
        // | 0 0 1  |
        
        // B = Scale(2, 1)
        // | 2 0 0 |
        // | 0 1 0 |
        // | 0 0 1 |
        
        // A * B
        // | 1 0 10 |   | 2 0 0 |   | 2 0 10 |
        // | 0 1 0  | * | 0 1 0 | = | 0 1 0  |
        // | 0 0 1  |   | 0 0 1 |   | 0 0 1  |
        // result: ScaleX=2, TransX=10.
        
        val c = PdfMatrix()
        c.setConcat(a, b)
        
        assertThat(c.values[MSCALE_X]).isEqualTo(2f)
        assertThat(c.values[MTRANS_X]).isEqualTo(10f)
    }

    @Test
    fun `preTranslate modifies matrix correctly`() {
        val matrix = PdfMatrix()
        matrix.setTranslate(10f, 10f) // Start at 10,10
        matrix.preTranslate(5f, 5f) // M' = M * T
        // M = Translate(10,10)
        // T = Translate(5,5)
        // M*T = Translate(15, 15)
        
        val values = matrix.values
        assertThat(values[MTRANS_X]).isEqualTo(15f)
        assertThat(values[MTRANS_Y]).isEqualTo(15f)
    }

    @Test
    fun `postTranslate modifies matrix correctly`() {
        val matrix = PdfMatrix()
        matrix.setTranslate(10f, 10f)
        // M' = T * M = Translate(5,5) * Translate(10,10) = Translate(15,15)
        matrix.postTranslate(5f, 5f)
        
        val values = matrix.values
        assertThat(values[MTRANS_X]).isEqualTo(15f)
        assertThat(values[MTRANS_Y]).isEqualTo(15f)
    }

    @Test
    fun `preScale modifies matrix correctly`() {
        val matrix = PdfMatrix()
        matrix.setTranslate(10f, 10f)
        matrix.preScale(2f, 2f) 
        // M' = M * S
        // | 1 0 10 |   | 2 0 0 |   | 2 0 10 |
        // | 0 1 10 | * | 0 2 0 | = | 0 2 10 |
        // | 0 0 1  |   | 0 0 1 |   | 0 0 1  |
        
        val values = matrix.values
        assertThat(values[MSCALE_X]).isEqualTo(2f)
        assertThat(values[MSCALE_Y]).isEqualTo(2f)
        assertThat(values[MTRANS_X]).isEqualTo(10f)
        assertThat(values[MTRANS_Y]).isEqualTo(10f)
    }

    @Test
    fun `postScale modifies matrix correctly`() {
        val matrix = PdfMatrix()
        matrix.setTranslate(10f, 10f)
        matrix.postScale(2f, 2f)
        // M' = S * M
        // | 2 0 0 |   | 1 0 10 |   | 2 0 20 |
        // | 0 2 0 | * | 0 1 10 | = | 0 2 20 |
        // | 0 0 1 |   | 0 0 1  |   | 0 0 1  |
        
        val values = matrix.values
        assertThat(values[MSCALE_X]).isEqualTo(2f)
        assertThat(values[MSCALE_Y]).isEqualTo(2f)
        assertThat(values[MTRANS_X]).isEqualTo(20f)
        assertThat(values[MTRANS_Y]).isEqualTo(20f)
    }
    
    @Test
    fun `preRotate modifies matrix correctly`() {
        val matrix = PdfMatrix()
        matrix.setTranslate(10f, 0f)
        matrix.preRotate(90f)
        // M' = M * R
        // | 1 0 10 |   | 0 -1 0 |   | 0 -1 10 |
        // | 0 1 0  | * | 1 0  0 | = | 1 0  0  |
        // | 0 0 1  |   | 0 0  1 |   | 0 0  1  |
        
        val values = matrix.values
        assertThat(values[MSCALE_X]).isWithin(0.0001f).of(0f)
        assertThat(values[MSKEW_X]).isWithin(0.0001f).of(-1f)
        // No change to translation because rotation is at origin (relative to current matrix space?)
        assertThat(values[MTRANS_X]).isWithin(0.0001f).of(10f)
    }

    @Test
    fun `postRotate modifies matrix correctly`() {
        val matrix = PdfMatrix()
        matrix.setTranslate(10f, 0f)
        matrix.postRotate(90f)
        // M' = R * M
        // | 0 -1 0 |   | 1 0 10 |   | 0 -1 0 |
        // | 1 0  0 | * | 0 1 0  | = | 1 0 10 |
        // | 0 0  1 |   | 0 0 1  |   | 0 0 1  |
        // Wait:
        // R row 0: 0, -1, 0. dot col 2 of M (10, 0, 1) = 0*10 + -1*0 + 0*1 = 0.
        // R row 1: 1, 0, 0. dot col 2 of M (10, 0, 1) = 1*10 + 0*0 + 0*1 = 10.
        
        val values = matrix.values
        assertThat(values[MTRANS_X]).isWithin(0.0001f).of(0f)
        assertThat(values[MTRANS_Y]).isWithin(0.0001f).of(10f)
    }
    
    @Test
    fun `isIdentity checks all fields`() {
        val matrix = PdfMatrix()
        assertThat(matrix.isIdentity()).isTrue()
        
        matrix.values[MPERSP_0] = 0.1f
        assertThat(matrix.isIdentity()).isFalse()
    }
    
    @Test
    fun `isAffine checks perspective fields`() {
        val matrix = PdfMatrix()
        assertThat(matrix.isAffine()).isTrue()
        
        matrix.values[MPERSP_0] = 0.1f
        assertThat(matrix.isAffine()).isFalse()

        matrix.reset()
        matrix.values[MPERSP_2] = 0.5f
        assertThat(matrix.isAffine()).isFalse()
    }
}
