pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("androidx") {
            from(files("gradle/androidx.versions.toml"))
        }
        create("compose") {
            from(files("gradle/compose.versions.toml"))
        }
        create("kotlinx") {
            from(files("gradle/kotlinx.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        // FIXME: Remove on Compose 1.8.0-alpha01
        maven("https://androidx.dev/snapshots/builds/11925800/artifacts/repository")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Yokai"
include(":app")
include(":core")
include(":data")
include(":domain")
include(":i18n")
include(":presentation:core")
include(":presentation:widget")
include(":source-api")
