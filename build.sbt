import scala.io.Source
import scalos.osbuild.{OSBuildPlugin, OSBuildPluginInternal}
import OSBuildPlugin.autoImport._

val toolScalaVersion = "2.10.6"

val libScalaVersion  = "2.11.8"

lazy val baseSettings = Seq(
  organization := "org.scalos",
  version      := "0.0.1",

  scalafmtConfig := Some(file(".scalafmt"))
)

lazy val toolSettings = baseSettings ++ Seq(scalaVersion := toolScalaVersion)

lazy val libSettings =
  baseSettings ++ OSBuildPlugin.projectSettings ++ Seq(
    scalaVersion := libScalaVersion,

    nativeEmitDependencyGraphPath := Some(file("kernel.dot"))
  )

lazy val osbuildplugin =
  project.in(file("osbuildplugin")).
    settings(toolSettings).
    settings(
      sbtPlugin := true,
      libraryDependencies ++= Seq(
        "org.scala-native" %% "tools" % "0.1-SNAPSHOT"
      )
    )
    
lazy val nativelib =
  project.in(file("nativelib")).
    settings(libSettings)

lazy val javalib =
  project.in(file("javalib")).
    settings(publishArtifact in (Compile, packageDoc) := false).
    settings(libSettings).
    dependsOn(nativelib)

lazy val scalalib =
  project.in(file("scalalib")).
    settings(libSettings).
    dependsOn(javalib)

lazy val oslib = project.in(file("oslib")).
  settings(libSettings).
  dependsOn(scalalib)

lazy val kernel =
  project.in(file("kernel")).
    settings(libSettings).
    settings(
      nativeVerbose := true,
      nativeClangOptions := Seq(
        "-Wall", 
        "-Wextra",
        "-O2", 
        "--target=i686-pc-none-elf", 
        "-ffreestanding", 
        "-fbuiltin", 
        "-fno-stack-protector",
        "-nostdinc", 
        "-Wno-override-module"
      ),
      nativeAssemblyOptions := Seq("-f", "elf", "-I", file("nativesupport").getAbsolutePath),
      nativeClangCOptions := Seq(
        "-std=c99",
        "-I", file("nativesupport/include").getAbsolutePath,
        "-I", file("nativesupport/include/libk").getAbsolutePath
      ),
      nativeCSources := Seq(
        file("nativesupport/stubs/gc_stubs.c"), 
        file("nativesupport/stubs/scala_stubs.c"),
        file("nativesupport/native/libk/ctype/isxdigit.c"),
        file("nativesupport/native/libk/ctype/toupper.c"),
        file("nativesupport/native/libk/stdio/_vsnprintf.c"),
        file("nativesupport/native/libk/stdio/vsprintf.c"),
        file("nativesupport/native/libk/stdlib/abort.c"),
        file("nativesupport/native/libk/string/memset.c"),
        file("nativesupport/native/libk/string/memchr.c"),
        file("nativesupport/native/libk/string/stpcpy.c"),
        file("nativesupport/native/libk/string/strcpy.c"),
        file("nativesupport/native/libk/string/strnlen.c"),
        file("nativesupport/native/bootloader.c"),
        file("nativesupport/native/video.c"),
        file("nativesupport/native/bochs.c")
      ),
      nativeAssemblySources := Seq(
        file("nativesupport/i386/multiboot.asm"),
        file("nativesupport/i386/start.asm")
      ),
      nativeCrtiSource := Some(file("nativesupport/i386/crti.asm")),
      nativeCrtnSource := Some(file("nativesupport/i386/crtn.asm")),
      nativeLinker := file(Process(Seq("which", "clang")).lines_!.head),
      nativeLinkerOptions := Seq("--target=i686-pc-none-elf", "-nostdlib", "-T", file("nativesupport/link.ld").getAbsolutePath),
      nativeGrubConfig := Some(file("nativesupport/grub.cfg")),
      nativeUseBochs := true,
      nativeBochsConfig := Some(file("nativesupport/bochscfg")),
      nativeBochsRcFile := Some(file("nativesupport/bochsrc"))
    ).
    dependsOn(oslib)
