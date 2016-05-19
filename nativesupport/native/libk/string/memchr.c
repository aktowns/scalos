#include <string.h>
#include <stdtypes.h>

#define SS (sizeof(size_t))
#define ALIGN (sizeof(size_t)-1)
#define ONES ((size_t)-1/255)
#define HIGHS (ONES * (255/2+1))
#define HASZERO(x) (((x)-ONES) & ~(x) & HIGHS)

void *memchr(const void *src, int c, size_t n) {
    const unsigned char *s = src;
    c = (unsigned char) c;
    for (; ((uintptr_t) s & ALIGN) && n && *s != c; s++, n--);
    if (n && *s != c) {
        const size_t *w;
        size_t k = ONES * c;
        for (w = (const void *) s; n >= SS && !HASZERO(*w ^ k); w++, n -= SS);
        for (s = (const void *) w; n && *s != c; s++, n--);
    }
    return n ? (void *) s : 0;
}
