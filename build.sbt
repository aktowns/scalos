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
      nativeClangOptions := Seq("-O2", "--target=i686-pc-none-elf", "-ffreestanding", "-Wno-override-module"),
      nativeAssemblyOptions := Seq("-f", "elf"),
      nativeCSources := Seq(
        file("nativesupport/stubs/gc_stubs.c"), 
        file("nativesupport/stubs/scala_stubs.c"),
        file("nativesupport/video.c")
      ),
      nativeAssemblySources := Seq(file("nativesupport/start.asm")),
      nativeLinker := file(Process(Seq("which", "ld")).lines_!.head),
      nativeLinkerOptions := Seq("-m", "elf_i386", "-T", file("nativesupport/link.ld").getAbsolutePath),
      nativeGrubConfig := Some(file("nativesupport/grub.cfg"))
    ).
    dependsOn(oslib)
