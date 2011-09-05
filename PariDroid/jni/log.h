#ifndef _PARIDROID_LOG_H
#define _PARIDROID_LOG_H

#include <errno.h>

#define LOG_INFO 1
#define LOG_ERROR 2

void log_open(const char *ident);

void log_write(int level, const char *format, ...) __attribute__ ((format(printf, 2, 3)));

#endif
