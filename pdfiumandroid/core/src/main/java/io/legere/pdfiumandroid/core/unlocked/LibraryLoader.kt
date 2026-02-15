package io.legere.pdfiumandroid.core.unlocked

interface LibraryLoader {
    fun load(libName: String)
}

internal object SystemLibraryLoader : LibraryLoader {
    override fun load(libName: String) {
        System.loadLibrary(libName)
    }
}
