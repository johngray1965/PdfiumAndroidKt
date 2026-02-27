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

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * A thread-safe, concurrent LRU cache for PDF page objects.
 * This base class holds the core Guava LoadingCache logic.
 */
abstract class PdfPageSuspendCacheBase<H : AutoCloseable>(
    dispatcher: CoroutineDispatcher,
    val maxSize: Long = CACHE_SIZE,
) : AutoCloseable {
    private val scope: CoroutineScope = CoroutineScope(dispatcher)

    private val cache: Cache<Int, Deferred<H>>

    init {
        val removalListener =
            RemovalListener<Int, Deferred<H>> { notification ->
                notification.value?.let { deferred ->
                    if (deferred.isCompleted) {
                        scope.launch {
                            runCatching { deferred.await().close() }
                        }
                    } else {
                        deferred.cancel()
                    }
                }
            }
        cache =
            CacheBuilder
                .newBuilder()
                .maximumSize(maxSize)
                .removalListener(removalListener)
                .build()
    }

    protected abstract suspend fun openPageAndText(pageIndex: Int): H?

    suspend fun get(pageIndex: Int): H {
        val deferred =
            getDeferred(pageIndex)
        return deferred.await()
    }

    fun getDeferred(pageIndex: Int): Deferred<H> =
        cache.asMap().computeIfAbsent(pageIndex) { key ->
            scope.async {
                // we need open pageIndex, not key
                openPageAndText(pageIndex)
                    ?: error("Page pageIndex: $pageIndex, key $key not found")
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getIfPresent(pageIndex: Int): H? {
        val deferred = cache.getIfPresent(pageIndex)
        return if (deferred != null && deferred.isCompleted) {
            runCatching { deferred.getCompleted() }.getOrNull()
        } else {
            null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun peek(pageIndex: Int): H? {
        val deferred = cache.getIfPresent(pageIndex)
        return if (deferred != null && deferred.isCompleted) {
            runCatching { deferred.getCompleted() }.getOrNull()
        } else {
            getDeferred(pageIndex)
            null
        }
    }

    override fun close() {
        cache.invalidateAll()
    }
}
