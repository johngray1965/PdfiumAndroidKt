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
abstract class PdfPageCacheBase<TPage : AutoCloseable, TTextPage : AutoCloseable>(
    maxSize: Long = CACHE_SIZE,
) : AutoCloseable {
    data class PageHolder<TPage : AutoCloseable, TTextPage : AutoCloseable>(
        val page: TPage,
        val textPage: TTextPage,
    ) : AutoCloseable {
        override fun close() {
            try {
                textPage.close()
            } finally {
                page.close()
            }
        }
    }

    private val removalListener =
        RemovalListener<Int, PageHolder<TPage, TTextPage>> {
            it.value?.close()
        }

    private val pageCache: LoadingCache<Int, PageHolder<TPage, TTextPage>> =
        CacheBuilder
            .newBuilder()
            .maximumSize(maxSize)
            .removalListener(removalListener)
            .build(
                CacheLoader.from { pageIndex ->
                    openPageAndText(pageIndex!!)
                },
            )

    /**
     * Abstract method to be implemented by subclasses to open a page and its text page.
     */
    protected abstract fun openPageAndText(pageIndex: Int): PageHolder<TPage, TTextPage>

    /**
     * Gets the page and text page holder from the cache, creating it if necessary.
     */
    fun get(pageIndex: Int): PageHolder<TPage, TTextPage> = pageCache.get(pageIndex)

    /**
     * Closes all currently cached pages.
     */
    override fun close() {
        pageCache.invalidateAll()
    }
}
