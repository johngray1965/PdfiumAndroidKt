# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class io.legere.pdfiumandroid.** { *; }

-keep interface io.legere.pdfiumandroid.** { public *; }

-keepclasseswithmembernames class io.legere.pdfiumandroid.** {
     public <methods>;
}

-keep class * extends io.legere.pdfiumandroid.api.LoggerInterface { *; }
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
-keep class io.legere.pdfiumandroid.api.AlreadyClosedBehavior { *; }
-keepclassmembers public class io.legere.pdfiumandroid.api.AlreadyClosedBehavior {
    public <init>(...);
}
-keep class io.legere.pdfiumandroid.api.Config { *; }
-keepclassmembers public class io.legere.pdfiumandroid.api.Config {
    public <init>(...);
}
-keep class io.legere.pdfiumandroid.api.Size { *; }
-keepclassmembers public class io.legere.pdfiumandroid.api.Size {
    public <init>(...);
}
