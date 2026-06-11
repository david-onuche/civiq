# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep CiviQ domain models and DTOs used for Firestore (de)serialization.
-keepclassmembers class com.civiq.app.data.remote.dto.** {
    *;
}
-keepclassmembers class com.civiq.app.domain.model.** {
    *;
}

# Firestore relies on reflection to map documents to POJOs.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.civiq.app.**$$serializer { *; }
-keepclassmembers class com.civiq.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.civiq.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Exceptions

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
