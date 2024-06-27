plugins {
    alias(kotlinx.plugins.multiplatform)
    alias(androidx.plugins.library)
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.sourceApi)
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
