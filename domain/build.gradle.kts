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
                api(projects.data)
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
