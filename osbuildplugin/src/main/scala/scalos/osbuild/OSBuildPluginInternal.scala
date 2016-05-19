package scalos.osbuild

import sbt._, Keys._, complete.DefaultParsers._
import scalanative.compiler.{Compiler => NativeCompiler, Opts => NativeOpts}
import OSBuildPlugin.autoImport._

object OSBuildPluginInternal {
  private def cpToStrings(cp: Seq[File]): Seq[String] = cp.map(_.getAbsolutePath)
  private def cpToString(cp: Seq[File]): String = cpToStrings(cp).mkString(java.io.File.pathSeparator)
  private def abs(file: File): String = file.getAbsolutePath

  /** Compiles application nir to llvm ir. */
  private def compileNir(opts: NativeOpts, log: Logger): Unit = {
    log.info(s"Compiling nir to ir with options: $opts")
    
    val compiler = new NativeCompiler(opts)
    compiler.apply()
  }
  
  private def compileC(clang: File, 
                       clangOpts: Seq[String], 
                       target: File, 
                       sources: Seq[File], 
                       log: Logger): Seq[File] = {
    sources.map { (source) =>
      log.info(s"Compiling $source using $clang $clangOpts to $target")

      val output = target / source.getName.replaceAll("\\.c$", ".o")
      val compile = abs(clang) +: (clangOpts ++ Seq("-c", "-o", abs(output), abs(source)))
       
      if ((Process(compile, target) ! log) != 0) {
        throw new IllegalStateException(s"failed to compile $sources")
      }

      output
    }
  }
  
  private def compileAsm(nasm: File, 
                         nasmOpts: Seq[String], 
                         target: File, 
                         sources: Seq[File], 
                         log: Logger): Seq[File] = {
    sources.map { (source) =>
      log.info(s"Compiling $source using $nasm $nasmOpts to $target")

      val output = target / source.getName.replaceAll("\\.asm$", ".o")
      val compile = abs(nasm) +: (nasmOpts ++ Seq("-o", abs(output), abs(source)))
      
      if ((Process(compile, target) ! log) != 0) {
        throw new IllegalStateException(s"Failed to compile $sources")
      }
      output
    }
  }
  
  private def compileLl(clang: File, 
                        clangOpts: Seq[String], 
                        target: File, 
                        sources: Seq[File], 
                        log: Logger): Seq[File] = {
    sources.map { (source) =>
      log.info(s"Compiling $source using $clang $clangOpts to $target")
      
      //val outpath = abs(output)
      val output = target / source.getName.replaceAll("\\.ll$", ".o")
      val compile = abs(clang) +: (clangOpts ++ Seq("-c", "-o", abs(output), abs(source)))
      
      if ((Process(compile, target) ! log) != 0) {
        throw new IllegalStateException(s"Failed to compile $sources") 
      }
      output
    }
  }
  
  private def linkObjects(linker: File, 
                          linkerOpts: Seq[String], 
                          target: File, 
                          objects: Seq[File], 
                          output: File,
                          log: Logger): File = {
    log.info(s"Linking $objects using $linker $linkerOpts to $output")
    val link = abs(linker) +: (linkerOpts ++ Seq("-o", abs(output)) ++ cpToStrings(objects))

    if ((Process(link, target) ! log) != 0) {
      throw new IllegalStateException(s"Failed to link: $objects")
    }
    
    output
  }
  
  private def createISO(mkRescue: File, 
                        kernel: File, 
                        config: File, 
                        name: String,
                        target: File, 
                        log: Logger): File = {
    log.info(s"Creating bootable iso for $kernel with config $config")
    
    val isodir = target / "isodir"
    val bootdir = isodir / "boot"
    val grubdir = bootdir / "grub"
    
    IO.createDirectory(isodir)
    IO.createDirectory(bootdir)
    IO.createDirectory(grubdir)
    
    IO.copyFile(config, grubdir / "grub.cfg")
    IO.copyFile(kernel, bootdir / "kernel.bin")
    
    // val iso = file(target / name + ".iso")
    val iso = file(name + ".iso") 
    val rescue = abs(mkRescue) +: Seq("-o", abs(iso), abs(isodir))
    
    if ((Process(rescue, target) ! log) != 0) {
      throw new IllegalStateException("failed to create iso")
    }

    iso
  }
  
  private def stripExecutable(strip: File, kernel: File, target: File, log: Logger): Unit = {
    log.info(s"Stripping $kernel")
    
    val stripCmd = abs(strip) +: Seq("--strip-all", abs(kernel))
    Process(stripCmd, target) ! log  
  }
  
  private def launchQemu(qemu: File,
                         cdrom: File, 
                         target: File, 
                         log: Logger): Unit = {
    
    val emu = abs(qemu) +: Seq("-cdrom", abs(cdrom))
    Process(emu, target) ! log    
  }
  
  private def launchBochs(terminal: File, 
                          bochs: File,
                          bochsConfig: Option[File], 
                          bochsRc: Option[File],
                          target: File, 
                          log: Logger): Unit = {
    
    val cfg = bochsConfig.map((x) => Seq("-qf", abs(x))).getOrElse(Seq())
    val rc = bochsRc.map((x) => Seq("-rc", abs(x))).getOrElse(Seq())
    
    val bochsCmd = abs(bochs) +: (cfg ++ rc)
    
    val emu = Seq(abs(terminal), "-e", bochsCmd.mkString(" "))
    log.info(s"Command line: $emu")
    Process(emu, target) ! log    
  }

  private def findClangObject(clang: File,
                              clangOpts: Seq[String],
                              obj: String): File = {
    val cmd = abs(clang) +: (clangOpts :+ s"-print-file-name=$obj")
    val out = Process(cmd)
    
    file(out.lines_!.head)
  }
                    

  lazy val projectSettings = Seq(
    addCompilerPlugin("org.scala-native" %% "nscplugin" % "0.1-SNAPSHOT"),

    nativeClang := file(Process(Seq("which", "clang")).lines_!.head),
    
    nativeNasm := file(Process(Seq("which", "nasm")).lines_!.head),

    nativeLinker := file(Process(Seq("which", "clang")).lines_!.head),

    nativeClangOptions := Seq(),
    
    nativeClangIROptions := Seq(),
    
    nativeClangCOptions := Seq(),
    
    nativeAssemblyOptions := Seq(),
    
    nativeCSources := Seq(),
    
    nativeAssemblySources := Seq(),
    
    nativeLinkerOptions := Seq(),

    nativeVerbose := false,

    nativeEmitDependencyGraphPath := None,
    
    nativeGrubMkRescue := file(Process(Seq("which", "grub2-mkrescue")).lines_!.head),
    
    nativeGrubConfig := None,

    nativeQemu := file(Process(Seq("which", "qemu-system-i386")).lines_!.head),
    
    nativeBochs := file(Process(Seq("which", "bochs")).lines_!.head),
    
    nativeBochsConfig := None,

    nativeBochsRcFile := None,
    
    nativeUseQemu := false,
    
    nativeUseBochs := false,
    
    nativeTerminal := file(Process(Seq("which", "gnome-terminal")).lines_!.head),
    
    nativeTerminalOptions := Seq("-e"),
    
    nativeStrip := file(Process(Seq("which", "strip")).lines_!.head),

    nativeCrtiSource := None,

    nativeCrtnSource := None, 

    nativeCrtBeginObject := findClangObject(nativeClang.value, Seq("--target=i686-linux-elf"), "crtbegin.o"),

    nativeCrtEndObject := findClangObject(nativeClang.value, Seq("--target=i686-linux-elf"), "crtend.o"),

    run := {
      val log         = streams.value.log
      val clang       = nativeClang.value
      val clangOpts   = nativeClangOptions.value
      val clangIrOpts = nativeClangIROptions.value
      val clangCOpts  = nativeClangCOptions.value
      val nasm        = nativeNasm.value
      val nasmOpts    = nativeAssemblyOptions.value
      val linker      = nativeLinker.value
      val linkerOpts  = nativeLinkerOptions.value
      val csources    = nativeCSources.value
      val asmsources  = nativeAssemblySources.value
      val entry       = (mainClass in Compile).value.get.toString
      val classpath   = cpToStrings((fullClasspath in Compile).value.map(_.data))
      val target      = (crossTarget in Compile).value
      val appll       = target / (moduleName.value + "-out.ll")
      val obj         = target / (moduleName.value + "-out.o")
      val executable  = target / moduleName.value
      val grubRescue  = nativeGrubMkRescue.value
      val grubConfig  = nativeGrubConfig.value
      val qemu        = nativeQemu.value
      val strip       = nativeStrip.value
      val verbose     = nativeVerbose.value
      val bochs       = nativeBochs.value    
      val bochsConfig = nativeBochsConfig.value
      val bochsRcFile = nativeBochsRcFile.value
      val useQemu     = nativeUseQemu.value
      val useBochs    = nativeUseBochs.value
      val terminal    = nativeTerminal.value
      val dotpath     = nativeEmitDependencyGraphPath.value
      val maybeCrti   = nativeCrtiSource.value
      val maybeCrtn   = nativeCrtnSource.value 
      val crtBegin    = nativeCrtBeginObject.value
      val crtEnd      = nativeCrtEndObject.value
      val opts        = new NativeOpts(classpath, abs(appll), dotpath.map(abs), entry, verbose)
      
      log.info(s"Creating directory $target")
      IO.createDirectory(target)
      
      compileNir(opts, log)
      val irObjs = compileLl(clang, clangOpts ++ clangIrOpts, target, Seq(appll), log)
      
      val cObjs = compileC(clang, clangOpts ++ clangCOpts, target, csources, log)
      val asmObjs = compileAsm(nasm, nasmOpts, target, asmsources, log)

      val preObj = maybeCrti
                    .map((crti) => compileAsm(nasm, nasmOpts, target, Seq(crti), log))
                    .map((crtiObj) => crtiObj :+ crtBegin).getOrElse(Seq())
      val postObj = maybeCrtn
                    .map((crtn) => compileAsm(nasm, nasmOpts, target, Seq(crtn), log))
                    .map((crtnObj) => crtEnd +: crtnObj).getOrElse(Seq())

      val objects = preObj ++ (irObjs ++ cObjs ++ asmObjs) ++ postObj

      val exc = linkObjects(linker, linkerOpts, target, objects, executable, log)
      
      IO.copyFile(exc, file("kernel.bin"))
      // stripExecutable(strip, executable, target, log)
      grubConfig match {
        case Some(cfg) => 
          val iso = createISO(grubRescue, exc, cfg, moduleName.value, target, log)
          if (useQemu) {
            launchQemu(qemu, iso, target, log)
          } else if (useBochs) { 
            launchBochs(terminal, bochs, bochsConfig, bochsRcFile, target, log)
          }
        case None => log.warn("No grub config supplied, not building iso")
      }
      
      IO.delete(target)
    }
  )
}
