package oslib

import scalanative.native._

@extern object nativeVideo {
  var videoMemory: Ptr[CShort] = extern
}

object Video { 
  final val VGA_WIDTH = 80
  final val VGA_HEIGHT = 25
   
  var row: Int = 0
  var column: Int = 0
  var colour: CShort = Colour.vgaColour(Colour.Red.value, Colour.White.value)
  
  final def initialize(): Unit = {
    var y: Int = 0 
    while (y < VGA_HEIGHT) {      
      var x: Int = 0
      while (x < VGA_WIDTH) {
        putEntryAt(' ', colour, x, y)
        x += 1
      }
      y += 1
    }
  }
  
  @inline final def putEntryAt(c: CChar, col: CShort, x: CInt, y: CInt): Unit = {
    val index = (y * VGA_WIDTH) + x
    nativeVideo.videoMemory(index) = vgaEntry(c, colour)
  } 
  
  @inline final def putc(c: CChar): Unit = {
    putEntryAt(c, colour, column, row)
    column += 1 
    if (column == VGA_WIDTH) {
      column = 0
      row += 1
      if (row == VGA_HEIGHT) {
         row = 0
      }
    }
  }

  @inline final def puts(str: CString): Unit = {
    val len = StringUtils.length(str)
    var i = 0
    while (i < len) {
      putc(str(i))
      i += 1
    }
  }

  @inline final def vgaEntry(chr: CChar, col: CShort): CShort = 
    (chr.toShort | (col << 8)).toShort
}
