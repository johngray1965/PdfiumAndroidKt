package io.legere.pdfiumandroid

class NativeLib {

    /**
     * A native method that is implemented by the 'pdfiumandroid' native library,
     * which is packaged with this application.
     */
    companion object {
        // Used to load the 'pdfiumandroid' library on application startup.
        init {
            System.loadLibrary("pdfiumandroid")
        }
    }
}
