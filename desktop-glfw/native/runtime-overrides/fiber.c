#include "fiber.h"
#include "definitions.h"
#include <stddef.h>
#include <locale.h>
#include <time.h>

#if TEAVM_UNIX
    #include <signal.h>
#endif
#if TEAVM_WINDOWS
    #include <Windows.h>
    #include <synchapi.h>
#endif
#if TEAVM_PSP
    #include <pspkernel.h>
#endif
#if defined(__APPLE__) && TEAVM_UNIX
    #define TEAVM_APPLE_FIBER_COMPAT 1
    #include <errno.h>
    #include <pthread.h>
#else
    #define TEAVM_APPLE_FIBER_COMPAT 0
#endif

#if TEAVM_UNIX && !TEAVM_APPLE_FIBER_COMPAT
    static timer_t teavm_queueTimer;
#endif
#if TEAVM_WINDOWS
    static HANDLE teavm_queueTimer;
#endif
#if TEAVM_APPLE_FIBER_COMPAT
    static pthread_mutex_t teavm_queueMutex = PTHREAD_MUTEX_INITIALIZER;
    static pthread_cond_t teavm_queueCondition = PTHREAD_COND_INITIALIZER;
    static int teavm_queueInterrupted = 0;
#endif

void teavm_initFiber() {

    #if TEAVM_UNIX
        #ifndef __EMSCRIPTEN__
            setlocale (LC_ALL, "");

            #if TEAVM_APPLE_FIBER_COMPAT
                pthread_mutex_lock(&teavm_queueMutex);
                teavm_queueInterrupted = 0;
                pthread_mutex_unlock(&teavm_queueMutex);
            #else
                struct sigaction sigact;
                sigact.sa_flags = 0;
                sigact.sa_handler = NULL;
                sigaction(SIGRTMIN, &sigact, NULL);

                sigset_t signals;
                sigemptyset(&signals);
                sigaddset(&signals, SIGRTMIN);
                sigprocmask(SIG_BLOCK, &signals, NULL);

                struct sigevent sev;
                sev.sigev_notify = SIGEV_SIGNAL;
                sev.sigev_signo = SIGRTMIN;
                timer_create(CLOCK_REALTIME, &sev, &teavm_queueTimer);
            #endif
        #endif
    #endif

    #if TEAVM_WINDOWS
        LARGE_INTEGER perf = { .QuadPart  = 0 };
        QueryPerformanceFrequency(&perf);
        teavm_perfFrequency = perf.QuadPart;
        QueryPerformanceCounter(&perf);
        teavm_perfInitTime = perf.QuadPart;

        teavm_queueTimer = CreateEvent(NULL, TRUE, FALSE, TEXT("TeaVM_eventQueue"));

        SYSTEMTIME unixEpochStart = {
            .wYear = 1970,
            .wMonth = 1,
            .wDayOfWeek = 3,
            .wDay = 1
        };
        FILETIME fileTimeStart;
        SystemTimeToFileTime(&unixEpochStart, &fileTimeStart);
        teavm_unixTimeOffset = fileTimeStart.dwLowDateTime | ((uint64_t) fileTimeStart.dwHighDateTime << 32);
    #endif
}

#if TEAVM_UNIX
    #ifdef __EMSCRIPTEN__
        void teavm_waitFor(int64_t timeout) {
            abort();
        }
        void teavm_interrupt() {
            abort();
        }
    #elif TEAVM_APPLE_FIBER_COMPAT
        void teavm_waitFor(int64_t timeout) {
            if (timeout <= 0) {
                return;
            }

            struct timespec wakeTime;
            clock_gettime(CLOCK_REALTIME, &wakeTime);
            wakeTime.tv_sec += timeout / 1000;
            wakeTime.tv_nsec += (timeout % 1000) * 1000000L;
            if (wakeTime.tv_nsec >= 1000000000L) {
                wakeTime.tv_sec += wakeTime.tv_nsec / 1000000000L;
                wakeTime.tv_nsec %= 1000000000L;
            }

            pthread_mutex_lock(&teavm_queueMutex);
            if (teavm_queueInterrupted) {
                teavm_queueInterrupted = 0;
                pthread_mutex_unlock(&teavm_queueMutex);
                return;
            }

            while (!teavm_queueInterrupted) {
                int waitResult = pthread_cond_timedwait(&teavm_queueCondition, &teavm_queueMutex, &wakeTime);
                if (waitResult == ETIMEDOUT) {
                    break;
                }
            }

            teavm_queueInterrupted = 0;
            pthread_mutex_unlock(&teavm_queueMutex);
        }

        void teavm_interrupt() {
            pthread_mutex_lock(&teavm_queueMutex);
            teavm_queueInterrupted = 1;
            pthread_cond_signal(&teavm_queueCondition);
            pthread_mutex_unlock(&teavm_queueMutex);
        }
    #else
        void teavm_waitFor(int64_t timeout) {
            struct itimerspec its = {0};
            its.it_value.tv_sec = timeout / 1000;
            its.it_value.tv_nsec = (timeout % 1000) * 1000000L;
            timer_settime(teavm_queueTimer, 0, &its, NULL);

            sigset_t signals;
            sigemptyset(&signals);
            sigaddset(&signals, SIGRTMIN);
            siginfo_t actualSignal;
            sigwaitinfo(&signals, &actualSignal);
        }

        void teavm_interrupt() {
            struct itimerspec its = {0};
            timer_settime(teavm_queueTimer, 0, &its, NULL);
            raise(SIGRTMIN);
        }
    #endif
#endif

#if TEAVM_WINDOWS
    void teavm_waitFor(int64_t timeout) {
        WaitForSingleObject(teavm_queueTimer, (DWORD) timeout);
        ResetEvent(teavm_queueTimer);
    }

    void teavm_interrupt() {
        SetEvent(teavm_queueTimer);
    }
#endif

#if TEAVM_PSP
    void teavm_waitFor(int64_t timeout) {
        // PSP implementation: simple delay
        if (timeout > 0) {
            sceKernelDelayThread(timeout * 1000); // timeout in ms, sceKernelDelayThread in us
        }
    }

    void teavm_interrupt() {
        // Stub for PSP
    }
#endif
