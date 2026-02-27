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

package io.legere.pdfiumandroid.core.jni

/**
 * Factory interface for providing implementations of native PDFium contracts.
 * This allows for dependency injection and easier testing of classes that interact
 * with the native layer. Implementations of this factory are intended for **internal use only**
 * within the PdfiumAndroid library.
 */
interface NativeFactory {
    /**
     * Provides an instance of [NativeCoreContract] for core native operations.
     * @return An implementation of [NativeCoreContract].
     */
    fun getNativeCore(): NativeCoreContract

    /**
     * Provides an instance of [NativeDocumentContract] for native document operations.
     * @return An implementation of [NativeDocumentContract].
     */
    fun getNativeDocument(): NativeDocumentContract

    /**
     * Provides an instance of [NativePageContract] for native page operations.
     * @return An implementation of [NativePageContract].
     */
    fun getNativePage(): NativePageContract

    /**
     * Provides an instance of [NativeTextPageContract] for native text page operations.
     * @return An implementation of [NativeTextPageContract].
     */
    fun getNativeTextPage(): NativeTextPageContract

    /**
     * Provides an instance of [NativePageLinkContract] for native page link operations.
     * @return An implementation of [NativePageLinkContract].
     */
    fun getNativePageLink(): NativePageLinkContract

    /**
     * Provides an instance of [NativeFindResultContract] for native find result operations.
     * @return An implementation of [NativeFindResultContract].
     */
    fun getNativeFindResult(): NativeFindResultContract
}

val defaultNativeFactory =
    object : NativeFactory {
        override fun getNativeCore(): NativeCoreContract = NativeCore()

        override fun getNativeDocument(): NativeDocumentContract = NativeDocument()

        override fun getNativePage(): NativePageContract = NativePage()

        override fun getNativeTextPage(): NativeTextPageContract = NativeTextPage()

        override fun getNativePageLink(): NativePageLinkContract = NativePageLink()

        override fun getNativeFindResult(): NativeFindResultContract = NativeFindResult()
    }
