# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# WebView JavaScript interface preservation
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve ModelViewScreen bridge if needed
-keepclassmembers class com.example.ui.screens.ModelViewScreen$* {
    public *;
}

# Preserve Retrofit/Moshi/Room (metadata libraries)
-keepattributes Signature,InnerClasses,EnclosingMethod
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep class androidx.room.** { *; }

# Keep data models
-keep class com.example.data.model.** { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
