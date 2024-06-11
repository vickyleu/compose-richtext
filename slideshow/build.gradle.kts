plugins {
//  id("org.jetbrains.dokka")
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  androidTarget()
  sourceSets {
    androidMain.get().dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material)
      implementation(compose.uiTooling)
    }
  }
}

android {
  namespace = "com.zachklipp.richtext.ui.slideshow"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
  }
  lint {
    targetSdk = libs.versions.android.targetSdk.get().toInt()
  }

  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
  }
}
