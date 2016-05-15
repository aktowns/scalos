package oslib

import scalanative.native._

object Colour {
  sealed trait VGAColour { def value: Int }
  case object Black extends VGAColour { val value = 0 }
  case object Blue extends VGAColour { val value = 1 }
  case object Green extends VGAColour { val value = 2 }
  case object Cyan extends VGAColour { val value = 3 }
  case object Red extends VGAColour { val value = 4 }
  case object Magenta extends VGAColour { val value = 5 }
  case object Brown extends VGAColour { val value = 6 }
  case object LightGrey extends VGAColour { val value = 7 }
  case object DarkGrey extends VGAColour { val value = 8 }
  case object LightBlue extends VGAColour { val value = 9 }
  case object LightGreen extends VGAColour { val value = 10 }
  case object LightCyan extends VGAColour { val value = 11 }
  case object LightRed extends VGAColour { val value = 12 }
  case object LightMagenta extends VGAColour { val value = 13 }
  case object LightBrown extends VGAColour { val value = 14 }
  case object White extends VGAColour { val value = 15 }

  // Not implemented.
  // implicit def colourToInt(colour: VGAColour): Int = colour.value
  // implicit def colourToShort(colour: VGAColour): Short = colour.value.toShort
  // likewise..
  // @inline final def vgaColour(fg: VGAColour, bg: VGAColour): CShort = { 
  //   (fg.value | (bg.value << 4)).toShort
  // }
  
  @inline final def vgaColour(fg: Int, bg: Int): CShort = (fg | (bg << 4)).toShort
}
