package scalos.osbuild

import sbt._

object OSBuildPlugin extends AutoPlugin {
  val autoImport = AutoImport

  object AutoImport {
    val nativeClang = settingKey[File]("Location of the clang++ compiler.")

    val nativeNasm = settingKey[File]("Location of the nasm compiler.")
    
    val nativeLinker = settingKey[File]("Location of the linker to use.")

    val nativeClangOptions = settingKey[Seq[String]]("Additional options that are passed to clang.")

    val nativeClangIROptions = settingKey[Seq[String]]("Additional options that are passed to clang.")

    val nativeClangCOptions = settingKey[Seq[String]]("Additional options that are passed to clang.")

    val nativeAssemblyOptions = settingKey[Seq[String]]("Additional options that are passed to Nasm.")

    val nativeCSources = settingKey[Seq[File]]("C Sources to link to.")
    
    val nativeAssemblySources = settingKey[Seq[File]]("Assembly Sources to link to.")

    val nativeLinkerOptions = settingKey[Seq[String]]("Additional options that are passed to the linker.")

    val nativeVerbose = settingKey[Boolean]("Enable verbose tool logging.")

    val nativeGrubMkRescue = settingKey[File]("Location of the grub2-mrescue executable.")

    val nativeGrubConfig = settingKey[Option[File]]("Build an ISO given a grub config")

    val nativeQemu = settingKey[File]("Location of the qemu executable.")

    val nativeStrip = settingKey[File]("Location of the strip executable.")

    val nativeEmitDependencyGraphPath = 
      settingKey[Option[File]]("If non-empty, emit linker graph to the given file path.")
  }

  override def projectSettings = OSBuildPluginInternal.projectSettings
}
