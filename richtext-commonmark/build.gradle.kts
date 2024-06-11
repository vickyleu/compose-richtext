plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.compose.compiler)
//  id("org.jetbrains.dokka")
}


android {
  namespace = "com.halilibo.richtext.commonmark"
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


kotlin {
  applyDefaultHierarchyTemplate()
  androidTarget()

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
    iosX64()
  ).forEach {
    it.compilations.getByName("main"){
      cinterops{
        val cmark by creating {
          defFile(file("src/nativeInterop/cinterop/cmark.def"))
//          header(file("src/nativeInterop/cinterop/cmark/cmark.h"))
//          header(file("src/nativeInterop/cinterop/cmark/cmark_export.h"))
//          header(file("src/nativeInterop/cinterop/cmark/cmark_version.h"))
          headers(*(file("src/nativeInterop/cinterop/cmark/include/").listFiles()!!.toList().toTypedArray()))
          extraOpts(
            "-libraryPath",
            file("src/nativeInterop/cinterop/cmark/${it.targetName}/").absolutePath.apply {
              println("cmark library path: $this")
            }
          )
        }
      }
    }
  }


  jvm()
  sourceSets {
    commonMain.get().dependencies {
      implementation(compose.runtime)
      api(projects.richtextUi)
      api(projects.richtextMarkdown)
      implementation(project.dependencies.platform(libs.coroutines.bom))
      api(libs.coroutines.core)
    }
    jvmMain.get().dependencies {
      implementation(libs.commonmark)
      implementation(libs.commonmark.ext.gfm.tables)
      implementation(libs.commonmark.ext.gfm.strikethrough)
      implementation(libs.commonmark.ext.autolink)
    }
    androidMain.get().dependencies {
      implementation(libs.commonmark)
      implementation(libs.commonmark.ext.gfm.tables)
      implementation(libs.commonmark.ext.gfm.strikethrough)
      implementation(libs.commonmark.ext.autolink)
    }
    jvmTest.get().dependencies {
      implementation(libs.kotlin.test.junit)
    }

//    targets.withType<KotlinNativeTarget> {
//      val targetName = this.name
//      val main by compilations.getting {
//        cinterops {
//          val cmark by creating {
//            defFile(file("src/nativeInterop/cinterop/cmark.def"))
//            header(file("src/nativeInterop/cinterop/cmark/cmark.h"))
//            header(file("src/nativeInterop/cinterop/cmark/cmark_export.h"))
//            header(file("src/nativeInterop/cinterop/cmark/cmark_version.h"))
//            extraOpts(
//              "-libraryPath",
//              file("src/nativeInterop/cinterop/cmark/$targetName/").absolutePath.apply {
//                println("cmark library path: $this")
//              }
//            )
//          }
//        }
//      }
//    }
  }
}

kotlin {
  @Suppress("OPT_IN_USAGE")
  compilerOptions {
    freeCompilerArgs = listOf(
      "-Xexpect-actual-classes", // remove warnings for expect classes
      "-Xskip-prerelease-check",
      "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
      "-opt-in=org.jetbrains.compose.resources.InternalResourceApi",
    )
  }
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
  }
}