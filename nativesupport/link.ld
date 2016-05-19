ENTRY(_start)

SECTIONS
{
	. = 0x10000;

    LD_KERNEL_START = . ;

    .multiboot_header ALIGN(4K) : {
        *(.multiboot_header)
    }

	.text BLOCK(4K) : ALIGN(4K) {
	    *(.init)
		*(.text)
	    *(.fini)
	}

	.rodata BLOCK(4K) : ALIGN(4K) {
		*(.rodata*)
	}

	.data BLOCK(4K) : ALIGN(4K) {
		*(.data)
	}

	.bss BLOCK(4K) : ALIGN(4K) {
		*(COMMON)
		*(.bss)
	}

    .ctors : {*(.ctors)}
    .dtors : {*(.dtors)}
    .tm_clone_table : {*(.tm_clone_table)}
    .jcr : {*(.jcr)}

	.spot BLOCK(4K): ALIGN(4K) {
	    *(.spot)
	}

    /DISCARD/ : {
        *(*)
    }

    LD_KERNEL_END = . ;
}
