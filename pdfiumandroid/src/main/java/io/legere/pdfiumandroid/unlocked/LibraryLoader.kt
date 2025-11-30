package io.legere.pdfiumandroid.unlocked

interface LibraryLoader {
    fun load(libName: String)
}

object SystemLibraryLoader : LibraryLoader {
    override fun load(libName: String) {
        System.loadLibrary(libName)
    }
}
