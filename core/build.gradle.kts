import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(kotlinx.plugins.multiplatform)
    alias(kotlinx.plugins.serialization)
    alias(androidx.plugins.library)
}

kotlin {
    androidTarget()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.i18n)

                // Logging
                api(libs.bundles.logging)

                api(libs.okio)

                api(libs.rxjava)
                api(project.dependencies.enforcedPlatform(kotlinx.coroutines.bom))
                api(kotlinx.coroutines.core)
                api(kotlinx.serialization.json)
                api(kotlinx.serialization.json.okio)

                implementation(libs.jsoup)
            }
        }
        val androidMain by getting {
            dependencies {
                // Dependency injection
                api(libs.injekt.core)

                // Network client
                api(libs.okhttp)
                api(libs.okhttp.logging.interceptor)
                api(libs.okhttp.dnsoverhttps)
                api(libs.okhttp.brotli)

                api(androidx.preference)

                implementation(libs.quickjs.android)

                api(libs.unifile)

                implementation(libs.libarchive)
            }
        }
    }
}

android {
    namespace = "yokai.core"
}

tasks {
    withType<KotlinCompile> {
        compilerOptions.freeCompilerArgs.addAll(
            "-Xcontext-receivers",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}
