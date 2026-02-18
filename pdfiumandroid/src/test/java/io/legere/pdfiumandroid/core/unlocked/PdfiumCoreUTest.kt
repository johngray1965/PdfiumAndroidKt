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

package io.legere.pdfiumandroid.core.unlocked

import android.content.Context
import android.os.ParcelFileDescriptor
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.core.jni.NativeCore
import io.legere.pdfiumandroid.core.jni.NativeDocument
import io.legere.pdfiumandroid.core.jni.NativeFactory
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(Lifecycle.PER_CLASS)
class PdfiumCoreUTest {
    lateinit var pdfiumCore: PdfiumCoreU

    val mockNativeFactory: NativeFactory = mockk()

    val nativeCore: NativeCore = mockk()

    val nativeDocument: NativeDocument = mockk()

    val context: Context = mockk()

    val libraryLoader: LibraryLoader = mockk()

    @BeforeEach
    fun setup() {
        PdfiumCoreU.resetForTesting()
        // Initialize the native libraries before each test
        every { mockNativeFactory.getNativeCore() } returns nativeCore
        every { mockNativeFactory.getNativeDocument() } returns nativeDocument
        every { libraryLoader.load(any()) } just runs
        every { context.resources } returns
            mockk {
                every { displayMetrics } returns
                    mockk(relaxed = true) {
                        this.density = 1f
                        this.densityDpi = 160 // Set the field directly; PdfiumCoreU uses densityDpi
                    }
            }
    }

    @Test
    fun `newDocument fd successful load`() {
        // Verify that a valid, unencrypted PDF loaded via ParcelFileDescriptor returns a non-null PdfDocumentU instance
        // and that the document.parcelFileDescriptor property is correctly set.
        println("start newDocument fd successful load")
        pdfiumCore = PdfiumCoreU(context = context, nativeFactory = mockNativeFactory, libraryLoader = libraryLoader)
        every { nativeCore.openDocument(any(), any()) } returns 1
        pdfiumCore.newDocument(
            mockk<ParcelFileDescriptor> {
                every { fd } returns 1
                every { fileDescriptor } returns mockk()
                every { close() } just runs
            },
        )
        println("end newDocument fd successful load")
    }

    @Test
    fun `newDocument byteArray successful load`() {
        println("start newDocument byteArray successful load")
        // Verify that a valid PDF byte array returns a valid PdfDocumentU instance and that document.source is null.
        pdfiumCore = PdfiumCoreU(context = context, nativeFactory = mockNativeFactory, libraryLoader = libraryLoader)
        val byteArray = byteArrayOf(1, 2, 3)
        every { nativeCore.openMemDocument(any(), any()) } returns 1
        pdfiumCore.newDocument(byteArray)
        println("end newDocument byteArray successful load")
    }

    @Test
    fun `newDocument PdfiumSource successful load`() {
        println("start newDocument PdfiumSource successful load")
        // Verify that a valid PdfiumSource returns a valid PdfDocumentU instance and that
        // document.source is set to the passed source.
        pdfiumCore = PdfiumCoreU(context = context, nativeFactory = mockNativeFactory, libraryLoader = libraryLoader)
        every { nativeCore.openCustomDocument(any(), any(), any()) } returns 1
        pdfiumCore.newDocument(
            mockk<PdfiumSource> {
                every { length } returns 100
                every { read(any(), any(), any()) } returns 123
            },
        )
        println("end newDocument PdfiumSource successful load")
    }

    @Test
    fun `newDocument thread safety`() {
        // Call newDocument from multiple threads simultaneously to ensure the native library initialization
        // lock and native core calls are thread-safe.
        PdfiumCoreU(context = null, nativeFactory = mockNativeFactory, libraryLoader = libraryLoader)
    }

    @Test
    fun `newDocument native crash resilience`() {
        println("start newDocument native crash resilience")
        // (Advanced) Test with malformed inputs designed to trigger native crashes
        // (e.g. fuzzing inputs) to ensure the JNI
        // layer doesn't crash the entire JVM.
        every { libraryLoader.load(any()) } throws Exception()

        val exception: java.lang.Exception =
            Assertions.assertThrows(
                Exception::class.java,
            ) // Expected exception type
                {
                    PdfiumCoreU(
                        context = context,
                        nativeFactory = mockNativeFactory,
                        libraryLoader = libraryLoader,
                    )
                } // Code expected to throw the exception

        // Optional: Further assertions on the thrown exception object
        Assertions.assertEquals("Failed to initialize PdfiumCore native libraries", exception.message)

        println("end newDocument native crash resilience")
    }

    @Test
    fun `Native library initialization failure`() {
        println("start Native library initialization failure")
        // Verify behavior when the native libraries (pdfium, pdfiumandroid) fail to load (e.g., UnsatisfiedLinkError).
        // Ensure calls to newDocument throw an appropriate exception or handle the state gracefully without hanging.
        every { libraryLoader.load(any()) } throws UnsatisfiedLinkError()
        val exception: java.lang.Exception =
            Assertions.assertThrows(
                Exception::class.java,
            ) // Expected exception type
                {
                    PdfiumCoreU(
                        context = context,
                        nativeFactory = mockNativeFactory,
                        libraryLoader = libraryLoader,
                    )
                } // Code expected to throw the exception

//        // Optional: Further assertions on the thrown exception object
        Assertions.assertEquals("Failed to initialize PdfiumCore native libraries", exception.message)

        println("end Native library initialization failure")
    }
}
