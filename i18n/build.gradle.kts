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
        val commonMain by getting {
            dependencies {
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
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
