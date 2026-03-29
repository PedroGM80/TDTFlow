# Media3 / ExoPlayer
# The library ships its own consumer ProGuard rules inside the AAR.
# Only suppress warnings for internal APIs we reference via @OptIn.
-dontwarn androidx.media3.**

# Ktor — keep only the Android HTTP engine entry point (used via reflection)
-keep class io.ktor.client.engine.android.AndroidClientEngine { *; }
-keep class io.ktor.client.engine.android.AndroidEngineConfig { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class com.pedrogm.tdtflow.**$$serializer { *; }
-keepclassmembers class com.pedrogm.tdtflow.** { *** Companion; }

# Coil — ships its own consumer ProGuard rules inside the AAR.
-dontwarn coil.**

# Compottie (Lottie animations)
-keep class io.github.alexzhirkevich.compottie.** { *; }
-dontwarn io.github.alexzhirkevich.compottie.**

# Lucide icons (Compose ImageVector data objects)
-keep class com.composables.icons.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
