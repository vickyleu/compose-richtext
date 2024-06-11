plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.compose.compiler)
//  id("org.jetbrains.dokka")
}

android {
  namespace = "com.zachklipp.richtext.ui.printing"
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



kotlin{
  androidTarget()
  sourceSets {
    commonMain.get().dependencies {
      implementation(compose.foundation)
      implementation(compose.uiTooling)
      // For slot table analysis.
      implementation(libs.ui.tooling.data)
      implementation(libs.activity.compose)
      // TODO Migrate off this.
      implementation(compose.material)
    }
  }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
  compilerOptions{
    freeCompilerArgs.add("-Xinline-classes")
  }
}
