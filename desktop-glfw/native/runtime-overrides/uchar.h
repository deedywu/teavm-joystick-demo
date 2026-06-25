#pragma once

#include <stddef.h>
#include <stdint.h>
#include <string.h>
#include <wchar.h>

#if TEAVM_PSP

typedef uint16_t char16_t;
typedef int char32_t;

static inline size_t c16rtomb(char * s, char16_t c16, mbstate_t * ps) {
    if (s) *s = (char) c16; // Rough conversion
    return 1;
}

static inline size_t mbrtoc16(char16_t * pc16, const char * s, size_t n, mbstate_t * ps) {
    if (pc16 && n > 0) *pc16 = (char16_t) *s; // Rough conversion
    return 1;
}

#else

#if defined(__APPLE__) && !__has_include(<uchar.h>)
#define TEAVM_MISSING_UCHAR_H_COMPAT 1

typedef uint16_t char16_t;
typedef uint32_t char32_t;

typedef struct {
    uint16_t pendingHighSurrogate;
} TeaVM_UCharState;

_Static_assert(sizeof(TeaVM_UCharState) <= sizeof(mbstate_t), "mbstate_t too small for TeaVM uchar fallback");

static inline TeaVM_UCharState teavm_loadUCharState(const mbstate_t* state) {
    TeaVM_UCharState result = { 0 };
    if (state != NULL) {
        memcpy(&result, state, sizeof(result));
    }
    return result;
}

static inline void teavm_storeUCharState(mbstate_t* state, TeaVM_UCharState value) {
    if (state == NULL) {
        return;
    }
    memset(state, 0, sizeof(*state));
    memcpy(state, &value, sizeof(value));
}

static inline size_t teavm_encodeUtf8(char* s, char32_t codePoint) {
    if (codePoint >= 0xD800u && codePoint <= 0xDFFFu) {
        codePoint = 0xFFFDu;
    } else if (codePoint > 0x10FFFFu) {
        codePoint = 0xFFFDu;
    }

    if (codePoint <= 0x7Fu) {
        if (s != NULL) {
            s[0] = (char) codePoint;
        }
        return 1;
    }

    if (codePoint <= 0x7FFu) {
        if (s != NULL) {
            s[0] = (char) (0xC0u | (codePoint >> 6));
            s[1] = (char) (0x80u | (codePoint & 0x3Fu));
        }
        return 2;
    }

    if (codePoint <= 0xFFFFu) {
        if (s != NULL) {
            s[0] = (char) (0xE0u | (codePoint >> 12));
            s[1] = (char) (0x80u | ((codePoint >> 6) & 0x3Fu));
            s[2] = (char) (0x80u | (codePoint & 0x3Fu));
        }
        return 3;
    }

    if (s != NULL) {
        s[0] = (char) (0xF0u | (codePoint >> 18));
        s[1] = (char) (0x80u | ((codePoint >> 12) & 0x3Fu));
        s[2] = (char) (0x80u | ((codePoint >> 6) & 0x3Fu));
        s[3] = (char) (0x80u | (codePoint & 0x3Fu));
    }
    return 4;
}

static inline size_t teavm_decodeUtf8(char32_t* codePoint, const char* s, size_t n) {
    if (n == 0) {
        return (size_t) -2;
    }

    unsigned char b0 = (unsigned char) s[0];
    if (b0 < 0x80u) {
        *codePoint = b0;
        return 1;
    }

    if (b0 < 0xC2u) {
        return (size_t) -1;
    }

    if (b0 < 0xE0u) {
        if (n < 2) {
            return (size_t) -2;
        }
        unsigned char b1 = (unsigned char) s[1];
        if ((b1 & 0xC0u) != 0x80u) {
            return (size_t) -1;
        }
        *codePoint = (char32_t) (((b0 & 0x1Fu) << 6) | (b1 & 0x3Fu));
        return 2;
    }

    if (b0 < 0xF0u) {
        if (n < 3) {
            return (size_t) -2;
        }
        unsigned char b1 = (unsigned char) s[1];
        unsigned char b2 = (unsigned char) s[2];
        if ((b1 & 0xC0u) != 0x80u || (b2 & 0xC0u) != 0x80u) {
            return (size_t) -1;
        }
        if ((b0 == 0xE0u && b1 < 0xA0u) || (b0 == 0xEDu && b1 >= 0xA0u)) {
            return (size_t) -1;
        }
        *codePoint = (char32_t) (((b0 & 0x0Fu) << 12) | ((b1 & 0x3Fu) << 6) | (b2 & 0x3Fu));
        return 3;
    }

    if (b0 < 0xF5u) {
        if (n < 4) {
            return (size_t) -2;
        }
        unsigned char b1 = (unsigned char) s[1];
        unsigned char b2 = (unsigned char) s[2];
        unsigned char b3 = (unsigned char) s[3];
        if ((b1 & 0xC0u) != 0x80u || (b2 & 0xC0u) != 0x80u || (b3 & 0xC0u) != 0x80u) {
            return (size_t) -1;
        }
        if ((b0 == 0xF0u && b1 < 0x90u) || (b0 == 0xF4u && b1 >= 0x90u)) {
            return (size_t) -1;
        }
        *codePoint = (char32_t) (((b0 & 0x07u) << 18) | ((b1 & 0x3Fu) << 12) | ((b2 & 0x3Fu) << 6) | (b3 & 0x3Fu));
        return 4;
    }

    return (size_t) -1;
}

static inline size_t c16rtomb(char * s, char16_t c16, mbstate_t * ps) {
    TeaVM_UCharState state = teavm_loadUCharState(ps);

    if (state.pendingHighSurrogate != 0) {
        if (c16 >= 0xDC00u && c16 <= 0xDFFFu) {
            char32_t codePoint = 0x10000u
                + ((((char32_t) state.pendingHighSurrogate - 0xD800u) << 10)
                    | ((char32_t) c16 - 0xDC00u));
            state.pendingHighSurrogate = 0;
            teavm_storeUCharState(ps, state);
            return teavm_encodeUtf8(s, codePoint);
        }

        state.pendingHighSurrogate = 0;
        teavm_storeUCharState(ps, state);
        return teavm_encodeUtf8(s, 0xFFFDu);
    }

    if (c16 >= 0xD800u && c16 <= 0xDBFFu) {
        state.pendingHighSurrogate = c16;
        teavm_storeUCharState(ps, state);
        return 0;
    }

    teavm_storeUCharState(ps, state);

    if (c16 >= 0xDC00u && c16 <= 0xDFFFu) {
        return teavm_encodeUtf8(s, 0xFFFDu);
    }

    return teavm_encodeUtf8(s, (char32_t) c16);
}

static inline size_t mbrtoc16(char16_t * pc16, const char * s, size_t n, mbstate_t * ps) {
    if (s == NULL) {
        teavm_storeUCharState(ps, (TeaVM_UCharState) { 0 });
        return 0;
    }

    char32_t codePoint = 0;
    size_t result = teavm_decodeUtf8(&codePoint, s, n);
    if (result == (size_t) -1 || result == (size_t) -2) {
        return result;
    }

    if (codePoint > 0xFFFFu) {
        codePoint = 0xFFFDu;
    }

    if (pc16 != NULL) {
        *pc16 = (char16_t) codePoint;
    }

    teavm_storeUCharState(ps, (TeaVM_UCharState) { 0 });
    return result;
}

#else

#include <uchar.h>

#endif

#endif
