#ifndef _STDIO_H
#define _STDIO_H

#include <stdarg.h>

#define NULL ((void*)0)

int vsprintf(char *buf, const char *fmt, va_list args);

#endif