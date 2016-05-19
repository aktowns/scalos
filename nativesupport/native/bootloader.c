#include <stdtypes.h>
#include <multiboot2.h>
#include <bootloader.h>
#include <bochs.h>
#include <string.h>
#include <stdlib.h>

char bootloader_command_line[256];
char bootloader_name[128];

bootloader_information_t bootloader_information;
framebuffer_graphics_information_t framebuffer_graphics_information;
memory_map_information_t memory_map_information;
bootloader_module_t module_information[10];

void retrieve_multiboot_information(unsigned long magic, unsigned long addr) {
    struct multiboot_tag *tag;
    unsigned size;

    for (int32_t i = 0; i < 10; i++) {
        module_information[i].start = 0;
        module_information[i].end = 0;
        module_information[i].command_line[0] = 0x0a;
    }

    if (magic != MULTIBOOT2_BOOTLOADER_MAGIC) {
        BochsConsolePrintf("Invalid magic number: 0x%x\n", magic);
        abort();
    }

    if (addr & 7) {
        BochsConsolePrintf("Unaligned mbi: 0x%x\n", addr);
        abort();
    }

    size = *(unsigned *) addr;
    BochsConsolePrintf("Announced mbi size 0x%x\n", size);
    for (tag = (struct multiboot_tag *) (addr + 8);
         tag->type != MULTIBOOT_TAG_TYPE_END;
         tag = (struct multiboot_tag *) ((multiboot_uint8_t *) tag + ((tag->size + 7) & ~7))) {
        switch (tag->type) {
            case MULTIBOOT_TAG_TYPE_CMDLINE: {
                strcpy(bootloader_command_line, ((struct multiboot_tag_string *) tag)->string);
                bootloader_command_line[100] = '\0';
                // strcpy(bootloader_information.command_line, ((struct multiboot_tag_string *) tag)->string);
                BochsConsolePrintf("Command line = %s\n", ((struct multiboot_tag_string *) tag)->string);
            }
                break;
            case MULTIBOOT_TAG_TYPE_BOOT_LOADER_NAME: {
                strcpy(bootloader_name, ((struct multiboot_tag_string *) tag)->string);
                bootloader_name[100] = '\0';
                bootloader_name[101] = 0;
                BochsConsolePrintf("addr=%p\n", bootloader_name);
                //strcpy(bootloader_information.bootloader, ((struct multiboot_tag_string *) tag)->string);
                BochsConsolePrintf("Boot loader name = %s\n", ((struct multiboot_tag_string *) tag)->string);
            }
                break;
            case MULTIBOOT_TAG_TYPE_APM: {
                struct multiboot_tag_apm *apm = ((struct multiboot_tag_apm *) tag);
                BochsConsolePrintf("APM version: %i\n", apm->version);
            }
                break;
            case MULTIBOOT_TAG_TYPE_VBE: {
                BochsConsolePrintf("VBE mode: %i\n", ((struct multiboot_tag_vbe *) tag)->vbe_mode);
            }
                break;
            case MULTIBOOT_TAG_TYPE_ACPI_OLD: {
                bootloader_information.acpi.rsdp = *((struct multiboot_tag_old_acpi *) tag)->rsdp;
                BochsConsolePrintf("old acpi rsdp: %x\n", ((struct multiboot_tag_old_acpi *) tag)->rsdp);
            }
                break;
            case MULTIBOOT_TAG_TYPE_ACPI_NEW: {
                bootloader_information.acpi.rsdp = *((struct multiboot_tag_new_acpi *) tag)->rsdp;
                BochsConsolePrintf("acpi rsdp: %x\n", ((struct multiboot_tag_new_acpi *) tag)->rsdp);
            }
                break;
            case MULTIBOOT_TAG_TYPE_ELF_SECTIONS: {
                struct multiboot_tag_elf_sections *elf = ((struct multiboot_tag_elf_sections *) tag);

                BochsConsolePrintf("elf sections: %i\n", elf->num);
            }
                break;
            case MULTIBOOT_TAG_TYPE_MODULE: {
                struct multiboot_tag_module *mod = ((struct multiboot_tag_module *) tag);

                for (int32_t i = 0; i < 10; i++) {
                    if (module_information[i].start == 0) {
                        BochsConsolePrintf("??? %x, %p\n", mod->mod_start, (void*)&mod->mod_start);
                        BochsConsolePrintf("Adding: '%s' to the modules\n", mod->cmdline);
                        module_information[i].start = mod->mod_start;
                        module_information[i].end = mod->mod_end;
                        strcpy(module_information[i].command_line, mod->cmdline);
                        break;
                    }
                }
                BochsConsolePrintf("Module at 0x%x-0x%x. Command line %s\n", mod->mod_start, mod->mod_end, mod->cmdline);
            }
                break;
            case MULTIBOOT_TAG_TYPE_BASIC_MEMINFO: {
                struct multiboot_tag_basic_meminfo *mem = ((struct multiboot_tag_basic_meminfo *) tag);

                bootloader_information.mem.lower = mem->mem_lower;
                bootloader_information.mem.upper = mem->mem_upper;

                BochsConsolePrintf("mem_lower = %iKB, mem_upper = %iKB\n", mem->mem_lower, mem->mem_upper);
            }
                break;
            case MULTIBOOT_TAG_TYPE_BOOTDEV: {
                struct multiboot_tag_bootdev *bootdev = (struct multiboot_tag_bootdev *) tag;

                bootloader_information.boot.device = bootdev->biosdev;
                bootloader_information.boot.slice = bootdev->slice;
                bootloader_information.boot.part = bootdev->part;

                BochsConsolePrintf("Boot device 0x%x,%i,%i\n", bootdev->biosdev, bootdev->slice, bootdev->part);
            }
                break;
            case MULTIBOOT_TAG_TYPE_MMAP: {
                struct multiboot_tag_mmap* mmap_tag = (struct multiboot_tag_mmap *) tag;

                uint8_t i = 0;
                for (multiboot_memory_map_t *mmap = mmap_tag->entries;
                        (uint8_t *) mmap < (uint8_t *) tag + tag->size;
                     mmap = (multiboot_memory_map_t *) ((unsigned long) mmap + mmap_tag->entry_size)) {

                    memory_map_information.entries[i].type = (uint8_t) mmap->type;
                    memory_map_information.entries[i].base_addr = (uint32_t) mmap->addr & 0xFFFFFFFF;
                    memory_map_information.entries[i].length = (uint32_t) mmap->len & 0xFFFFFFFF;

                    BochsConsolePrintf("base_addr=0x%x, length=%i, type=%i\n",
                                       memory_map_information.entries[i].base_addr,
                                       memory_map_information.entries[i].length,
                                       memory_map_information.entries[i].type);
                    i++;
                }
                memory_map_information.count = i;
                BochsConsolePrintf("%i memory map entries\n", memory_map_information.count);
            }
                break;
            case MULTIBOOT_TAG_TYPE_FRAMEBUFFER: {
                struct multiboot_tag_framebuffer *tagfb = (struct multiboot_tag_framebuffer *) tag;

                framebuffer_graphics_information.height = tagfb->common.framebuffer_height;
                framebuffer_graphics_information.width = tagfb->common.framebuffer_width;
                framebuffer_graphics_information.bpp = tagfb->common.framebuffer_bpp;
                framebuffer_graphics_information.addr = (uintptr_t) tagfb->common.framebuffer_addr;
                framebuffer_graphics_information.pitch = tagfb->common.framebuffer_pitch;

                BochsConsolePrintf("height=%i width=%i bpp=%i addr=%x pitch=%i\n",
                                   framebuffer_graphics_information.height,
                                   framebuffer_graphics_information.width,
                                   framebuffer_graphics_information.bpp,
                                   framebuffer_graphics_information.addr,
                                   framebuffer_graphics_information.pitch);
            }
                break;
            default: {
                BochsConsolePrintf("Unknown Tag 0x%x, Size 0x%x\n", tag->type, tag->size);
            }
        }
    }
    tag = (struct multiboot_tag *) ((multiboot_uint8_t *) tag + ((tag->size + 7) & ~7));
    BochsConsolePrintf("Total mbi size 0x%x\n", (unsigned) tag - addr);
}



