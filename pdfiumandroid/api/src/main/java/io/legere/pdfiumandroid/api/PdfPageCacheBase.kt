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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.cache.RemovalListener

internal const val CACHE_SIZE = 64L

/**
 * A thread-safe, concurrent LRU cache for PDF page objects.
 * This base class holds the core Guava LoadingCache logic.
 */
abstract class PdfPageCacheBase<H : AutoCloseable>(
    val maxSize: Long = CACHE_SIZE,
) : AutoCloseable {
    private val removalListener =
        RemovalListener<Int, H> {
            it.value?.close()
        }

    private val pageCache: LoadingCache<Int, H> =
        CacheBuilder
            .newBuilder()
            .maximumSize(maxSize)
            .removalListener(removalListener)
            .build(
                CacheLoader.from { pageIndex ->
                    openPageAndText(pageIndex) ?: error("Page $pageIndex not found")
                },
            )

    /**
     * Abstract method to be implemented by subclasses to open a page and its text page.
     */
    protected abstract fun openPageAndText(pageIndex: Int): H?

    /**
     * Gets the page and text page holder from the cache, creating it if necessary.
     */
    fun get(pageIndex: Int): H = pageCache.get(pageIndex)

    /**
     * Closes all currently cached pages.
     */
    override fun close() {
        pageCache.invalidateAll()
    }
}
