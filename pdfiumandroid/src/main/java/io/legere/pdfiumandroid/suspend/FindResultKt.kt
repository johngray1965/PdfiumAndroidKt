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

package io.legere.pdfiumandroid.suspend

import io.legere.pdfiumandroid.core.unlocked.FindResultU
import kotlinx.coroutines.CoroutineDispatcher
import java.io.Closeable

/**
 * Suspending version of [io.legere.pdfiumandroid.FindResult] that provides asynchronous access
 * to text search results within a PDF page.
 *
 * This class wraps the native [io.legere.pdfiumandroid.core.unlocked.FindResultU] object and dispatches its operations
 * to a [CoroutineDispatcher] to ensure non-blocking execution.
 * It allows for navigating through search results, getting the current result index,
 * and the total count of results in a suspendable manner.
 *
 * @property findResult The underlying unlocked native find result object.
 * @property dispatcher The [CoroutineDispatcher] to use for suspending calls.
 */
@Suppress("unused")
class FindResultKt internal constructor(
    internal val findResult: FindResultU,
    private val dispatcher: CoroutineDispatcher,
) : Closeable {
    /**
     * Suspending version of [io.legere.pdfiumandroid.FindResult.findNext].
     * Finds the next occurrence of the search pattern asynchronously.
     *
     * @return `true` if a next occurrence is found, `false` otherwise.
     */
    suspend fun findNext(): Boolean =
        wrapSuspend(dispatcher) {
            findResult.findNext()
        }

    /**
     * Suspending version of [io.legere.pdfiumandroid.FindResult.findPrev].
     * Finds the previous occurrence of the search pattern asynchronously.
     *
     * @return `true` if a previous occurrence is found, `false` otherwise.
     */
    suspend fun findPrev(): Boolean =
        wrapSuspend(dispatcher) {
            findResult.findPrev()
        }

    /**
     * Suspending version of [io.legere.pdfiumandroid.FindResult.getSchResultIndex].
     * Gets the index of the currently found search result asynchronously.
     *
     * @return The 0-based index of the current search result.
     */
    suspend fun getSchResultIndex(): Int =
        wrapSuspend(dispatcher) {
            findResult.getSchResultIndex()
        }

    /**
     * Suspending version of [io.legere.pdfiumandroid.FindResult.getSchCount].
     * Gets the total count of search results found asynchronously.
     *
     * @return The total number of occurrences of the search pattern.
     */
    suspend fun getSchCount(): Int =
        wrapSuspend(dispatcher) {
            findResult.getSchCount()
        }

    /**
     * Suspending version of [io.legere.pdfiumandroid.FindResult.closeFind].
     * Closes the find operation and releases associated native resources asynchronously.
     * This method is called automatically when [close] is invoked.
     */
    suspend fun closeFind() {
        wrapSuspend(dispatcher) {
            findResult.closeFind()
        }
    }

    /**
     * Closes the [FindResultKt] object and releases all associated native resources.
     * This makes the object unusable after this call.
     */
    override fun close() {
        findResult.closeFind()
    }
}
