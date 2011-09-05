#include <stdio.h>
#include <syslog.h>
#include <stdarg.h>
#include <sys/types.h>
#include <sys/select.h>
#include <android/log.h>

#include "log.h"

void log_open(const char *ident)
{
  return;
}

void log_write(int level, const char *format, ...)
{
  int linux_level;
  va_list arglist;
  
  switch (level) {
  case (LOG_INFO):
    linux_level = ANDROID_LOG_INFO;
    break;
  case (LOG_ERROR):
    linux_level = ANDROID_LOG_ERROR;
    break;
  default:
    return;
  }

  va_start(arglist, format);
  
  __android_log_vprint(linux_level, "paridroid-helper", format, arglist);
  
  va_end(arglist);

  return;
}
