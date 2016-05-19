#include <string.h>
#include <stdtypes.h>

#define ALIGN (sizeof(size_t))
#define ONES ((size_t)-1/255)
#define HIGHS (ONES * (255/2+1))
#define HASZERO(x) (((x)-ONES) & ~(x) & HIGHS)

char *stpcpy(char *restrict d, const char *restrict s) {
    size_t *wd;
    const size_t *ws;

    if ((uintptr_t) s % ALIGN == (uintptr_t) d % ALIGN) {
        for (; (uintptr_t) s % ALIGN; s++, d++)
            if (!(*d = *s)) return d;
        wd = (void *) d;
        ws = (const void *) s;
        for (; !HASZERO(*ws); *wd++ = *ws++);
        d = (void *) wd;
        s = (const void *) ws;
    }
    for (; (*d = *s); s++, d++);

    return d;
}
