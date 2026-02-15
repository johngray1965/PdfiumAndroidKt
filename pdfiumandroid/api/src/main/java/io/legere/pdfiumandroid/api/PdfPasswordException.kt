package io.legere.pdfiumandroid.api

import java.io.IOException

/**
 * PdfPasswordException is thrown when a password is required to open a document
 */
class PdfPasswordException(
    msg: String? = null,
) : IOException(msg)
