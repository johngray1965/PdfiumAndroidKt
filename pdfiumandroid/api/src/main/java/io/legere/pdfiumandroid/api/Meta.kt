package io.legere.pdfiumandroid.api

/**
 * Represents the metadata of a PDF document.
 *
 * @property title The document's title.
 * @property author The document's author.
 * @property subject The document's subject.
 * @property keywords Keywords associated with the document.
 * @property creator The application that created the original document.
 * @property producer The application that converted the original document to PDF.
 * @property creationDate The date and time the document was created.
 * @property modDate The date and time the document was last modified.
 */
data class Meta(
    var title: String? = null,
    var author: String? = null,
    var subject: String? = null,
    var keywords: String? = null,
    var creator: String? = null,
    var producer: String? = null,
    var creationDate: String? = null,
    var modDate: String? = null,
)
