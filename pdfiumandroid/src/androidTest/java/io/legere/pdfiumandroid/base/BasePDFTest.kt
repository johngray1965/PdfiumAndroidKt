package io.legere.pdfiumandroid.base

import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

@Suppress("unused")
open class BasePDFTest {
    // set to true to skip tests that are not implemented yet
    // set to false to force unimplemented tests to fail
    val notImplementedAssetValue = false

    val noResultRect = RectF(-1f, -1f, -1f, -1f)
    val noResultFloatArray = floatArrayOf(-1f, -1f, -1f, -1f)

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

    fun getPdfPath(filename: String): ParcelFileDescriptor? {
        val appContext = InstrumentationRegistry.getInstrumentation().context
        val assetManager = appContext.assets
        try {
            val input = assetManager.open(filename)
            val path = appContext.filesDir.path + "/" + filename
            input.copyTo(appContext.openFileOutput(filename, 0))
            assetManager.close()
            return ParcelFileDescriptor
                .open(
                    File(path),
                    ParcelFileDescriptor.MODE_READ_ONLY,
                )
        } catch (e: Exception) {
            Log.e(BasePDFTest::class.simpleName, "Ugh", e)
        }
        assetManager.close()
        return null
    }
}
