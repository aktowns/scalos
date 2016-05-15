package kernel

import scalanative.native._, oslib._

object Main {
  def main(args: Array[String]): Unit = {
    Video.initialize()
    
    Video.puts(c"Hello Scala World!")
  }
}
