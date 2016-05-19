#include <stdtypes.h>
#include <bochs.h>

uint16_t* video_memory = (uint16_t*) 0xB8000;

void kernel_early(void) { 
    BochsConsolePuts("early\n");
}

void interrupt_handler(int32_t i, int32_t code) {
    if (i != 32) {
        BochsConsolePrintf("Interupt received: %i ~> %i\n", i, code);
    }
    
    //if (i == 13) // We've hit a general protection fault. 
    BochsBreak();
}

int32_t syscall_handler(uint32_t n, uint32_t a, uint32_t b, uint32_t c, uint32_t d, uint32_t e, uint32_t f) {
    BochsConsolePuts("syscall handler");
    return 0;
}
