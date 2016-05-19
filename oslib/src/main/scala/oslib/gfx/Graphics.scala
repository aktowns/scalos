package oslib.gfx

import scalanative.native._

object Graphics {
  sealed trait Colour { def r: CInt; def g: CInt; def b: CInt }
  case object White extends Colour { val r = 255; val g = 255; val b = 255 }
  case object Black extends Colour { val r = 0; val g = 0; val b = 0 }
  case object Red extends Colour { val r = 255; val g = 0; val b = 0 }
  case object Lime extends Colour { val r = 0; val g = 255; val b = 0 }
  case object Blue extends Colour { val r = 0; val g = 0; val b = 255 }
  case object Yellow extends Colour { val r = 255; val g = 255; val b = 0 }
  case object Aqua extends Colour { val r = 0; val g = 255; val b = 255 }
  case object Magenta extends Colour { val r = 255; val g = 0; val b = 255 }
  case object Silver extends Colour { val r = 192; val g = 192; val b = 192 }
  
  //@inline def colour2hex(c: Colour): CChar = 
  //  ((c.r << 16) | (c.g << 8) | c.b)
}
