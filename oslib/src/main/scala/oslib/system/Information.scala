package oslib.system

import scalanative.native._

@extern object Bootloader {
  val bootloader_name: CString = extern
  val bootloader_command_line: CString = extern

  val framebuffer_graphics_information: FramebufferGraphicsInformation = extern
  val bootloader_information: BootloaderInformation = extern
  val memory_map_information: MemoryMapInformation = extern
  val module_information: Array[BootloaderModuleInformation] = extern
}

// This file cherry picks and wraps the multiboot2 information structures
package Memory {
  sealed trait Status         { def code: CInt } 
  case object Available       { val code = 1 }
  case object Reserved        { val code = 2 }
  case object ACPIReclaimable { val code = 3 }
  case object NVS             { val code = 4 }
  case object BadRam          { val code = 5 }
}

@struct class FramebufferGraphicsInformation(
  val height: UInt,
  val width:  UInt,
  val bpp:    UByte,
  val addr:   Ptr[UInt],
  val pitch:  UInt
)

@struct class ACPIInformation( 
  val rsdp: UByte
)

@struct class MemoryInformation(
  val upper: UInt,
  val lower: UInt
)

@struct class BootDeviceInformation(
  val device: UInt,
  val slice:  UInt,
  val part:   UInt
)

@struct class MemoryMapEntry(
  val base_addr: UInt,
  val length:    UInt,
  val typ:       UByte
)

@struct class MemoryMapInformation(
  val count:   UByte,
  val entries: Array[MemoryMapEntry]
)

@struct class BootloaderInformation(
  val command_line: CString,
  val bootloader: CString,

  val acpi: ACPIInformation,
  val mem: MemoryInformation,
  val boot: BootDeviceInformation
)

@struct class BootloaderModuleInformation(
  val command_line: CString,
  val start:        UInt,
  var end:          UInt
)

