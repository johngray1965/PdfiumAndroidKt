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

private const val CACHE_SIZE = 64L

/**
 * A thread-safe, concurrent LRU cache for PDF page objects.
 * This base class holds the core Guava LoadingCache logic.
 */
abstract class PdfPageSuspendCacheBase<H : AutoCloseable>(
    dispatcher: CoroutineDispatcher,
    maxSize: Long = CACHE_SIZE,
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
