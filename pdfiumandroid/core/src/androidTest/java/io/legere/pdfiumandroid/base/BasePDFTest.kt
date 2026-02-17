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

package io.legere.pdfiumandroid.base

import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import java.io.File

@Suppress("unused")
open class BasePDFTest {
    // set to true to skip tests that are not implemented yet
    // set to false to force unimplemented tests to fail
    val notImplementedAssetValue = false

    val noResultRect = RectF(-1f, -1f, -1f, -1f)
    val noResultFloatArray = floatArrayOf(-1f, -1f, -1f, -1f)

    @After
    fun dumpCoverage() {
        try {
            // Use the target application's cache directory for a reliably writable path.
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
            val outputPath = targetContext.cacheDir.absolutePath + "/default.profraw"

//            NativeCore().dumpCoverageData(outputPath)
        } catch (e: Throwable) {
            // Log a warning if dumping fails, but do not fail the test suite.
            Log.w(BasePDFTest::class.simpleName, "Failed to dump coverage data", e)
        }
    }

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
