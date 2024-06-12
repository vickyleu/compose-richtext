import com.android.build.gradle.LibraryExtension
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties
import java.util.concurrent.Executors

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  dependencies {
    val dokkaVersion = libs.versions.dokka.get()
    classpath("org.jetbrains.dokka:dokka-base:$dokkaVersion")
  }
}

plugins {
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.jetbrains.compose) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.dokka)
  id("maven-publish")
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
    compilerOptions {
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





allprojects {
  val properties = Properties().apply {
    runCatching { rootProject.file("local.properties") }
      .getOrNull()
      .takeIf { it?.exists() ?: false }
      ?.reader()
      ?.use(::load)
  }
  val environment: Map<String, String?> = System.getenv()
  val myExtra = mutableMapOf<String, Any>()
  myExtra["githubToken"] = properties["github.token"] as? String
    ?: environment["GITHUB_TOKEN"] ?: ""

  val libs = rootDir.resolve("gradle/libs.versions.toml")
  val map = hashMapOf<String, String>()
  libs.useLines {
    it.forEach { line ->
      if (line.contains("=") && line.replace(" ", "").startsWith("#").not()) {
        val (key, value) = line.split("=")
        map[key.replace(" ", "").removeSurrounding("\"")] =
          value.replace(" ", "").removeSurrounding("\"")
      }
    }
  }
  val jvmTarget = map["jvmTarget"] ?: "11"
  val rootProjectName  = rootDir.name
  val mavenAuthor = "vickyleu"
  val mavenGroup = "com.$mavenAuthor.richtext"
  val currentName = project.name.replace("$rootProjectName-", "")

  val mGroup = mavenGroup
  val mVersion = "1.0.2"
  afterEvaluate {
    if (project.name.startsWith(rootProjectName)||project.name.contains("printing")) {
      return@afterEvaluate
    }
    if (project.extensions.findByName("android") != null) {
      val ext = project.extensions.findByType<LibraryExtension>()
        ?: project.extensions.findByType<com.android.build.gradle.AppExtension>()
      if (ext != null && ext is LibraryExtension) {
      } else {
        return@afterEvaluate
      }
    }else{
      return@afterEvaluate
    }
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")

    tasks.withType<PublishToMavenRepository> {
      val isMac = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
      onlyIf {
        isMac.also {
          if (!isMac) logger.error(
            """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
          )
        }
      }
    }
    if (project.extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class) != null) {
      val kotlin =
        project.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class)
      kotlin.apply {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        this.compilerOptions {
          freeCompilerArgs = listOf(
            "-Xexpect-actual-classes", // remove warnings for expect classes
            "-Xskip-prerelease-check",
            "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
          )
        }
        this.jvmToolchain {
          languageVersion.set(JavaLanguageVersion.of(jvmTarget.toInt()))
        }
        this.targets.withType<KotlinNativeTarget> {
          binaries.all {
            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
          }
        }
      }
    }
    afterEvaluate {
      val javadocJar by tasks.registering(Jar::class) {
        dependsOn(tasks.dokkaHtml)
        from(tasks.dokkaHtml.flatMap(org.jetbrains.dokka.gradle.DokkaTask::outputDirectory))
        archiveClassifier = "javadoc"
      }
      publishing {
        val projectName = rootProjectName
        repositories {
          maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$mavenAuthor/${projectName}")
            credentials {
              username = "$mavenAuthor"
              password = myExtra["githubToken"]?.toString()
            }
          }
        }

        repositories {
          maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$mavenAuthor/${projectName}")
            credentials {
              username = "$mavenAuthor"
              password = myExtra["githubToken"]?.toString()
            }
          }
        }
        val shouldRegistering = when {
          project.extensions.findByName("javaPlatform") != null -> true
          project.extensions.findByName("android") != null -> {
            true
          }
//                project.extensions.findByName("java")!=null->true
          else -> false
        }
        if (shouldRegistering) {
          afterEvaluate {
            if (project.extensions.findByName("android") != null) {
              if (components.findByName("release") == null) {
                return@afterEvaluate
              }
            }
            publications.register<MavenPublication>(
              if (project.extensions.findByName("android") != null) "release" else "java"
            )
            {

              when {
                project.extensions.findByName("javaPlatform") != null -> {
                  from(components["javaPlatform"])
                }

                project.extensions.findByName("android") != null -> {
                  from(components["release"])
                }

                else -> Unit
              }
              version = mVersion
              groupId = mGroup
              if (artifactId.startsWith("${rootProjectName.split("-").last()}-")) {
                val artifact =
                  artifactId.replace("${rootProjectName.split("-").last()}-", "")
                artifactId = "$artifact-android"
              }else{
                artifactId = "$artifactId-android"
              }
              pom {
                url = "https://github.com/$mavenAuthor/${projectName}"
                name = projectName
                description = """
                Visit the project on GitHub to learn more.
            """.trimIndent()
                inceptionYear = "2024"
                licenses {
                  license {
                    name = "Apache-2.0 License"
                    url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                  }
                }
                developers {
                  developer {
                    id = "halilibo"
                    name = "halilibo"
                    email = ""
                    roles = listOf("Mobile Developer")
                    timezone = "GMT+8"
                  }
                }
                contributors {
                  // contributor {}
                }
                scm {
                  tag = "HEAD"
                  url = "https://github.com/$mavenAuthor/${projectName}"
                  connection = "scm:git:github.com/$mavenAuthor/${projectName}.git"
                  developerConnection =
                    "scm:git:ssh://github.com/$mavenAuthor/${projectName}.git"
                }
                issueManagement {
                  system = "GitHub"
                  url = "https://github.com/$mavenAuthor/${projectName}/issues"
                }
                ciManagement {
                  system = "GitHub Actions"
                  url = "https://github.com/$mavenAuthor/${projectName}/actions"
                }
              }
            }
          }
        }
        afterEvaluate{
          publications.withType<MavenPublication> {
            val old = artifactId
            if(old.endsWith("-android"))return@withType
            artifact(javadocJar) // Required a workaround. See below
            version = mVersion
            groupId = mGroup

            if (artifactId.startsWith("${rootProjectName.split("-").last()}-")) {
              val artifact = artifactId.replace("${rootProjectName.split("-").last()}-", "")
              artifactId = artifact
            }
            pom {
              url = "https://github.com/$mavenAuthor/${projectName}"
              name = projectName
              description = """
                Visit the project on GitHub to learn more.
            """.trimIndent()
              inceptionYear = "2024"
              licenses {
                license {
                  name = "Apache-2.0 License"
                  url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                }
              }
              developers {
                developer {
                  id = "halilibo"
                  name = "halilibo"
                  email = ""
                  roles = listOf("Mobile Developer")
                  timezone = "GMT+8"
                }
              }
              contributors {
                // contributor {}
              }
              scm {
                tag = "HEAD"
                url = "https://github.com/$mavenAuthor/${projectName}"
                connection = "scm:git:github.com/$mavenAuthor/${projectName}.git"
                developerConnection =
                  "scm:git:ssh://github.com/$mavenAuthor/${projectName}.git"
              }
              issueManagement {
                system = "GitHub"
                url = "https://github.com/$mavenAuthor/${projectName}/issues"
              }
              ciManagement {
                system = "GitHub Actions"
                url = "https://github.com/$mavenAuthor/${projectName}/actions"
              }
            }
          }
        }

      }
    }
    tasks.dokkaHtml {
      offlineMode = false
      moduleName = currentName
      dokkaSourceSets {
        configureEach {
          reportUndocumented = true
          noAndroidSdkLink = false
          noStdlibLink = false
          noJdkLink = false
          jdkVersion = jvmTarget.toInt()
        }
      }
    }

// TODO: Remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
//  Thanks to KSoup repository for this code snippet
    tasks.withType(AbstractPublishToMaven::class).configureEach {
      dependsOn(tasks.withType(Sign::class))
    }
  }
}


tasks.register("deletePackages") {

  val libs = rootDir.resolve("gradle/libs.versions.toml")
  val map = hashMapOf<String, String>()
  libs.useLines {
    it.forEach { line ->
      if (line.contains("=") && line.startsWith("#").not()) {
        val (key, value) = line.split("=")
        map[key
          .replace(" ", "").removeSurrounding("\"")] =
          value
            .replace(" ", "").removeSurrounding("\"")
      }
    }
  }


  val mavenGroup = "com.vickyleu.richtext"

  apply(plugin = "org.jetbrains.dokka")
  apply(plugin = "maven-publish")

  group = "publishing"
  description = "Delete all packages in the GitHub Packages registry"

  val keyword = "${mavenGroup}"
  println("keyword: $keyword")
  val properties = Properties().apply {
    runCatching { rootProject.file("local.properties") }
      .getOrNull()
      .takeIf { it?.exists() ?: false }
      ?.reader()
      ?.use(::load)
  }
  val environment: Map<String, String?> = System.getenv()
  val myExtra = mutableMapOf<String, Any>()
  myExtra["githubToken"] = properties["github.token"] as? String
    ?: environment["GITHUB_TOKEN"] ?: ""
  val headers = mapOf(
    "Accept" to "application/vnd.github.v3+json",
    "Authorization" to "Bearer ${myExtra["githubToken"]}",
    "X-GitHub-Api-Version" to "2022-11-28"
  )
  doLast {
    runBlocking {
      val executor = Executors.newFixedThreadPool(10)
      val scope = CoroutineScope(executor.asCoroutineDispatcher())
      val fetchJobs = packageTypes.flatMap { packageType ->
        visibilityTypes.map { visibility ->
          scope.async {
            fetchPackages(packageType, visibility, headers)
          }
        }
      }
      fetchJobs.awaitAll().forEach { packages ->
        allPackages.addAll(packages)
      }
      val deleteJobs = allPackages.filter { pkg ->
        val packageName = pkg["name"] as String
        packageName.contains(keyword)
      }.map { pkg ->
        val packageType = pkg["package_type"] as String
        val packageName = pkg["name"] as String
        scope.async {
          deletePackage(packageType, packageName, headers)
        }
      }
      try {
        deleteJobs.awaitAll()
        executor.shutdown()
      } catch (e: Exception) {
        println("删除包失败: ${e.message}")
      }
    }
  }
}

val packageTypes = listOf("npm", "maven", "docker", "container")
val visibilityTypes = listOf("public", "private", "internal")
val allPackages = mutableListOf<Map<String, Any>>()

fun fetchPackages(
  packageType: String,
  visibility: String,
  headers: Map<String, String>
): List<Map<String, Any>> {
  val packages = mutableListOf<Map<String, Any>>()
  var page = 1

  while (true) {
    val url =
      URL("https://api.github.com/user/packages?package_type=$packageType&visibility=$visibility&page=$page&per_page=100")
    val connection = url.openConnection() as HttpURLConnection

    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

    if (connection.responseCode == 200) {
      val response = connection.inputStream.bufferedReader().use { it.readText() }
      val batch: List<Map<String, Any>> = jacksonObjectMapper().readValue(response)
      if (batch.isEmpty()) break
      packages.addAll(batch)
      page++
    } else {
      println("获取$packageType ($visibility) 包列表失败，错误代码: ${connection.responseCode} ${connection.responseMessage}")
      println(connection.inputStream.bufferedReader().use { it.readText() })
      break
    }
  }

  return packages
}

fun deletePackage(packageType: String, packageName: String, headers: Map<String, String>) {
  val url = URL("https://api.github.com/user/packages/$packageType/$packageName")
  val connection = url.openConnection() as HttpURLConnection
  connection.requestMethod = "DELETE"
  headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

  if (connection.responseCode == 204 || connection.responseCode == 200) {
    println("$packageName 删除成功")
  } else {
    println("$packageName 删除失败，错误代码: ${connection.responseCode}")
    println(connection.inputStream.bufferedReader().use { it.readText() })
  }
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
