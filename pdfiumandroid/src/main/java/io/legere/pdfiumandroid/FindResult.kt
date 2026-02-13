package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.unlocked.FindResultU
import java.io.Closeable

/**
 * Represents the result of a text search operation within a PDF page.
 *
 * This class wraps the native [FindResultU] object and provides thread-safe access to its methods
 * using the [wrapLock] mechanism. It allows for navigating through search results,
 * getting the current result index, and the total count of results.
 *
 * @property findResult The underlying unlocked native find result object.
 */
@Suppress("TooManyFunctions")
class FindResult(
    internal val findResult: FindResultU,
) : Closeable {
    /**
     * Finds the next occurrence of the search pattern.
     *
     * @return `true` if a next occurrence is found, `false` otherwise.
     */
    fun findNext(): Boolean =
        wrapLock {
            findResult.findNext()
        }

    /**
     * Finds the previous occurrence of the search pattern.
     *
     * @return `true` if a previous occurrence is found, `false` otherwise.
     */
    fun findPrev(): Boolean =
        wrapLock {
            findResult.findPrev()
        }

    /**
     * Gets the index of the currently found search result.
     *
     * @return The 0-based index of the current search result.
     */
    fun getSchResultIndex(): Int =
        wrapLock {
            findResult.getSchResultIndex()
        }

    /**
     * Gets the total count of search results found.
     *
     * @return The total number of occurrences of the search pattern.
     */
    fun getSchCount(): Int =
        wrapLock {
            findResult.getSchCount()
        }

    /**
     * Closes the find operation and releases associated native resources.
     * This method is called automatically when [close] is invoked.
     */
    fun closeFind() {
        wrapLock {
            findResult.closeFind()
        }
    }

    /**
     * Closes the find operation and releases all associated native resources.
     * This makes the [FindResult] object unusable after this call.
     */
    override fun close() {
        wrapLock {
            findResult.close()
        }
    }
}
