# Media3 / ExoPlayer
# The library ships its own consumer ProGuard rules inside the AAR.
# Only suppress warnings for internal APIs we reference via @OptIn.
-dontwarn androidx.media3.**

# Ktor — keep the Android HTTP engine (used via reflection)
# We use wildcards because some engine classes are internal in Ktor 3.
-keep class io.ktor.client.engine.android.** { *; }
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
# Compottie works with R8 out of the box. No additional configuration is required.
-dontwarn io.github.alexzhirkevich.compottie.**

# Lucide icons (Compose ImageVector data objects)
# Icons are referenced directly in code, R8 traces usage automatically.

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-dontwarn com.google.firebase.crashlytics.**

# Google Cast
-keep class com.pedrogm.tdtflow.cast.CastOptionsProvider { *; }
