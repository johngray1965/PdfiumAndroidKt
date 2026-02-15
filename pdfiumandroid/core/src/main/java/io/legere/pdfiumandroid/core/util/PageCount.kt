package io.legere.pdfiumandroid.core.util

/**
 * A data class used internally, potentially for counting pages related to a native pointer.
 *
 * @property pagePtr The native pointer to a PDF page.
 * @property count An integer count, possibly related to the number of references or similar.
 */
data class PageCount(
    val pagePtr: Long,
    var count: Int,
)
