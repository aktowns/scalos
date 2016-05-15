package oslib

import scalanative.native._

object StringUtils {
  @inline final def length(string: CString): CInt = {
    var s = 0
    var cursor = string(0)
    
    while (cursor != 0) {
      s += 1
      cursor = string(s)
    }
    
    s
  }
}