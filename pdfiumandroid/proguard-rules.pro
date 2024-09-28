# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class io.legere.pdfiumandroid.** { *; }

-keep interface io.legere.pdfiumandroid.** { public *; }
-keep class * extends io.legere.pdfiumandroid.LoggerInterface { *; }
-dontwarn java.lang.invoke.StringConcatFactory
-keepclasseswithmembernames class io.legere.pdfiumandroid.** {
     public <methods>;
}

-keep class io.legere.pdfiumandroid.suspend.PdfDocumentKt { *; }
-keepclassmembers public class io.legere.pdfiumandroid.suspend.PdfDocumentKt {
    public <init>(...);
}
-keep class io.legere.pdfiumandroid.suspend.PdfPageKt { *; }
-keepclassmembers public class io.legere.pdfiumandroid.suspend.PdfPageKt {
    public <init>(...);
}
-keep class io.legere.pdfiumandroid.suspend.PdfTextPageKt { *; }
-keepclassmembers public class io.legere.pdfiumandroid.suspend.PdfTextPageKt {
    public <init>(...);
}
-keep class io.legere.pdfiumandroid.suspend.PdfiumCoreKt { *; }
-keepclassmembers public class io.legere.pdfiumandroid.suspend.PdfiumCoreKt {
    public <init>(...);
}
-keep class io.legere.pdfiumandroid.util.AlreadyClosedBehavior { *; }
-keepclassmembers public class io.legere.pdfiumandroid.util.AlreadyClosedBehavior {
    public <init>(...);
}
-keep class io.legere.pdfiumandroid.util.Config { *; }
-keepclassmembers public class io.legere.pdfiumandroid.util.Config {
    public <init>(...);
}
-keep class io.legere.pdfiumandroid.util.Size { *; }
-keepclassmembers public class io.legere.pdfiumandroid.util.Size {
    public <init>(...);
}
