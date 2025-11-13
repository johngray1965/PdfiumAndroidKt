# PdfiumAndroidKt

[![Android CI](https://github.com/johngray1965/PdfiumAndroidKt/actions/workflows/android.yml/badge.svg)](https://github.com/johngray1965/PdfiumAndroidKt/actions/workflows/android.yml)

A Pdfium Android library using the latest stable version Pdfium.  Written in Kotlin with coroutines to easily talk to the native code off the main thread.

Largely rewritten, but based on https://github.com/barteksc/PdfiumAndroid

This version give you three versions of the API, one that using suspend functions, one that use arrow (and suspend functions) and one plain API with nothing fancy (and that should work from Java).

This is a object-oriented version.   PfdiumCore gives you options to open up the PDF, all the methods on document are from PdfDocumment.   Likewise there's an PdfPage and PdfTextPage.   You get the Page objects from the PdfDocument, all the things that operation on the pages.   

For using suspend functions, use PdfiumCoreKt, and it'll return PdfDocummentK,  PdfDocummentK give PdfPageKt and PdfTextPageKt.   All the  <Blah>Kt classes have suspend functions, and will do there work on the dispatcher that's passed into PdfiumCoreKt when its created.

For using arrow functions, use PdfiumCoreKtF, and it'll return PdfDocummentKF,  PdfDocummentKF give PdfPageKtF and PdfTextPageKtF.   All the  <Blah>KtF classes have arrow functions, and will do there work on the dispatcher that's passed into PdfiumCoreKtF when its created.
  
PdfDocument, PdfPage, PdfTextPage and the Kt and KtF versions all uses Closable.

To use it, add the following in your app's build.gradle:
```
    implementation("io.legere:pdfiumandroid:1.0.35")
```

The arrow support is in a separate module.  To use it, add the following in your app's build.gradle:
```
    implementation("io.legere:pdfium-android-kt-arrow:1.0.35")
```
For more information on arrow-kt, see the https://arrow-kt.io/

## A few notes on performance.

Opening documents and pages (as well the text pages) are relatively expensive operations. You probably want to avoid, for instance, opening the page on demand.  Open the page and keep it open until you're done with it.

The bitmap rendering API supports RGB_565 format, but its slow.  The underlying pdfium APIs work with ARGB_888. The RGB_565 support has to allocate a buffer for ARGB_888, get the data, covert the data, release the buffer.  The ARGB_888 support writes directly to the bitmap without any buffer allocation or conversion.

Rendering directly to a Surface is fast, and doesn't require the memory overhead of bitmaps.

## What this project does

We provide Android bindings for Pdfium.  Pdfium is a library that Google produces.

We don't do user interfaces.   We are open to helping integrate this with your UI on contract basis.

We are open to Pull Requests (PRs), but only if they keep the to scope of providing Android binding for Pdfium.   And we don't accept PRs that customize the Pdfium libraries.