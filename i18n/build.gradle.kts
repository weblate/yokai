import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    androidTarget()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
            }
        }
        androidMain {
        }
    }
}

android {
    namespace = "yokai.i18n"
}

multiplatformResources {
    resourcesPackage.set("yokai.i18n")
}

tasks {
    // FIXME: Migrate fully to MR
//    val localesConfigTask = registerLocalesConfigTask(project)
//    preBuild {
//        dependsOn(localesConfigTask)
//    }

    withType<KotlinCompile> {
        compilerOptions.freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
        )
    }
}
