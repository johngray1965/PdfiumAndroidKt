package io.legere.pdfiumandroid.suspend

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalListener
import io.legere.pdfiumandroid.util.PdfPageCacheBase
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
abstract class PdfPageSuspendCacheBase<TPage : AutoCloseable, TTextPage : AutoCloseable>(
    dispatcher: CoroutineDispatcher,
    maxSize: Long = CACHE_SIZE,
) : AutoCloseable {
    private val scope: CoroutineScope = CoroutineScope(dispatcher)

    private val cache: Cache<Int, Deferred<PdfPageCacheBase.PageHolder<TPage, TTextPage>>>

    init {
        val removalListener =
            RemovalListener<Int, Deferred<PdfPageCacheBase.PageHolder<TPage, TTextPage>>> { notification ->
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

    protected abstract suspend fun openPageAndText(pageIndex: Int): PdfPageCacheBase.PageHolder<TPage, TTextPage>

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun get(pageIndex: Int): PdfPageCacheBase.PageHolder<TPage, TTextPage> {
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
