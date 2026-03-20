plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

val androidCompileSdk: String by project
val androidMinSdk: String by project
val jvmVersion: String by project

android {
    namespace = "com.pedrogm.tdtflow.data"
    compileSdk = androidCompileSdk.toInt()

    defaultConfig {
        minSdk = androidMinSdk.toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion)
        targetCompatibility = JavaVersion.toVersion(jvmVersion)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(jvmVersion))
    }
}

dependencies {
    implementation(project(":domain"))

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Android
    implementation(libs.androidx.core.ktx)

    // Testing
    testImplementation(libs.junit)
}
