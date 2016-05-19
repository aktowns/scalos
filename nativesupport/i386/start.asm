;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; boot.asm
;;
;; this file sets up the GDT, IDT, global ctors,
;; interrupts and deals with multiboot
;;
bits 32

%macro generate_intr_handlers 3
    %assign i %2
    %assign count %3 + 1
    %rep count
        %if i == %3
            %exitrep
        %endif
        global intr%+i:function
        intr%+i:
            %if i == 8 || i == 10 || i == 11 || i == 12 || i == 13 || i == 14 || i == 17
                push dword i
            %else
                push dword 0
                push dword i
            %endif
            jmp %1
        %assign i i + 1
    %endrep
%endmacro

; Uhh ohhhh
%macro breakpoint 0
    xchg bx, bx
%endmacro

section .text

; C
extern retrieve_multiboot_information, kernel_early, main, _init
extern interrupt_handler, syscall_handler
; Linker Script
extern LD_KERNEL_START, LD_KERNEL_END

global kernel_start:data
kernel_start dd LD_KERNEL_START
global kernel_end:data
kernel_end dd LD_KERNEL_END

global _start:function
_start:
    cli                                     ; clear interrupts

    push ebx                                ; ptr to the multiboot structure
    push eax                                ; the multiboot magic

    call populate_idt                       ; populate the interrupt descriptor table
    lgdt [gdt_init]                         ; load the global descriptor table
    call gdt_flush                          ; flush the grub supplied gdt

    lidt [idt_init]                         ; load the interrupt descriptor table

    call retrieve_multiboot_information     ; initialize initial information structures passed from grub2

    mov ax, 2*8                             ; selector two is flat 4gb data
    mov ds, ax                              ; set stack and data segments to selector 2
    mov ss, ax                              ;
    mov ax, 5*8                             ; set TSS to selector 5
    ltr ax                                  ; load the TSS
    mov ax, 0                               ; unused segments are nulled out
    mov es, ax                              ;
    mov fs, ax                              ;
    mov gs, ax                              ;
    
    mov sp, 0xfff0                          ; set up initial C stack
    mov bp, 0xfff0                          ; set up initial C stack

    call kernel_early                       ; initialize the core kernel before running the global constructors.
    call _init                              ; call the global constructors. (crti)
    call main                        	    ; transfer control to the main kernel.
    
    cli                                     ; hang if kernel_main unexpectedly returns.
.Lhang:
    hlt
    jmp .Lhang

align 16
global gdt
gdt:
    dw 0x0000, 0x0000, 0x0000, 0x0000                ; seg 0 - null
    dw 0xffff, 0x0000, 0x9a00, 0x0cf                 ; seg 1 - kernel flat 4GB code
    dw 0xffff, 0x0000, 0x9200, 0x0cf                 ; seg 2 - kernel flat 4GB data
    dw 0xffff, 0x0000, 0xfa00, 0x0cf                 ; seg 3 - user flat 4GB code
    dw 0xffff, 0x0000, 0xf200, 0x0cf                 ; seg 4 - user flat 4GB data
    dw 0x0068, tss - kernel_start + 16, 8901h, 00cfh ; seg 5 - TSS
global gdt_init
gdt_init:
    dw gdt_init - gdt - 1
    dd gdt
gdt_flush:
    mov ax, 0x10            ; 0x10 is the offset in the GDT to our data segment
    mov ds, ax
    mov es, ax
    mov fs, ax
    mov gs, ax
    mov ss, ax
    jmp 0x08:_gdt_flush     ; 0x08 is the offset to our code segment: Far jump!
_gdt_flush:
    ret                     ; Returns back to the C code!

align 16
tss: 
    dd          0
global interrupt_stack_pointer
interrupt_stack_pointer:
    dd          0xfff0
    dd          2*8
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0
    dd          0

global total_memory:data
total_memory:
    dw          128

generate_intr_handlers intr_handler, 0, 47
generate_intr_handlers intr_syscall, 48, 48

intr_handler: 
    push        ds
    push        ebp             ; push regs
    push        edi
    push        esi
    push        edx
    push        ecx
    push        ebx
    push        eax
    push        dword [esp+36]  ; push interrupt code
    push        dword [esp+36]  ; push interrupt number
    mov         eax, 2*8        ; switch to kernel data seg
    mov         ds, eax
    call        interrupt_handler
    add         esp, 4          ; remove interrupt number
    add         esp, 4          ; remove interrupt code
    jmp intr_return

intr_syscall: 
    push        ds
    push        ebp             ; push regs
    push        edi
    push        esi
    push        edx
    push        ecx
    push        ebx
    push        eax             ; note these *are* the syscall args
    mov         eax, 2*8        ; switch to kernel data seg
    mov         ds, eax
    call        syscall_handler
    add         esp, 4          ; remove the old eax
    jmp syscall_return

global intr_return:function
intr_return:
    pop         eax
syscall_return: 
    pop         ebx
    pop         ecx
    pop         edx
    pop         esi
    pop         edi
    pop         ebp
    pop         ds
    add         esp, 4          ; remove interrupt num
    add         esp, 4          ; remove detail code
    iret                        ; iret gets the intr context

idt:
    %rep 49
        dw 0x0000            ; offset(0:15)-address of interrupt function (handler)
        dw 8                 ; code segment (0x08)
        db 0                 ; hard coded all zeros
        db 10001110b         ; 1000b
                             ; 1-segment present flag
                             ; 00-privilege level
                             ; 0-hard coded value
                             ; 1110b
                             ; 1-size of gate 1-32 bit, 0-16 bit
                             ; 110-hard coded value for type interrupt gate
        dw 0x0000            ; offset(16:31)
    %endrep
idt_init: 
    dw          idt_init-idt  ; Base
    dd          idt           ; limit
populate_idt:
    %assign i 0
    %rep 49
        lea ebx, [idt + (8 * i)]
        mov eax, intr %+ i
        mov word [ebx], ax
        shr eax, 16
        mov word [ebx + 6], ax
        %assign i i + 1
    %endrep
    ret

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spot
;;
section .spot
ode_to_spot: 
    db      'Felis catus is your taxonomic nomenclature.'
    db      'An endothermic quadruped, carnivorous by nature.'
    db      'Your visual, olfactory, and auditory senses'
    db      'Contribute to your hunting skill and natural defences.'
    db      'I find myself intrigued by your subvocal oscillations.'
    db      'A singular development of cat communications'
    db      'That obviates your basic hedonistic predilection,'
    db      'For a rhythmic stroking of your fur to demonstrate affection.'
    db      'A tail is quite essential for your acrobatic talents.'
    db      'You would not be so agile if you lacked its counterbalance.'
    db      'And when not being utilised to aid in locomotion'
    db      'It often serves to illustrate the state of your emotions.'
    db      'Oh, Spot, the complex levels of behaviour you display'
    db      'Connote a fairly well developed cognitive array.'
    db      'And though you are not sentient, Spot, and do not comprehend'
    db      'I nonetheless consider you a true and valued friend.'
