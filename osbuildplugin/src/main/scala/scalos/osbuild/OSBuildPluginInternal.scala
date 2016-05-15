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
      
      // log.info(s"Commandline: $compile")  
      
      Process(compile, target) ! log
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
      
      // log.info(s"Commandline: $compile")  
      
      Process(compile, target) ! log
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
      
      // log.info(s"Commandline: $compile")  
      
      Process(compile, target) ! log
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
    
    // log.info(s"Commandline: $link")  

    Process(link, target) ! log
    
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
    IO.copyFile(kernel, bootdir / "kernel")
    
    val iso = file(target / name + ".iso")
    val rescue = abs(mkRescue) +: Seq("-o", abs(iso), abs(isodir))
    
    Process(rescue, target) ! log

    iso
  }
  
  private def launchQemu(qemu: File,
                         cdrom: File, 
                         target: File, 
                         log: Logger): Unit = {
    
    val emu = abs(qemu) +: Seq("-cdrom", abs(cdrom))
    Process(emu, target) ! log    
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
      val verbose     = nativeVerbose.value
      val dotpath     = nativeEmitDependencyGraphPath.value
      val opts        = new NativeOpts(classpath, abs(appll), dotpath.map(abs), entry, verbose)
      
      log.info(s"Creating directory $target")
      IO.createDirectory(target)
      
      compileNir(opts, log)
      val irObjs = compileLl(clang, clangOpts ++ clangIrOpts, target, Seq(file(abs(appll))), log)
      val cObjs = compileC(clang, clangOpts ++ clangCOpts, target, csources, log)
      val asmObjs = compileAsm(nasm, nasmOpts, target, asmsources, log)
      
      val exc = linkObjects(linker, linkerOpts, target, irObjs ++ cObjs ++ asmObjs, executable, log)
      grubConfig match {
        case Some(cfg) => 
          val iso = createISO(grubRescue, exc, cfg, moduleName.value, target, log)
          launchQemu(qemu, iso, target, log)
        case None => log.warn("No grub config supplied, not building iso")
      }
    }
  )
}
