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

package io.legere.pdfiumandroid.api

import androidx.annotation.Keep

/**
 * Configuration class for the PdfiumAndroid library.
 *
 * @property logger The [io.legere.pdfiumandroid.api.LoggerInterface] implementation to use for
 * logging within the library.
 *                  Defaults to [io.legere.pdfiumandroid.api.DefaultLogger].
 * @property alreadyClosedBehavior Defines how the library reacts when an operation is attempted
 *                                 on a PDFium object that has already been closed.
 *                                 Defaults to [AlreadyClosedBehavior.EXCEPTION].
 */
@Keep
data class Config(
    val logger: LoggerInterface = DefaultLogger(),
    val alreadyClosedBehavior: AlreadyClosedBehavior = AlreadyClosedBehavior.EXCEPTION,
)

/**
 * Defines the behavior when an operation is attempted on an already closed PDFium object.
 */
@Keep
enum class AlreadyClosedBehavior {
    /** Throws an [IllegalStateException] when an operation is attempted on a closed object. */
    EXCEPTION,

    /** Ignores the operation when an operation is attempted on a closed object. */
    IGNORE,

    /** Logs a debug message when an operation is attempted on a closed object. */
    LOG,
}

var pdfiumConfig = Config()

/**
 * Handles the scenario where an operation is attempted on an already closed object.
 * The behavior is determined by [pdfiumConfig.alreadyClosedBehavior].
 *
 * @param isClosed A boolean indicating whether the object is currently closed.
 * @return `true` if the object is closed, `false` otherwise.
 * @throws IllegalStateException if [pdfiumConfig.alreadyClosedBehavior] is
 * [AlreadyClosedBehavior.EXCEPTION] and `isClosed` is `true`.
 */
fun handleAlreadyClosed(isClosed: Boolean): Boolean {
    if (isClosed) {
        when (pdfiumConfig.alreadyClosedBehavior) {
            AlreadyClosedBehavior.EXCEPTION -> {
                error("Already closed")
            }

            AlreadyClosedBehavior.LOG -> {
                pdfiumConfig.logger.d(
                    "PdfiumCore",
                    "Already closed",
                )
            }

            AlreadyClosedBehavior.IGNORE -> {
                // do nothing
            }
        }
    }
    return isClosed
}
