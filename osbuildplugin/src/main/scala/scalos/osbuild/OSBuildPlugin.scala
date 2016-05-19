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
    
    val nativeBochs = settingKey[File]("Location of the bochs executable.")
    
    val nativeBochsConfig = settingKey[Option[File]]("Location of the bochs config file.")

    val nativeBochsRcFile = settingKey[Option[File]]("Location of the bochs rc file.")
    
    val nativeUseQemu = settingKey[Boolean]("Should we launch qemu on the iso?")
    
    val nativeUseBochs = settingKey[Boolean]("Should we launch bochs on the iso?")

    val nativeStrip = settingKey[File]("Location of the strip executable.")
    
    val nativeTerminal = settingKey[File]("Location of a terminal executable.")
    
    val nativeTerminalOptions = settingKey[Seq[String]]("Terminal executable options")

    val nativeCrtiSource = settingKey[Option[File]]("the global contructor source file")

    val nativeCrtnSource = settingKey[Option[File]]("the global destructor source file") 

    val nativeCrtBeginObject = settingKey[File]("the global constructor object provided by your compiler") 

    val nativeCrtEndObject = settingKey[File]("the global destructor object provided by your compiler") 

    val nativeEmitDependencyGraphPath = 
      settingKey[Option[File]]("If non-empty, emit linker graph to the given file path.")
  }

  override def projectSettings = OSBuildPluginInternal.projectSettings
}
