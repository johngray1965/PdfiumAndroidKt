# PdfiumAndroidKt

[![Android CI](https://github.com/johngray1965/PdfiumAndroidKt/actions/workflows/android.yml/badge.svg)](https://github.com/johngray1965/PdfiumAndroidKt/actions/workflows/android.yml)

A Pdfium Android library using the latest stable version Pdfium.  Written in Kotlin with coroutines to easily talk to the native code off the main thread.

Largely rewritten, but based on https://github.com/barteksc/PdfiumAndroid

This version give you two versions of the API, one that using suspend function, and one that doesn't (and that should work from Java).

This is a object-oriented version.   PfdiumCore is give you options to open up the PDF, all the methods on document are from PdfDocumment.   Likewise there's an PdfPage and PdfTextPage.   You get the Page objects from the PdfDocument, all the things that operation on the pages.   

For using suspend functions, use PdfiumCoreKt, and it'll return PdfDocummentK,  PdfDocummentK give PdfPageKt and PdfTextPageKt.   All the  <Blah>Kt classes have suspend functions, and will do there work on the dispathcer that's passed into PdfiumCoreKt when its created.
  
PdfDocument, PdfPage, PdfTextPage and Kt versions all uses Closable.

To use it, add the following app's build.gradle:
```
    implementation("io.legere:pdfiumandroid:1.0.13")
```
