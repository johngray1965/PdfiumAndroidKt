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

@file:Suppress("unused")

package io.legere.pdfiumandroid.arrow

import android.os.ParcelFileDescriptor
import arrow.core.Either
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.api.Config
import io.legere.pdfiumandroid.api.LockManager
import io.legere.pdfiumandroid.api.PdfiumSource
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU
import io.legere.pdfiumandroid.core.unlocked.PdfiumCoreU.Companion.lock
import kotlinx.coroutines.CoroutineDispatcher

/**
 * PdfiumCoreKtF is the main entry-point for access to the PDFium API.
 * @property dispatcher the [CoroutineDispatcher] to use for suspending calls
 * @constructor create a [PdfiumCoreKtF] from a [PdfiumCore]
 */
class PdfiumCoreKtF(
    private val dispatcher: CoroutineDispatcher,
    config: Config = Config(),
    private val coreInternal: PdfiumCoreU = PdfiumCoreU(config = config),
) {
    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(fd: ParcelFileDescriptor): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(fd), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        fd: ParcelFileDescriptor,
        password: String?,
    ): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(fd, password), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: ByteArray?): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(data), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: ByteArray?,
        password: String?,
    ): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(data, password), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(data: PdfiumSource): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(data), dispatcher)
        }

    /**
     * suspend version of [PdfiumCore.newDocument]
     */
    suspend fun newDocument(
        data: PdfiumSource,
        password: String?,
    ): Either<PdfiumKtFErrors, PdfDocumentKtF> =
        wrapEither(dispatcher) {
            PdfDocumentKtF(coreInternal.newDocument(data, password), dispatcher)
        }

    fun setLockManager(lockManager: LockManager) {
        lock = lockManager
    }
}
