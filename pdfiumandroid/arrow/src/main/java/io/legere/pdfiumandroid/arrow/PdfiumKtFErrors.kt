package io.legere.pdfiumandroid.arrow

/**
 * A sealed class representing various error conditions that can occur within the PdfiumAndroidKtF library.
 * This allows for structured and exhaustive error handling using Arrow's [Either] type.
 */
sealed class PdfiumKtFErrors {
    /**
     * Represents a generic runtime exception that occurred during a PDFium operation.
     *
     * @property message A descriptive message about the runtime exception.
     */
    data class RuntimeException(
        val message: String,
    ) : PdfiumKtFErrors()

    /**
     * Indicates that an operation was attempted on a PDFium object that has already been closed.
     *
     * @property message A descriptive message indicating the object was already closed.
     */
    data class AlreadyClosed(
        val message: String,
    ) : PdfiumKtFErrors()

    /**
     * Represents an error where a constraint (e.g., an invalid argument or state) was violated.
     * This is a generic error for constraints that don't fit other specific error types.
     */
    data object ConstraintError : PdfiumKtFErrors()
}

/**
 * Converts a given [Throwable] into a [PdfiumKtFErrors] instance.
 *
 * This utility function inspects the type and message of the [Throwable] to provide
 * a more specific [PdfiumKtFErrors] if possible (e.g., [PdfiumKtFErrors.AlreadyClosed]),
 * otherwise it defaults to [PdfiumKtFErrors.RuntimeException].
 *
 * @param e The [Throwable] to convert.
 * @return A [PdfiumKtFErrors] representing the encountered exception.
 */
fun exceptionToPdfiumKtFError(e: Throwable): PdfiumKtFErrors =
    if (e is IllegalStateException && e.message?.contains("Already closed") == true) {
        PdfiumKtFErrors.AlreadyClosed(e.message ?: "Unknown error")
    } else {
        PdfiumKtFErrors.RuntimeException(e.message ?: "Unknown error")
    }
