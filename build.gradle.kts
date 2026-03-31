buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    jacoco
}

val jacocoVersion = libs.versions.jacoco.get()

subprojects {
    apply(plugin = "jacoco")

    extensions.configure<JacocoPluginExtension> {
        toolVersion = jacocoVersion
    }

    tasks.withType<Test> {
        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports for all modules"

    val subprojects = listOf(project(":app"), project(":data"), project(":domain"))
    
    // Forzamos la ejecución de los tests
    dependsOn(":domain:test")
    dependsOn(":app:testDebugUnitTest")
    dependsOn(":data:testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        // Esta es la ruta que Codacy busca por defecto
        xml.outputLocation.set(file("${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*", "**/*$[0-9]*.*",
        "**/*_HiltModules*.*", "**/*Hilt*.*", "**/dagger/hilt/**/*.*",
        "**/*_Factory*.*", "**/*_MembersInjector*.*", "**/*_ViewBinding*.*"
    )

    classDirectories.setFrom(files(subprojects.flatMap { sub ->
        listOf(
            fileTree(sub.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(fileFilter) },
            fileTree(sub.layout.buildDirectory.dir("intermediates/javac/debug/classes")) { exclude(fileFilter) },
            fileTree(sub.layout.buildDirectory.dir("classes/kotlin/main")) { exclude(fileFilter) }
        )
    }))

    sourceDirectories.setFrom(files(subprojects.flatMap { sub ->
        listOf("${sub.projectDir}/src/main/java", "${sub.projectDir}/src/main/kotlin")
    }))

    executionData.setFrom(files(subprojects.flatMap { sub ->
        fileTree(sub.layout.buildDirectory) {
            include(
                "jacoco/*.exec",
                "outputs/unit_test_code_coverage/**/*.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/*.exec"
            )
        }.files
    }))
}
