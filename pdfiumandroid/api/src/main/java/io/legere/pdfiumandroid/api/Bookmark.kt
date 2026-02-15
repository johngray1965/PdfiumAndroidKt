package io.legere.pdfiumandroid.api

/**
 * Represents a bookmark (table of contents entry) within a PDF document.
 *
 * @property children A mutable list of child bookmarks, allowing for a hierarchical structure.
 * @property title The title of the bookmark.
 * @property pageIdx The 0-based page index that this bookmark points to.
 * @property mNativePtr The native pointer to the underlying FPDF_BOOKMARK object.
 */
data class Bookmark(
    val children: MutableList<Bookmark> = ArrayList(),
    var title: String? = null,
    var pageIdx: Long = 0,
    var mNativePtr: Long = 0,
)
