# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/vedant/android/android-studio/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-printmapping mapping.txt

-keepattributes Signature
-keepattributes *Annotation*,EnclosingMethod,Signature
-keep public class your.class.* {
public void set(*);
public ** get*();
}

-dontwarn java.lang.invoke**
-dontwarn com.googlecode.mp4parser**

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class org.telegram.SQLite.* {*;}

# See http://support.crashlytics.com/knowledgebase/articles/202143-eclipse-with-proguard
-keepattributes SourceFile,LineNumberTable
