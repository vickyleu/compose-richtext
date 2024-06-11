@file:Suppress("UnstableApiUsage")

pluginManagement {
  listOf(repositories, dependencyResolutionManagement.repositories).forEach {
    it.apply {
      mavenCentral()
      gradlePluginPortal()
      google {
        content {
          includeGroupByRegex(".*google.*")
          includeGroupByRegex(".*android.*")
        }
      }
      maven(url = "https://androidx.dev/storage/compose-compiler/repository")
      maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
  }
}
dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
  repositories {
    mavenCentral()
    google {
      content {
        includeGroupByRegex(".*google.*")
        includeGroupByRegex(".*android.*")
      }
    }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    maven {
      setUrl("https://jitpack.io")
      content {
        includeGroupByRegex("com.github.*")
      }
    }
  }
}

include(":printing")
include(":richtext-ui")
include(":richtext-ui-material")
include(":richtext-ui-material3")
include(":richtext-commonmark")
include(":richtext-markdown")
include(":android-sample")
include(":desktop-sample")
include(":slideshow")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "compose-richtext"
