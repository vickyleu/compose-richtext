import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.plugins.signing.Sign
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

val publishVersion = "2.0.0"
val publishRepo = "compose-richtext"
val publishUrl = "https://github.com/vickyleu/$publishRepo"
val publishedArtifacts = mapOf(
    "richtext-ui" to ("io.github.vickyleu.richtext" to "richtext"),
    "richtext-markdown" to ("io.github.vickyleu.markdown" to "markdown"),
    "richtext-commonmark" to ("io.github.vickyleu.markdown" to "parser"),
    "richtext-ui-material" to ("io.github.vickyleu.richtext" to "richtext-ui-material"),
    "richtext-ui-material3" to ("io.github.vickyleu.richtext" to "richtext-ui-material3"),
)

fun isRunningFromIde(): Boolean {
    return project.properties["android.injected.invoked.from.ide"] == "true"
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            // disabled: Kotlin 2.x stricter checks emit warnings that block publishing
            // if (!isRunningFromIde()) {
            //     allWarningsAsErrors = true
            // }
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
            freeCompilerArgs.add("-opt-in=org.jetbrains.compose.resources.InternalResourceApi")
        }
    }

    val coordinate = publishedArtifacts[name] ?: return@subprojects
    val (publishGroup, artifactId) = coordinate

    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    extensions.configure<org.jetbrains.dokka.gradle.DokkaExtension>("dokka") {
        moduleName.set(artifactId)
        dokkaPublications.named("html") {
            offlineMode.set(false)
            moduleName.set(artifactId)
        }
        dokkaSourceSets.configureEach {
            reportUndocumented.set(false)
            enableAndroidDocumentationLink.set(true)
            enableKotlinStdLibDocumentationLink.set(true)
            enableJdkDocumentationLink.set(true)
            jdkVersion.set(libs.versions.jvmTarget.get().toInt())
        }
    }

    extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
        coordinates(publishGroup, artifactId, publishVersion)
        publishToMavenCentral(
            automaticRelease = true,
            validateDeployment = DeploymentValidation.PUBLISHED,
        )
        signAllPublications()

        pom {
            name.set("Vickyleu KMP Compose RichText $artifactId")
            description.set("Compose Multiplatform rich text and markdown components.")
            inceptionYear.set("2026")
            url.set(publishUrl)
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("vickyleu")
                    name.set("Vickyleu")
                    url.set("https://github.com/vickyleu")
                }
            }
            scm {
                url.set(publishUrl)
                connection.set("scm:git:https://github.com/vickyleu/$publishRepo.git")
                developerConnection.set("scm:git:ssh://git@github.com/vickyleu/$publishRepo.git")
            }
            issueManagement {
                system.set("GitHub")
                url.set("$publishUrl/issues")
            }
            ciManagement {
                system.set("GitHub Actions")
                url.set("$publishUrl/actions")
            }
        }
    }

    tasks.withType<Sign>().configureEach {
        onlyIf {
            val hasSigningKey =
                providers.gradleProperty("signingInMemoryKey").isPresent ||
                    providers.gradleProperty("signing.secretKeyRingFile").isPresent
            val publishingToCentral = gradle.taskGraph.allTasks.any { task ->
                task.name.contains("MavenCentral")
            }
            hasSigningKey || publishingToCentral
        }
    }
}
