#ifndef __BOCHS_H
#define __BOCHS_H

#include <stdtypes.h>
#include <stdarg.h>

static __inline void outb(unsigned char __val, unsigned short __port) {
    __asm__ volatile ("outb %0,%1" : : "a" (__val), "dN" (__port));
}

static __inline void outw(unsigned short __val, unsigned short __port) {
    __asm__ volatile ("outw %0,%1" : : "a" (__val), "dN" (__port));
}

#define BochsBreak() outw(0x8A00,0x8A00); outw(0x08AE0, 0x8A00)
#define BochsConsolePrintChar(c) outb(c, 0xe9)

static char debug_printbuf[1024];

static inline void BochsConsolePrint(const char* data, size_t data_length) {
        for (size_t i = 0; i < data_length; i++) {
                    BochsConsolePrintChar((int) ((const unsigned char *) data)[i]);
                        }
}

static inline void BochsConsolePrintf(const char* restrict format, ...) {
        va_list args;
            va_start(args, format);

                size_t length = vsprintf(debug_printbuf, format, args);
                    BochsConsolePrint(debug_printbuf, length);

                        va_end(args);
}

static inline void BochsConsolePuts(const char* data) {
        BochsConsolePrintf("%s\n", data);
}

void BochsBreakExt();
void BochsConsolePrintExt(const char* data, size_t data_length);
void BochsConsolePrintfExt(const char* restrict format, ...);
void BochsConsolePutsExt(const char* data);

#endif
