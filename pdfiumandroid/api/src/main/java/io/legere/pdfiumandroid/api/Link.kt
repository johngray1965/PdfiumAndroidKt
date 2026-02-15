package io.legere.pdfiumandroid.api

import android.graphics.RectF

/**
 * Represents a link (e.g., internal page link or URI link) found on a PDF page.
 *
 * @property bounds The bounding rectangle of the link on the page.
 * @property destPageIdx The 0-based destination page index if this is an internal link,
 * or `null` if it's a URI link.
 * @property uri The URI string if this is a web link, or `null` if it's an internal page link.
 */
class Link(
    val bounds: RectF,
    val destPageIdx: Int?,
    val uri: String?,
)
