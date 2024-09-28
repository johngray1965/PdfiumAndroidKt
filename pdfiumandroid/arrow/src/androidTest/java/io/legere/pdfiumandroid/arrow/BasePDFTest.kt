package io.legere.pdfiumandroid.arrow

import android.graphics.RectF
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry

@Suppress("unused")
open class BasePDFTest {
    // set to true to skip tests that are not implemented yet
    // set to false to force unimplemented tests to fail
    val notImplementedAssetValue = false

    val noResultRect = RectF(-1f, -1f, -1f, -1f)

    fun getPdfBytes(filename: String): ByteArray? {
        val appContext = InstrumentationRegistry.getInstrumentation().context
        val assetManager = appContext.assets
        try {
            val input = assetManager.open(filename)
            return input.readBytes()
        } catch (e: Exception) {
            Log.e(BasePDFTest::class.simpleName, "Ugh", e)
        }
        assetManager.close()
        return null
    }
}
