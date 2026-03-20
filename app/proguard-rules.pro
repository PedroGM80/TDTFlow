# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class com.pedrogm.tdtflow.**$$serializer { *; }
-keepclassmembers class com.pedrogm.tdtflow.** { *** Companion; }

# Coil
-keep class coil.** { *; }

# Compottie
-keep class io.github.alexzhirkevich.compottie.** { *; }
-dontwarn io.github.alexzhirkevich.compottie.**

# Lucide
-keep class com.composables.icons.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
