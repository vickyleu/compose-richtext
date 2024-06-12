plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  applyDefaultHierarchyTemplate()
  androidTarget()
  iosArm64()
  iosSimulatorArm64()
  iosX64()
  jvm()
  sourceSets {
    commonMain.get().dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.uiUtil)
    }
  }
}

android {
  namespace = "com.halilibo.richtext.ui"
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

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }

}
