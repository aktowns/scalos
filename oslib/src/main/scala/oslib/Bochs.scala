package oslib

import scalanative.native._

@extern object nativeBochs {
  def BochsConsolePrintCharExt(c: CChar): Unit = extern
  def BochsBreakExt(): Unit = extern
  def BochsConsolePrintExt(data: CString, length: CSize): Unit = extern
  def BochsConsolePutsExt(data: CString): Unit = extern
}

object Bochs {
    final def puts(data: CString): Unit = {
        nativeBochs.BochsConsolePutsExt(data)
    } 
}
