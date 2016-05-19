#include <stdarg.h>

int _vsnprintf(char *buf, int n, const char *fmt, va_list args);

int vsprintf(char *buf, const char *fmt, va_list args) {
    return _vsnprintf(buf, (~0U)>>1, fmt, args);
}
