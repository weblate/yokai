plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "yokai.presentation.widget"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(projects.core)
    implementation(projects.domain)
    implementation(projects.presentationCore)

    implementation(androidx.glance.appwidget)

    implementation(libs.coil3)
}
