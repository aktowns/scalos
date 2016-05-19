package kernel

import scalanative.native._, oslib._
import oslib.system.Bootloader

object Main {
  def main(args: Array[String]): Unit = {
    Bochs.puts(c"Booting..")
    nativeBochs.BochsConsolePrintExt(Bootloader.bootloader_name, 10)
    
    var c = 0
    while (Bootloader.bootloader_name(c) != 0) {
      nativeBochs.BochsConsolePrintCharExt(Bootloader.bootloader_name(c))
      nativeBochs.BochsBreakExt()
      c += 1
    }

    val x = Bootloader.bootloader_name + 1
    Bochs.puts(x)
    Bochs.puts(c"WHYYY")
    // Video.initialize()
    
    // Video.puts(c"Hello Scala World!")
  }
}
