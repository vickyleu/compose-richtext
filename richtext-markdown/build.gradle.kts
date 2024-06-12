plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.compose.compiler)
//  id("org.jetbrains.dokka")
}

android {
  namespace = "com.halilibo.richtext.markdown"
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
      @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
      implementation(compose.material3)
      api(projects.richtextUi)
    }

    androidMain.get().dependencies {
      implementation(libs.coil.compose)
      implementation(libs.annotatedtext)
      implementation(libs.commonmark)
      implementation(libs.commonmark.ext.gfm.tables)
      implementation(libs.commonmark.ext.gfm.strikethrough)
      implementation(libs.commonmark.ext.autolink)
    }

    iosMain.get().dependencies {
      implementation(libs.coil.compose)
    }

    jvmMain.get().dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.okhttp)
      implementation(libs.commonmark)
      implementation(libs.commonmark.ext.gfm.tables)
      implementation(libs.commonmark.ext.gfm.strikethrough)
      implementation(libs.commonmark.ext.autolink)
    }

    jvmTest.get().dependencies {
      implementation(libs.kotlin.test.junit)
    }
  }
}