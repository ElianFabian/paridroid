/*  This file was created by Configure. Any change made to it will be lost
 *  next time Configure is run. */
#ifndef __CONFIG_H__
#define __CONFIG_H__
#define UNIX
#define GPHELP "/data/local/bin/gphelp"
#define GPDATADIR "/data/local/bin/share/pari"
#define SHELL_Q '\''

#define PARIVERSION "GP/PARI CALCULATOR Version 2.6.0 (development git-9e6995b)"
#define PARIINFO "arm-linux-androideabi (portable C/GMP-5.0.1 kernel) 32-bit version"
#define PARI_VERSION_CODE 132608
#define PARI_VERSION(a,b,c) (((a) << 16) + ((b) << 8) + (c))
#define PARI_VERSION_SHIFT 8
#define PARI_VCSVERSION "git-9e6995b"

#define PARI_DOUBLE_FORMAT 1
#define GCC_VERSION "4.4.3 (arm-linux-androideabi, Ubuntu 11.04) "
#define __HAS_NO_ASM__
#define ASMINLINE

/*  Location of GNU gzip program (enables reading of .Z and .gz files). */
#define GNUZCAT
#define ZCAT "/bin/gzip -dc"

#define HAS_ISATTY
#define HAS_ALARM
#define USE_GETRUSAGE 1
#define HAS_SIGACTION
#define HAS_WAITPID
#define HAS_GETENV
#define HAS_SETSID
#define HAS_DLOPEN
#define STACK_CHECK
#define HAS_VSNPRINTF
#define HAS_TIOCGWINSZ
#define HAS_STRFTIME
#define HAS_STAT
#endif
