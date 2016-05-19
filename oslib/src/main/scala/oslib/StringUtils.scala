package oslib

import scalanative.native._

object StringUtils {
  final def strlen(string: CString): CInt = {
    var s = 0
    var cursor = string(0)
    
    while (cursor != 0) {
      cursor = string(s)
      s += 1
    }
    
    s
  }
  
  // final def strcmp(s1: CString, s2: CString): CInt = {
  //  var ind = 0
  //  while (s1(ind) == s2(ind) && s1(ind) != 0) { ind += 1 }
  //  
  //  s1(ind) - s2(ind)
  // } 
}