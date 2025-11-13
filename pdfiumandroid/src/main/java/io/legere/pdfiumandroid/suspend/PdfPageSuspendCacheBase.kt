package io.legere.pdfiumandroid.suspend

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
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

    protected abstract suspend fun openPageAndText(pageIndex: Int): H

    suspend fun get(pageIndex: Int): H {
        val deferred =
            cache.asMap().computeIfAbsent(pageIndex) { key ->
                scope.async {
                    openPageAndText(key)
                }
            }
        return deferred.await()
    }

    override fun close() {
        cache.invalidateAll()
    }
}
