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
                implementation(projects.source.api)
            }
        }
        val androidMain by getting {
            dependencies {
            }
        }
    }
}

android {
    namespace = "yokai.domain"
}
