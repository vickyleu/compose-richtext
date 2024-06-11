plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.compose)
  id(libs.plugins.kotlin.android.get().pluginId)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.zachklipp.richtext.sample"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  lint {
    targetSdk = libs.versions.android.targetSdk.get().toInt()
  }

  buildFeatures {
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
  }

  kotlinOptions {
    jvmTarget = libs.versions.jvmTarget.get()
  }

}

dependencies {
  implementation(projects.printing)
  implementation(projects.richtextCommonmark)
  implementation(projects.richtextUiMaterial3)
  implementation(projects.slideshow)
  implementation(libs.appcompat)
  implementation(libs.activity.compose)
  implementation(compose.foundation)
  implementation(compose.materialIconsExtended)
  implementation(compose.material3)
  implementation(compose.uiTooling)
}
