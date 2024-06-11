import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.compose.compiler)
}

kotlin{
  jvm()
  sourceSets {
    jvmMain {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(projects.richtextUiMaterial)
        implementation(libs.commonmark)
      }
    }
  }

}

compose.desktop {
  application {
    mainClass = "com.halilibo.richtext.desktop.MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "jvm"
      packageVersion = "1.0.0"
    }
  }
}