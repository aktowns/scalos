section .init
    pop ebp
    ret

section .fini
    push ebp
    mov ebp, esp
