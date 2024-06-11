import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

//buildscript {
//  repositories {
//    mavenCentral()
//    gradlePluginPortal()
//    google()
//  }
//
//  dependencies {
//    classpath(BuildPlugins.androidGradlePlugin)
//    classpath(Kotlin.binaryCompatibilityValidatorPlugin)
//    classpath(Kotlin.gradlePlugin)
//    classpath(ktlint)
//  }
//}

plugins {
  alias(libs.plugins.android.library) apply  false
  alias(libs.plugins.android.application) apply  false
  alias(libs.plugins.jetbrains.compose) apply  false
  alias(libs.plugins.compose.compiler) apply  false
  alias(libs.plugins.kotlin.multiplatform) apply  false

//  id("org.jetbrains.dokka") version "1.8.10"
}

val javaVersion = JavaVersion.toVersion(libs.versions.jvmTarget.get())
check(JavaVersion.current().isCompatibleWith(javaVersion)) {
  "This project needs to be run with Java ${javaVersion.getMajorVersion()} or higher (found: ${JavaVersion.current()})."
}



//tasks.withType<DokkaMultiModuleTask>().configureEach {
//  outputDirectory.set(rootProject.file("docs/api"))
//  failOnWarning.set(true)
//}

// See https://stackoverflow.com/questions/25324880/detect-ide-environment-with-gradle
fun isRunningFromIde(): Boolean {
  return project.properties["android.injected.invoked.from.ide"] == "true"
}
allprojects {
  if (tasks.findByName("testClasses") == null) {
    try {
      tasks.register("testClasses")
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
subprojects {
  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions{
      // Allow warnings when running from IDE, makes it easier to experiment.
      if (!isRunningFromIde()) {
        allWarningsAsErrors = true
      }
      freeCompilerArgs = listOf(
        "-Xexpect-actual-classes", // remove warnings for expect classes
        "-Xskip-prerelease-check",
        "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
        "-opt-in=org.jetbrains.compose.resources.InternalResourceApi",
      )
    }
  }

//  tasks.withType<KotlinCompile>().all {
//    kotlinOptions {
//      // Allow warnings when running from IDE, makes it easier to experiment.
//      if (!isRunningFromIde()) {
//        allWarningsAsErrors = true
//      }
//
//      freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xexpect-actual-classes")
//    }
//  }

  // taken from https://github.com/google/accompanist/blob/main/build.gradle
  /*afterEvaluate {
    if (tasks.findByName("dokkaHtmlPartial") == null) {
      // If dokka isn't enabled on this module, skip
      return@afterEvaluate
    }

    tasks.withType<AbstractPublishToMaven>().configureEach {
      dependsOn(tasks.withType<Sign>())
    }

    tasks.named<DokkaTaskPartial>("dokkaHtmlPartial").configure {
      dokkaSourceSets.configureEach {
        reportUndocumented.set(true)
        skipEmptyPackages.set(true)
        skipDeprecated.set(true)
        jdkVersion.set(11)

        // Add Android SDK packages
        noAndroidSdkLink.set(false)
      }
    }

    val javadocJar by tasks.registering(Jar::class) {
      dependsOn(tasks.dokkaJavadoc)
      archiveClassifier.set("javadoc")
      from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    }

    if (tasks.names.contains("publishKotlinMultiplatformPublicationToMavenRepository")) {
      tasks.named("publishKotlinMultiplatformPublicationToMavenRepository").configure {
        dependsOn("signJvmPublication")
      }
    }

    if (tasks.names.contains("publishAndroidReleasePublicationToMavenRepository")) {
      tasks.named("publishAndroidReleasePublicationToMavenRepository").configure {
        dependsOn("signJvmPublication")
      }
    }
  }*/

  /*afterEvaluate {
    fun MavenPublication.configure() {
      groupId = property("GROUP").toString()
      version = property("VERSION_NAME").toString()

      artifact(tasks.named("javadocJar").get())

      pom {
        name.set(property("POM_NAME").toString())
        description.set(property("POM_DESCRIPTION").toString())
        url.set("https://github.com/halilozercan/compose-richtext")

        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
          }
        }
        developers {
          developer {
            id.set("halilozercan")
            name.set("Halil Ozercan")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/halilozercan/compose-richtext.git")
          url.set("https://github.com/halilozercan/compose-richtext/")
          developerConnection.set("scm:git:ssh://git@github.com/halilozercan/compose-richtext.git")
        }
      }
    }

    *//*extensions.findByType<PublishingExtension>()?.apply {
      repositories {
        maven {
          val localProperties = gradleLocalProperties(rootProject.rootDir)

          val sonatypeUsername =
            localProperties.getProperty("SONATYPE_USERNAME") ?: System.getenv("SONATYPE_USERNAME")

          val sonatypePassword =
            localProperties.getProperty("SONATYPE_PASSWORD") ?: System.getenv("SONATYPE_PASSWORD")

          val releasesRepoUrl =
            uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
          val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
          val version = property("VERSION_NAME").toString()
          url = uri(
            if (version.endsWith("SNAPSHOT")) {
              snapshotsRepoUrl
            } else {
              releasesRepoUrl
            }
          )
          credentials {
            username = sonatypeUsername
            password = sonatypePassword
          }
        }
      }

      publications.withType<MavenPublication>().configureEach {
        configure()
      }
    }

    extensions.findByType<SigningExtension>()?.apply {
      val localProperties = gradleLocalProperties(rootProject.rootDir)

      val gpgPrivateKey =
        localProperties.getProperty("GPG_PRIVATE_KEY")
          ?: System.getenv("GPG_PRIVATE_KEY")
          ?: return@apply

      val gpgPrivatePassword =
        localProperties.getProperty("GPG_PRIVATE_PASSWORD")
          ?: System.getenv("GPG_PRIVATE_PASSWORD")
          ?: return@apply

      val publishing = extensions.findByType<PublishingExtension>()
        ?: return@apply

      useInMemoryPgpKeys(
        gpgPrivateKey.replace("\\n", "\n"),
        gpgPrivatePassword
      )
      sign(publishing.publications)
    }*//*
  }*/
}

//disable until the library reaches 1.0.0-beta01
//apply plugin: 'binary-compatibility-validator'
//apiValidation {
//  // Ignore all sample projects, since they're not part of our API.
//  // Only leaf project name is valid configuration, and every project must be individually ignored.
//  // See https://github.com/Kotlin/binary-compatibility-validator/issues/3
//  ignoredProjects += project('sample').name
//  ignoredProjects += project('desktop').name
//  ignoredProjects += project('richtext-ui-kmm').name
//  ignoredProjects += project('richtext-commonmark-kmm').name
//}
