__attribute__((__noreturn__))
void abort(void) {
    while (1) { }
    _builtin_unreachable();
}
