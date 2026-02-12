package io.legere.pdfiumandroid

import io.legere.pdfiumandroid.unlocked.FindResultU
import java.io.Closeable

@Suppress("TooManyFunctions")
class FindResult(
    internal val findResult: FindResultU,
) : Closeable {
    fun findNext(): Boolean =
        wrapLock {
            findResult.findNext()
        }

    fun findPrev(): Boolean =
        wrapLock {
            findResult.findPrev()
        }

    fun getSchResultIndex(): Int =
        wrapLock {
            findResult.getSchResultIndex()
        }

    fun getSchCount(): Int =
        wrapLock {
            findResult.getSchCount()
        }

    fun closeFind() {
        wrapLock {
            findResult.closeFind()
        }
    }

    override fun close() {
        wrapLock {
            findResult.close()
        }
    }
}
