#include <stdtypes.h>
#include <bochs.h>

static int position = 0x150000;

void scalanative_init() {
    BochsConsolePuts("scalanative_init called");
}

void* scalanative_alloc(void* info, size_t size) {
    void* ptr = (void*)position;

    BochsConsolePrintf("scalanative_alloc called for size: %i, given address=%p\n", size, ptr);
    
    position = position + size;

    return ptr;
}
