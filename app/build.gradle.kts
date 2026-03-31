plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val androidCompileSdk: String by project
val androidMinSdk: String by project
val androidTargetSdk: String by project
val jvmVersion: String by project

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "com.pedrogm.tdtflow"
    compileSdk = androidCompileSdk.toInt()

    defaultConfig {
        applicationId = "com.pedrogm.tdtflow"
        minSdk = androidMinSdk.toInt()
        targetSdk = androidTargetSdk.toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("RELEASE_KEYSTORE_PATH") ?: "release-key.jks")
            // Priorizamos RELEASE_KEYSTORE_PASSWORD para el almacén
            storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD") ?: System.getenv("RELEASE_KEY_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Crashlytics desactivado en debug para builds más rápidos
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion)
        targetCompatibility = JavaVersion.toVersion(jvmVersion)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// In AGP 9.x+, renaming APKs via the new Variant API (androidComponents) 
// is still being finalized. If outputFileName is unresolved, 
// you can influence the name via archivesName or by opting into the legacy DSL.
// For now, we'll use a project-level setting to influence the name.
base {
    archivesName.set("TDTFlow")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(jvmVersion))
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // Lucide Icons (sustituye a material-icons-extended)
    implementation(libs.lucide.icons)

    // Compottie (Lottie animations)
    implementation(libs.compottie)
    implementation(libs.compottie.dot)
    implementation(libs.compottie.network)

    // Media3 (ExoPlayer)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)

    // TV Compose
    implementation(libs.tv.compose.material)

    // Android Auto / Automotive
    implementation(libs.androidx.car.app)


    // Images
    implementation(libs.coil.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
