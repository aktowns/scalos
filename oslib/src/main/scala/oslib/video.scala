package oslib

import scalanative.native._

@extern object video {
  var video_memory: Ptr[CShort] = extern
}

object Video {
  def colour(fg: Short, bg: Short): CShort =
    (fg | (bg << 4)).toShort
  
  def vga_entry(chr: CChar, col: CShort): CShort = {
    var c = chr.toShort
    (c | (col << 8)).toShort
  }

  def puts(str: CString): Unit = {
    var i = 0
    while (i < length(str)) {
      video.video_memory(i) = vga_entry(str(i), colour(4.toShort, 15.toShort))
      i += 1
    }
  }
  
  def length(string: CString): CInt = {
    var s = 0
    var cursor = string(0)
    
    while (cursor != 0) {
      s += 1
      cursor = string(s)
    }
    
    s
  }
}
