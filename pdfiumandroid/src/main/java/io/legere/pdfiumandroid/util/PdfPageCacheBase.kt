package io.legere.pdfiumandroid.util

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.cache.RemovalListener

private const val CACHE_SIZE = 64L

/**
 * A thread-safe, concurrent LRU cache for PDF page objects.
 * This base class holds the core Guava LoadingCache logic.
 */
abstract class PdfPageCacheBase<H : AutoCloseable>(
    maxSize: Long = CACHE_SIZE,
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
