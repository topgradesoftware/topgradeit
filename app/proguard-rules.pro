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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Performance optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Keep essential classes
-keep class topgrade.parent.com.parentseeks.** { *; }
-keepclassmembers class topgrade.parent.com.parentseeks.** {
    *;
}

# Keep Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Volley
-keep class com.android.volley.** { *; }
-keep class com.android.volley.toolbox.** { *; }

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Keep Paper DB
-keep class io.paperdb.** { *; }
-dontwarn io.paperdb.**

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Keep Material Design
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep Navigation
-keepnames class androidx.navigation.fragment.NavHostFragment
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Keep Security Crypto
-keep class androidx.security.** { *; }
-dontwarn androidx.security.**

# Keep CircleImageView
-keep class de.hdodenhof.circleimageview.** { *; }

# Keep SearchableSpinner
-keep class com.github.MdFarhanRaja.** { *; }

# Keep Image Cropper
-keep class com.vanniktech.** { *; }

# Keep Zoomage
-keep class com.jsibbold.** { *; }

# Keep StickyHeader
-keep class com.github.shuhart.** { *; }

# Keep SDP/SSP
-keep class com.intuit.** { *; }

# Keep PinView
-keep class com.github.davidpizarro.** { *; }

# Performance optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable implementations
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep R classes
-keep class **.R$* {
    public static <fields>;
}

# Keep BuildConfig
-keep class **.BuildConfig {
    public static final boolean DEBUG;
    public static final String APPLICATION_ID;
    public static final String BUILD_TYPE;
    public static final String VERSION_NAME;
    public static final int VERSION_CODE;
}

# Remove unused code
-dontwarn **
-ignorewarnings

# Optimize string operations
-optimizations !code/removal/duplicate
-optimizations !code/removal/unused

# Keep essential Android classes
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Keep essential Java classes
-keep class java.lang.** { *; }
-keep class java.util.** { *; }
-keep class java.io.** { *; }

# Performance: Remove unused attributes
-keepattributes !LocalVariableTable,!LocalVariableTypeTable

# Performance: Keep only essential attributes
-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod

# Performance: Optimize method calls
-optimizations !method/inlining/*

# Performance: Keep method names for debugging
-keepattributes SourceFile,LineNumberTable

# Performance: Remove debug information in release
-repackageclasses ''
-allowaccessmodification
