package yokai.build

import org.gradle.api.Project
import java.io.File

val Project.generatedBuildDir: File get() = project.layout.buildDirectory.asFile.get().resolve("generated/yokai")
