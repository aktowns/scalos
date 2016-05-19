#include <stdtypes.h>
#include <stdarg.h>
#include <stdio.h>
#include <bochs.h>

void BochsBreakExt() {
    BochsBreak();
}

void BochsConsolePrintCharExt(const char c) {
    BochsConsolePrintf("c=%c p=%p\n", c, c);
    BochsConsolePrintChar(c);
}

void BochsConsolePrintExt(const char* data, size_t data_length) {
    BochsConsolePrintf("showing data given at address=%p\n", data);
    BochsConsolePrint(data, data_length);
}

void BochsConsolePrintfExt(const char* restrict format, ...) {
    va_list args;
    va_start(args, format);

    size_t length = vsprintf(debug_printbuf, format, args);
    BochsConsolePrint(debug_printbuf, length);

    va_end(args);
}

void BochsConsolePutsExt(const char* data) {
    BochsConsolePuts(data);
}
