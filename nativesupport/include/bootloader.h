#ifndef _BOOTLOADER_H
#define _BOOTLOADER_H

typedef struct {
    uint32_t height;
    uint32_t width;
    uint8_t bpp;
    uintptr_t addr;
    uint32_t pitch;
} framebuffer_graphics_information_t;

typedef struct {
    uint8_t rsdp;
} bootloader_information_acpi_t;

typedef struct {
    uint32_t upper;
    uint32_t lower;
} bootloader_information_meminfo_t;

typedef struct {
    uint32_t device;
    uint32_t slice;
    uint32_t part;
} bootloader_information_bootdevice_t;

#define MMAP_MEMORY_AVAILABLE          1
#define MMAP_MEMORY_RESERVED           2
#define MMAP_MEMORY_ACPI_RECLAIMABLE   3
#define MMAP_MEMORY_NVS                4
#define MMAP_MEMORY_BADRAM             5

typedef struct {
    uint32_t base_addr;
    uint32_t length;
    uint8_t type;
} __attribute__((packed)) memory_map_information_entry_t;

typedef struct {
    uint8_t count;
    memory_map_information_entry_t entries[16];
} __attribute__((packed)) memory_map_information_t;

typedef struct {
    char command_line[256];
    char bootloader[128];

    bootloader_information_acpi_t acpi;
    bootloader_information_meminfo_t mem;
    bootloader_information_bootdevice_t boot;
} __attribute__((packed)) bootloader_information_t;

typedef struct {
    char command_line[256];
    uint32_t start;
    uint32_t end;
} bootloader_module_t;

extern framebuffer_graphics_information_t framebuffer_graphics_information;
extern bootloader_information_t bootloader_information;
extern memory_map_information_t memory_map_information;
extern bootloader_module_t module_information[10];

#endif
