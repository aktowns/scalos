#ifndef _STRING_H
#define _STRING_H

#include <stdtypes.h>

void *memset(void *dest, int c, size_t n);
void *memchr(const void *src, int c, size_t n);
char *stpcpy(char *restrict d, const char *restrict s);
char *strcpy(char *restrict dest, const char *restrict src);
size_t strnlen(const char *s, size_t n);

#endif
